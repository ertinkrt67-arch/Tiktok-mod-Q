
import argparse
import hashlib
import io
import os
import shutil
import struct
import subprocess
import sys
import tempfile
import unittest
from unittest import mock

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from soufit import __main__ as cli, sign, toolchain as toolchain_module


def write_zip(path, members):
    import zipfile
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as archive:
        for name, data in members.items():
            archive.writestr(name, data)


def digest(path):
    with open(path, "rb") as handle:
        return hashlib.sha256(handle.read()).hexdigest()


def read_bytes(path):
    with open(path, "rb") as handle:
        return handle.read()


def write_bytes(path, data):
    with open(path, "wb") as handle:
        handle.write(data)


def with_v2_block(path, out):
    data = bytearray(read_bytes(path))
    eocd = data.rfind(sign.EOCD_SIGNATURE)
    central = struct.unpack_from("<I", data, eocd + 16)[0]
    data[central:central] = sign.V2_MAGIC
    struct.pack_into("<I", data, eocd + 16 + len(sign.V2_MAGIC), central + len(sign.V2_MAGIC))
    write_bytes(out, bytes(data))


FAKE_JAVA = '''#!PYTHON
import os, sys, zipfile
args = sys.argv[1:]
apk = args[args.index("-a") + 1]
if not apk.endswith(".apk") or not os.path.isfile(apk):
    sys.stderr.write("provided apk path or file '%s' does not exist\\n" % apk)
    sys.exit(1)
if "--ks" not in args:
    sys.exit(1)
with zipfile.ZipFile(apk, "a") as archive:
    archive.writestr("META-INF/CERT.SF", b"sf")
    archive.writestr("META-INF/CERT.RSA", b"rsa")
    archive.writestr("META-INF/MANIFEST.MF", b"mf")
'''


class FakeTools:
    def __init__(self):
        self.calls = []

    def sign(self, apk_path, key=None, alias="androiddebugkey", store_password="android",
             key_password="android"):
        if not apk_path.endswith(".apk") or not os.path.isfile(apk_path):
            raise RuntimeError("provided apk path or file '%s' does not exist" % apk_path)
        self.calls.append((key, alias, store_password, key_password))
        import zipfile
        with zipfile.ZipFile(apk_path, "a") as archive:
            archive.writestr("META-INF/CERT.SF", b"sf")
            archive.writestr("META-INF/CERT.RSA", b"rsa")
            archive.writestr("META-INF/MANIFEST.MF", b"mf")


class CommandLineTest(unittest.TestCase):
    def test_a_bare_apk_means_build(self):
        self.assertEqual(cli.normalise(["tiktok.apk", "-o", "x.apk"]),
                         ["build", "tiktok.apk", "-o", "x.apk"])

    def test_commands_and_help_are_left_alone(self):
        for argv in (["sign", "a.apk"], ["verify", "a.apk"], ["tools", "hashes"], ["-h"], []):
            self.assertEqual(cli.normalise(argv), argv)

    def test_build_takes_the_signing_options(self):
        args = cli.build_parser().parse_args(
            ["build", "t.apk", "--keystore", "k.p12", "--alias", "a", "--password-file", "p"])
        self.assertEqual((args.keystore, args.alias, args.password_file), ("k.p12", "a", "p"))

    def test_build_sign_takes_the_password_flags(self):
        args = cli.build_parser().parse_args(
            ["build-sign", "t.apk", "--keystore", "k.p12", "--keystore-pass", "pw", "--key-pass", "kp"])
        self.assertEqual((args.keystore_pass, args.key_pass), ("pw", "kp"))

    def test_build_sign_is_a_command(self):
        self.assertEqual(cli.normalise(["build-sign", "t.apk"]), ["build-sign", "t.apk"])

    def test_bad_accent_is_a_usage_error(self):
        parser = cli.build_parser()
        with self.assertRaises(SystemExit):
            cli.parse_accent("nothex", parser)


