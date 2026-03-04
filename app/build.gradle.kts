import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

version = 1

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) = extensions.getByName<CloudstreamExtension>("cloudstream").configuration()
fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

cloudstream {
    description = "Film e SerieTV via VixSrc con catalogo TMDB"
    authors = listOf("MC")
    status = 1
    tvTypes = listOf("TvSeries", "Movie")
    language = "it"
}

android {
    namespace = "it.vixstreamcs"
    defaultConfig {
        minSdk = 21
        compileSdkVersion(35)
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.addAll(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
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
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.google.code.gson:gson:2.10.1")
}
