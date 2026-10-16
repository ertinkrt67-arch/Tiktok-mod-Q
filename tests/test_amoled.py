import pathlib
import struct
import sys
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

from soufit import amoled
from soufit.arsc import Arsc, Package, StringPool

PACKAGE = 0x7F
ATTR_TYPE, STYLE_TYPE = 1, 2
ATTRS = ["agh", "agj", "agl", "agn", "agp", "agr", "aic", "aii", "aie", "aik", "aig", "aim", "avs"]
GREY = 0xFF20292E
DARK = 0xFF16191D


def utf16_pool(strings):
    body = b""
    offsets = []
    for text in strings:
        offsets.append(len(body))
        body += struct.pack("<H", len(text)) + text.encode("utf-16-le") + b"\0\0"
    body += b"\0" * ((4 - len(body) % 4) % 4)
    strings_start = 28 + 4 * len(strings)
    size = strings_start + len(body)
    head = struct.pack("<HHI", 0x0001, 28, size) + struct.pack("<IIIII", len(strings), 0, 0, strings_start, 0)
    return head + b"".join(struct.pack("<I", o) for o in offsets) + body


def type_chunk(type_id, entries):
    config = struct.pack("<I", 4)
    header = 20 + len(config)
    offsets = []
    blob = b""
    for entry in entries:
        if entry is None:
            offsets.append(0xFFFFFFFF)
            continue
        offsets.append(len(blob))
        blob += entry
    table = b"".join(struct.pack("<I", o) for o in offsets)
    entries_start = header + len(table)
    size = entries_start + len(blob)
    head = struct.pack("<HHI", 0x0201, header, size) + struct.pack("<BBHII", type_id, 0, 0, len(entries), entries_start)
    return head + config + table + blob


def simple(key):
    return struct.pack("<HHI", 8, 0, key) + struct.pack("<HBBI", 8, 0, 0x10, 0)


def bag(key, maps):
    head = struct.pack("<HHIII", 16, 0x0001, key, 0, len(maps))
    return head + b"".join(struct.pack("<IHBBI", name, 8, 0, kind, data) for name, kind, data in maps)


def table(style_name="a_e", with_surfaces=True):
    keys = ATTRS + [style_name]
    key_index = {name: i for i, name in enumerate(keys)}
    attr_entries = [simple(key_index[name]) for name in ATTRS] if with_surfaces else [simple(key_index["avs"])]
    attr_ids = {name: 0x7F010000 | i for i, name in enumerate(ATTRS)}
    maps = [
        (attr_ids["agh"], 0x1C, 0xFF000000),
        (attr_ids["agn"], 0x1C, 0xFF000000),
        (attr_ids["agj"], 0x1C, DARK),
        (attr_ids["agl"], 0x1C, GREY),
        (attr_ids["agp"], 0x1C, DARK),
        (attr_ids["agr"], 0x1C, GREY),
        (attr_ids["aic"], 0x1C, 0xFF191B1E),
        (attr_ids["aii"], 0x1C, 0xFF191B1E),
        (attr_ids["aie"], 0x1C, 0xFF292D32),
        (attr_ids["aik"], 0x1C, 0xFF292D32),
        (attr_ids["aig"], 0x1C, 0xFF333D43),
        (attr_ids["aim"], 0x1C, 0xFF333D43),
        (attr_ids["avs"], 0x1C, 0xFFF7F8F9),
    ]
    style_entries = [bag(key_index[style_name], maps)]
    keys_pool = utf16_pool(keys)
    types_pool = utf16_pool(["attr", "style"])
    attr_chunk = type_chunk(ATTR_TYPE, attr_entries)
    style_chunk = type_chunk(STYLE_TYPE, style_entries)
    blob = keys_pool + types_pool + attr_chunk + style_chunk
    arsc = Arsc.__new__(Arsc)
    arsc.data = bytearray(blob)
    arsc.dirty = False
    package = Package(PACKAGE, "test")
    package.type_names = StringPool(arsc.data, len(keys_pool))
    package.key_names = StringPool(arsc.data, 0)
    package.types = {
        ATTR_TYPE: [len(keys_pool) + len(types_pool)],
        STYLE_TYPE: [len(keys_pool) + len(types_pool) + len(attr_chunk)],
    }
    arsc.packages = [package]
    return arsc, attr_ids


def read_bag(arsc, name_index_style=STYLE_TYPE):
    package = arsc.packages[0]
    chunk = package.types[name_index_style][0]
    _kind, header, _size = struct.unpack_from("<HHI", arsc.data, chunk)
    count, start = struct.unpack_from("<II", arsc.data, chunk + 12)
    at = chunk + start + struct.unpack_from("<I", arsc.data, chunk + header)[0]
    size = struct.unpack_from("<H", arsc.data, at)[0]
    maps = {}
    for index in range(struct.unpack_from("<I", arsc.data, at + 12)[0]):
        item = at + size + 12 * index
        maps[struct.unpack_from("<I", arsc.data, item)[0]] = struct.unpack_from("<I", arsc.data, item + 8)[0]
    return maps


class AmoledStyleTest(unittest.TestCase):
    def test_grey_surfaces_turn_black_and_the_rest_stays(self):
        arsc, ids = table()
        lines = amoled.bake(arsc)
        self.assertEqual(["AMOLED: a_e has 10 grey surface values turned black"], lines)
        self.assertTrue(arsc.dirty)
        maps = read_bag(arsc)
        for name in ("agj", "agl", "agp", "agr", "aic", "aii", "aie", "aik", "aig", "aim"):
            self.assertEqual(0xFF000000, maps[ids[name]], name)
        self.assertEqual(0xFF000000, maps[ids["agh"]])
        self.assertEqual(0xFFF7F8F9, maps[ids["avs"]], "text colour is left alone")

    def test_second_pass_changes_nothing(self):
        arsc, _ids = table()
        amoled.bake(arsc)
        arsc.dirty = False
        self.assertEqual(["AMOLED: a_e has 0 grey surface values turned black"], amoled.bake(arsc))
        self.assertFalse(arsc.dirty)

    def test_missing_style_leaves_the_table_alone(self):
        arsc, _ids = table(style_name="a_x")
        before = bytes(arsc.data)
        self.assertEqual(["AMOLED: style a_e not found in this build, left as TikTok ships it"], amoled.bake(arsc))
        self.assertEqual(before, bytes(arsc.data))
        self.assertFalse(arsc.dirty)

    def test_missing_surface_names_leave_the_table_alone(self):
        arsc, _ids = table(with_surfaces=False)
        before = bytes(arsc.data)
        lines = amoled.bake(arsc)
        self.assertTrue(lines[0].startswith("AMOLED: no surface attributes"))
        self.assertEqual(before, bytes(arsc.data))


if __name__ == "__main__":
    unittest.main()
