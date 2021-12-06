const val KOTLINX_SERIALIZATION_VERSION = "1.3.0"
const val STATELY_VERSION = "1.1.10"
const val SQLDELIGHT_VERSION = "1.4.4"
const val REFLECTION_VERSION = "0.1.0"
const val DATETIME_VERSION = "0.3.1"

object KMMDeps {
    object Kotlin {
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${KOTLINX_SERIALIZATION_VERSION}"
        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${DATETIME_VERSION}"
    }

    object Stately {
        const val common = "co.touchlab:stately-common:$STATELY_VERSION"
        const val concurrency = "co.touchlab:stately-concurrency:$STATELY_VERSION"
        const val collections = "co.touchlab:stately-iso-collections:${STATELY_VERSION}-a1"
        const val isolate = "co.touchlab:stately-isolate:${STATELY_VERSION}-a1"
    }

    object SqlDelight {
        const val androidDriver = "com.squareup.sqldelight:android-driver:$SQLDELIGHT_VERSION"
        const val nativeDriver = "com.squareup.sqldelight:native-driver:$SQLDELIGHT_VERSION"
    }

    object Common {
        const val reflection_ios = "com.android.kmm.common:reflection:$REFLECTION_VERSION"
    }
}
