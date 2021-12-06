plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

version = "1.0"

kotlin {
    android()
    iosX64()
    iosArm64()
    //iosSimulatorArm64() sure all ios dependencies support this target

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "kmmshared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
//                implementation(KMMDeps.Kotlin.serialization)
//                implementation(KMMDeps.Kotlin.datetime)
//                implementation(KMMDeps.Stately.common)
//                implementation(KMMDeps.Stately.collections)
//                implementation(KMMDeps.Stately.concurrency)
//                implementation(KMMDeps.Stately.isolate)
            }
        }
        val androidMain by getting {
            dependencies {
//                implementation(KMMDeps.SqlDelight.androidDriver)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        //val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependencies {
//                implementation(KMMDeps.SqlDelight.nativeDriver)
            }
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            //iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
}

//sqldelight {
//    database("SharedDB") {
//        packageName = "com.android.kmm.db"
//        verifyMigrations = true
//    }
//}
//
//tasks.withType<DokkaTask>().configureEach {
//    dokkaSourceSets {
//        registering {
//            this.jdkVersion.set(8)
//            noStdlibLink.set(true)
//            this.sourceRoots.from(file("src/commonMain/kotlin"))
//        }
//    }
//}