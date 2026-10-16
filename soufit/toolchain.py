
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
import tempfile
import hashlib
import json
import urllib.request
import zipfile
from typing import Optional

SMALI_JAR = (
    "https://repo1.maven.org/maven2/org/apktool/apktool-cli/3.0.3/apktool-cli-3.0.3.jar"
)
R8_JAR = "https://dl.google.com/dl/android/maven2/com/android/tools/r8/8.7.18/r8-8.7.18.jar"
PLATFORM_ZIP = "https://dl.google.com/android/repository/platform-35_r02.zip"
PLATFORM_MEMBER = "android-35/android.jar"
SIGNER_JAR = (
    "https://github.com/patrickfav/uber-apk-signer/releases/download/v1.3.0/"
    "uber-apk-signer-1.3.0.jar"
)


PINNED = {
    "smali.jar": None,
    "r8.jar": None,
    "uber-apk-signer.jar": None,
    "platform.zip": None,
}


def _sha256(path: str) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(1 << 20), b""):
            digest.update(chunk)
    return digest.hexdigest()


class Toolchain:
    def __init__(self, directory: str, quiet: bool = False):
        self.directory = directory
        self.quiet = quiet
        os.makedirs(directory, exist_ok=True)
        self._pins_path = os.path.join(directory, "pins.json")

    def _recorded(self) -> dict:
        if os.path.exists(self._pins_path):
            with open(self._pins_path, encoding="utf-8") as handle:
                return json.load(handle)
        return {}

    def _verify(self, path: str, name: str) -> None:
        actual = _sha256(path)
        expected = PINNED.get(name) or self._recorded().get(name)
        if expected is None:
            return
        if actual != expected:
            raise RuntimeError(
                "%s does not match its SHA-256 (expected %s, got %s). Delete %s and "
                "try again; if it keeps happening, the download is not what it was."
                % (name, expected, actual, path))

    def _record(self, path: str, name: str) -> None:
        if PINNED.get(name) is not None:
            return
        recorded = self._recorded()
        recorded.setdefault(name, _sha256(path))
        with open(self._pins_path, "w", encoding="utf-8") as handle:
            json.dump(recorded, handle, indent=2)

    def hashes(self) -> dict:
        out = {}
        for name in PINNED:
            path = os.path.join(self.directory, name)
            if os.path.exists(path):
                out[name] = _sha256(path)
        return out

    def _say(self, message: str) -> None:
        if not self.quiet:
            print(message)

    def _fetch(self, url: str, name: str) -> str:
        if not url.startswith("https://"):
            raise RuntimeError("refusing to download %s over a non-HTTPS address" % name)
        path = os.path.join(self.directory, name)
        if os.path.exists(path):
            self._verify(path, name)
            return path
        self._say("  fetching %s" % name)
        temporary = path + ".part"
        with urllib.request.urlopen(url) as response, open(temporary, "wb") as out:
            shutil.copyfileobj(response, out)
        self._verify(temporary, name)
        self._record(temporary, name)
        os.replace(temporary, path)
        return path

    @property
    def smali(self) -> str:
        return self._fetch(SMALI_JAR, "smali.jar")

    @property
    def r8(self) -> str:
        return self._fetch(R8_JAR, "r8.jar")

    @property
    def signer(self) -> str:
        return self._fetch(SIGNER_JAR, "uber-apk-signer.jar")

    @property
    def android_jar(self) -> str:
        path = os.path.join(self.directory, "android.jar")
        if os.path.exists(path):
            return path
        archive = self._fetch(PLATFORM_ZIP, "platform.zip")
        self._say("  unpacking android.jar")
        with zipfile.ZipFile(archive) as zf, open(path, "wb") as out:
            shutil.copyfileobj(zf.open(PLATFORM_MEMBER), out)
        os.remove(archive)
        return path


    def framework_constant(self, field: str) -> int:
        result = subprocess.run(
            ["javap", "-cp", self.android_jar, "-constants", "android.R$style"],
            capture_output=True,
            text=True,
        )
        for line in result.stdout.splitlines():
            if (" " + field + " = ") in line:
                return int(line.rsplit("=", 1)[1].strip().rstrip(";"))
        raise RuntimeError("android.jar has no android.R.style.%s" % field)

    def check_attribute_ids(self, ids: dict) -> None:
        result = subprocess.run(
            ["javap", "-cp", self.android_jar, "-constants", "android.R$attr"],
            capture_output=True,
            text=True,
        )
        known = {}
        for line in result.stdout.splitlines():
            if " = " not in line or not line.strip().endswith(";"):
                continue
            name = line.split()[-3]
            known[name] = int(line.rsplit("=", 1)[1].strip().rstrip(";"))
        for name, value in ids.items():
            if name in known and known[name] != value:
                raise RuntimeError(
                    "android:%s is 0x%08x in android.jar, not 0x%08x"
                    % (name, known[name], value)
                )

    def compile_dex(self, sources_dir: str, out_dir: str, min_api: int,
                    stubs_dir: Optional[str] = None) -> str:
        classes = os.path.join(out_dir, "classes")
        stubs = os.path.join(out_dir, "stubs")
        dex = os.path.join(out_dir, "dex")
        for path in (classes, stubs, dex):
            shutil.rmtree(path, ignore_errors=True)
            os.makedirs(path)

        classpath = self.android_jar
        if stubs_dir:
            stub_sources = _java_under(stubs_dir)
            if stub_sources:
                _run(["javac", "-nowarn", "-Xlint:-options", "-encoding", "UTF-8",
                      "-cp", self.android_jar, "--release", "17", "-d", stubs]
                     + stub_sources)
                classpath = self.android_jar + os.pathsep + stubs

        sources = _java_under(sources_dir)
        if not sources:
            raise RuntimeError("no java sources under %s" % sources_dir)

        _run(
            ["javac", "-nowarn", "-Xlint:-options", "-encoding", "UTF-8",
             "-cp", classpath, "--release", "17", "-d", classes] + sources
        )
        class_files = _classes_under(classes)
        program = os.path.join(out_dir, "mod-classes.jar")
        with zipfile.ZipFile(program, "w", zipfile.ZIP_DEFLATED) as archive:
            for path in class_files:
                archive.write(path, os.path.relpath(path, classes).replace(os.sep, "/"))
        command = [_java(), "-cp", self.r8, "com.android.tools.r8.D8", "--release",
                   "--min-api", str(min_api), "--lib", self.android_jar]
        if stubs_dir and _classes_under(stubs):
            command += ["--classpath", stubs]
        _run(command + ["--output", dex, program])
        return os.path.join(dex, "classes.dex")

    def sign(self, apk_path: str, key: Optional[str] = None,
             alias: str = "androiddebugkey", store_password: str = "android",
             key_password: str = "android") -> None:
        command = [_java(), "-jar", self.signer, "-a", apk_path, "--allowResign", "--overwrite"]
        if not key:
            _run(command)
            return

        keytool = _keytool()
        probe = subprocess.run(
            [keytool, "-list", "-keystore", key, "-storepass", store_password],
            capture_output=True, text=True,
        )
        details = probe.stdout + probe.stderr
        if probe.returncode != 0:
            if "password was incorrect" in details:
                raise RuntimeError(
                    "the keystore password is wrong. Set SOUFIT_KEYSTORE_PASSWORD, pass "
                    "--password-file, or type it when asked. If the key has its own password, "
                    "set SOUFIT_KEY_PASSWORD."
                )
            raise RuntimeError("keytool could not read the supplied keystore: " + details.strip())
        match = re.search(r"Keystore type:\s*([A-Za-z0-9_-]+)", details)
        if not match:
            raise RuntimeError("keytool did not report the keystore type")
        store_type = match.group(1).upper()

        with tempfile.TemporaryDirectory(prefix="soufit-sign-") as temp:
            signer_key = key
            if store_type != "JKS":
                signer_key = os.path.join(temp, "signing.jks")
                _run([
                    keytool, "-importkeystore", "-srckeystore", key,
                    "-srcstoretype", store_type, "-srcstorepass", store_password,
                    "-srcalias", alias, "-destkeystore", signer_key,
                    "-deststoretype", "JKS", "-deststorepass", store_password,
                    "-destalias", alias, "-destkeypass", key_password, "-noprompt",
                ])
            command += [
                "--ks", signer_key, "--ksAlias", alias,
                "--ksPass", store_password, "--ksKeyPass", key_password,
            ]
            _run(command)


