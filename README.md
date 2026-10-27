# 嬉皮漫畫（Hipmh）Tachimanga 擴充

供 iOS Tachimanga 使用的非官方嬉皮漫畫擴充倉庫。

> [!NOTE]
> 目前版本為 `v1.4`，Tachimanga 內部顯示為 `1.6.4`。已加入儲存庫的使用者，重新整理擴充套件頁面後即可更新。

## 漫畫語言設定

在「嬉皮漫畫」來源設定中開啟「漫畫語言」，可選擇：

- 不限語言（原始排序，預設）。
- 優先簡體中文。
- 優先繁體中文。

設定影響搜尋、人氣及最新列表的每頁排序；其他語言版本仍會保留，各語言內維持原始順序。切換後請重新整理列表。

網站把不同語言版本視為獨立作品，因此此設定不會更換已加入書庫的作品與章節。若要把既有作品改成另一版本，可使用 Tachimanga 的來源遷移功能選擇對應作品。

第一次對某批作品使用優先排序時，會查詢作品的實際語言資料；已查過的資訊會暫存以減少重複等待。使用原始排序不會增加語言查詢。

## 圖片線路設定

- 預設使用網站的線路 2。
- 在「嬉皮漫畫」來源設定中開啟「圖片線路」，可選擇「線路 2（預設）」或「線路 1」。
- 切換後請重新載入章節。已下載的圖片不需要重新下載；若 App 仍保留舊的頁面清單，請重新開啟 App 後再載入該章節。
- 保留 v1.2 的套件名稱、來源 ID 與簽署金鑰，更新時不需要遷移書庫。

### 手動安裝

取得 [v1.4 JAR](apk/tachimanga-hipmh-v1.4.jar)，存到 iPhone 的「檔案」App，再於 Tachimanga「瀏覽」→「擴充」→「＋」選取檔案匯入。Tachimanga 可能顯示版本為 `1.6.4`。

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

- 新版成品：[v1.4 JAR](apk/tachimanga-hipmh-v1.4.jar)。使用 Tachimanga 時也可直接從擴充儲存庫更新。
- 上一版正式發佈：[v1.2 Release](https://github.com/Onedaytalk/tachimanga-hipmh-extension/releases/tag/v1.2)
- 檔案校驗碼：[`SHA256SUMS.txt`](SHA256SUMS.txt)
- 無法安裝、搜尋或閱讀時，請透過 [Issues](https://github.com/Onedaytalk/tachimanga-hipmh-extension/issues) 回報，並附上 Tachimanga 版本、擴充版本、錯誤畫面與重現步驟。

## 版本

| 版本 | 內容 |
|---|---|
| `v1.4` | 目前版本，新增漫畫語言優先排序，保留其他語言版本。 |
| `v1.3` | 預設圖片線路 2，並提供線路切換設定，保留於 `archive/v1.3`。 |
| `v1.2` | 減少更新漫畫時的重複網路請求，保留於 `archive/v1.2`。 |
| `v1.1` | 第一個可用版本，完整保留於 `archive/v1.1`。 |

Tachimanga 倉庫索引只會列出目前版本。舊成品保留於 `archive/v1.1`、`archive/v1.2` 或 `archive/v1.3`。

詳細變更請參閱 [`CHANGELOG.md`](CHANGELOG.md)。

## 倉庫內容

- `src/zh/hipmhfast`：目前版本的原始碼；目錄名稱僅為既有套件的技術識別。
- `archive/v1.1`、`archive/v1.2`、`archive/v1.3`：舊版原始碼與成品。
- `apk/`：已簽署 APK／JAR，目前索引列出 `v1.4`。
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

所以想請各位不要幫此專案按星星，而是去上面三個專案按。拜託!!!

感謝上述專案及其貢獻者。本專案為非官方個人維護版本，與上述專案、Tachimanga 及嬉皮漫畫網站均無官方關係；各第三方內容仍依其原授權條款使用。

## 聲明

1. 本專案與嬉皮漫畫網站、Tachimanga 及 Keiyoushi 均無官方關係。擴充只負責解析公開可存取的網站內容，實際內容及服務可用性由原網站決定。
2. 本專案最初為解決個人使用需求而建立，現公開提供給有相同需求的 Tachimanga 使用者；若網站改版導致擴充失效，歡迎透過 Issues 回報。

## 授權

本專案保留上游 Apache License 2.0，詳見 [`LICENSE`](LICENSE) 與 [`NOTICE.md`](NOTICE.md)。
