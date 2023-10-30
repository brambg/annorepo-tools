all: help
TAG = annorepo-tools
DOCKER_DOMAIN = registry.diginfra.net/tt
SHELL=/bin/bash
PERF_SRC=$(shell find performance-tester/src/ -type f)
version_fn = $(shell cat .make/.version 2>/dev/null)

.make:
	mkdir -p .make

.make/.version: .make pom.xml
	mvn help:evaluate -Dexpression=project.version -q -DforceStdout > .make/.version

performance-tester/target/performance-tester-$(call version_fn).jar: .make/.version  $(PERF_SRC) pom.xml performance-tester/pom.xml
	mvn --projects performance-tester --also-make package

.PHONY: run-performance-tester
run-performance-tester: performance-tester/target/performance-tester-$(call version_fn).jar
	java -jar performance-tester/target/performance-tester-$(call version_fn).jar

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
	@echo "  run-performance-tester - to run the performance tester"
	@echo
