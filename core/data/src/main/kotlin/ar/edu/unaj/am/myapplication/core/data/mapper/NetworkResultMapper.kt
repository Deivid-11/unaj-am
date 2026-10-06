package ar.edu.unaj.am.myapplication.core.data.mapper

import ar.edu.unaj.am.myapplication.core.domain.model.AppResult
import ar.edu.unaj.am.myapplication.core.domain.model.DomainError
import ar.edu.unaj.am.myapplication.core.network.NetworkResult

/**
 * Traduce errores técnicos (HTTP 404, IOException) a errores que entiende el negocio.
 * Así la UI nunca ve un código HTTP ni una excepción.
 */
internal inline fun <T, R> NetworkResult<T>.toAppResult(onSuccess: (T) -> R): AppResult<R> =
    when (this) {
        is NetworkResult.Success -> AppResult.Success(onSuccess(data))
        is NetworkResult.HttpError -> AppResult.Error(
            if (code == 404) DomainError.NotFound else DomainError.Unknown(message)
        )
        is NetworkResult.NetworkError -> AppResult.Error(DomainError.Network)
        is NetworkResult.ParseError -> AppResult.Error(DomainError.Unknown(message))
    }
