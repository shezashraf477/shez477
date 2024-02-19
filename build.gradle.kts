configurations.all {
    resolutionStrategy.eachDependency { details ->
        if (details.requested.group == "com.fasterxml.jackson.core" &&
            details.requested.name == "jackson-databind"
        ) {
            details.useVersion("2.13.1")
        }
    }
}
