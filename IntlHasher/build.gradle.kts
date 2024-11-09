version = "1.0.0"
description = "Adds command to hash strings in discord-intl way"

aliucord {
    // Changelog of your plugin
    changelog.set("""Nothing yet.""".trimIndent())

    // Excludes this plugin from the updater, meaning it won't show up for users.
    // Set this if the plugin is unfinished
    excludeFromUpdaterJson.set(true)
}

dependencies {
    implementation("com.joom.xxhash:xxhash-android:1.2.0")
}
