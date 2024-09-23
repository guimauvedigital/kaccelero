# i18n-ktor-freemarker

An i18n plugin for Ktor Freemarker

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:i18n-ktor-freemarker:0.3.0")
```

```xml

<dependencies>
    <dependency>
        <groupId>dev.kaccelero</groupId>
        <artifactId>i18n-ktor-freemarker-jvm</artifactId>
        <version>0.3.0</version>
    </dependency>
</dependencies>
```

## Usage

### Setup

```kotlin
install(I18n) {
    // ... (see i18n-ktor documentation)
}
install(FreeMarker) {
    // Add our directive
    setSharedVariable("t", TDirective(i18n))

    // ... (see ktor-freemarker documentation)
}
```

### Response

Give the locale to the template, so that it can resolve automatically.

```kotlin
call.respondTemplate(
    "template.ftl",
    mapOf(
        "locale" to call.locale,
        // ...
    )
)
```

### Template

```html
<@t key="greeting" />
```

If you want to supply arguments, you can use the `args` parameter.

```html
<@t key="greeting" args=["Nathan"] />
```

You can also use the `locale` parameter to override the current locale (resolved by the model).

```html
<@t locale="fr" key="greeting" />
```