def _java_under(root: str) -> list:
    out = []
    for dirpath, _dirs, files in os.walk(root):
        out += [os.path.join(dirpath, f) for f in files if f.endswith(".java")]
    return sorted(out)


def _classes_under(root: str) -> list:
    out = []
    for dirpath, _dirs, files in os.walk(root):
        out += [os.path.join(dirpath, f) for f in files if f.endswith(".class")]
    return sorted(out)


def _java() -> str:
    javac = shutil.which("javac")
    if javac:
        name = "java.exe" if os.name == "nt" else "java"
        beside = os.path.join(os.path.dirname(os.path.abspath(javac)), name)
        if os.path.isfile(beside):
            return beside
    return shutil.which("java") or "java"


def _keytool() -> str:
    javac = shutil.which("javac")
    if javac:
        name = "keytool.exe" if os.name == "nt" else "keytool"
        beside = os.path.join(os.path.dirname(os.path.abspath(javac)), name)
        if os.path.isfile(beside):
            return beside
    found = shutil.which("keytool")
    if not found:
        raise RuntimeError("keytool is required to sign with the supplied keystore")
    return found


def _run(command: list) -> None:
    environment = dict(os.environ)
    environment.pop("JAVA_TOOL_OPTIONS", None)
    result = subprocess.run(command, capture_output=True, text=True, env=environment)
    if result.returncode != 0:
        sys.stderr.write(result.stdout + result.stderr)
        raise RuntimeError("%s failed" % os.path.basename(command[0]))
