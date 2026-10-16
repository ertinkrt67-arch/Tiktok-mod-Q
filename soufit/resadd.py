
from __future__ import annotations

import struct
from typing import Dict, List

RES_TABLE = 0x0002
RES_STRING_POOL = 0x0001
RES_TABLE_PACKAGE = 0x0200
RES_TABLE_TYPE = 0x0201
RES_TABLE_TYPE_SPEC = 0x0202

TYPE_STRING = 0x03
UTF8_FLAG = 1 << 8


def add_files(data: bytes, package_id: int, type_name: str,
              names_and_paths: List[tuple], package_name: str = "soufit") -> bytes:
    table_kind, table_header, table_size = struct.unpack_from("<HHI", data, 0)
    if table_kind != RES_TABLE:
        raise ValueError("not a resource table")
    package_count = struct.unpack_from("<I", data, 8)[0]

    paths = [path for _name, path in names_and_paths]
    grown, first_string = _append_strings(data, table_header, paths)

    package = _package(package_id, type_name,
                       [name for name, _path in names_and_paths],
                       list(range(first_string, first_string + len(paths))),
                       package_name)

    out = bytearray(grown)
    out.extend(package)
    struct.pack_into("<I", out, 8, package_count + 1)
    struct.pack_into("<I", out, 4, len(out))
    return bytes(out)


def add_adaptive_icons(data: bytes, package_id: int,
                       names_and_paths: List[tuple]) -> bytes:
    kind, header, _size = struct.unpack_from("<HHI", data, 0)
    if kind != RES_TABLE or not names_and_paths:
        raise ValueError("not a resource table or no icons")
    package_count = struct.unpack_from("<I", data, 8)[0]
    paths = [row[1] for row in names_and_paths] + [row[2] for row in names_and_paths]
    grown, first = _append_strings(data, header, paths)
    count = len(names_and_paths)
    package = _package_adaptive(
        package_id, [row[0] for row in names_and_paths],
        list(range(first, first + count)),
        list(range(first + count, first + 2 * count)),
    )
    out = bytearray(grown)
    out.extend(package)
    struct.pack_into("<I", out, 8, package_count + 1)
    struct.pack_into("<I", out, 4, len(out))
    return bytes(out)


def _package_adaptive(package_id: int, keys: List[str], legacy: List[int],
                      adaptive: List[int]) -> bytes:
    type_pool = _pool(["mipmap"])
    key_pool = _pool(keys)
    body = type_pool + key_pool + _spec(len(keys))
    body += _type(len(keys), legacy) + _type(len(keys), adaptive, sdk=26)
    header_size = 288
    head = bytearray(header_size)
    struct.pack_into("<HHI", head, 0, RES_TABLE_PACKAGE, header_size,
                     header_size + len(body))
    struct.pack_into("<I", head, 8, package_id)
    name = "soufit".encode("utf-16-le")
    head[12:12 + len(name)] = name
    struct.pack_into("<4I", head, 268,
                     header_size, 1, header_size + len(type_pool), len(keys))
    return bytes(head) + body


def _append_strings(data: bytes, pool_at: int, strings: List[str]):
    kind, header_size, size = struct.unpack_from("<HHI", data, pool_at)
    if kind != RES_STRING_POOL:
        raise ValueError("the table does not start with its string pool")
    count, style_count, flags, strings_start, styles_start = struct.unpack_from(
        "<5I", data, pool_at + 8)
    utf8 = bool(flags & UTF8_FLAG)

    string_offsets_at = pool_at + header_size
    style_offsets_at = string_offsets_at + 4 * count
    strings_at = pool_at + strings_start
    styles_at = pool_at + styles_start if styles_start else pool_at + size
    end_of_pool = pool_at + size

    strings_length = styles_at - strings_at

    encoded = bytearray()
    added = []
    for text in strings:
        added.append(strings_length + len(encoded))
        encoded.extend(_encode(text, utf8))
    while (strings_length + len(encoded)) % 4:
        encoded.append(0)

    out = bytearray()
    out.extend(data[:style_offsets_at])
    for offset in added:
        out.extend(struct.pack("<I", offset))
    out.extend(data[style_offsets_at:strings_at])
    out.extend(data[strings_at:styles_at])
    out.extend(encoded)
    out.extend(data[styles_at:end_of_pool])
    out.extend(data[end_of_pool:])

    moved = 4 * len(strings)
    struct.pack_into("<I", out, pool_at + 8, count + len(strings))
    struct.pack_into("<I", out, pool_at + 20, strings_start + moved)
    if styles_start:
        struct.pack_into("<I", out, pool_at + 24, styles_start + moved + len(encoded))
    struct.pack_into("<I", out, pool_at + 4, size + moved + len(encoded))
    return out, count


