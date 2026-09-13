# 嬉皮漫畫 Tachimanga 擴充

這是供 iOS Tachimanga 使用的非官方嬉皮漫畫擴充倉庫。

## 版本

| 版本 | 內容 |
|---|---|
| `v1.1` | 第一個可用版本，完整保留於 `archive/v1.1`。 |
| `v1.2` | 目前版本，減少更新漫畫時的重複網路請求。 |

Tachimanga 倉庫只會列出目前版本。需要退回時，可以從 `archive/v1.1` 手動取得舊成品。

詳細變更請參閱 [`CHANGELOG.md`](CHANGELOG.md)。

## 加入 Tachimanga

在 Tachimanga 開啟「更多」→「擴充套件」→「擴充套件儲存庫」→「加入」，貼上：

```text
https://raw.githubusercontent.com/Onedaytalk/tachimanga-hipmh-extension/main/index.pb
```

如果目前的 Tachimanga 版本無法讀取現代索引，可以改用相容入口：

```text
https://raw.githubusercontent.com/Onedaytalk/tachimanga-hipmh-extension/main/index.min.json
```

加入後回到擴充套件頁面重新整理，搜尋「嬉皮漫畫」。

## 倉庫內容

- `src/zh/hipmhfast`：目前版本的原始碼；目錄名稱僅為既有套件的技術識別。
- `archive/v1.1`：`v1.1` 的原始碼與成品。
- `apk/`：`v1.2` 的已簽署 APK／JAR。
- `icon/`：擴充圖示。
- `index.pb`：Tachimanga 使用的現代索引，包含明確的 `jarUrl`。
- `index.min.json`：舊格式相容索引。
- `index.json`：方便人工檢查的現代索引內容。
- `repo.json`：倉庫描述資料。

## 原始碼建置

這個倉庫只保存本擴充的來源覆蓋檔，不能單獨執行 Gradle 建置。建置時須把 `src/zh/hipmhfast` 放進相容版本的 [Keiyoushi extensions-source](https://github.com/keiyoushi/extensions-source) 專案。

後續版本必須維持原套件名稱、來源 ID 與簽署金鑰；更新規則請參閱 [`docs/RELEASING.md`](docs/RELEASING.md)。

## 聲明

本專案與嬉皮漫畫網站、Tachimanga 及 Keiyoushi 均無官方關係。擴充只負責解析公開可存取的網站內容，實際內容及服務可用性由原網站決定。

## 授權

本專案保留上游 Apache License 2.0，詳見 [`LICENSE`](LICENSE) 與 [`NOTICE.md`](NOTICE.md)。
