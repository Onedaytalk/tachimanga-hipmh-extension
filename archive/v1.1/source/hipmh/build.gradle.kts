import io.github.keiyoushi.gradle.api.ContentWarning

plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Hipmh"
    versionCode = 1
    contentWarning = ContentWarning.MIXED
    libVersion = "1.6"

    source {
        name = "\u5b09\u76ae\u6f2b\u756b"
        baseUrl = "https://m.hipmh.com"
        lang = "zh"
    }

    deeplink {
        host("hipmh.com")
        host("*.hipmh.com")
        path("/works/..*")
    }

    deeplink {
        host("reader.hipmh.top")
        path("/chapter/..*")
    }
}
