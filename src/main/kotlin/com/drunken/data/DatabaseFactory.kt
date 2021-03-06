package com.drunken.data

import com.drunken.data.table.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

class DatabaseFactory {

    init {
        Database.connect(dataSource())
        transaction {
            val tables = listOf<Table>(
                UserTable, KelompokTable, LombaTable, KeanggotaanTable, HistoryLombaTable
            )
            tables.forEach {
                SchemaUtils.create(it)
            }
        }
    }

    private fun dataSource(): HikariDataSource {
        val config = HikariConfig()
            .apply {
                driverClassName = System.getenv("JDBC_DRIVER")
//                jdbcUrl = System.getenv("DATABASE_URL")
                maximumPoolSize = 6
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"

                val uri = URI(System.getenv("DATABASE_URL"))
                val username = uri.userInfo.split(":").toTypedArray()[0]
                val password = uri.userInfo.split(":").toTypedArray()[1]
                jdbcUrl = "jdbc:postgresql://" + uri.host + ":" + uri.port + uri.path + "?sslmode=require" + "&user=$username&password=$password"
            }

        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }

}