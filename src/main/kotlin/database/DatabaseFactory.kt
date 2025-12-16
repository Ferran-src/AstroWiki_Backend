package org.example.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database


object DatabaseFactory {
    private val dotenv = dotenv(){
        filename = "astroEnv.env"
        directory = "${System.getProperty("user.dir")}"
    }

    fun init() {
        val config = HikariConfig().apply {

            jdbcUrl = dotenv["DATABASE_URL"] ?: error("DATABASE_URL no encontrada en .env")
            username = dotenv["DATABASE_USER"] ?: error("DATABASE_USER no encontrado en .env")
            password = dotenv["DATABASE_PASSWORD"] ?: error("DATABASE_PASSWORD no encontrado en .env")

            // Driver de la base de datos
            driverClassName = "org.postgresql.Driver"

            // Configuración del pool de conexiones
            maximumPoolSize = 20
            minimumIdle = 5
            idleTimeout = 300000
            maxLifetime = 1200000
            connectionTimeout = 30000


            addDataSourceProperty("tcpKeepAlive", "true")
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        val dataSource = HikariDataSource(config)

        Database.connect(dataSource)

    }
}