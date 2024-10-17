#!/bin/bash
mvn clean compile package

java -jar target/noobchain-0.2.1-jar-with-dependencies.jar
