# 發佈與更新流程

## 不可變更的識別資料

後續更新必須保留：

- 套件名稱：`eu.kanade.tachiyomi.extension.zh.hipmhfast`
- 來源 ID：`1279151922080842372`
- 簽署憑證 SHA-256：`3e71fa43d85407d60b8f776cb84a6627d615436e8dd1c6b2345de920bfa72d84`

若變更其中任何一項，Tachimanga 可能把它視為不同擴充，或無法覆蓋現有版本。

## 版本規則

公開版本 `v1.2` 在擴充建置系統中的設定為：

```kotlin
versionCode = 2
libVersion = "1.6"
```

建置後對應：

```text
versionName = 1.6.2
versionCode = 106002
```

其中 `1.6` 是 Tachimanga 擴充函式庫相容版本。因此 GitHub 上標示為 `v1.2`，Tachimanga 內部可能顯示 `1.6.2`，兩者代表同一次發佈。

下一次修改程式時，將模組內的 `versionCode` 改為 `3`，GitHub 標示為 `v1.3`，建置結果應為 `1.6.3`／`106003`。不要用相同版本號覆蓋不同內容。

## 手動發佈步驟

1. 在 Keiyoushi `extensions-source` 建置環境內修改並測試 `src/zh/hipmhfast`。
2. 增加模組的 `versionCode`。
3. 使用與目前版本相同的私有簽署金鑰建置 APK 與 JAR。
4. 驗證新 JAR 的憑證指紋仍與本頁記錄相同。
5. 把新檔案放入本倉庫的 `apk/`，並使用 `tachimanga-hipmh-v1.x.jar`／`.apk` 的公開版號檔名。
6. 更新 `tools/generate_repository.py` 中對應版本與檔名。
7. 執行：

   ```powershell
   python .\tools\generate_repository.py
   ```

8. 檢查 `index.pb`、`index.json`、`index.min.json`、`repo.json` 與 `SHA256SUMS.txt` 都已更新。
9. 提交並推送至 GitHub。Tachimanga 重新整理倉庫後，才會看到較高版本的更新。

## 金鑰安全

- 不得提交 `*.jks`、`*.keystore`、密碼、Token 或 Base64 金鑰內容。
- 至少保留兩份離線簽署金鑰備份。
- 若日後使用 GitHub Actions，只能把金鑰與密碼放進 GitHub Actions Secrets。
- 簽署金鑰遺失後，已安裝的使用者將無法直接升級到以新金鑰簽署的版本。
