@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    application
    alias(libs.plugins.style)
    alias(libs.plugins.jagr.gradle)
    alias(libs.plugins.javafx)
}

version = file("version").readLines().first()

jagr {
    assignmentId.set("projekt")
    submissions {
        val main by creating {
            // studentId.set("")
            // firstName.set("")
            // lastName.set("")
        }
    }
}

dependencies {
    implementation(libs.annotations)
    implementation(libs.algoutils.student)
    testImplementation(libs.junit.core)
}

application {
    mainClass.set("projekt.Main")
}

tasks {
    val runDir = File("build/run")
    withType<JavaExec> {
        doFirst {
            runDir.mkdirs()
        }
        workingDir = runDir
    }
    test {
        doFirst {
            runDir.mkdirs()
        }
        workingDir = runDir
        useJUnitPlatform()
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

javafx {
    version = "17.0.1"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}