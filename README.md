# 嬉皮漫畫（Hipmh）Tachimanga 擴充

供 iOS Tachimanga 使用的非官方嬉皮漫畫擴充倉庫。

> [!NOTE]
> 目前版本為 `v1.2`。一般使用者只需加入下方倉庫網址，不需要手動下載 APK／JAR。

## 一分鐘安裝

1. 複製以下倉庫網址：

   ```text
   https://raw.githubusercontent.com/Onedaytalk/tachimanga-hipmh-extension/main/index.pb
   ```

2. 在 Tachimanga 開啟「更多」→「擴充套件」→「擴充套件儲存庫」→「加入」，貼上網址。
3. 回到擴充套件頁面並重新整理。
4. 搜尋「嬉皮漫畫」，安裝後即可使用。

<details>
<summary>無法載入倉庫時，使用相容入口</summary>

```text
https://raw.githubusercontent.com/Onedaytalk/tachimanga-hipmh-extension/main/index.min.json
```

</details>

## 下載、校驗與問題回報

- 正式版本與成品：[v1.2 Release](https://github.com/Onedaytalk/tachimanga-hipmh-extension/releases/tag/v1.2)
- 檔案校驗碼：[`SHA256SUMS.txt`](SHA256SUMS.txt)
- 無法安裝、搜尋或閱讀時，請透過 [Issues](https://github.com/Onedaytalk/tachimanga-hipmh-extension/issues) 回報，並附上 Tachimanga 版本、擴充版本、錯誤畫面與重現步驟。

如果這個擴充對你有幫助，歡迎按下 Star，讓其他 Tachimanga 使用者更容易找到它。

## 版本

| 版本 | 內容 |
|---|---|
| `v1.2` | 目前版本，減少更新漫畫時的重複網路請求。 |
| `v1.1` | 第一個可用版本，完整保留於 `archive/v1.1`。 |

Tachimanga 倉庫只會列出目前版本。需要退回時，可以從 `archive/v1.1` 手動取得舊成品。

詳細變更請參閱 [`CHANGELOG.md`](CHANGELOG.md)。

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

## 參考與鳴謝

本專案為個人維護的非官方擴充，程式實作與維護過程使用 OpenAI Codex 協助。嬉皮漫畫的解析邏輯是依據網站當時公開呈現的頁面與網路介面實作，並非直接複製其他嬉皮漫畫擴充。

本專案的建置架構與相容介面以 [Keiyoushi extensions-source](https://github.com/keiyoushi/extensions-source) 為基礎。開發與除錯期間亦參考下列公開專案：

- [LittleSurvival／copymanga-copy20](https://github.com/LittleSurvival/copymanga-copy20)：中文漫畫擴充與倉庫發布方式參考。
- [ZhanZiyuan／tachiyomi](https://github.com/ZhanZiyuan/tachiyomi)：Tachiyomi 程式架構與擴充相容性參考。
- [Tachimanga](https://github.com/tachimanga/tachimanga)：本擴充主要使用的 iOS 應用程式。

感謝上述專案及其貢獻者。本專案為非官方個人維護版本，與上述專案、Tachimanga 及嬉皮漫畫網站均無官方關係；各第三方內容仍依其原授權條款使用。

## 聲明

1. 本專案與嬉皮漫畫網站、Tachimanga 及 Keiyoushi 均無官方關係。擴充只負責解析公開可存取的網站內容，實際內容及服務可用性由原網站決定。
2. 本專案最初為解決個人使用需求而建立，現公開提供給有相同需求的 Tachimanga 使用者；若網站改版導致擴充失效，歡迎透過 Issues 回報。

## 授權

本專案保留上游 Apache License 2.0，詳見 [`LICENSE`](LICENSE) 與 [`NOTICE.md`](NOTICE.md)。
