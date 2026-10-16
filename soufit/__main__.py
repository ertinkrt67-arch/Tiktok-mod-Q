
from __future__ import annotations

import argparse
import os
import sys
import tempfile
from typing import List, Optional

from .build import Build
from .sign import SigningError, check, sign_into
from .toolchain import Toolchain

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BUILD_DIR = os.path.join(ROOT, "build")
UNSIGNED_APK = os.path.join(BUILD_DIR, "soufit-unsigned.apk")
SIGNED_APK = os.path.join(BUILD_DIR, "soufit.apk")
COMMANDS = ("build", "build-sign", "sign", "verify", "tools")


def normalise(argv: List[str]) -> List[str]:
    if not argv or argv[0] in ("-h", "--help") or argv[0] in COMMANDS:
        return list(argv)
    return ["build"] + list(argv)


def add_signing_options(parser: argparse.ArgumentParser) -> None:
    group = parser.add_argument_group(
        "signing",
        "Without --keystore the built-in debug key is used, which is fine for testing "
        "and not for release. The keystore password comes from SOUFIT_KEYSTORE_PASSWORD, "
        "from --password-file, or by asking; the key password from SOUFIT_KEY_PASSWORD, "
        "defaulting to the keystore password.",
    )
    group.add_argument("--keystore", metavar="PATH", help="sign with this keystore (PKCS12 or JKS)")
    group.add_argument("--alias", help="key alias (default: the keystore's first key)")
    group.add_argument("--password-file", metavar="PATH",
                       help="read the keystore password from the first line of this file")
    group.add_argument("--keystore-pass", metavar="PASSWORD",
                       help="keystore password on the command line (visible to other users and in shell history; prefer --password-file or the environment)")
    group.add_argument("--key-pass", metavar="PASSWORD",
                       help="key password, if it differs from the keystore password")


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="build.sh", description="Build, sign and check soufiT.")
    commands = parser.add_subparsers(dest="command", metavar="COMMAND")

    build = commands.add_parser("build", help="build the mod from a TikTok apk, then sign it")
    build.add_argument("apk", help="a universal TikTok apk (type APK, not BUNDLE)")
    build.add_argument("--unsigned", action="store_true",
                       help="stop after building; writes build/soufit-unsigned.apk (or -o)")
    build.add_argument("-o", "--out", help="where the finished apk goes")
    build.add_argument("--accent", metavar="RRGGBB",
                       help="bake this colour in where TikTok's pink is a picture rather than code")
    build.add_argument("--work", default=os.path.join(ROOT, "work"),
                       help="scratch space for the dex files being rewritten")
    build.add_argument("--tools", default=os.path.join(ROOT, "tools"),
                       help="where the downloaded build tools are kept")
    add_signing_options(build)

    build_sign = commands.add_parser("build-sign", help="build and sign; only the signed apk is kept")
    build_sign.add_argument("apk", help="a universal TikTok apk (type APK, not BUNDLE)")
    build_sign.add_argument("-o", "--out", help="where the signed apk goes (default: build/soufit.apk)")
    build_sign.add_argument("--accent", metavar="RRGGBB",
                            help="bake this colour in where TikTok's pink is a picture rather than code")
    build_sign.add_argument("--work", default=os.path.join(ROOT, "work"),
                            help="scratch space for the dex files being rewritten")
    build_sign.add_argument("--tools", default=os.path.join(ROOT, "tools"),
                            help="where the downloaded build tools are kept")
    add_signing_options(build_sign)

    sign = commands.add_parser("sign", help="sign an apk that is already built")
    sign.add_argument("apk", help="the apk to sign; it is not changed")
    sign.add_argument("-o", "--out", help="where the signed apk goes (default: build/soufit.apk)")
    sign.add_argument("--tools", default=os.path.join(ROOT, "tools"),
                      help="where the downloaded build tools are kept")
    add_signing_options(sign)

    verify = commands.add_parser("verify", help="check that an apk carries a signature")
    verify.add_argument("apk")

    tools = commands.add_parser("tools", help="look after the downloaded build tools")
    tools.add_argument("action", choices=["hashes"], help="hashes: print the SHA-256 of each cached tool")
    tools.add_argument("--tools", default=os.path.join(ROOT, "tools"))
    return parser


