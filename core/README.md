# core

Core of kaccelero.

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:core:0.6.10")
```

```xml

<dependency>
    <groupId>dev.kaccelero</groupId>
    <artifactId>core-jvm</artifactId>
    <version>0.6.10</version>
</dependency>
```

## Usage

### First UseCase

Create a new class that extends `IUseCase` or `ISuspendUseCase`:

```kotlin
interface IMyUseCase : IUseCase<Input, Output>
```

```kotlin
class MyUseCase(
    private val dependency1: Dependency1,
    // ...
) : IMyUseCase {

    // If you want to use suspend functions, use `ISuspendUseCase` instead
    override fun invoke(input: Input): Output {
        // Do something with dependencies and input
        // ...

        // Return output
        return Output()
    }

}
```

Then, you can use it like this: (example with Koin, but you can use any DI library, or even instantiate it manually)

```kotlin
single<IMyUseCase> { MyUseCase(get(), /*...*/) }
```

```kotlin
val useCase = get<IMyUseCase>()
val output = useCase(Input())
```

### Variants

`IUseCase` and `ISuspendUseCase` are the base interfaces taking one input and returning one output.
We made some variants to make it easier to use:

- `IUnitUseCase` and `IUnitSuspendUseCase` for no input
- `IPairUseCase` and `IPairSuspendUseCase` for two inputs
- `ITripleUseCase` and `ITripleSuspendUseCase` for three inputs
- `IQuadUseCase` and `IQuadSuspendUseCase` for four inputs

### Models

A common use of UseCases is to make things with a model. That's why we made an interface for models with associated
UseCases:

```kotlin
data class MyModel(
    override val id: Long,
    val property1: String,
    // ...
) : IModel<Long, CreateMyModelPayload, UpdateMyModelPayload>
```

```kotlin
data class CreateMyModelPayload(
    val property1: String,
    // ...
)
```

```kotlin
data class UpdateMyModelPayload(
    val property1: String?,
    // ...
)
```

`CreateMyModelPayload` and `UpdateMyModelPayload` are payloads used to create and update the model.
In case you don't support creating or updating your model, you can use `Unit` instead.

Then, you can create and use associated UseCases:

```kotlin
class ListMyModelUseCase : IListModelUseCase<MyModel> {
    /* ... */
}
```

```kotlin
class GetMyModelUseCase : IGetModelUseCase<MyModel, Long> {
    /* ... */
}
```

```kotlin
class CreateMyModelUseCase : ICreateModelUseCase<MyModel, CreateMyModelPayload> {
    /* ... */
}
```

```kotlin
class UpdateMyModelUseCase : IUpdateModelUseCase<MyModel, Long, UpdateMyModelPayload> {
    /* ... */
}
```

```kotlin
class DeleteMyModelUseCase : IDeleteModelUseCase<MyModel, Long> {
    /* ... */
}
```

Expecting those interfaces can help you to make your code more generic and reusable.

Of course, you can also use suspending variants:
`IListModelSuspendUseCase`, `IGetModelSuspendUseCase`, `ICreateModelSuspendUseCase`, `IUpdateModelSuspendUseCase`
and `IDeleteModelSuspendUseCase`.

### Models with Repositories

We also provide `IModelRepository` and `IModelSuspendRepository` to make repositories for your models:

```kotlin
class MyModelRepository(
    private val dependency1: Dependency1,
    // ...
) : IModelRepository<MyModelRepository, MyModel, Long, CreateMyModelPayload, UpdateMyModelPayload> {

    override fun list(context: IContext?): MyModel? {
        /* ... */
    }

    override fun list(pagination: Pagination, context: IContext?): MyModel? {
        /* ... */
    }

    override fun get(id: Id, context: IContext?): MyModel? {
        /* ... */
    }

    override fun create(payload: CreateMyModelPayload, context: IContext?): MyModel? {
        /* ... */
    }

    override fun update(id: Id, payload: UpdateMyModelPayload, context: IContext?): Boolean {
        /* ... */
    }

    override fun delete(id: Id, context: IContext?): Boolean {
        /* ... */
    }

}
```

Methods are optionals, so you can implement only what you need.

We also provide default implementations for `IListModelUseCase`, `IGetModelUseCase`, `ICreateModelUseCase`,
`IUpdateModelUseCase`and `IDeleteModelUseCase`:

```kotlin
class ListMyModelUseCase(repository: MyModelRepository) :
    ListModelFromRepositoryUseCase<MyModel>(repository)
```

```kotlin
class GetMyModelUseCase(repository: MyModelRepository) :
    GetModelFromRepositoryUseCase<MyModel, Long>(repository)
```

```kotlin
class CreateMyModelUseCase(repository: MyModelRepository) :
    CreateModelFromRepositoryUseCase<MyModel, CreateMyModelPayload>(repository)
```

```kotlin
class UpdateMyModelUseCase(repository: MyModelRepository) :
    UpdateModelFromRepositoryUseCase<MyModel, Long, UpdateMyModelPayload>(repository)
```

```kotlin
class DeleteMyModelUseCase(repository: MyModelRepository) :
    DeleteModelFromRepositoryUseCase<MyModel, Long>(repository)
```

Suspend variants are available too:
`ListModelFromRepositorySuspendUseCase`, `GetModelFromRepositorySuspendUseCase`,
`CreateModelFromRepositorySuspendUseCase`,
`UpdateModelFromRepositorySuspendUseCase` and `DeleteModelFromRepositorySuspendUseCase`.
