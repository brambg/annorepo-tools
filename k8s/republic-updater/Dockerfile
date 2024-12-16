FROM openjdk:23-ea-33-jdk-oraclelinux8

RUN microdnf update && \
    microdnf install -y curl && \
    groupadd -r annorepo && \
    useradd --no-log-init -r -g annorepo annorepo
USER annorepo
WORKDIR /apps/annorepo-updater
COPY ./republic-updater/target/republic-updater-*.jar republic-updater.jar
COPY ./republic-updater/data/provenance.csv data/provenance.csv
COPY ./k8s/republic-updater/entrypoint.sh entrypoint.sh
COPY ./republic-updater/conf/republic.yml config.yml
ENTRYPOINT ["./entrypoint.sh"]
