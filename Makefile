all: help
TAG = annorepo-tools
GLOBALISE_UPDATER_TAG=annorepo-globalise-updater
REPUBLIC_UPDATER_TAG=annorepo-republic-updater
DOCKER_DOMAIN = registry.diginfra.net/tt
SHELL=/bin/bash
version_fn = $(shell cat .make/.version 2>/dev/null)

.make:
	mkdir -p .make

.make/.version: .make pom.xml
	mvn help:evaluate -Dexpression=project.version -q -DforceStdout > .make/.version

# performance-tester
performance-tester/target/performance-tester-$(call version_fn).jar: .make/.version $(shell find performance-tester/src/main -type f) pom.xml performance-tester/pom.xml
	mvn --projects performance-tester --also-make package

.PHONY: run-performance-tester
run-performance-tester: performance-tester/target/performance-tester-$(call version_fn).jar
	java -jar performance-tester/target/performance-tester-$(call version_fn).jar

# globalise-updater
globalise-updater/target/globalise-updater-$(call version_fn).jar: .make/.version $(shell find globalise-updater/src/main -type f) pom.xml globalise-updater/pom.xml
	mvn --projects globalise-updater --also-make package

.PHONY: run-globalise-updater-local
run-globalise-updater-local: globalise-updater/target/globalise-updater-$(call version_fn).jar
	cd globalise-updater && java -jar target/globalise-updater-$(call version_fn).jar conf/local.yml

.make/.globalise-docker: .make k8s/globalise-updater/Dockerfile
	docker build -t $(GLOBALISE_UPDATER_TAG):$(call version_fn) -f k8s/globalise-updater/Dockerfile .
	@touch $@

.make/.push-globalise-updater: k8s/globalise-updater/Dockerfile globalise-updater/target/globalise-updater-$(call version_fn).jar
	docker build -t $(GLOBALISE_UPDATER_TAG):$(call version_fn) --platform=linux/amd64 -f k8s/globalise-updater/Dockerfile .
	docker tag $(GLOBALISE_UPDATER_TAG):$(call version_fn) $(DOCKER_DOMAIN)/$(GLOBALISE_UPDATER_TAG):$(call version_fn)
	docker push $(DOCKER_DOMAIN)/$(GLOBALISE_UPDATER_TAG):$(call version_fn)
	@touch $@

.PHONY: push-globalise-updater
push-globalise-updater: .make/.push-globalise-updater

# republic-updater
republic-updater/target/republic-updater-$(call version_fn).jar: .make/.version $(shell find republic-updater/src/main -type f) pom.xml republic-updater/pom.xml
	mvn --projects republic-updater --also-make package

.PHONY: run-republic-updater-local
run-republic-updater-local: republic-updater/target/republic-updater-$(call version_fn).jar
	cd republic-updater && java -jar target/republic-updater-$(call version_fn).jar conf/local.yml

.make/.republic-docker: .make k8s/republic-updater/Dockerfile
	docker build -t $(REPUBLIC_UPDATER_TAG):$(call version_fn) -f k8s/republic-updater/Dockerfile .
	@touch $@

.make/.push-republic-updater: k8s/republic-updater/Dockerfile republic-updater/target/republic-updater-$(call version_fn).jar
	docker build -t $(REPUBLIC_UPDATER_TAG):$(call version_fn) --platform=linux/amd64 -f k8s/republic-updater/Dockerfile .
	docker tag $(REPUBLIC_UPDATER_TAG):$(call version_fn) $(DOCKER_DOMAIN)/$(REPUBLIC_UPDATER_TAG):$(call version_fn)
	docker push $(DOCKER_DOMAIN)/$(REPUBLIC_UPDATER_TAG):$(call version_fn)
	@touch $@

.PHONY: push-republic-updater
push-republic-updater: .make/.push-republic-updater

# other
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
	@echo
	@echo "  run-performance-tester      - to run the performance tester"
	@echo "  run-globalise-updater-local - to run the globalise updater using local settings"
	@echo "  run-republic-updater-local  - to run the republic updater using local settings"
	@echo
	@echo "  push-globalise-updater - to create a docker image for globalise-updater and push it to $(DOCKER_DOMAIN)"
	@echo "  push-republic-updater  - to create a docker image for republic-updater and push it to $(DOCKER_DOMAIN)"
