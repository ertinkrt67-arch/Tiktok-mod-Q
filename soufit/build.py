
from __future__ import annotations

import base64
import json
import os
import re
import time
from typing import Dict, List, Optional

from . import accent as accent_module, amoled, artwork, prerecolour, dexcache, dexpatch, icon as icon_module, manifest as manifest_module, nightly, resadd, widget
from .apkzip import Apk, STORED
from .arsc import Arsc
from . import axml as axml_module
from .axml import Axml
from .toolchain import Toolchain

PACKAGE = "bk.soufit"
SETTINGS_ACTIVITY = PACKAGE + ".ui.SettingsActivity"
STARTUP_PROVIDER = PACKAGE + ".core.soufiTProvider"
ALARM_RECEIVER = PACKAGE + ".social.StreakAlarm"
ALARM_PERMISSIONS = (
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.USE_EXACT_ALARM",
    "android.permission.WAKE_LOCK",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.ACCESS_NETWORK_STATE",
)
ALARM_ACTIONS = [
    "android.intent.action.BOOT_COMPLETED",
    "android.intent.action.MY_PACKAGE_REPLACED",
    "android.intent.action.TIME_SET",
    "android.intent.action.TIMEZONE_CHANGED",
    PACKAGE + ".STREAK_FIRE",
]

TIKTOK_SETTINGS = "com.ss.android.ugc.aweme.setting.ui.SettingContainerActivity"
LABEL = "soufiT"

SETTINGS_LABEL = "soufiT settings"
SETTINGS_THEME = "Theme_DeviceDefault_Light_NoActionBar"

AUTHORITY_MARKER = ".soufit"


def emoji_asset_entries(asset_root: str) -> List[tuple]:
    manifest_path = os.path.join(asset_root, "emoji", "packs.json")
    with open(manifest_path, "rb") as source:
        raw = source.read()
    if len(raw) > 65536:
        raise ValueError("emoji pack manifest exceeds 64 KiB")
    manifest = json.loads(raw.decode("utf-8"))
    if not isinstance(manifest, dict) or not isinstance(manifest.get("packs"), list):
        raise ValueError("emoji pack manifest must contain a packs list")
    entries = []
    ids = set()
    paths = set()
    for pack in manifest["packs"]:
        if not isinstance(pack, dict):
            raise ValueError("emoji pack entry must be an object")
        pack_id, name, version, asset = (pack.get(key) for key in
                                          ("id", "name", "version", "asset"))
        if not isinstance(pack_id, str) or not re.fullmatch(r"[a-z0-9_-]{1,32}", pack_id):
            raise ValueError("invalid emoji pack id")
        if pack_id in ids:
            raise ValueError("duplicate emoji pack id: " + pack_id)
        if not isinstance(name, str) or not name.strip() or len(name) > 48:
            raise ValueError("invalid emoji pack name: " + pack_id)
        if not isinstance(version, str) or len(version) > 24:
            raise ValueError("invalid emoji pack version: " + pack_id)
        if not isinstance(asset, str) or len(asset) > 120:
            raise ValueError("invalid emoji pack asset: " + pack_id)
        normalized = asset.replace("\\", "/")
        if normalized.startswith("/") or ".." in normalized.split("/"):
            raise ValueError("emoji pack asset escapes assets directory: " + pack_id)
        if not re.fullmatch(r"[A-Za-z0-9_.-]+(?:/[A-Za-z0-9_.-]+)*\.(?:ttf|otf)", normalized):
            raise ValueError("emoji pack asset must be a TTF/OTF: " + pack_id)
        if normalized in paths:
            raise ValueError("duplicate emoji pack asset: " + normalized)
        source_path = os.path.join(asset_root, *normalized.split("/"))
        if not os.path.isfile(source_path):
            raise FileNotFoundError("emoji pack asset missing: " + normalized)
        if os.path.getsize(source_path) > 24 * 1024 * 1024:
            raise ValueError("emoji font exceeds 24 MiB: " + normalized)
        min_api = pack.get("minAndroid", 29)
        if not isinstance(min_api, int) or min_api < 1 or min_api > 100:
            raise ValueError("invalid minimum Android version: " + pack_id)
        ids.add(pack_id)
        paths.add(normalized)
        entries.append(("assets/soufit/" + normalized, source_path))
    entries.append(("assets/soufit/emoji/packs.json", manifest_path))
    return entries

def _constant(label: str) -> str:
    return "".join(c if c.isalnum() else "_" for c in label).upper()


