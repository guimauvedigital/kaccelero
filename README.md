# Kaccelero

> ⚠️ DEPRECATED
>
> Kaccelero is no longer maintained and is considered deprecated as of November 24th, 2025.
>
> You can keep using the [available packages](https://klibs.io/project/guimauvedigital/kaccelero) if you currently use
> Kaccelero, but we strongly recommend migrating to modern Ktor best practices.
>
> We also recommend going with Ktor directly for new projects.

## Why is this deprecated?

After extensive development and real-world usage, we've realized that Ktor's native tools combined with modern
architectural patterns provide a better, more maintainable solution than what Kaccelero offered.
The main issues with Kaccelero were:

- Unnecessary abstraction layer that hid Ktor's actual capabilities
- Limited flexibility when projects grew in complexity
- Harder to debug due to the additional abstraction
- Maintenance burden of keeping up with Ktor updates
- Difficult onboarding for new team members unfamiliar with the framework

## What should you use instead?

We recommend using native Ktor features combined with clean architecture patterns:

- Modern Ktor Stack
- Ktor Resources for type-safe routing
- Ktor Plugins (StatusPages, ContentNegotiation, Authentication, etc.)
- Dependency Injection with Koin
- Clean Architecture separation (domain/infrastructure/presentation)
- Repository Pattern for data access
- UseCase Pattern for business logic

## Reference Implementation

We're building [Shortt](https://github.com/nathanfallet/shortt) as a reference project demonstrating modern Ktor best
practices without Kaccelero:

- Clean Architecture (domain/infrastructure/presentation)
- Type-safe routing with Ktor Resources
- Dependency Injection with Koin
- Repository & UseCase patterns
- Proper error handling with StatusPages
- Request/Response DTOs
- Comprehensive testing
- OpenTelemetry observability
- Offline-first support

👉 Check out Shortt for a complete example of production-ready Ktor architecture.

# Migration Guide

If you're currently using Kaccelero, here's how to migrate:

- Replace Kaccelero routing → Use Ktor Resources
- Replace Kaccelero controllers → Use Ktor route functions with clean dependencies
- Adopt clean architecture → Separate domain/infrastructure/presentation layers

See the Shortt project structure for a complete example.

## Questions?

If you have questions about migration or need help adopting modern Ktor patterns, please open an issue or check out
the Shortt documentation.

Thank you to everyone who used Kaccelero. This deprecation reflects our learning and commitment to promoting better,
more maintainable practices in the Kotlin ecosystem. 🙏
