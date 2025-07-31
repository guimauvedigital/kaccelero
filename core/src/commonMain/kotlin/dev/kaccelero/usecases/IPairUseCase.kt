package dev.kaccelero.usecases


interface IPairUseCase<Input1, Input2, Output> : IGenericUseCase {

    operator fun invoke(input1: Input1, input2: Input2): Output

}
