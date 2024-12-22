package com.medblocks;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class MigrationService implements Configurable {

    private static final String[] FIRST_NAMES = {
            "John", "Jane", "Michael", "Emily", "Robert", "Sophia", "David", "Olivia", "James", "Emma",
            "William", "Isabella", "Charles", "Mia", "Daniel"
    };

    private static final String[] LAST_NAMES = {
            "Doe", "Smith", "Johnson", "Brown", "Williams", "Jones", "Garcia", "Martinez", "Hernandez", "Lopez",
            "Gonzalez", "Wilson", "Anderson", "Taylor", "Thomas"
    };

    public static final String OBSERVATION_TYPE_HEART_RATE = "HeartRate";
    public static final String OBSERVATION_TYPE_BLOOD_PRESSURE = "BloodPressure";
    public static final String[] OBSERVATION_TYPES = {
            OBSERVATION_TYPE_HEART_RATE,
            OBSERVATION_TYPE_BLOOD_PRESSURE
    };

    // HeartRate attributes
    public static final String OBSERVATION_HEART_RATE_ATTR_RATE = "rate";

    // BloodPressure attributes
    public static final String OBSERVATION_BLOOD_PRESSURE_ATTR_SYSTOLIC = "systolic";
    public static final String OBSERVATION_BLOOD_PRESSURE_ATTR_DIASTOLIC = "diastolic";

    // SQL
    public static final String SQL_TABLES_FILE = "sql1_tables.sql";

    public static void generateTables() throws Exception {
        String createTablesSql = MigrationService.readSqlTableFile(SQL_TABLES_FILE);
        MigrationService.executeSql(createTablesSql, ConnectionManager.getInstance().getDbConnection());
    }

    public static void generateData() throws IOException, SQLException, ClassNotFoundException {
        MigrationService.generatePatientData(ConnectionManager.getInstance().getDbConnection());
    }

    public static String readSqlTableFile(String fileName) throws IOException {
        InputStream inputStream = MigrationService.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("SQL file not found: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }
            return sql.toString();
        }
    }


    public static String[] readSqlInsertLinesFile(String fileName) throws IOException {
        InputStream inputStream = MigrationService.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IOException("SQL file not found: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().toArray(String[]::new);
        }
    }

    public static void generatePatientData(Connection connection) {
        int numberOfPatientsToGenerate = configService.getConfigDataGeneratePatients();
        int numberOfObservationsToGenerate = configService.getConfigDataGenerateObservations();

        for (int i = 0; i < numberOfPatientsToGenerate; i++) {
            Pair<String, String> patientInsertSql = generatePatientInsertStatement();
            try {
                log.info("SQL: " + patientInsertSql.getRight());
                executeSql(patientInsertSql.getRight(), connection);
                generateObservationsForPatient(connection, patientInsertSql.getLeft(), numberOfObservationsToGenerate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * returns Pair<pateintId, insertPatientSQL>
     */
    private static Pair<String, String> generatePatientInsertStatement() {
        String patientId = UUID.randomUUID().toString();
        String firstName = FIRST_NAMES[ThreadLocalRandom.current().nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
        String dateOfBirth = generatePatientRandomDateOfBirth();
        return Pair.of(patientId, String.format(
                "INSERT INTO patients (id, first_name, last_name, date_of_birth) VALUES ('%s', '%s', '%s', '%s');",
                patientId, firstName, lastName, dateOfBirth
        ));
    }

    private static String generatePatientRandomDateOfBirth() {
        LocalDate startDate = LocalDate.of(1950, 1, 1);
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = LocalDate.of(2020, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay).toString();
    }

    public static void generateObservationsForPatient(Connection connection, String patientId, int count) {
        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < count; i++) {
            String observationId = UUID.randomUUID().toString();

            // Random date within last 30 days
            LocalDate randomDate = LocalDate.now().minusDays(random.nextInt(30));

            // pick a random observation type
            String randomObservationType = OBSERVATION_TYPES[ThreadLocalRandom.current().nextInt(OBSERVATION_TYPES.length)];

            List<String> sqlListForInsert = new ArrayList<>();

            // insert the observation
            String insertObservationSQL = String.format("INSERT INTO observations (id, patient_id, observation_date, observation_type) VALUES ('%s', '%s', '%s', '%s');",
                    observationId, patientId, randomDate, randomObservationType);
            sqlListForInsert.add(insertObservationSQL);

            if (randomObservationType.equals(OBSERVATION_TYPE_HEART_RATE)) {
                // Random value between 60 and 100
                int heartRate = 60 + random.nextInt(41);

                // insert the observation attr for actual heart_rate
                String insertObservationHeartRateValue = String.format("INSERT INTO observation_attributes (observation_id, attr_name, attr_value) VALUES ('%s', '%s', '%s');",
                        observationId, OBSERVATION_HEART_RATE_ATTR_RATE, String.valueOf(heartRate));
                sqlListForInsert.add(insertObservationHeartRateValue);
            }

            if (randomObservationType.equals(OBSERVATION_TYPE_BLOOD_PRESSURE)) {
                // ToDo generate a "Pearson correlation coefficient" beetween 0.6 and 0.8 to use for systolic and diastolic values,
                //  so that they are not too random like 180/60

                int randomSystolic = ThreadLocalRandom.current().nextInt(70, 130 + 1);
                int randomDiastolic = ThreadLocalRandom.current().nextInt(50, 90 + 1);

                String sqlToInsertSystolic = String.format("INSERT INTO observation_attributes (observation_id, attr_name, attr_value) VALUES ('%s', '%s', '%s');",
                        observationId,OBSERVATION_BLOOD_PRESSURE_ATTR_SYSTOLIC, String.valueOf(randomSystolic));
                sqlListForInsert.add(sqlToInsertSystolic);

                String sqlToInsertDiastolic = String.format("INSERT INTO observation_attributes (observation_id, attr_name, attr_value) VALUES ('%s', '%s', '%s');",
                        observationId,OBSERVATION_BLOOD_PRESSURE_ATTR_DIASTOLIC, String.valueOf(randomDiastolic));
                sqlListForInsert.add(sqlToInsertDiastolic);
            }

            // execute the generated SQL
            for (String sql : sqlListForInsert) {
                try {
                    executeSql(sql, connection);
                    log.info("SQL: " + sql);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    public static void executeSql(String sql, Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
