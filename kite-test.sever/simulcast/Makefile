lint: src
	eslint src

build: src
	babel src --out-dir dist

test: build
	node dist/test

start: build
	node dist/server

build-client:
	webpack

dev:
	webpack --watch --progress

.PHONY: lint build test start build-client
