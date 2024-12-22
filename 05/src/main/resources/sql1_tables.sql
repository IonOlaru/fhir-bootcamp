CREATE TABLE IF NOT EXISTS patients (
    id uuid PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    date_of_birth DATE
);

CREATE TABLE IF NOT EXISTS observations (
    id uuid PRIMARY KEY,
    patient_id uuid NOT NULL REFERENCES patients(id),
    observation_type VARCHAR(16) NOT NULL,
    observation_date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS observation_attributes (
    observation_id uuid REFERENCES observations(id),
    attr_name VARCHAR(64) NOT NULL,
    attr_value VARCHAR(64)
);

TRUNCATE TABLE observation_attributes;
DELETE FROM observations;
DELETE FROM patients;
