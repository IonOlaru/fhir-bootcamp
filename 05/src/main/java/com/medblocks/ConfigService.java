package com.medblocks;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigService {

    // app properties
    public static final String CONFIG_APP_PORT = "app.port";
    public static final String CONFIG_DATA_GENERATE_PATIENTS = "app.data.generate.patients";
    public static final String CONFIG_DATA_GENERATE_OBSERVATIONS = "app.data.generate.observations";
    public static final String CONFIG_DATA_GENERATE_DATA = "app.data.generate.data";

    // pgSQL properties
    public static final String CONFIG_PG_JDBC_DRIVER = "pg.jdbcDriver";
    public static final String CONFIG_PG_JDBC_URL = "pg.jdbcUrl";
    public static final String CONFIG_PG_USERNAME = "pg.username";
    public static final String CONFIG_PG_PASSWORD = "pg.password";

    // yugabyte properties
    public static final String CONFIG_YB_HOST = "yb.host";
    public static final String CONFIG_YB_PORT = "yb.port";
    public static final String CONFIG_YB_USERNAME = "yb.username";
    public static final String CONFIG_YB_PASSWORD = "yb.password";
    public static final String CONFIG_YB_DATABASE = "yb.db";
    public static final String CONFIG_YB_SSL_MODE = "yb.sslMode";
    public static final String CONFIG_YB_SSL_ROOT_CERT = "yb.sslRootCert";

    private static final String PROPERTIES_FILE = "app.properties";

    private final Properties settings = new Properties();

    private ConfigService() {
        log.info("Instantiating ConfigService");
        try {
            loadConfigs();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfigs() throws IOException {
        log.info("Loading config file: {}", PROPERTIES_FILE);
        InputStream inputStream = this.getClass().getResourceAsStream("/" + PROPERTIES_FILE);
        if (inputStream != null) {
            settings.load(inputStream);
            log.info("Settings loaded {} items", settings.size());
        } else {
            log.info("No settings found");
        }
    }

    private static class Holder {
        private static final ConfigService INSTANCE = new ConfigService();
    }

    public static ConfigService getInstance() {
        return Holder.INSTANCE;
    }

    public String getAppPort() {
        return settings.getProperty(CONFIG_APP_PORT);
    }

    public String getPgJdbcDriver() {
        return settings.getProperty(CONFIG_PG_JDBC_DRIVER);
    }

    public String getPgJdbcUrl() {
        return settings.getProperty(CONFIG_PG_JDBC_URL);
    }

    public String getPgUsername() {
        return settings.getProperty(CONFIG_PG_USERNAME);
    }

    public String getPgPassword() {
        return settings.getProperty(CONFIG_PG_PASSWORD);
    }

    public String getYbHost() {
        return settings.getProperty(CONFIG_YB_HOST);
    }

    public String getYbPort() {
        return settings.getProperty(CONFIG_YB_PORT);
    }

    public String getYbUsername() {
        return settings.getProperty(CONFIG_YB_USERNAME);
    }

    public String getYbPassword() {
        return settings.getProperty(CONFIG_YB_PASSWORD);
    }

    public String getYbSslMode() {
        return settings.getProperty(CONFIG_YB_SSL_MODE);
    }

    public String getYbSslRootCert() {
        return settings.getProperty(CONFIG_YB_SSL_ROOT_CERT);
    }

    public String getYbDatabase() {
        return settings.getProperty(CONFIG_YB_DATABASE);
    }

    public int getConfigDataGeneratePatients() {
        return Integer.parseInt(settings.getProperty(CONFIG_DATA_GENERATE_PATIENTS));
    }

    public int getConfigDataGenerateObservations() {
        return Integer.parseInt(settings.getProperty(CONFIG_DATA_GENERATE_OBSERVATIONS));
    }

    public boolean getConfigDataGenerateData() {
        return Boolean.parseBoolean(settings.getProperty(CONFIG_DATA_GENERATE_DATA));
    }
}
