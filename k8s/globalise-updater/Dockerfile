FROM openjdk:23-ea-33-jdk-oraclelinux8

RUN microdnf update && \
    microdnf install -y curl && \
    groupadd -r annorepo && \
    useradd --no-log-init -r -g annorepo annorepo
USER annorepo
WORKDIR /apps/annorepo-updater
COPY ./globalise-updater/target/globalise-updater-*.jar globalise-updater.jar
COPY ./globalise-updater/data/pages.lang.tsv pages.lang.tsv
COPY ./k8s/globalise-updater/entrypoint.sh entrypoint.sh
COPY ./globalise-updater/conf/globalise.yml config.yml
ENTRYPOINT ["./entrypoint.sh"]