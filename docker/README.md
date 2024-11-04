# Docker primjer

## Ručno pokretanje

### Server
```bash
cd docker/server

./mvnw spring-boot:run
```

### Client
```bash
cd docker/client

./mvnw spring-boot:run
```

## Docker

### Server
```bash
cd docker/server
./mvnw clean package
docker build -t server .
```

### Client
```bash
cd docker/client
./mvnw clean package
docker build -t client .
```

## Docker compose

```bash
docker compose build
docker compose up
```

## Primjer rezultata

```
docker-client1-1  | 2024-11-04T19:15:03.205Z  INFO 1 --- [docker-client] [           main] dev.jerkicv.docker.DockerApplication     : Min response time: 9ms
docker-client1-1  | 2024-11-04T19:15:03.205Z  INFO 1 --- [docker-client] [           main] dev.jerkicv.docker.DockerApplication     : Max response time: 2001ms
docker-client1-1  | 2024-11-04T19:15:03.205Z  INFO 1 --- [docker-client] [           main] dev.jerkicv.docker.DockerApplication     : Avg response time: 992.3857142857142ms
docker-client1-1  | 2024-11-04T19:15:03.205Z  INFO 1 --- [docker-client] [           main] dev.jerkicv.docker.DockerApplication     : Mean response time: 992.0ms
docker-client1-1  | =====================================
docker-client1-1  | 2024-11-04T19:15:03.213Z  INFO 1 --- [docker-client] [           main] dev.jerkicv.docker.DockerApplication     : Success rate: 0.8898305084745762
docker-client1-1  | =====================================
```
