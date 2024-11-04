// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Adicionar KSP
    id("com.google.devtools.ksp") version "1.9.0-1.0.12" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false // Verifique a versão mais recente
}
