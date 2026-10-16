import pathlib
import shutil
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
TINT = ROOT / "inject" / "java" / "bk" / "soufit" / "theme" / "Tint.java"

CHECK = r'''
package bk.soufit;import bk.soufit.theme.Tint;public class TintCheck {
    static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        check(Tint.darkUnder(0xFF000000), "black is a dark ground");
        check(Tint.darkUnder(0x00121212), "alpha does not make a dark ground light");
        check(!Tint.darkUnder(0xFFFFFFFF), "white is not a dark ground");
        check(!Tint.darkUnder(0x00FFFFFF), "fully transparent white is still a light ground");

        check(Tint.darkGlass(false, 0xFF121212), "dark page under a light theme flag must be dark glass");
        check(Tint.darkGlass(true, 0xFFFFFFFF), "a dark theme is dark glass even over a light ground");
        check(!Tint.darkGlass(false, 0xFFF2F2F2), "light theme over a light page stays light glass");

        check(Tint.glassBody(false) == 0xFF1C1C1E, "dark glass body without full AMOLED");
        check(Tint.glassBody(true) == 0xFF000000, "dark glass body is black with full AMOLED");

        check(Tint.surface(true, true, 0xFF222222) == 0xFF000000, "full AMOLED makes a dark surface black");
        check(Tint.surface(true, false, 0xFF222222) == 0xFF222222, "without full AMOLED the surface is unchanged");
        check(Tint.surface(false, true, 0xFFEEEEEE) == 0xFFEEEEEE, "full AMOLED leaves a light surface alone");
        System.out.println("tint ok");
    }
}
'''


class TintRuntimeTest(unittest.TestCase):
    def test_glass_and_amoled_colour_decisions(self):
        work = pathlib.Path(tempfile.mkdtemp(prefix="tint-"))
        try:
            src = work / "src"
            pkg = src / "bk" / "soufit"
            pkg.mkdir(parents=True)
            shutil.copy(TINT, pkg / "Tint.java")
            (pkg / "TintCheck.java").write_text(CHECK, encoding="utf-8")
            out = work / "out"
            out.mkdir()
            compiled = subprocess.run(
                ["javac", "-d", str(out), str(pkg / "Tint.java"), str(pkg / "TintCheck.java")],
                capture_output=True, text=True)
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            ran = subprocess.run(
                ["java", "-cp", str(out), "bk.soufit.TintCheck"],
                capture_output=True, text=True)
            self.assertEqual(0, ran.returncode, ran.stderr)
            self.assertIn("tint ok", ran.stdout)
        finally:
            shutil.rmtree(work)


if __name__ == "__main__":
    unittest.main()
