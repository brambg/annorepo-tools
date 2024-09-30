all: help
TAG = annorepo-tools
UPDATER_TAG=annorepo-globalise-updater
DOCKER_DOMAIN = registry.diginfra.net/tt
SHELL=/bin/bash
version_fn = $(shell cat .make/.version 2>/dev/null)

.make:
	mkdir -p .make

.make/.version: .make pom.xml
	mvn help:evaluate -Dexpression=project.version -q -DforceStdout > .make/.version

performance-tester/target/performance-tester-$(call version_fn).jar: .make/.version $(shell find performance-tester/src/main -type f) pom.xml performance-tester/pom.xml
	mvn --projects performance-tester --also-make package

globalise-updater/target/globalise-updater-$(call version_fn).jar: .make/.version $(shell find globalise-updater/src/main -type f) pom.xml globalise-updater/pom.xml
	mvn --projects globalise-updater --also-make package

.PHONY: run-performance-tester
run-performance-tester: performance-tester/target/performance-tester-$(call version_fn).jar
	java -jar performance-tester/target/performance-tester-$(call version_fn).jar

.PHONY: run-globalise-updater-local
run-globalise-updater-local: globalise-updater/target/globalise-updater-$(call version_fn).jar
	cd globalise-updater && java -jar target/globalise-updater-$(call version_fn).jar conf/local.yml

.make/.docker: .make k8s/globalise-updater/Dockerfile
	docker build -t $(UPDATER_TAG):$(call version_fn) -f k8s/globalise-updater/Dockerfile .
	@touch $@

.make/.push-updater: k8s/globalise-updater/Dockerfile globalise-updater/target/globalise-updater-$(call version_fn).jar
	docker build -t $(UPDATER_TAG):$(call version_fn) --platform=linux/amd64 -f k8s/globalise-updater/Dockerfile .
	docker tag $(UPDATER_TAG):$(call version_fn) $(DOCKER_DOMAIN)/$(UPDATER_TAG):$(call version_fn)
	docker push $(DOCKER_DOMAIN)/$(UPDATER_TAG):$(call version_fn)
	@touch $@

.PHONY: push
push: .make/.push-updater

.PHONY: clean
clean:
	mvn clean

.PHONY: version-update
version-update:
	mvn versions:set && mvn versions:commit && find . -name dependency-reduced-pom.xml -delete

.PHONY: tests
tests:
	mvn test -Dmaven.plugin.validation=VERBOSE

.PHONY: help
help:
	@echo "make-tools for $(TAG)"
	@echo
	@echo "Please use \`make <target>', where <target> is one of:"
	@echo "  tests                  - to test the project"
	@echo "  clean                  - to remove generated files"
	@echo "  version-update         - to update the project version"
	@echo "  push                   - to create a docker image for globalise-updater and push it to $(DOCKER_DOMAIN)"
	@echo
	@echo "  run-performance-tester      - to run the performance tester"
	@echo "  run-globalise-updater-local - to run the globalise updater using local settings"
	@echo
