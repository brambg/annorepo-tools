all: help
TAG = annorepo-tools
DOCKER_DOMAIN = registry.diginfra.net/tt
SHELL=/bin/bash
PERF_SRC=$(shell find performance-tester/src/main -type f)
UPD_SRC=$(shell find globalise-updater/src/main -type f)
version_fn = $(shell cat .make/.version 2>/dev/null)

.make:
	mkdir -p .make

.make/.version: .make pom.xml
	mvn help:evaluate -Dexpression=project.version -q -DforceStdout > .make/.version

performance-tester/target/performance-tester-$(call version_fn).jar: .make/.version  $(PERF_SRC) pom.xml performance-tester/pom.xml
	mvn --projects performance-tester --also-make package

globalise-updater/target/globalise-updater-$(call version_fn).jar: .make/.version  $(UPD_SRC) pom.xml globalise-updater/pom.xml
	mvn --projects globalise-updater --also-make package

.PHONY: run-performance-tester
run-performance-tester: performance-tester/target/performance-tester-$(call version_fn).jar
	java -jar performance-tester/target/performance-tester-$(call version_fn).jar

.PHONY: run-globalise-updater-local
run-globalise-updater-local: globalise-updater/target/globalise-updater-$(call version_fn).jar
	cd globalise-updater && java -jar target/globalise-updater-$(call version_fn).jar conf/local.yml

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
	@echo
