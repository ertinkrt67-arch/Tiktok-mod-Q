
from __future__ import annotations

import getpass
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile
from dataclasses import dataclass
from typing import Callable, Mapping, Optional, Tuple

from . import toolchain as toolchain_module
from .apkzip import Apk

STORE_PASSWORD_VARIABLE = "SOUFIT_KEYSTORE_PASSWORD"
KEY_PASSWORD_VARIABLE = "SOUFIT_KEY_PASSWORD"

V2_MAGIC = b"APK Sig Block 42"
EOCD_SIGNATURE = b"PK\x05\x06"
EOCD_SEARCH = 65557


class SigningError(RuntimeError):
    pass


@dataclass
class Signature:
    v1: bool
    v2: bool


@dataclass
class KeyInfo:
    alias: str
    sha256: str


def ask_for_password(prompt: str) -> str:
    if not sys.stdin.isatty():
        raise SigningError(
            "no keystore password: set %s, or pass --password-file, because there is "
            "no terminal to type it on" % STORE_PASSWORD_VARIABLE
        )
    return getpass.getpass(prompt)


def resolve_passwords(keystore: Optional[str], password_file: Optional[str],
                      environment: Mapping[str, str],
                      ask: Callable[[str], str] = ask_for_password,
                      store_password: Optional[str] = None,
                      key_password: Optional[str] = None) -> Tuple[Optional[str], Optional[str]]:
    if not keystore:
        return None, None
    store = store_password or None
    if not store and password_file:
        try:
            with open(password_file, encoding="utf-8") as handle:
                store = handle.readline().rstrip("\r\n")
        except OSError as error:
            raise SigningError("cannot read the password file: %s" % error)
    if not store:
        store = environment.get(STORE_PASSWORD_VARIABLE)
    if not store:
        store = ask("keystore password: ")
    if not store:
        raise SigningError("the keystore password is empty")
    key = key_password or environment.get(KEY_PASSWORD_VARIABLE) or store
    return store, key


def key_info(keystore: str, store_password: str, alias: Optional[str] = None) -> KeyInfo:
    keytool = toolchain_module._keytool()
    command = [keytool, "-list", "-v", "-keystore", keystore, "-storepass", store_password]
    if alias:
        command += ["-alias", alias]
    result = subprocess.run(command, capture_output=True, text=True)
    details = result.stdout + result.stderr
    if result.returncode != 0:
        if "password was incorrect" in details:
            raise SigningError("the keystore password is wrong")
        raise SigningError("keytool could not read %s: %s" % (keystore, details.strip()))
    name = re.search(r"^Alias name:\s*(.+?)\s*$", details, re.MULTILINE)
    fingerprint = re.search(r"SHA256:\s*([0-9A-F:]+)", details)
    if not name or not fingerprint:
        raise SigningError("keytool printed no certificate for %s" % keystore)
    return KeyInfo(name.group(1), fingerprint.group(1))


def check(path: str) -> Signature:
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
    meta = [name for name in names if name.startswith("META-INF/")]
    v1 = ("META-INF/MANIFEST.MF" in names
          and any(name.upper().endswith(".SF") for name in meta)
          and any(name.upper().endswith((".RSA", ".DSA", ".EC")) for name in meta))
    return Signature(v1=v1, v2=_has_v2_block(path))


def _has_v2_block(path: str) -> bool:
    with open(path, "rb") as handle:
        handle.seek(0, os.SEEK_END)
        size = handle.tell()
        handle.seek(max(0, size - EOCD_SEARCH))
        tail = handle.read()
        at = tail.rfind(EOCD_SIGNATURE)
        if at < 0 or at + 22 > len(tail):
            return False
        central = struct.unpack_from("<I", tail, at + 16)[0]
        if central < len(V2_MAGIC):
            return False
        handle.seek(central - len(V2_MAGIC))
        return handle.read(len(V2_MAGIC)) == V2_MAGIC


def sign_into(source: str, output: str, tools, keystore: Optional[str] = None,
              alias: Optional[str] = None, password_file: Optional[str] = None,
              environment: Optional[Mapping[str, str]] = None,
              ask: Callable[[str], str] = ask_for_password,
              store_password: Optional[str] = None,
              key_password: Optional[str] = None) -> Tuple[Optional[KeyInfo], Signature]:
    environment = os.environ if environment is None else environment
    if not os.path.isfile(source) or not zipfile.is_zipfile(source):
        raise SigningError("no apk at %s" % source)
    if keystore and not os.path.isfile(keystore):
        raise SigningError("no keystore at %s" % keystore)

    store, key = resolve_passwords(keystore, password_file, environment, ask,
                                   store_password, key_password)
    info = key_info(keystore, store, alias) if keystore else None
    if info:
        alias = info.alias

    os.makedirs(os.path.dirname(os.path.abspath(output)), exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="soufit-sign-") as scratch:
        staged = os.path.join(scratch, "soufit.apk")
        apk = Apk(source)
        try:
            apk.drop_signature()
            apk.write(staged)
        finally:
            apk.close()
        tools.sign(staged, keystore, alias or "androiddebugkey", store or "android", key or "android")
        signature = check(staged)
        if not (signature.v1 or signature.v2):
            raise SigningError("signing did not leave a signature in the apk")
        shutil.move(staged, output)
    return info, signature