class PasswordTest(unittest.TestCase):
    @staticmethod
    def no_asking(prompt):
        raise AssertionError("should not have asked for a password")

    def test_the_environment_supplies_both_passwords(self):
        env = {"SOUFIT_KEYSTORE_PASSWORD": "store", "SOUFIT_KEY_PASSWORD": "key"}
        self.assertEqual(sign.resolve_passwords("k.p12", None, env, self.no_asking), ("store", "key"))

    def test_the_key_password_defaults_to_the_store_password(self):
        env = {"SOUFIT_KEYSTORE_PASSWORD": "store"}
        self.assertEqual(sign.resolve_passwords("k.p12", None, env, self.no_asking), ("store", "store"))

    def test_the_debug_key_needs_no_password(self):
        self.assertEqual(sign.resolve_passwords(None, None, {}, self.no_asking), (None, None))

    def test_a_password_flag_beats_the_environment_and_the_file(self):
        env = {"SOUFIT_KEYSTORE_PASSWORD": "fromenv"}
        self.assertEqual(sign.resolve_passwords("k.p12", None, env, self.no_asking,
                                                store_password="fromflag"), ("fromflag", "fromflag"))

    def test_a_missing_password_is_asked_for(self):
        self.assertEqual(sign.resolve_passwords("k.p12", None, {}, lambda p: "typed"), ("typed", "typed"))

    def test_an_empty_password_is_refused(self):
        with self.assertRaises(sign.SigningError):
            sign.resolve_passwords("k.p12", None, {}, lambda p: "")

    def test_a_password_file_beats_the_environment(self):
        directory = tempfile.mkdtemp(prefix="soufit-pw-")
        try:
            path = os.path.join(directory, "pw.txt")
            with open(path, "w", encoding="utf-8") as handle:
                handle.write("fromfile\nignored\n")
            env = {"SOUFIT_KEYSTORE_PASSWORD": "fromenv"}
            self.assertEqual(sign.resolve_passwords("k.p12", path, env, self.no_asking)[0], "fromfile")
        finally:
            shutil.rmtree(directory)

    def test_without_a_terminal_it_says_how_to_give_the_password(self):
        with mock.patch.object(sys, "stdin", io.StringIO("")):
            with self.assertRaises(sign.SigningError) as context:
                sign.ask_for_password("keystore password: ")
        self.assertIn("SOUFIT_KEYSTORE_PASSWORD", str(context.exception))


class CheckTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.mkdtemp(prefix="soufit-check-")

    def tearDown(self):
        shutil.rmtree(self.directory)

    def path(self, name):
        return os.path.join(self.directory, name)

    def test_a_jar_signature_is_found(self):
        write_zip(self.path("a.apk"), {"META-INF/MANIFEST.MF": b"m", "META-INF/CERT.SF": b"s",
                                      "META-INF/CERT.RSA": b"r", "classes.dex": b"d"})
        result = sign.check(self.path("a.apk"))
        self.assertTrue(result.v1)
        self.assertFalse(result.v2)

    def test_an_unsigned_apk_has_neither(self):
        write_zip(self.path("a.apk"), {"classes.dex": b"d"})
        result = sign.check(self.path("a.apk"))
        self.assertFalse(result.v1)
        self.assertFalse(result.v2)

    def test_the_v2_block_is_found_before_the_central_directory(self):
        write_zip(self.path("a.apk"), {"classes.dex": b"d" * 50})
        with_v2_block(self.path("a.apk"), self.path("b.apk"))
        self.assertTrue(sign.check(self.path("b.apk")).v2)
        self.assertFalse(sign.check(self.path("a.apk")).v2)

    def test_a_signature_file_without_a_manifest_is_not_v1(self):
        write_zip(self.path("a.apk"), {"META-INF/CERT.SF": b"s", "META-INF/CERT.RSA": b"r"})
        self.assertFalse(sign.check(self.path("a.apk")).v1)


class SignTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.mkdtemp(prefix="soufit-sign-")
        self.source = os.path.join(self.directory, "built.apk")
        self.output = os.path.join(self.directory, "out", "soufit.apk")
        write_zip(self.source, {"classes.dex": b"dex", "META-INF/OLD.RSA": b"old",
                                "META-INF/OLD.SF": b"old", "META-INF/MANIFEST.MF": b"old"})

    def tearDown(self):
        shutil.rmtree(self.directory)

    def test_the_input_is_left_alone_and_the_old_signature_is_dropped(self):
        before = digest(self.source)
        tools = FakeTools()
        info, signature = sign.sign_into(self.source, self.output, tools, environment={})
        self.assertEqual(digest(self.source), before)
        self.assertIsNone(info)
        self.assertTrue(signature.v1)
        import zipfile
        names = zipfile.ZipFile(self.output).namelist()
        self.assertNotIn("META-INF/OLD.RSA", names)
        self.assertIn("classes.dex", names)
        self.assertEqual(tools.calls, [(None, "androiddebugkey", "android", "android")])

    def test_a_failed_signing_leaves_no_output_behind(self):
        class Silent(FakeTools):
            def sign(self, apk_path, *args, **kwargs):
                self.calls.append(apk_path)
        with self.assertRaises(sign.SigningError):
            sign.sign_into(self.source, self.output, Silent(), environment={})
        self.assertFalse(os.path.exists(self.output))
        self.assertEqual(os.listdir(os.path.dirname(self.output)), [])

    def test_a_missing_input_is_reported(self):
        with self.assertRaises(sign.SigningError):
            sign.sign_into(os.path.join(self.directory, "nope.apk"), self.output, FakeTools(), environment={})


