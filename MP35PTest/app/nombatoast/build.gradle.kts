/*
 * Copyright (c) 2022. Nomba Financial Services
 *
 * author: Victor Shoaga
 * email: victor.shoaga@nomba.com
 * github: @inventvictor
 *
 */

plugins {
    id("nombax.android.library")
    id("nombax.android.library.jacoco")
    kotlin("kapt")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.nomba.pro.core.nombatoast"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.inventvictor.motiontoast)
}