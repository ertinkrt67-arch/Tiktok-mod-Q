
from __future__ import annotations

import argparse
import json
import math
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Tuple

LOW, MEDIUM, HIGH = 0, 1, 2
LEVEL_NAMES = {LOW: "low", MEDIUM: "medium", HIGH: "high"}
MARK = {LOW: "•", MEDIUM: "▲", HIGH: "⚠"}

MAX_DEX = 16 * 1024 * 1024
MAX_TOTAL = 17 * 1024 * 1024
ALLOWED = {"manifest.json", "classes.dex", "icon.png"}
NESTED = (".dex", ".jar", ".apk", ".so", ".zip", ".mtp", ".aar")

SENSITIVE_CLASSES = (
    "Runtime", "ProcessBuilder", "ClassLoader", "DexFile", "Telephony",
    "ContactsContract", "AccountManager", "Clipboard", "WebView", "Cipher",
    "Socket", "Contacts", "Method", "Field",
)
FRAMEWORK_NAME = re.compile(r"(android|java|javax|dalvik)(\.[A-Za-z_]\w*)+\.[A-Z][\w$]*")
ENCODED = re.compile(r"[A-Za-z0-9+/=_-]+")
B64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

FRAMEWORK_PREFIXES = ("Landroid/", "Ljava/", "Ljavax/", "Ldalvik/", "Lkotlin/",
                      "Lorg/", "Lcom/google/", "Landroidx/")


class ScanError(Exception):
    pass


@dataclass
class Report:
    findings: Dict[str, int] = field(default_factory=dict)

    def add(self, text: str, level: int) -> None:
        self.findings[text] = max(level, self.findings.get(text, LOW))

    @property
    def risk(self) -> int:
        return max(self.findings.values(), default=LOW)

    def summary(self) -> str:
        lines = ["Risk level: " + LEVEL_NAMES[self.risk]]
        for text, level in self.findings.items():
            lines.append("%s %s" % (MARK[level], text))
        if not self.findings:
            lines.append("Nothing unusual found.")
        return "\n".join(lines)



def check_archive(path: str, report: Report) -> Dict[str, bytes]:
    try:
        archive = zipfile.ZipFile(path)
    except (zipfile.BadZipFile, OSError) as error:
        raise ScanError("not an archive: %s" % error)
    total = 0
    contents: Dict[str, bytes] = {}
    with archive:
        for info in archive.infolist():
            name = info.filename
            parts = name.split("/")
            if name.startswith("/") or "\\" in name or ".." in parts or ":" in parts[0]:
                report.add("Archive path leaves the plugin: " + name, HIGH)
                continue
            if name.endswith("/"):
                continue
            if name not in ALLOWED:
                level = HIGH if name.lower().endswith(NESTED) else MEDIUM
                report.add("Extra file in the plugin: " + name, level)
                continue
            total += info.file_size
            if total > MAX_TOTAL:
                report.add("The archive unpacks to too large a size", HIGH)
                break
            contents[name] = archive.read(name)
    return contents


def check_manifest(contents: Dict[str, bytes], report: Report) -> Optional[str]:
    raw = contents.get("manifest.json")
    if raw is None:
        raise ScanError("the plugin has no manifest.json")
    try:
        manifest = json.loads(raw.decode("utf-8"))
    except (UnicodeDecodeError, ValueError) as error:
        raise ScanError("manifest.json cannot be read: %s" % error)
    entry = manifest.get("entry")
    if not isinstance(entry, str) or not entry:
        report.add("manifest.json names no entry point", MEDIUM)
        return None
    return entry



class Dex:

    def __init__(self, data: bytes):
        if len(data) < 112 or len(data) > MAX_DEX or data[:4] != b"dex\n" or data[7] != 0:
            raise ScanError("not a DEX file")
        if data[4:7].decode("ascii", "replace") not in ("035", "037", "038", "039", "040"):
            raise ScanError("unsupported DEX version")
        self.b = data
        if self.u32(32) != len(data) or self.u32(36) != 112 or self.u32(40) != 0x12345678:
            raise ScanError("invalid DEX header")
        self.strings = [self._read_string(i) for i in range(self.u32(56))]
        type_off = self.u32(68)
        self.types = [self.strings[self.u32(type_off + 4 * i)] for i in range(self.u32(64))]
        self.method_count, self.method_off = self.u32(88), self.u32(92)
        self.class_count, self.class_off = self.u32(96), self.u32(100)

    def u32(self, p: int) -> int:
        if p < 0 or p + 4 > len(self.b):
            raise ScanError("table out of DEX bounds")
        return struct.unpack_from("<I", self.b, p)[0]

    def u16(self, p: int) -> int:
        if p < 0 or p + 2 > len(self.b):
            raise ScanError("table out of DEX bounds")
        return struct.unpack_from("<H", self.b, p)[0]

    def _uleb_at(self, pos: List[int]) -> int:
        value = 0
        for i in range(5):
            if pos[0] >= len(self.b):
                raise ScanError("table out of DEX bounds")
            v = self.b[pos[0]]
            pos[0] += 1
            value |= (v & 127) << (i * 7)
            if v & 128 == 0:
                break
        return value

    def _read_string(self, index: int) -> str:
        offset = self.u32(self.u32(60) + 4 * index)
        pos = [offset]
        self._uleb_at(pos)
        end = self.b.find(b"\x00", pos[0])
        if end < 0:
            raise ScanError("DEX string does not end")
        return self.b[pos[0]:end].decode("utf-8", "replace")

    def methods(self):
        for i in range(self.method_count):
            p = self.method_off + i * 8
            yield self.types[self.u16(p)], self.strings[self.u32(p + 4)]

    def native_methods(self) -> int:
        found = 0
        for i in range(self.class_count):
            at = self.u32(self.class_off + i * 32 + 24)
            if at == 0:
                continue
            cursor = [at]
            sf, inf, dm, vm = (self._uleb_at(cursor) for _ in range(4))
            for _ in range(sf + inf):
                self._uleb_at(cursor)
                self._uleb_at(cursor)
            for _ in range(dm + vm):
                self._uleb_at(cursor)
                flags = self._uleb_at(cursor)
                self._uleb_at(cursor)
                if flags & 256:
                    found += 1
        return found


