dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        mavenLocal()
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        mavenCentral()
    }
}

rootProject.name = "FOP-2324-Projekt-Root"
