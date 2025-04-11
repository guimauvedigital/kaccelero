package dev.kaccelero.database

import org.jetbrains.exposed.sql.Transaction

fun Transaction.sentry() {
    registerInterceptor(SentryTransactionInterceptor)
}
