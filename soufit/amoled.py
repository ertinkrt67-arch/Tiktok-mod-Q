from __future__ import annotations

import struct
from typing import Iterator, List, Tuple

from .arsc import Arsc, ENTRY_FLAG_COMPLEX, FLAG_OFFSET16, FLAG_SPARSE, NO_ENTRY16, NO_ENTRY32

STYLE = "a_e"
SURFACES = ("agj", "agl", "agp", "agr", "aic", "aii", "aie", "aik", "aig", "aim")
BLACK = 0xFF000000
COLOUR_TYPES = (0x1C, 0x1D, 0x1E, 0x1F)


def bake(arsc: Arsc) -> List[str]:
    attrs = _attr_ids(arsc)
    surfaces = {attrs[name] for name in SURFACES if name in attrs}
    if not surfaces:
        return ["AMOLED: no surface attributes in this build, style %s left as TikTok ships it" % STYLE]

    styles = 0
    changed = 0
    for package in arsc.packages:
        if not _named(package):
            continue
        for type_id in list(package.types):
            if package.type_names.get(type_id - 1) != "style":
                continue
            for _res_id, at, name in _entries(arsc, package, type_id):
                if name != STYLE:
                    continue
                styles += 1
                changed += _blacken(arsc, at, surfaces)
    if not styles:
        return ["AMOLED: style %s not found in this build, left as TikTok ships it" % STYLE]
    return ["AMOLED: %s has %d grey surface values turned black" % (STYLE, changed)]


def _named(package) -> bool:
    return getattr(package, "type_names", None) is not None and getattr(package, "key_names", None) is not None


def _attr_ids(arsc: Arsc) -> dict:
    out = {}
    for package in arsc.packages:
        if not _named(package):
            continue
        for type_id in list(package.types):
            if package.type_names.get(type_id - 1) != "attr":
                continue
            for res_id, _at, name in _entries(arsc, package, type_id):
                if name in SURFACES:
                    out.setdefault(name, res_id)
    return out


def _entries(arsc: Arsc, package, type_id: int) -> Iterator[Tuple[int, int, str]]:
    data = arsc.data
    for chunk in package.types.get(type_id, []):
        _kind, header, _size = struct.unpack_from("<HHI", data, chunk)
        flags = data[chunk + 9]
        count, start = struct.unpack_from("<II", data, chunk + 12)
        table = chunk + header
        for index in range(count):
            if flags & FLAG_SPARSE:
                entry_id, off = struct.unpack_from("<HH", data, table + 4 * index)
                offset = off * 4
            elif flags & FLAG_OFFSET16:
                entry_id = index
                off = struct.unpack_from("<H", data, table + 2 * index)[0]
                if off == NO_ENTRY16:
                    continue
                offset = off * 4
            else:
                entry_id = index
                off = struct.unpack_from("<I", data, table + 4 * index)[0]
                if off == NO_ENTRY32:
                    continue
                offset = off
            at = chunk + start + offset
            key = struct.unpack_from("<I", data, at + 4)[0]
            res_id = (package.id << 24) | (type_id << 16) | entry_id
            yield res_id, at, package.key_names.get(key)


def _blacken(arsc: Arsc, at: int, surfaces: set) -> int:
    data = arsc.data
    flags = struct.unpack_from("<H", data, at + 2)[0]
    if not flags & ENTRY_FLAG_COMPLEX:
        return 0
    size = struct.unpack_from("<H", data, at)[0]
    count = struct.unpack_from("<I", data, at + 12)[0]
    changed = 0
    for index in range(count):
        item = at + size + 12 * index
        name = struct.unpack_from("<I", data, item)[0]
        kind = data[item + 7]
        if name not in surfaces or kind not in COLOUR_TYPES:
            continue
        if struct.unpack_from("<I", data, item + 8)[0] == BLACK:
            continue
        struct.pack_into("<I", data, item + 8, BLACK)
        changed += 1
    if changed:
        arsc.dirty = True
    return changed