def parse_accent(text: Optional[str], parser: argparse.ArgumentParser) -> Optional[int]:
    if not text:
        return None
    try:
        return 0xFF000000 | int(text.lstrip("#"), 16)
    except ValueError:
        parser.error("--accent wants six hex digits, like 8DD1B0")


def report_signing(info, signature, output: str) -> None:
    if info:
        print("signed with %s, key SHA-256 %s" % (info.alias, info.sha256))
    else:
        print("signed with the built-in debug key: fine for testing, not for release")
    if not signature.v2:
        print("warning: no v2 signature; Android 7.0 and later will refuse this apk")
    print("signed apk: %s" % output)


def sign_now(source: str, output: str, args) -> int:
    if args.keystore_pass:
        print("warning: --keystore-pass is visible to other users in the process list; "
              "prefer SOUFIT_KEYSTORE_PASSWORD or --password-file", file=sys.stderr)
    info, signature = sign_into(source, output, Toolchain(args.tools), args.keystore,
                                args.alias, args.password_file, store_password=args.keystore_pass,
                                key_password=args.key_pass)
    report_signing(info, signature, output)
    return 0


def cmd_build(args, parser) -> int:
    accent = parse_accent(args.accent, parser)
    tools = Toolchain(args.tools)
    unsigned = args.out if args.unsigned and args.out else UNSIGNED_APK
    Build(args.apk, unsigned, ROOT, tools, args.work, accent).run()
    if args.unsigned:
        print("unsigned apk: %s" % unsigned)
        return 0
    return sign_now(unsigned, args.out or SIGNED_APK, args)


def cmd_build_sign(args, parser) -> int:
    accent = parse_accent(args.accent, parser)
    tools = Toolchain(args.tools)
    with tempfile.TemporaryDirectory(prefix="soufit-build-") as scratch:
        unsigned = os.path.join(scratch, "soufit-unsigned.apk")
        Build(args.apk, unsigned, ROOT, tools, args.work, accent).run()
        return sign_now(unsigned, args.out or SIGNED_APK, args)


def cmd_sign(args) -> int:
    return sign_now(args.apk, args.out or SIGNED_APK, args)


def cmd_verify(args) -> int:
    if not os.path.isfile(args.apk):
        raise SigningError("no apk at %s" % args.apk)
    signature = check(args.apk)
    print("v1 (JAR) signature:          %s" % ("yes" if signature.v1 else "no"))
    print("v2 (APK signing block):      %s" % ("yes" if signature.v2 else "no"))
    if not signature.v2:
        print("warning: no v2 signature; Android 7.0 and later will refuse this apk")
    if not (signature.v1 or signature.v2):
        print("this apk is not signed")
        return 1
    return 0


def cmd_tools(args) -> int:
    for name, digest in sorted(Toolchain(args.tools, quiet=True).hashes().items()):
        print("%s  %s" % (digest, name))
    return 0


def main(argv: Optional[List[str]] = None) -> int:
    argv = normalise(sys.argv[1:] if argv is None else argv)
    parser = build_parser()
    if not argv:
        parser.print_help()
        return 0
    args = parser.parse_args(argv)
    if args.command is None:
        parser.print_help()
        return 0
    try:
        if args.command == "build":
            return cmd_build(args, parser)
        if args.command == "build-sign":
            return cmd_build_sign(args, parser)
        if args.command == "sign":
            return cmd_sign(args)
        if args.command == "verify":
            return cmd_verify(args)
        return cmd_tools(args)
    except Exception as error:
        label = {"build": "Build", "build-sign": "Build and signing", "sign": "Signing",
                 "verify": "Verify", "tools": "Tools"}[args.command]
        print("\n\033[1;31m%s failed:\033[0m %s" % (label, error), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
