#!/bin/bash
mvn clean package

java -jar target/noobchain-1.0-jar-with-dependencies.jar
