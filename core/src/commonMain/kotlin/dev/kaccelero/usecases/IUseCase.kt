package dev.kaccelero.usecases


interface IUseCase<Input, Output> : IGenericUseCase {

    operator fun invoke(input: Input): Output

}
