# SC4051 BOOKIE - A simple booking system

## Project structure
The whole project is organized as one Maven project with 4 modules:
- client: the client module.
- common: shared libraries, utilities, and objects across client and server. Serialization and deserialization utility (SerializeUtils) can be found here.
- middleware: shared libraries and objects related to networking between server and client.
- server: the server module.

Useful files:
- PROTOCOL.md: describes the message serialization and client invocations.

## Build and run

0. Requirements:
- Java 17

1. Use Maven to build the parent project:
```commandline
cd path/to/booking-server
mvn clean install -DskipTests
```
We should obtain the required executable jar files to run the server and client:\
server jar: `server/target/server-1.0-SNAPSHOT.jar`\
client jar: `client/target/client-1.0-SNAPSHOT-jar-with-dependencies.jar`

Or you can use pre-buit JARs from `dist/`

2. Run the server:
```commandline
java -jar path/to/server/jar
```
**IMPORTANT**:
- In the working directory where we launch the java executable JAR for server, there must be a file `data/facilities.csv` containing information about facilities. A sample file is provided in `data/`.
- An `application.properties` file can be placed in the working directory from which the java server is launched to configure its behaviour.
- Server, once launched, cannot be stopped unless the process is killed.

3. Run the client:
```commandline
java -jar path/to/client/jar <server_address> <server_port>
```

## Server Configurations
- server.port (int): Port which the server listens on (default: 55555).
- server.at_most_once.enabled (boolean): Enable at-most-once semantic (default: false).
- server.simulated.message.drop.rate (double): The probability at which request messages will not be processed by the server to simulate request message loss (default: 0.0).
- server.simulated.response.withhold (int): The number of responses which is intentionally discarded by the server every time a request (with retry) is made. Setting this number to force the retry behaviour on the client and simulate response message loss (default: 0).