# dropwizard-sse-svc2scv

Communication between Dropwizard services using server-side events

## Build

    mvn clean package

## Start producer (server)

    java -jar server\target\server-1.0-SNAPSHOT.jar server server\dev.yml

## Start consumer (client)

    java -jar client\target\client-1.0-SNAPSHOT.jar server client\dev.yml

## Start consumer (client) using CURL

    curl -N http://localhost:8080/events