@unittest.skipUnless(shutil.which("keytool"), "keytool is needed to make a test keystore")
class KeystoreTest(unittest.TestCase):
    PASSWORD = "test-pass-1"

    @classmethod
    def setUpClass(cls):
        cls.directory = tempfile.mkdtemp(prefix="soufit-keystore-")
        cls.keystore = os.path.join(cls.directory, "release.p12")
        subprocess.run(
            ["keytool", "-genkeypair", "-keystore", cls.keystore, "-storetype", "PKCS12",
             "-storepass", cls.PASSWORD, "-keypass", cls.PASSWORD, "-alias", "soufit",
             "-dname", "CN=soufiT test", "-keyalg", "RSA", "-keysize", "2048",
             "-validity", "3650", "-noprompt"],
            check=True, capture_output=True,
        )

    @classmethod
    def tearDownClass(cls):
        shutil.rmtree(cls.directory)

    def test_the_first_key_and_its_fingerprint_are_read(self):
        info = sign.key_info(self.keystore, self.PASSWORD)
        self.assertEqual(info.alias, "soufit")
        self.assertRegex(info.sha256, r"^([0-9A-F]{2}:){31}[0-9A-F]{2}$")

    def test_a_wrong_password_is_reported_plainly(self):
        with self.assertRaises(sign.SigningError) as context:
            sign.key_info(self.keystore, "not-the-password")
        self.assertIn("password is wrong", str(context.exception))

    def test_build_sign_leaves_only_the_signed_apk(self):
        class FakeBuild:
            def __init__(self, apk, out, root, tools, work, accent):
                self.out = out
            def run(self):
                write_zip(self.out, {"classes.dex": b"dex"})
                return self.out
        outdir = os.path.join(self.directory, "build")
        with mock.patch.object(cli, "Build", FakeBuild), \
                mock.patch.object(cli, "Toolchain", lambda directory, **kwargs: FakeTools()):
            code = cli.main(["build-sign", "tiktok.apk", "--keystore", self.keystore,
                             "--keystore-pass", self.PASSWORD, "-o", os.path.join(outdir, "soufit.apk")])
        self.assertEqual(code, 0)
        self.assertEqual(os.listdir(outdir), ["soufit.apk"])

    def test_the_real_signing_path_is_handed_a_file_the_signer_accepts(self):
        source = os.path.join(self.directory, "built-real.apk")
        write_zip(source, {"classes.dex": b"dex"})
        script = os.path.join(self.directory, "fake-java")
        with open(script, "w", encoding="utf-8") as handle:
            handle.write(FAKE_JAVA.replace("PYTHON", sys.executable))
        os.chmod(script, 0o755)
        output = os.path.join(self.directory, "out-real", "soufit.apk")
        with mock.patch.object(toolchain_module.Toolchain, "signer", new_callable=mock.PropertyMock,
                               return_value="fake-signer.jar"), \
                mock.patch.object(toolchain_module, "_java", return_value=script):
            info, signature = sign.sign_into(
                source, output, toolchain_module.Toolchain(self.directory), keystore=self.keystore,
                environment={"SOUFIT_KEYSTORE_PASSWORD": self.PASSWORD})
        self.assertEqual(info.alias, "soufit")
        self.assertTrue(signature.v1)
        self.assertTrue(os.path.isfile(output))
        self.assertEqual(os.listdir(os.path.dirname(output)), ["soufit.apk"])

    def test_a_signed_build_uses_the_keystore_alias_and_passwords(self):
        source = os.path.join(self.directory, "built.apk")
        write_zip(source, {"classes.dex": b"dex"})
        tools = FakeTools()
        info, _ = sign.sign_into(source, os.path.join(self.directory, "soufit.apk"), tools,
                                 keystore=self.keystore,
                                 environment={"SOUFIT_KEYSTORE_PASSWORD": self.PASSWORD})
        self.assertEqual(info.alias, "soufit")
        self.assertEqual(tools.calls, [(self.keystore, "soufit", self.PASSWORD, self.PASSWORD)])

    def test_a_pkcs12_keystore_is_converted_before_the_signer_sees_it(self):
        apk = os.path.join(self.directory, "to-sign.apk")
        write_zip(apk, {"classes.dex": b"dex"})
        real_run = toolchain_module._run
        seen = []

        def record(command):
            if "-jar" in command:
                seen.append(command)
                return
            real_run(command)

        with mock.patch.object(toolchain_module.Toolchain, "signer", new_callable=mock.PropertyMock,
                               return_value="fake-signer.jar"), \
                mock.patch.object(toolchain_module, "_run", record):
            toolchain_module.Toolchain(self.directory).sign(
                apk, self.keystore, "soufit", self.PASSWORD, self.PASSWORD)
        self.assertEqual(len(seen), 1)
        command = seen[0]
        self.assertIn("--ksAlias", command)
        self.assertEqual(command[command.index("--ksAlias") + 1], "soufit")
        self.assertTrue(command[command.index("--ks") + 1].endswith("signing.jks"))


if __name__ == "__main__":
    unittest.main()
