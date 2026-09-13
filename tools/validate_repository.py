#!/usr/bin/env python3
"""Validate generated repository files and decode the Protobuf wire format."""

from __future__ import annotations

import gzip
import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def _read_varint(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        if offset >= len(data) or shift >= 70:
            raise ValueError("Invalid Protobuf varint")
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if byte < 0x80:
            return value, offset
        shift += 7


def _fields(data: bytes) -> list[tuple[int, int, object]]:
    result = []
    offset = 0
    while offset < len(data):
        key, offset = _read_varint(data, offset)
        number = key >> 3
        wire_type = key & 0x07
        if wire_type == 0:
            value, offset = _read_varint(data, offset)
        elif wire_type == 2:
            length, offset = _read_varint(data, offset)
            end = offset + length
            if end > len(data):
                raise ValueError(f"Field {number} extends past the message")
            value = data[offset:end]
            offset = end
        else:
            raise ValueError(f"Unsupported wire type {wire_type} in field {number}")
        result.append((number, wire_type, value))
    return result


def _one(fields: list[tuple[int, int, object]], number: int) -> object:
    values = [value for field, _, value in fields if field == number]
    if len(values) != 1:
        raise ValueError(f"Expected one field {number}, found {len(values)}")
    return values[0]


def _text(value: object) -> str:
    if not isinstance(value, bytes):
        raise TypeError("Expected a length-delimited string")
    return value.decode("utf-8")


def _decode_source(data: bytes) -> dict[str, object]:
    fields = _fields(data)
    return {
        "id": str(_one(fields, 1)),
        "name": _text(_one(fields, 2)),
        "language": _text(_one(fields, 3)),
        "homeUrl": _text(_one(fields, 4)),
    }


def _decode_resources(data: bytes) -> dict[str, object]:
    fields = _fields(data)
    return {
        "apkUrl": _text(_one(fields, 1)),
        "iconUrl": _text(_one(fields, 2)),
        "jarUrl": _text(_one(fields, 501)),
    }


def _decode_extension(data: bytes) -> dict[str, object]:
    fields = _fields(data)
    source_values = [value for field, _, value in fields if field == 8]
    return {
        "name": _text(_one(fields, 1)),
        "packageName": _text(_one(fields, 2)),
        "resources": _decode_resources(_one(fields, 3)),
        "extensionLib": _text(_one(fields, 4)),
        "versionCode": str(_one(fields, 5)),
        "versionName": _text(_one(fields, 6)),
        "contentWarningValue": _one(fields, 7),
        "sources": [_decode_source(value) for value in source_values],
    }


def _decode_index(data: bytes) -> dict[str, object]:
    fields = _fields(data)
    contact = _fields(_one(fields, 4))
    extension_list = _fields(_one(fields, 101))
    return {
        "name": _text(_one(fields, 1)),
        "badgeLabel": _text(_one(fields, 2)),
        "signingKey": _text(_one(fields, 3)),
        "website": _text(_one(contact, 1)),
        "extensions": [
            _decode_extension(value)
            for field, _, value in extension_list
            if field == 1
        ],
    }


def _validate_checksums() -> None:
    expected = {}
    for line in (ROOT / "SHA256SUMS.txt").read_text(encoding="ascii").splitlines():
        digest, filename = line.split("  ", maxsplit=1)
        expected[filename] = digest
    for filename, digest in expected.items():
        actual = hashlib.sha256((ROOT / filename).read_bytes()).hexdigest()
        if actual != digest:
            raise ValueError(f"Checksum mismatch: {filename}")


def main() -> None:
    compressed = (ROOT / "index.pb").read_bytes()
    decoded = _decode_index(gzip.decompress(compressed))
    modern = json.loads((ROOT / "index.json").read_text(encoding="utf-8"))
    legacy = json.loads((ROOT / "index.min.json").read_text(encoding="utf-8"))
    repo = json.loads((ROOT / "repo.json").read_text(encoding="utf-8"))

    if decoded["name"] != modern["name"]:
        raise ValueError("index.pb and index.json repository names differ")
    if decoded["signingKey"] != modern["signingKey"]:
        raise ValueError("index.pb and index.json signing keys differ")
    if len(decoded["extensions"]) != len(modern["extensionList"]["extensions"]):
        raise ValueError("index.pb and index.json extension counts differ")
    if len(legacy) != len(decoded["extensions"]):
        raise ValueError("Legacy and modern extension counts differ")
    if not repo["index_v2"].endswith("/index.pb"):
        raise ValueError("repo.json does not point to index.pb")

    for protobuf_ext, json_ext, legacy_ext in zip(
        decoded["extensions"], modern["extensionList"]["extensions"], legacy
    ):
        for key in ("name", "packageName", "extensionLib", "versionCode", "versionName"):
            if protobuf_ext[key] != json_ext[key]:
                raise ValueError(f"index.pb and index.json differ for {json_ext['name']}: {key}")
        if protobuf_ext["resources"] != json_ext["resources"]:
            raise ValueError(f"Resource URLs differ for {json_ext['name']}")
        if protobuf_ext["sources"] != json_ext["sources"]:
            raise ValueError(f"Sources differ for {json_ext['name']}")
        if legacy_ext["pkg"] != json_ext["packageName"]:
            raise ValueError(f"Legacy package differs for {json_ext['name']}")
        if not protobuf_ext["resources"]["jarUrl"].endswith(".jar"):
            raise ValueError(f"Missing JAR URL for {json_ext['name']}")

    _validate_checksums()
    print(
        "Repository validation passed: "
        + ", ".join(
            f"{extension['name']} {extension['versionName']}"
            for extension in decoded["extensions"]
        )
    )


if __name__ == "__main__":
    main()
