# Welcome to the OpenTelemetry Glitch Java Workshop!

In this Glitch workshop, we will instrument a Java application. Note this
workshop is meant to come after the Node workshop. The application and workshop
steps can be found in `/src/main/java`. The answer to this workshop can be
found in `src_instrumented`. Have suggestions on how to improve this lab? PRs
welcomed!


## How to run

### Prerequisites
* Java 1.8.231
* Be on the project root folder

### 1 - Compile 
```bash
gradlew fatJar
```

### 2 - Start the Server
```bash
java -cp ./build/libs/opentelemetry-ikea-workshop-all-0.1.0.jar backend.BackEnd
```
 
### 3 - Start the Client
```bash
java -cp ./build/libs/opentelemetry-ikea-workshop-all-0.1.0.jar frontend.FrontEnd
```