package dev.kaccelero.database

import io.sentry.ITransaction
import io.sentry.Sentry
import io.sentry.SpanStatus
import io.sentry.TransactionOptions
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.StatementInterceptor
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi

object SentryTransactionInterceptor : StatementInterceptor {

    private val sentryTransactions: MutableMap<String, ITransaction> = mutableMapOf()

    override fun beforeExecution(transaction: Transaction, context: StatementContext) {
        val sentryTransaction = Sentry.startTransaction(
            /* name = */ context.sql(transaction),
            /* operation = */ "db",
            /* transactionOptions = */TransactionOptions().apply {
                isBindToScope = true
            }
        )
        sentryTransactions[transaction.id] = sentryTransaction
    }

    override fun afterExecution(
        transaction: Transaction,
        contexts: List<StatementContext>,
        executedStatement: PreparedStatementApi,
    ) {
        val sentryTransaction = sentryTransactions[transaction.id] ?: return
        sentryTransaction.finish(SpanStatus.OK)
        sentryTransactions.remove(transaction.id)
    }

}
