# routers-ktor

Ktor implementation of routers.

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:routers-ktor:0.4.13")
```

```xml

<dependency>
    <groupId>dev.kaccelero</groupId>
    <artifactId>routers-ktor-jvm</artifactId>
    <version>0.4.13</version>
</dependency>
```

## Usage

Declare a model as specified in [core](../core/README.md) to use it with routers:

```kotlin
// MyModel.kt
data class MyModel(
    override val id: Long,
    val property1: String,
    // ...
) : IModel<Long, CreateMyModelPayload, UpdateMyModelPayload>
```

```kotlin
// CreateMyModelPayload.kt
data class CreateMyModelPayload(
    val property1: String,
    // ...
)
```

```kotlin
// UpdateMyModelPayload.kt
data class UpdateMyModelPayload(
    val property1: String?,
    // ...
)
```

Then, create a controller and a router for this model:

```kotlin
class MyController(
    private val dependency1: Dependency1,
    // ...
) : IModelController<MyModel, Long, CreateMyModelPayload, UpdateMyModelPayload>
```

```kotlin
class MyRouter(
    controller: MyController
) : AbstractModelRouter<MyModel, Long, CreateMyModelPayload, UpdateMyModelPayload>(
    typeInfo<MyModel>(),
    typeInfo<CreateMyModelPayload>(),
    typeInfo<UpdateMyModelPayload>(),
    controller,
    MyController::class
)
```

You can also use some predefined types of routers, like the `APIModelRouter` ready for REST APIs.

There are also child routers, which can be used to create sub-routes.
For example, if you have a `User` model with a `Post` model where posts are created by users,
you can create a `UserRouter`, and a `PostRouter` which will be a child of the user router:

```kotlin
class PostRouter(
    controller: PostController,
    userRouter: UserRouter
) : AbstractChildModelRouter<Post, Long, CreatePostPayload, UpdatePostPayload, User>(
    typeInfo<Post>(),
    typeInfo<CreatePostPayload>(),
    typeInfo<UpdatePostPayload>(),
    controller,
    userRouter,
    PostController::class
)
```

Now it's time to write some routes in our controller.

```kotlin
@APIMapping // Will map your method in the API router
@TemplateMapping("template.ftl") // Will map your method in the Template router and render with `template.ftl`
@Path(path = "/path/to/route") // Route will be available at `/path/to/route`, prefixed with the router path (depending on model)
suspend fun myMethod(): Output {
    // ...
    return Output()
}
```

For routes related to CRUD operations on models, you can replace `@Path`
with `@ListPath`, `@GetPath`, `@CreatePath`, `@UpdatePath` or `@DeletePath`.
In that case, the path parameter of the annotation is optional and will be inferred automatically depending on the
router you choose to use (API or Template).

Here is an exemple for the `@UpdatePath` annotation, to show `@Id` and `@Payload` annotations as well:

```kotlin
@APIMapping
@TemplateMapping("update.ftl")
@UpdatePath
suspend fun update(@Id id: Long, @Payload payload: UpdateMyModelPayload): MyModel {
    // e.g. Call your usecases and return the updated model
}
```
