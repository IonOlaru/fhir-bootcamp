package com.medblocks.utils;

import com.yugabyte.ysql.YBClusterAwareDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class ConnectionManager implements Configurable {

    private Connection connectionPgSQL;
    private Connection connectionYugaByte;

    private final static String DB_CONNECTION_POSTGRES = "POSTGRES";
    private final static String DB_CONNECTION_YUGA_BYTE = "YUGABYTE";

    private static class Holder {
        private static final ConnectionManager INSTANCE = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return ConnectionManager.Holder.INSTANCE;
    }

    public Connection getDbConnection() throws SQLException, ClassNotFoundException {
        if (configService.getConfigAppDbType().equals(DB_CONNECTION_POSTGRES))
            return getPostgresConnection();

        if (configService.getConfigAppDbType().equals(DB_CONNECTION_YUGA_BYTE))
            return getYugaByteConnection();

        throw new RuntimeException("Choose a DB connection type.");
    }

    private Connection getPostgresConnection() throws ClassNotFoundException, SQLException {
        if (connectionPgSQL != null)
            return connectionPgSQL;

        log.info("Establishing Postgres connection...");
        Class.forName(configService.getPgJdbcDriver());
        String jdbcUrl = configService.getPgJdbcUrl();
        String username = configService.getPgUsername();
        String password = configService.getPgPassword();
        connectionPgSQL = DriverManager.getConnection(jdbcUrl, username, password);

        return connectionPgSQL;
    }

    private Connection getYugaByteConnection() throws SQLException {
        if (connectionYugaByte != null)
            return connectionYugaByte;

        log.info("Establishing Yugabyte connection...");
        YBClusterAwareDataSource ds = new YBClusterAwareDataSource();
        ds.setUrl("jdbc:yugabytedb://" + configService.getYbHost() + ":" + configService.getYbPort() + "/" + configService.getYbDatabase());
        ds.setUser(configService.getYbUsername());
        ds.setPassword(configService.getYbPassword());
        String sslMode = configService.getYbSslMode();

        if (!sslMode.isEmpty() && !sslMode.equalsIgnoreCase("disable")) {
            ds.setSsl(true);
            ds.setSslMode(sslMode);

            if (!configService.getYbSslRootCert().isEmpty())
                ds.setSslRootCert(configService.getYbSslRootCert());
        }
        connectionYugaByte = ds.getConnection();
        return connectionYugaByte;
    }
}