def _java_name(owner: str) -> str:
    return owner[1:-1].replace("/", ".")


class Build:
    def __init__(self, apk_path: str, out_path: str, root: str, tools: Toolchain,
                 workspace: str, accent: Optional[int] = None):
        self.apk_path = apk_path
        self.out_path = out_path
        self.root = root
        self.tools = tools
        self.workspace = workspace
        self.accent = dexpatch.TIKTOK_PINK if accent is None else accent
        self.started = time.time()
        self.patched_dex_count = 0
        self.total_dex_count = 0
        self.rewrite_count = 0
        self.static_target_count = 0

    def say(self, message: str) -> None:
        print("\033[1;36m==>\033[0m %s" % message)

    def detail(self, message: str) -> None:
        print("    " + message)


    def run(self) -> str:
        os.makedirs(self.workspace, exist_ok=True)
        os.makedirs(os.path.dirname(os.path.abspath(self.out_path)), exist_ok=True)

        self.say("Opening the apk")
        apk = Apk(self.apk_path)
        self.detail("%d entries" % len(apk.entries))

        manifest = Axml.parse(apk.read("AndroidManifest.xml"))
        package = manifest_module.package_name(manifest)
        self.detail("package %s, staying as it is" % package)
        self.detail("application class %s" % manifest_module.application_class(manifest))

        api = manifest_module.min_sdk(manifest)
        dex_format = dexpatch.dex_format(apk.read("classes.dex"))
        self.detail("minSdk %d, dex %s" % (api, dex_format))

        arsc = Arsc(apk.read("resources.arsc"))

        self.say("The accent colour")
        self.detail("#%06X" % (self.accent & 0xFFFFFF))
        moved: Dict[int, int] = {}
        for line in accent_module.bake(apk, arsc, dexpatch.TIKTOK_PINK, self.accent, moved):
            self.detail(line)

        self.write_baked_colour(moved)
        self.write_emblem()
        self.write_badges()
        self.write_icons()
        self.write_version(manifest_module.version_name(manifest))
        self.find_anchors(apk)
        self.write_theme(arsc)
        for line in amoled.bake(arsc):
            self.detail(line)
        self.write_shots(manifest)

        self.say("Building the mod's own dex")
        dex_path = self.tools.compile_dex(
            os.path.join(self.root, "inject", "java"), self.workspace, api,
            os.path.join(self.root, "inject", "stubs"),
        )
        injected = open(dex_path, "rb").read()
        if dexpatch.dex_format(injected) != dex_format:
            raise RuntimeError(
                "the mod compiled to dex %s and the apk is dex %s -- an Android "
                "on the apk's minSdk (%d) would refuse to load it"
                % (dexpatch.dex_format(injected), dex_format, api)
            )
        missing = dexpatch.missing_targets(injected)
        if missing:
            raise RuntimeError(
                "the rewrites would land on %d method(s) the mod does not define:\n    %s\n"
                "smali assembles a call to a method that does not exist without "
                "complaining -- it would be a NoSuchMethodError on whichever "
                "screen reaches that call site first"
                % (len(missing), "\n    ".join(missing))
            )
        self.detail("%d bytes, dex %s, every rewrite has somewhere to land"
                    % (len(injected), dexpatch.dex_format(injected)))

        self.say("Name and icon")
        theme = self.tools.framework_constant(SETTINGS_THEME)
        for where in manifest_module.set_label(manifest, LABEL):
            self.detail("label on %s" % where)
        self.tools.check_attribute_ids(axml_module.ATTR_IDS)
        manifest_module.add_activity(manifest, SETTINGS_ACTIVITY, SETTINGS_LABEL, theme)
        self.detail("%s declared, reached from TikTok's own settings" % SETTINGS_ACTIVITY)
        manifest_module.add_activity(manifest, PACKAGE + ".plugins.PluginEditorActivity", SETTINGS_LABEL, theme)

        if not manifest_module.has_activity(manifest, TIKTOK_SETTINGS):
            raise RuntimeError(
                "%s is not in this apk -- TikTok's settings screen has been "
                "renamed, and the soufiT row would never appear in it"
                % TIKTOK_SETTINGS
            )

        for permission in ALARM_PERMISSIONS:
            manifest_module.add_permission(manifest, permission)
        for permission in manifest_module.DENIED_PERMISSIONS:
            count = manifest_module.deny_permission(manifest, permission)
            if count:
                self.detail("%s refused: the contacts prompt cannot appear" % permission.rsplit(".", 1)[-1])
        manifest_module.add_receiver(manifest, ALARM_RECEIVER, ALARM_ACTIONS)
        self.detail("%s declared: streak sends wake on the system clock" % ALARM_RECEIVER)
        manifest_module.add_provider(
            manifest, STARTUP_PROVIDER, "%s.soufit" % package)
        self.detail("%s declared: the mod starts with the app" % STARTUP_PROVIDER)
        self.detail("the soufiT row goes on top of %s" % TIKTOK_SETTINGS.rsplit(".", 1)[-1])

        self.say("Provider authorities")
        shared = manifest_module.shared_authorities(manifest, package)
        renames = {old: old + AUTHORITY_MARKER for old in shared}
        if renames:
            for new in manifest_module.rename_authorities(manifest, renames):
                self.detail(new)
            self.detail("%d the package name does not cover, now ours alone"
                        % len(renames))
        else:
            self.detail("every authority is spelled with the package name, nothing to do")

        master = open(os.path.join(self.root, artwork.MASTER_PNG), "rb").read()
        for line in icon_module.replace_everywhere(apk, arsc, manifest, master):
            self.detail(line.strip())

        self.say("Icons to choose from")
        extended = self.add_icon_choices(apk, arsc, manifest)
        extended = self.add_widget(apk, extended, manifest)
        self.add_assets(apk)
        self.add_prerecoloured(apk)

        apk.replace("AndroidManifest.xml", manifest.build())
        if extended is not None:
            apk.replace("resources.arsc", extended, STORED)
        elif arsc.dirty:
            apk.replace("resources.arsc", arsc.build(), STORED)

        self.say("Rewriting the bytecode")
        self.patch_dex_files(apk, api, renames)

        self.write_build_info()
        dex_path = self.tools.compile_dex(
            os.path.join(self.root, "inject", "java"), self.workspace, api,
            os.path.join(self.root, "inject", "stubs"),
        )
        injected = open(dex_path, "rb").read()
        if dexpatch.dex_format(injected) != dex_format or dexpatch.missing_targets(injected):
            raise RuntimeError("build diagnostics changed the injected method surface")

        name = dexpatch.next_dex_name(apk.names())
        apk.add(name, injected)
        self.detail("the mod's classes go in as %s" % name)

        self.say("Writing the apk")
        for gone in apk.drop_signature():
            self.detail("dropped %s" % gone)
        apk.write(self.out_path)
        apk.close()
        self.detail("%.0f MB" % (os.path.getsize(self.out_path) / 1e6))

        self.say("Built in %.0f s" % (time.time() - self.started))
        return self.out_path

    def write_baked_colour(self, moved: Dict[int, int]) -> None:
        keys = sorted(moved, key=_signed)
        path = os.path.join(self.root, "inject", "java", "bk", "soufit",
                            "core",
                            "Baked.java")
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class Baked {\n\n"
                "    private Baked() {}\n\n"
                "    public static final int ACCENT = 0x%08X;\n\n"
                "%s"
                "}\n" % (self.accent, _tables(keys, moved))
            )
        if keys:
            self.detail("%d shades listed for the mod to recognise" % len(keys))

    def write_emblem(self) -> None:
        source = os.path.join(self.root, "assets", "app-icon", "mipmap-xxhdpi",
                              "ic_launcher_foreground.png")
        with open(source, "rb") as handle:
            png = base64.b64encode(handle.read()).decode("ascii")
        chunks = [png[at:at + 76] for at in range(0, len(png), 76)]
        body = ",\n                ".join('"%s"' % chunk for chunk in chunks)

        path = os.path.join(self.root, "inject", "java", "bk", "soufit",
                            "core",
                            "Emblem.java")
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class Emblem {\n\n"
                "    private Emblem() {}\n\n"
                "    public static final String PNG = join(\n                %s);\n\n"
                "    public static String join(String... parts) {\n"
                "        StringBuilder out = new StringBuilder();\n"
                "        for (String part : parts) out.append(part);\n"
                "        return out.toString();\n"
                "    }\n"
                "}\n" % body
            )
        self.detail("emblem: %d bytes of png in the code" % os.path.getsize(source))

    def write_badges(self) -> None:
        folder = os.path.join(self.root, "assets", "badges")
        names = []
        if os.path.isdir(folder):
            names = sorted(f for f in os.listdir(folder) if f.lower().endswith(".png"))
        entries = []
        for name in names:
            with open(os.path.join(folder, name), "rb") as handle:
                data = base64.b64encode(handle.read()).decode("ascii")
            chunks = [data[at:at + 76] for at in range(0, len(data), 76)]
            body = ",\n                    ".join('"%s"' % chunk for chunk in chunks)
            entries.append('            case "%s":\n                return decode(join(\n                    %s));'
                           % (name, body))
        cases = "\n".join(entries)
        path = os.path.join(self.root, "inject", "java", "bk", "soufit",
                            "core",
                            "BadgeArt.java")
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class BadgeArt {\n\n"
                "    private BadgeArt() {}\n\n"
                "    public static byte[] png(String name) {\n"
                "        if (name == null) return null;\n"
                "        switch (name) {\n"
                "%s"
                "            default:\n"
                "                break;\n"
                "        }\n"
                "        return null;\n"
                "    }\n\n"
                "    private static byte[] decode(String encoded) {\n"
                "        return android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);\n"
                "    }\n\n"
                "    public static String join(String... parts) {\n"
                "        StringBuilder out = new StringBuilder();\n"
                "        for (String part : parts) out.append(part);\n"
                "        return out.toString();\n"
                "    }\n"
                "}\n" % cases
            )
        self.detail("badges: %d picture(s) packed in the mod" % len(names))

    def write_icons(self) -> None:
        folder = os.path.join(self.root, "assets", "icons")
        names = sorted(f[:-4] for f in os.listdir(folder) if f.endswith(".png"))
        entries = []
        for name in names:
            with open(os.path.join(folder, name + ".png"), "rb") as handle:
                data = base64.b64encode(handle.read()).decode("ascii")
            chunks = [data[at:at + 72] for at in range(0, len(data), 72)]
            body = "\n                + ".join('"%s"' % chunk for chunk in chunks)
            entries.append('        PNG.put("%s",\n                %s);' % (name, body))

        path = os.path.join(self.root, "inject", "java", "bk", "soufit",
                            "core",
                            "Icons.java")
        with open(path, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "import java.util.HashMap;\n"
                "import java.util.Map;\n\n"
                "public final class Icons {\n\n"
                "    private Icons() {}\n\n"
                "    public static final Map<String, String> PNG = new HashMap<String, String>();\n\n"
                "    static {\n%s\n    }\n"
                "}\n" % "\n".join(entries)
            )
        self.detail("%d icons in the code" % len(names))

    def write_version(self, tiktok: str) -> None:
        path = os.path.join(self.root, "VERSION")
        with open(path, encoding="utf-8") as handle:
            mod = handle.read().strip()

        out = os.path.join(self.root, "inject", "java", "bk", "soufit",
                           "core",
                           "Version.java")
        with open(out, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class Version {\n\n"
                "    private Version() {}\n\n"
                "    public static final String MOD = \"%s\";\n"
                "    public static final String TIKTOK = \"%s\";\n"
                "}\n" % (mod, tiktok)
            )
        self.detail("soufiT %s on TikTok %s" % (mod, tiktok))

    def write_theme(self, arsc: Arsc) -> None:
        owned, safe = nightly.theme_colours(arsc, dexpatch.TIKTOK_PINK)
        self.detail("%d colours belong to the theme; %d of them mean nothing "
                    "else anywhere" % (len(owned), len(safe)))

        owned = sorted(owned, key=_signed)
        safe = sorted(safe, key=_signed)

        out = os.path.join(self.root, "inject", "java", "bk", "soufit",
                           "theme",
                           "Nightly.java")
        with open(out, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.theme;\n\n"
                "public final class Nightly {\n\n"
                "    private Nightly() {}\n\n"
                "    public static boolean owns(int colour) {\n"
                "        return has(OWNED, colour);\n"
                "    }\n\n"
                "    public static boolean unmistakable(int colour) {\n"
                "        return has(UNMISTAKABLE, colour);\n"
                "    }\n\n"
                "    public static boolean has(int[] list, int colour) {\n"
                "        int low = 0;\n"
                "        int high = list.length - 1;\n"
                "        while (low <= high) {\n"
                "            int middle = (low + high) >>> 1;\n"
                "            int here = list[middle];\n"
                "            if (here == colour) return true;\n"
                "            if (here < colour) low = middle + 1;\n"
                "            else high = middle - 1;\n"
                "        }\n"
                "        return false;\n"
                "    }\n\n"
            )
            for name, values in (("OWNED", owned), ("UNMISTAKABLE", safe)):
                handle.write("    public static final int[] %s = {\n" % name)
                for i in range(0, len(values), 6):
                    row = ", ".join("0x%08X" % value for value in values[i:i + 6])
                    handle.write("        %s,\n" % row)
                handle.write("    };\n\n")
            handle.write("}\n")

    def add_assets(self, apk: Apk) -> None:
        asset_root = os.path.join(self.root, "assets")
        entries = emoji_asset_entries(asset_root)
        entries.append(("assets/soufit/meow.ogg", os.path.join(asset_root, "media", "meow.ogg")))
        for eye in ("burn_eye_open.png", "burn_eye_closed.png"):
            entries.append(("assets/soufit/" + eye, os.path.join(asset_root, "media", eye)))
        for destination, path in entries:
            with open(path, "rb") as handle:
                raw = handle.read()
            apk.add(destination, raw, STORED)
            self.detail("%s: %d kB" % (destination, len(raw) // 1024))

    def add_prerecoloured(self, apk: Apk) -> None:
        def read(path: str):
            try:
                return apk.read("assets/" + path)
            except Exception:
                return None

        made = skipped = size = 0
        for path, name in prerecolour.candidates(apk.names()):
            try:
                source = apk.read(path)
                files = {}
                for colour in prerecolour.COLOURS:
                    done = prerecolour.make(name, source, colour, read)
                    if done is None:
                        raise ValueError("not handled")
                    files[prerecolour.shipped_path(name, colour)] = done
            except Exception as error:
                skipped += 1
                self.detail("%s: left to the app (%s)" % (name, error))
                continue
            for destination, data in files.items():
                apk.add(destination, data)
                size += len(data)
            made += 1
        self.detail("%d like animations ready for %d colours (%d kB), %d left to the app"
                    % (made, len(prerecolour.COLOURS), size // 1024, skipped))


    ICONS = [
        ("grafiti", "Graffiti"),
        ("shine", "Shiny"),
        ("tiktok", "TikTok cosplay"),
        ("materialyou", "Material Design 3"),
        ("material3", "Material 3"),
        ("doodle", "Doodle"),
        ("glitch", "Glitch"),
        ("google", "Google"),
        ("dotted", "Dotted"),
        ("terminal", "Terminal"),
        ("yaai", "You are an idiot!"),
        ("deltamargyt", "Deltamargyt"),
        ("mteam", "Mteam"),
        ("deled", "today i draw a picture"),
        ("margytcraft", "Margytcraft"),
        ("tigr", "ayy tigrr"),
        ("astolfo", "what is this mess"),
        ("glamour", "Glamour"),
        ("tilcayo", "Chubby Tilcayo"),
    ]

    ICON_PACKAGE = 0x30
    ICON_ALIAS = "bk.soufit.Icon"

    def _icon_files(self) -> List[str]:
        return [os.path.join(self.root, "assets", "app-icon", "alternates", key + ".png")
                for key, _label in self.ICONS]

    def write_shots(self, manifest: Axml) -> None:
        entry, _target = manifest_module.launcher_entry(manifest)

        rows = []
        for index, (key, label) in enumerate(self.ICONS):
            path = os.path.join(self.root, "assets", "app-icon", "alternates", key + ".png")
            with open(path, "rb") as handle:
                raw = handle.read()
            rows.append((key, label, "%s%d" % (self.ICON_ALIAS, index),
                         base64.b64encode(raw).decode("ascii")))

        out = os.path.join(self.root, "inject", "java", "bk", "soufit",
                           "core",
                           "Shots.java")
        with open(out, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class Shots {\n\n"
                "    private Shots() {}\n\n"
                "    public static final String DEFAULT = \"%s\";\n\n"
                "    public static final String[] DEFAULT_PNG = %s;\n\n"
                "    public static final String[] KEYS = {\n"
                % (entry, _chunked(_thumbnail(os.path.join(self.root, artwork.MASTER_PNG))))
            )
            for key, _label, _component, _png in rows:
                handle.write("        \"%s\",\n" % key)
            handle.write("    };\n\n    public static final String[] LABELS = {\n")
            for _key, label, _component, _png in rows:
                handle.write("        \"%s\",\n" % label)
            handle.write("    };\n\n    public static final String[] COMPONENTS = {\n")
            for _key, _label, component, _png in rows:
                handle.write("        \"%s\",\n" % component)
            handle.write("    };\n\n    public static final String[][] PNG = {\n")
            for _key, _label, _component, png in rows:
                handle.write("        %s,\n" % _chunked(png))
            handle.write("    };\n}\n")
        self.detail("%d icons to choose from" % len(rows))

    def add_icon_choices(self, apk: Apk, arsc: Arsc, manifest: Axml):
        entry, target = manifest_module.launcher_entry(manifest)
        label = LABEL

        names = []
        for index, (key, _label) in enumerate(self.ICONS):
            path = "res/soufit/%s.png" % key
            with open(self._icon_files()[index], "rb") as handle:
                apk.add(path, handle.read())
            names.append((key, path))

        app_index = len(self.ICONS)
        app_path = "res/soufit/app_icon.png"
        with open(os.path.join(self.root, "assets", "app-icon", "mipmap-xxxhdpi", "ic_launcher.png"), "rb") as handle:
            apk.add(app_path, handle.read())
        names.append(("app_icon", app_path))

        extended = resadd.add_files(arsc.build(), self.ICON_PACKAGE, "mipmap", names)
        self.detail("%d icons added to the resource table as package 0x%02X"
                    % (len(names), self.ICON_PACKAGE))
        manifest_module.set_application_icon(manifest, (self.ICON_PACKAGE << 24) | (1 << 16) | app_index)

        for index, (key, name) in enumerate(self.ICONS):
            res_id = (self.ICON_PACKAGE << 24) | (1 << 16) | index
            manifest_module.add_icon_alias(
                manifest, "%s%d" % (self.ICON_ALIAS, index), target, res_id,
                label, enabled=False)
        self.detail("%d launcher entries declared, all off but %s"
                    % (len(self.ICONS), entry.rsplit(".", 1)[-1]))
        return extended

    WIDGET = PACKAGE + ".core.CatWidget"

    def add_widget(self, apk: Apk, table: bytes, manifest: Axml) -> bytes:
        apk.add(widget.LAYOUT_PATH, widget.layout_xml())
        apk.add(widget.XML_PATH, widget.info_xml())
        table = resadd.add_files(table, widget.LAYOUT_PACKAGE, "layout",
                                 [("soufit_widget", widget.LAYOUT_PATH)], "soufit.layout")
        table = resadd.add_files(table, widget.XML_PACKAGE, "xml",
                                 [("soufit_widget_info", widget.XML_PATH)], "soufit.xml")
        manifest_module.add_widget(manifest, self.WIDGET, widget.XML_ID)
        self.detail("a home-screen widget declared, its layout 0x%08X" % widget.LAYOUT_ID)
        return table

    def find_anchors(self, apk: Apk) -> None:
        dexes = {name: apk.read(name) for name in apk.names()
                 if name.endswith(".dex")}
        dexpatch.FOUND = dexpatch.find_statics(dexes)

        lines = []
        for label, _descriptor, _ours, _target in (
                dexpatch.DISCOVERED_STATICS + dexpatch.DISCOVERED_VIRTUALS):
            found = dexpatch.FOUND.get(label)
            if found is None:
                self.detail("%s: not found in this release" % label)
                continue
            owner, name = found
            self.detail("%s: %s->%s" % (label, owner, name))
            lines.append((label, owner, name))
        self.static_target_count = len(lines)

        out = os.path.join(self.root, "inject", "java", "bk", "soufit",
                           "core",
                           "Anchors.java")
        with open(out, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class Anchors {\n\n"
                "    private Anchors() {}\n"
            )
            for label, owner, name in lines:
                handle.write(
                    "\n"
                    "    public static final String %s = \"%s\";\n"
                    "    public static final String %s_METHOD = \"%s\";\n"
                    % (_constant(label), _java_name(owner),
                       _constant(label), name)
                )
            handle.write("}\n")

    def write_build_info(self) -> None:
        out = os.path.join(self.root, "inject", "java", "bk", "soufit",
                           "core",
                           "BuildInfo.java")
        with open(out, "w", encoding="utf-8") as handle:
            handle.write(
                "package bk.soufit.core;\n\n"
                "public final class BuildInfo {\n"
                "    private BuildInfo() {}\n"
                "    public static final int PATCHED_DEX = %d;\n"
                "    public static final int TOTAL_DEX = %d;\n"
                "    public static final int REWRITES = %d;\n"
                "    public static final int STATIC_TARGETS = %d;\n"
                "}\n"
                % (self.patched_dex_count, self.total_dex_count,
                   self.rewrite_count, self.static_target_count)
            )


    def patch_dex_files(self, apk: Apk, api: int, literals: Dict[str, str]) -> None:
        names = sorted(n for n in apk.names() if n.endswith(".dex"))
        self.total_dex_count = len(names)
        candidates: List[str] = []
        for name in names:
            if dexpatch.interesting(apk.read(name), literals):
                candidates.append(name)
        self.detail("%d of %d dex files mention it" % (len(candidates), len(names)))

        telephony_labels = {label for label, _pattern, _target in dexpatch.rules()}
        calls = 0
        pink = 0
        like_hooks = 0
        speed_lock = 0
        edge_speed_lock = 0
        feature_hits = {"seekbar":0,"comment copy":0,"native comment clipboard":0,"publication date":0,"visible publication date":0,"double-tap heart":0,"native heart tint":0,"comment dim exclusion":0,"ads":0}
        feature_hits["native comment text actions"]=0
        feature_hits["native loading indicator"]=0
        feature_hits[dexpatch.FLAG_LABEL]=0
        feature_hits["native avatar shape"]=0
        feature_hits["native avatar ring"]=0
        feature_hits[dexpatch.HDR_LABEL]=0
        feature_hits[dexpatch.START_LABEL]=0
        feature_hits["native missing string"]=0
        feature_hits[dexpatch.SETTINGS_LABEL]=0
        feature_hits["native window brightness"]=0
        feature_hits["native card corners"]=0
        feature_hits["native right-area slots"]=0
        feature_hits["native player resume"]=0
        feature_hits["native offline choices"]=0
        feature_hits["native offline row"]=0
        feature_hits["native offline count accepted"]=0
        feature_hits["native offline comments response"]=0
        for feature in ("native repost","native repost avatar","native profile layout","native message timestamps","native notification guide","native friends order","native notification filters","native author date slot"):feature_hits[feature]=0
        total = 0
        patched_dex_count = 0
        results = dexcache.patch_all(
            {name: apk.read(name) for name in candidates}, self.tools.smali, api,
            os.path.join(self.workspace, "patch"), literals, self.detail
        )
        for name in candidates:
            patched, counts = results[name]
            if not counts:
                continue
            apk.replace(name, patched)
            patched_dex_count += 1
            feature_hits["seekbar"] += counts.get("setSeekBarShowType (any owner)",0)
            feature_hits["comment copy"] += sum(v for key,v in counts.items()
                if "BaseCommentCell;" in key and "setOnLongClickListener" in key)
            feature_hits["publication date"] += sum(v for key,v in counts.items()
                if "getDesc" in key and any(anchor in key for anchor in dexpatch.DATE_CAPTIONS))
            feature_hits["double-tap heart"] += sum(v for key,v in counts.items() if "LX/0HCj;" in key and "setImageDrawable" in key)
            feature_hits["native heart tint"] += sum(v for key,v in counts.items() if "LX/0HCj;" in key and "setTintColor" in key)
            feature_hits["comment dim exclusion"] += counts.get("native comment cell protection",0)
            feature_hits["native comment text actions"] += counts.get("native comment text actions",0)
            feature_hits["native loading indicator"] += counts.get("native loading indicator",0)
            feature_hits[dexpatch.FLAG_LABEL] += counts.get(dexpatch.FLAG_LABEL,0)
            feature_hits[dexpatch.HDR_LABEL] += counts.get(dexpatch.HDR_LABEL,0)
            feature_hits[dexpatch.START_LABEL] += counts.get(dexpatch.START_LABEL,0)
            feature_hits["native missing string"] += sum(v for key,v in counts.items() if "getString" in key and ("0TFu" in key or "0TVv" in key))
            feature_hits[dexpatch.SETTINGS_LABEL] += counts.get(dexpatch.SETTINGS_LABEL,0)
            feature_hits["native window brightness"] += sum(v for key,v in counts.items() if key.startswith(dexpatch.WINDOW_LABEL))
            feature_hits["native avatar ring"] += sum(v for key,v in counts.items() if key.startswith("Canvas->drawArc") and any(ring in key for ring in dexpatch.STORY_RINGS))
            feature_hits["native avatar shape"] += sum(v for key,v in counts.items() if key.startswith("Path->addCircle"))
            feature_hits["native card corners"] += counts.get("native card corners",0)
            feature_hits["native right-area slots"] += counts.get("native right-area slots",0)
            feature_hits["native player resume"] += counts.get("native player resume",0)
            feature_hits["native offline choices"] += counts.get("native offline choices",0)
            feature_hits["native offline row"] += counts.get("native offline row",0)
            feature_hits["native offline count accepted"] += counts.get("native offline count accepted",0)
            feature_hits["native offline comments response"] += counts.get("native offline comments response",0)
            for feature in ("native repost","native repost avatar","native profile layout","native message timestamps","native notification guide"):feature_hits[feature]+=counts.get(feature,0)
            feature_hits["native author date slot"]+=counts.get("native author date slot",0)
            feature_hits["native friends order"]+=sum(v for key,v in counts.items() if "FriendsListResponse" in key and "getUserList" in key)
            feature_hits["native notification filters"]+=sum(v for key,v in counts.items() if "NotificationManager" in key and "notify" in key)
            feature_hits["visible publication date"] += counts.get("visible publication date",0)
            feature_hits["ads"] += sum(v for key,v in counts.items() if "FeedItemList" in key and "getItems" in key)
            feature_hits["native comment clipboard"] += sum(v for key,v in counts.items() if "0oir->LIZ" in key)
            hits = sum(counts.values())
            total += hits
            calls += sum(v for label, v in counts.items() if label in telephony_labels)
            pink += counts.get("the pink itself", 0)
            like_hooks += counts.get("feed Lottie setAnimation", 0)
            speed_lock += counts.get("hold speed, feed touch", 0)
            edge_speed_lock += counts.get("hold speed, edge touch", 0)
            self.detail(
                "%s: %d (%s)"
                % (name, hits, ", ".join("%s x%d" % (k.split("(")[0], v)
                                         for k, v in sorted(counts.items())))
            )
        self.detail("%d telephony call sites, %d places the accent colour was written "
                    "down, %d rewrites in all" % (calls, pink, total))
        self.patched_dex_count = patched_dex_count
        self.rewrite_count = total
        missing=[feature for feature,hits in feature_hits.items() if not hits]
        if feature_hits["native offline comments response"] < 4:
            missing.append("native offline comments response (need all four)")
        if 0 < feature_hits[dexpatch.SETTINGS_LABEL] < 2:
            missing.append(dexpatch.SETTINGS_LABEL + " (need both halves)")
        if feature_hits["native right-area slots"]<4 and "native right-area slots" not in missing:
            missing.append("native right-area slots (need all four)")
        if missing:
            raise RuntimeError("Required feature hooks did not match: "+", ".join(missing)
                               +". This APK is unsupported; no silent feature omissions.")
        self.detail("Feature hooks: "+", ".join("%s x%d"%item for item in feature_hits.items()))
        if not like_hooks:
            raise RuntimeError(
                "TikTok's feed-like animation entry did not match: this build "
                "would leave the heart animation unchanged"
            )
        if not speed_lock or not edge_speed_lock:
            raise RuntimeError(
                "TikTok's feed touch listener did not match: the hold speed the "
                "mod lets people choose would never be applied"
            )
        if not pink:
            self.detail("WARNING: the accent colour is not a constant in this apk any "
                        "more, so the colour picker will have nothing to change")
        if not calls:
            raise RuntimeError(
                "not one call site matched -- the method signatures have moved, "
                "and the mod would do nothing at all"
            )


def _tables(keys: List[int], moved: Dict[int, int]) -> str:
    if not keys:
        return "    public static final int[] BAKED = {};\n    public static final int[] FROM = {};\n"
    out = []
    for name, values in (("BAKED", keys), ("FROM", [moved[k] for k in keys])):
        lines = []
        for at in range(0, len(values), 6):
            lines.append("        " + ", ".join("0x%08X" % v for v in values[at:at + 6]))
        out.append("    public static final int[] %s = {\n%s,\n    };\n" % (name, ",\n".join(lines)))
    return "\n".join(out)


def _chunked(encoded: str) -> str:
    pieces = [encoded[at:at + 20000] for at in range(0, len(encoded), 20000)]
    return "{" + ", ".join('"%s"' % piece for piece in pieces) + "}"


def _thumbnail(path: str) -> str:
    from PIL import Image

    image = Image.open(path).convert("RGBA")
    image.thumbnail((192, 192), Image.LANCZOS)
    from io import BytesIO
    out = BytesIO()
    image.save(out, "PNG", optimize=True)
    return base64.b64encode(out.getvalue()).decode("ascii")


def _signed(value: int) -> int:
    return value - (1 << 32) if value >= (1 << 31) else value
