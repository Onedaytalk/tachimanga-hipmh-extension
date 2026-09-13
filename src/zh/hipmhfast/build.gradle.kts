import io.github.keiyoushi.gradle.api.ContentWarning

plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Hipmh"
    versionCode = 2
    contentWarning = ContentWarning.MIXED
    libVersion = "1.6"

    source {
        name = "\u5b09\u76ae\u6f2b\u756b"
        baseUrl = "https://m.hipmh.com"
        lang = "zh"
        id = 1_279_151_922_080_842_372L
    }
}
