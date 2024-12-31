FROM openjdk:23-slim

# LiteFS
RUN apt-get update -y && apt-get install -y ca-certificates fuse3 sqlite3
COPY --from=flyio/litefs:0.5 /usr/local/bin/litefs /usr/local/bin/litefs

COPY target/*-0.0.1-SNAPSHOT.jar /app.jar
COPY litefs.yml .

ENTRYPOINT litefs mount
