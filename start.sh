#!/bin/bash

export PORT=3000
export PERSISTENT_THREAD_TIMEOUT=45
export HTTP_CONNECTION_MGR_THREAD_COUNT=10
export PERSISTENT_THREAD_DEFAULT_PER_ROUTE=5
export HTTP_CLIENT_SOCKET_TIMEOUT=30000
export HTTP_CLIENT_CONNECTION_TIMEOUT=1000
export USER_SERVICE_ENDPOINT='https://userservicev3.integration.karmalab.net:56783'
export TRIP_SERVICE_ENDPOINT='http://wwwexpediacom.integration.sb.karmalab.net'
export DATABASE_URL='jdbc:postgresql://localhost/caldb'
export DB_CONN_TIMEOUT=10000
export POOL_NAME='pgsql'
export STATSD_HOST='localhost'
export STATSD_PORT=8125
export STATSD_INTERVAL=15
export EXPIRES_IN_HOURS=8
export NET_POOL_SIZE=20
export SCHEDULER_INTERVAL=300000

JAVA_HOME=$(/usr/libexec/java_home -v1.8)
$JAVA_HOME/bin/java -Xms512m -Xmx2g -Dcom.sun.management.jmxremote \
                                    -Dcom.sun.management.jmxremote.port=1098 \
                                    -Dcom.sun.management.jmxremote.local.only=false \
                                    -Dcom.sun.management.jmxremote.authenticate=false \
                                    -Dcom.sun.management.jmxremote.ssl=false -jar calendario-service.jar
