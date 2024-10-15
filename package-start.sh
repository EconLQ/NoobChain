#!/bin/bash
mvn clean compile package

java -jar target/noobchain-2.0-jar-with-dependencies.jar
