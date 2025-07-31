package dev.kaccelero.usecases


interface IUnitUseCase<Output> : IGenericUseCase {

    operator fun invoke(): Output

}
