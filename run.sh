#!/bin/sh
spring_instrument_path=$(find /app/lib/ -name "spring-instrument-*.jar")
exec java -javaagent:${spring_instrument_path} ${JAVA_OPTS:= } -cp app:app/lib/* ru.yakovlev.Application "${@}"