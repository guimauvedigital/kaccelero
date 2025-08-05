# health-ktor

Health check for Ktor projects.

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:health-ktor:0.6.3")
```

```xml

<dependency>
    <groupId>dev.kaccelero</groupId>
    <artifactId>health-ktor-jvm</artifactId>
    <version>0.6.3</version>
</dependency>
```

## Usage

Install the plugin, and optionally add some checks:

```kotlin
install(Health) {
    // Add a check on `/healthz` endpoint
    healthCheck("database") {
        // Check your database connection and return a boolean
    }
    // You can also create checks on `/readyz` with `readyCheck(...)`
    // Or a custom endpoint with `customCheck("/custom", "database") { ... }`
}
```
