// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
}

val ktor_version: String by project
dependencies {

    implementation("io.ktor:ktor-client-core:${ktor_version}")
    implementation("io.ktor:ktor-client-cio:${ktor_version}")

    implementation("org.osmdroid:osmdroid-android:6.1.6")
    implementation("org.osmdroid:osmdroid-wms:6.1.6")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.6")
    implementation("org.osmdroid:osmdroid-geopackage:6.1.6")
}