def framework(type_name: str) -> bool:
    return type_name.startswith(FRAMEWORK_PREFIXES)


def simple_name(type_name: str) -> str:
    t = type_name[:-1] if type_name.endswith(";") else type_name
    return t.rsplit("/", 1)[-1]


def entropy(s: str) -> float:
    if not s:
        return 0.0
    n = len(s)
    counts: Dict[str, int] = {}
    for ch in s:
        counts[ch] = counts.get(ch, 0) + 1
    return -sum((c / n) * math.log(c / n, 2) for c in counts.values())


def unbase64(s: str) -> Optional[bytes]:
    t = s.replace("-", "+").replace("_", "/").rstrip("=")
    if not t or len(t) % 4 == 1:
        return None
    out = bytearray()
    bits = value = 0
    for ch in t:
        v = B64.find(ch)
        if v < 0:
            return None
        value = (value << 6) | v
        bits += 6
        if bits >= 8:
            bits -= 8
            out.append((value >> bits) & 255)
    return bytes(out)


def readable(data: Optional[bytes]) -> bool:
    if not data or len(data) < 16:
        return False
    text = sum(1 for c in data if 0x20 <= c < 0x7F or c in (0x0A, 0x0D, 0x09))
    return text * 10 >= len(data) * 9


def analyse_dex(data: bytes, entry: Optional[str], report: Report) -> None:
    dex = Dex(data)
    types: Set[str] = set(dex.types)

    plugin = short = 0
    for t in dex.types:
        if t.startswith("Ldalvik/system/") and ("ClassLoader" in t or "DexFile" in t):
            report.add("Loads additional executable code", HIGH)
        if t.startswith(("Ljava/lang/reflect/", "Ljava/lang/invoke/")):
            report.add("Reflection: some actions cannot be determined statically", MEDIUM)
        if t.startswith(("Ljava/net/", "Lokhttp", "Lretrofit")):
            report.add("Network access", MEDIUM)
        if t == "Landroid/webkit/WebView;":
            report.add("WebView and possible JavaScript execution", MEDIUM)
        if t.startswith(("Landroid/telephony/", "Landroid/provider/Contacts", "Landroid/accounts/")):
            report.add("Access to personal device data", MEDIUM)
        if t == "Landroid/content/ClipboardManager;":
            report.add("Clipboard access", LOW)
        if t.startswith(("Ljava/io/", "Ljava/nio/file/")):
            report.add("Direct access to the app's files", LOW)
        if t.startswith("L") and not framework(t):
            plugin += 1
            if len(simple_name(t)) <= 2:
                short += 1
    if plugin >= 8 and short * 10 >= plugin * 6:
        report.add("Class names are obfuscated: %d of %d" % (short, plugin), MEDIUM)
    if entry and ("L" + entry.replace(".", "/") + ";") not in types:
        report.add("Plugin entry point not found in the code: " + entry, HIGH)

    for owner, name in dex.methods():
        if (owner == "Ljava/lang/Runtime;" and name == "exec") or \
           (owner == "Ljava/lang/ProcessBuilder;" and name == "start"):
            report.add("Runs system commands", HIGH)
        if owner in ("Ljava/lang/System;", "Ljava/lang/Runtime;") and name in ("load", "loadLibrary"):
            report.add("Loads native libraries", HIGH)
        if owner == "Ljava/lang/Class;" and name == "forName":
            report.add("Class lookup by name: API may be loaded covertly", MEDIUM)
        if owner in ("Lcat/narezany/margyt/plugin/PluginContext;",) and name in ("fetch", "download"):
            report.add("Network through the mod API", MEDIUM)
        if owner == "Lbk/soufit/core/Net;":
            report.add("Network through the mod API", MEDIUM)
        if owner in ("Lcat/narezany/margyt/plugin/PluginContext;", "Lbk/soufit/core/soufiT;") \
           and name == "context":
            report.add("Access to the TikTok context", LOW)
        if owner == "Ljava/lang/ClassLoader;" and name == "loadClass":
            report.add("Loads classes by name", MEDIUM)

    hosts: List[str] = []
    encoded = 0
    for s in dex.strings:
        if s.startswith(("http://", "https://")):
            host = re.split(r"[/?:]", s.split("://", 1)[1], 1)[0]
            if host and len(hosts) < 5 and host not in hosts:
                hosts.append(host)
        if len(s) >= 32 and " " not in s and "." not in s and ENCODED.fullmatch(s) \
           and readable(unbase64(s)):
            encoded += 1
        if FRAMEWORK_NAME.fullmatch(s):
            simple = s.rsplit(".", 1)[-1]
            if any(word in simple for word in SENSITIVE_CLASSES):
                report.add("A system class name is written as a string: the call is hidden from review", HIGH)
        if s == "/system/bin/su" or "busybox" in s or "su -c" in s:
            report.add("Searches for root access", HIGH)

    if encoded >= 2:
        report.add("Hidden text: encoded strings that read as text or code: %d" % encoded,
                   MEDIUM)
    if hosts:
        report.add("Addresses in the code: " + ", ".join(hosts), LOW)
    if dex.native_methods():
        report.add("Native methods outside the reach of Java checks", HIGH)