def _encode(text: str, utf8: bool) -> bytes:
    if utf8:
        raw = text.encode("utf-8")
        if len(raw) > 0x7F:
            raise ValueError("a path that long needs the two-byte form")
        return bytes([len(text), len(raw)]) + raw + b"\x00"
    raw = text.encode("utf-16-le")
    if len(text) > 0x7FFF:
        raise ValueError("too long")
    return struct.pack("<H", len(text)) + raw + b"\x00\x00"


def _package(package_id: int, type_name: str, keys: List[str],
             string_indexes: List[int], package_name: str = "soufit") -> bytes:
    type_pool = _pool([type_name])
    key_pool = _pool(keys)

    count = len(keys)
    spec = _spec(count)
    entries = _type(count, string_indexes)

    header_size = 288
    body = type_pool + key_pool + spec + entries
    head = bytearray(header_size)
    struct.pack_into("<HHI", head, 0, RES_TABLE_PACKAGE, header_size,
                     header_size + len(body))
    struct.pack_into("<I", head, 8, package_id)
    name = package_name.encode("utf-16-le")
    head[12:12 + len(name)] = name
    struct.pack_into("<4I", head, 268,
                     header_size, 1, header_size + len(type_pool), count)
    return bytes(head) + body


def _pool(strings: List[str]) -> bytes:
    offsets = bytearray()
    body = bytearray()
    for text in strings:
        offsets.extend(struct.pack("<I", len(body)))
        body.extend(_encode(text, True))
    while len(body) % 4:
        body.append(0)

    header_size = 28
    strings_start = header_size + len(offsets)
    size = strings_start + len(body)
    head = bytearray(header_size)
    struct.pack_into("<HHI", head, 0, RES_STRING_POOL, header_size, size)
    struct.pack_into("<5I", head, 8, len(strings), 0, UTF8_FLAG, strings_start, 0)
    return bytes(head) + bytes(offsets) + bytes(body)


def _spec(count: int) -> bytes:
    header_size = 16
    size = header_size + 4 * count
    out = bytearray(size)
    struct.pack_into("<HHI", out, 0, RES_TABLE_TYPE_SPEC, header_size, size)
    out[8] = 1
    struct.pack_into("<I", out, 12, count)
    for i in range(count):
        struct.pack_into("<I", out, header_size + 4 * i, 0)
    return bytes(out)


def _type(count: int, string_indexes: List[int], sdk: int = 0) -> bytes:
    config = bytearray(64)
    struct.pack_into("<I", config, 0, 64)
    struct.pack_into("<H", config, 24, sdk)

    entries = bytearray()
    offsets = bytearray()
    for index in string_indexes:
        offsets.extend(struct.pack("<I", len(entries)))
        entries.extend(struct.pack("<HHI", 8, 0, 0))
        entries.extend(struct.pack("<HBBI", 8, 0, TYPE_STRING, index))

    header_size = 20 + len(config)
    entries_start = header_size + len(offsets)
    size = entries_start + len(entries)

    out = bytearray(header_size)
    struct.pack_into("<HHI", out, 0, RES_TABLE_TYPE, header_size, size)
    out[8] = 1
    struct.pack_into("<II", out, 12, count, entries_start)
    out[20:20 + len(config)] = config
    return bytes(out) + bytes(offsets) + bytes(entries)
