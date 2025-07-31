package dev.kaccelero.usecases


interface IQuadUseCase<Input1, Input2, Input3, Input4, Output> : IGenericUseCase {

    operator fun invoke(input1: Input1, input2: Input2, input3: Input3, input4: Input4): Output

}
