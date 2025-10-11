plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
include("modules:domain")
include("modules:host")
include("modules:http")
include("modules:repository")
include("modules:repository_jdbi")
include("modules:services")

rootProject.name = "Daw_DP"