JADX_RULES = [
    (r"Runtime\.getRuntime\(\)\s*\.exec|new\s+ProcessBuilder|ProcessBuilder\(", "Runs system commands", HIGH),
    (r"DexClassLoader|PathClassLoader|InMemoryDexClassLoader", "Loads additional executable code", HIGH),
    (r"System\.loadLibrary|System\.load\(|Runtime\.getRuntime\(\)\.load", "Loads native libraries", HIGH),
    (r"Class\.forName\s*\([^)]*\+", "Class name is assembled at run time", HIGH),
    (r"\.invoke\s*\(", "Calls methods by name (reflection)", MEDIUM),
    (r"Base64\.decode\s*\(", "Decodes strings in the code", MEDIUM),
    (r"Cipher\.getInstance|SecretKeySpec", "Encryption in the code", MEDIUM),
    (r"SmsManager|sendTextMessage", "Sends SMS", HIGH),
    (r"getDeviceId|getSubscriberId|getSimSerialNumber|getAccounts\s*\(", "Access to personal device data", MEDIUM),
]


def scan_jadx(dex_path: str, jadx: str, report: Report) -> None:
    out = tempfile.mkdtemp(prefix="mtp-jadx-")
    try:
        result = subprocess.run([jadx, "-d", out, dex_path], capture_output=True, text=True, timeout=600)
        if result.returncode not in (0, 1):
            raise ScanError("jadx could not parse the dex: " + result.stderr[-300:])
        sources = []
        for root, _, names in os.walk(out):
            for name in names:
                if name.endswith(".java"):
                    with open(os.path.join(root, name), encoding="utf-8", errors="replace") as handle:
                        sources.append(handle.read())
        code = "\n".join(sources)
        if not code:
            report.add("jadx got no source code: the bytecode check was done, but it is not enough", MEDIUM)
            return
        for pattern, text, level in JADX_RULES:
            if re.search(pattern, code):
                report.add(text, level)
        names = re.findall(r"\b(?:class|interface|enum)\s+(\w+)", code)
        if len(names) >= 8 and sum(1 for n in names if len(n) <= 2) * 10 >= len(names) * 6:
            report.add("Sources are obfuscated: short class names in the decompiled output", MEDIUM)
    except FileNotFoundError:
        raise ScanError("jadx not found at: " + jadx)
    finally:
        shutil.rmtree(out, ignore_errors=True)



def scan(path: str, jadx: Optional[str] = None) -> Report:
    report = Report()
    contents = check_archive(path, report)
    entry = check_manifest(contents, report)
    if "classes.dex" not in contents:
        raise ScanError("the plugin has no classes.dex")
    analyse_dex(contents["classes.dex"], entry, report)
    if jadx:
        with tempfile.TemporaryDirectory(prefix="mtp-dex-") as folder:
            dex_path = os.path.join(folder, "classes.dex")
            with open(dex_path, "wb") as handle:
                handle.write(contents["classes.dex"])
            scan_jadx(dex_path, jadx, report)
    return report


def main(argv: Optional[List[str]] = None) -> int:
    parser = argparse.ArgumentParser(description="Check a soufiT plugin (.mtp)")
    parser.add_argument("plugin", help="path to the .mtp file")
    parser.add_argument("--jadx", help="path to jadx: decompile the dex and check the sources")
    args = parser.parse_args(argv)
    try:
        report = scan(args.plugin, args.jadx)
    except ScanError as error:
        print("Could not check the plugin: %s" % error, file=sys.stderr)
        return 3
    print(report.summary())
    return {LOW: 0, MEDIUM: 1, HIGH: 2}[report.risk]


if __name__ == "__main__":
    sys.exit(main())
