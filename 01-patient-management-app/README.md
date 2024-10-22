## Prerequisites

Before you begin, ensure you have met the following requirements:

- **Java 21**: 

Make sure you have JDK 21 installed on your machine.
You can check your Java version by running:
  ```bash
  java -version
```

Start the app
```
./gradlew quarkusRun
```
the output should look like this
```
$ ./gradlew quarkusRun
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2024-10-22 10:54:33,949 WARN  [io.qua.config] (main) Unrecognized configuration key "quarkus" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo
2024-10-22 10:54:34,612 INFO  [io.quarkus] (main) patient-management-app 1.0-SNAPSHOT on JVM (powered by Quarkus 3.15.1) started in 0.845s. Listening on: http://0.0.0.0:8080
2024-10-22 10:54:34,613 INFO  [io.quarkus] (main) Profile prod activated. 
2024-10-22 10:54:34,613 INFO  [io.quarkus] (main) Installed features: [cdi, micrometer, qute, qute-web, resteasy, resteasy-client, resteasy-client-jackson, resteasy-qute, smallrye-context-propagation, smallrye-health, vertx]
```

Open your browser and point it to `http://localhost:8080`