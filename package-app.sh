#!/bin/bash
mvn clean compile package

java -jar target/noobchain-0.2.3-jar-with-dependencies.jar
