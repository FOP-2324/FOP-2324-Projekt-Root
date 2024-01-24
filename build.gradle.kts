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
             studentId.set("")
             firstName.set("")
             lastName.set("")
        }
    }
    graders {
        val graderPrivate by creating {
            graderName.set("Projekt-Private")
            rubricProviderName.set("projekt.Projekt_RubricProvider")
            config.set(
                org.sourcegrade.jagr.launcher.env.Config(
                    executor = org.sourcegrade.jagr.launcher.env.Executor(),
                    transformers = org.sourcegrade.jagr.launcher.env.Transformers(
                        timeout = org.sourcegrade.jagr.launcher.env.Transformers.TimeoutTransformer(enabled = false),
                    ),
                ),
            )
            configureDependencies {
                implementation(libs.algoutils.tutor)
                implementation(libs.junit.pioneer)
            }
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
