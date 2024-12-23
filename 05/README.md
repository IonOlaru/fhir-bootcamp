## FHIR Server

#### Postgres
This app works by default on Postgres 13 which can be started using `docker-compose.yaml` file.
```
docker-compose up
```
Once Postgres docker container is up, run
```
./gradlew run
```
to start the app.

#### Yugabyte
YugabyteDB is a PostgreSQL-compatible resilient, scalable, flexible and distributed database for cloud native apps.

If you choose to use Yugabyte, you can create a cloud account here: https://cloud.yugabyte.com/signup
Once you create the account, use host, username and passwors in `app.properties` and change the next set of properties:
- `yb.host` - cloud hostname
- `yb.username` - cloud username
- `yb.password` - cloud password
- `yb.sslRootCert` - this file is included in the source code, adjust the path to match your local project

#### Generate data
Use this property to create tables and generate random test data everytime the app starts.
```
app.data.generate.data=true
```

#### Open the app
Open your browser at
- http://localhost:8080/metadata
- http://localhost:8080/Observation
- http://localhost:8080/Patient
