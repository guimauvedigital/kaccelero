# security-key-ktor

An API key plugin for Ktor

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:security-key-ktor:0.6.10")
```

```xml

<dependency>
    <groupId>dev.kaccelero</groupId>
    <artifactId>security-key-ktor-jvm</artifactId>
    <version>0.6.10</version>
</dependency>
```

## Usage

Install the plugin:

```kotlin
val myKey = environment.config.property("key").getString() // Take it from environment variable or config file

authentication {
    apiKey("api-key") {
        validate { keyFromHeader ->
            keyFromHeader.takeIf { it == myKey }?.let { AppPrincipal(it) }
        }
    }
}
```
