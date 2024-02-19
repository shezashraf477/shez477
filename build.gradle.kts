configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == "com.fasterxml.jackson.core" &&
            details.requested.name == "jackson-databind"
        ) {
            details.useVersion("2.13.1")
        }
    }
}
dependencyManagement {
    dependencies {
        dependency("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    }
}
