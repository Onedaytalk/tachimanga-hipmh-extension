#!/usr/bin/env python3
"""Generate deterministic Tachimanga repository indexes without external packages."""

from __future__ import annotations

import gzip
import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REPOSITORY = "Onedaytalk/tachimanga-hipmh-extension"
REPOSITORY_URL = f"https://github.com/{REPOSITORY}"
RAW_BASE_URL = f"https://raw.githubusercontent.com/{REPOSITORY}/main"
SIGNING_KEY = "3e71fa43d85407d60b8f776cb84a6627d615436e8dd1c6b2345de920bfa72d84"

EXTENSIONS = [
    {
        "name": "Hipmh",
        "packageName": "eu.kanade.tachiyomi.extension.zh.hipmhfast",
        "apk": "tachimanga-hipmh-v1.2.apk",
        "jar": "tachimanga-hipmh-v1.2.jar",
        "extensionLib": "1.6",
        "versionCode": 106002,
        "versionName": "1.6.2",
        "contentWarning": 2,
        "source": {
            "id": 1279151922080842372,
            "name": "嬉皮漫畫",
            "language": "zh",
            "homeUrl": "https://m.hipmh.com",
        },
    },
]

CONTENT_WARNING_NAMES = {
    0: "CONTENT_WARNING_UNSPECIFIED",
    1: "CONTENT_WARNING_SAFE",
    2: "CONTENT_WARNING_MIXED",
    3: "CONTENT_WARNING_NSFW",
}


def _varint(value: int) -> bytes:
    if value < 0:
        raise ValueError("Only non-negative integers are supported")
    output = bytearray()
    while value > 0x7F:
        output.append((value & 0x7F) | 0x80)
        value >>= 7
    output.append(value)
    return bytes(output)


def _key(field_number: int, wire_type: int) -> bytes:
    return _varint((field_number << 3) | wire_type)


def _integer(field_number: int, value: int) -> bytes:
    return _key(field_number, 0) + _varint(value)


def _payload(field_number: int, value: bytes) -> bytes:
    return _key(field_number, 2) + _varint(len(value)) + value


def _string(field_number: int, value: str) -> bytes:
    return _payload(field_number, value.encode("utf-8"))


def _encode_source(source: dict[str, object]) -> bytes:
    return b"".join(
        [
            _integer(1, int(source["id"])),
            _string(2, str(source["name"])),
            _string(3, str(source["language"])),
            _string(4, str(source["homeUrl"])),
        ]
    )


def _encode_resources(extension: dict[str, object]) -> bytes:
    package_name = str(extension["packageName"])
    return b"".join(
        [
            _string(1, f"{RAW_BASE_URL}/apk/{extension['apk']}"),
            _string(2, f"{RAW_BASE_URL}/icon/{package_name}.png"),
            _string(501, f"{RAW_BASE_URL}/apk/{extension['jar']}"),
        ]
    )


def _encode_extension(extension: dict[str, object]) -> bytes:
    return b"".join(
        [
            _string(1, str(extension["name"])),
            _string(2, str(extension["packageName"])),
            _payload(3, _encode_resources(extension)),
            _string(4, str(extension["extensionLib"])),
            _integer(5, int(extension["versionCode"])),
            _string(6, str(extension["versionName"])),
            _integer(7, int(extension["contentWarning"])),
            _payload(8, _encode_source(extension["source"])),
        ]
    )


def _encode_index() -> bytes:
    contact = _string(1, REPOSITORY_URL)
    extension_list = b"".join(_payload(1, _encode_extension(ext)) for ext in EXTENSIONS)
    return b"".join(
        [
            _string(1, "Onedaytalk Hipmh Extensions"),
            _string(2, "HIPMH"),
            _string(3, SIGNING_KEY),
            _payload(4, contact),
            _payload(101, extension_list),
        ]
    )


