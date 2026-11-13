import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Properties

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

version = 3

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) = extensions.getByName<CloudstreamExtension>("cloudstream").configuration()
fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

cloudstream {
    description = "Film e SerieTV via VixSrc con catalogo TMDB"
    authors = listOf("MC")
    version = 10
    status = 1
    tvTypes = listOf("TvSeries", "Movie")
    language = "it"
    setRepo(System.getenv("GITHUB_REPOSITORY") ?: "MicheleCicinelli/VixStreamCS")
}

android {
    namespace = "it.vixstreamcs"

    extensions.configure<com.android.build.gradle.BaseExtension> {
        buildFeatures.buildConfig = true
    }

    defaultConfig {
        minSdk = 21
        compileSdkVersion(35)
        targetSdk = 35

        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        } else {
            logger.warn("local.properties non trovato. La chiave TMDB_API_KEY sarà vuota.")
        }

        val tmdbApiKey = properties.getProperty("TMDB_API_KEY")
            ?: System.getenv("TMDB_API_KEY")
            ?: ""

        if (tmdbApiKey.isEmpty()) {
            logger.warn("TMDB_API_KEY non impostata. L'estensione non funzionerà correttamente.")
        }

        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions",
                "-Xannotation-default-target=param-property"
            )
        }
    }
}

dependencies {
    val implementation by configurations
    val cloudstream by configurations

    cloudstream("com.lagradost:cloudstream3:pre-release")

    implementation(kotlin("stdlib"))
    implementation("com.github.Blatzar:NiceHttp:0.4.16")
    implementation("org.jsoup:jsoup:1.22.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("com.google.code.gson:gson:2.10.1")
}