def _modern_json() -> dict[str, object]:
    extensions = []
    for extension in EXTENSIONS:
        source = extension["source"]
        package_name = str(extension["packageName"])
        extensions.append(
            {
                "name": extension["name"],
                "packageName": package_name,
                "resources": {
                    "apkUrl": f"{RAW_BASE_URL}/apk/{extension['apk']}",
                    "iconUrl": f"{RAW_BASE_URL}/icon/{package_name}.png",
                    "jarUrl": f"{RAW_BASE_URL}/apk/{extension['jar']}",
                },
                "extensionLib": extension["extensionLib"],
                "versionCode": str(extension["versionCode"]),
                "versionName": extension["versionName"],
                "contentWarning": CONTENT_WARNING_NAMES[int(extension["contentWarning"])],
                "sources": [
                    {
                        "id": str(source["id"]),
                        "name": source["name"],
                        "language": source["language"],
                        "homeUrl": source["homeUrl"],
                    }
                ],
            }
        )
    return {
        "name": "Onedaytalk Hipmh Extensions",
        "badgeLabel": "HIPMH",
        "signingKey": SIGNING_KEY,
        "contact": {"website": REPOSITORY_URL},
        "extensionList": {"extensions": extensions},
    }


def _legacy_json() -> list[dict[str, object]]:
    extensions = []
    for extension in EXTENSIONS:
        source = extension["source"]
        extensions.append(
            {
                "name": f"Tachiyomi: {extension['name']}",
                "pkg": extension["packageName"],
                "apk": extension["jar"],
                "lang": source["language"],
                "code": extension["versionCode"],
                "version": extension["versionName"],
                "nsfw": 1 if int(extension["contentWarning"]) >= 2 else 0,
                "sources": [
                    {
                        "name": source["name"],
                        "lang": source["language"],
                        "id": str(source["id"]),
                        "baseUrl": source["homeUrl"],
                    }
                ],
            }
        )
    return extensions


def _write_json(path: Path, value: object, *, compact: bool = False) -> None:
    options = {"ensure_ascii": False}
    if compact:
        options["separators"] = (",", ":")
    else:
        options["indent"] = 2
    path.write_text(json.dumps(value, **options) + "\n", encoding="utf-8")


def _validate_assets() -> None:
    missing = []
    for extension in EXTENSIONS:
        for filename in (extension["apk"], extension["jar"]):
            path = ROOT / "apk" / str(filename)
            if not path.is_file():
                missing.append(str(path.relative_to(ROOT)))
        icon = ROOT / "icon" / f"{extension['packageName']}.png"
        if not icon.is_file():
            missing.append(str(icon.relative_to(ROOT)))
    if missing:
        raise FileNotFoundError("Missing repository assets: " + ", ".join(missing))


def _write_checksums() -> None:
    files = {
        ROOT / "apk" / str(extension[filename_key])
        for extension in EXTENSIONS
        for filename_key in ("apk", "jar")
    }
    archive = ROOT / "archive"
    if archive.is_dir():
        files.update(
            path
            for path in archive.rglob("*")
            if path.is_file() and path.suffix in {".apk", ".jar"}
        )

    lines = []
    for path in sorted(files, key=lambda item: item.as_posix()):
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        lines.append(f"{digest}  {path.relative_to(ROOT).as_posix()}")
    (ROOT / "SHA256SUMS.txt").write_text("\n".join(lines) + "\n", encoding="ascii")


def main() -> None:
    _validate_assets()
    _write_json(ROOT / "index.json", _modern_json())
    _write_json(ROOT / "index.min.json", _legacy_json(), compact=True)
    _write_json(
        ROOT / "repo.json",
        {
            "index_v2": f"{RAW_BASE_URL}/index.pb",
            "meta": {
                "name": "Onedaytalk Hipmh Extensions",
                "website": REPOSITORY_URL,
                "signingKeyFingerprint": SIGNING_KEY,
            },
        },
    )
    (ROOT / "index.pb").write_bytes(gzip.compress(_encode_index(), compresslevel=9, mtime=0))
    _write_checksums()
    print("Generated index.pb, index.json, index.min.json, repo.json, and SHA256SUMS.txt")


if __name__ == "__main__":
    main()
