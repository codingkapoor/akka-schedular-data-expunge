#!/bin/bash

BIN_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
HOME_DIR="$(dirname "$BIN_DIR")"
CONF_DIR="${HOME_DIR}/conf"
LIB_DIR="${HOME_DIR}/lib"
LOG_DIR="${HOME_DIR}/logs"

if [ ! -d ${CONF_DIR} ] || [ ! -d ${LIB_DIR} ] || [ ! -d ${LOG_DIR} ]; then
  echo "`date`: Mandatory directory check failed."
  exit 0
fi

echo "Starting Data Expunge..."

nohup java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5007 \
    -server -Dlogback.configurationFile="${CONF_DIR}/logback.xml" \
	-Dconf.dir="${CONF_DIR}" -Dlogs="${LOG_DIR}" -cp "${LIB_DIR}/*:${CONF_DIR}/*" \
	com.codingkapoor.dataexpunge.core.DataExpunge > "${LOG_DIR}/stdout.log" 2>&1 &

data_expunge_pid=$!

echo
echo "Data Expunge started with PID [$data_expunge_pid] at [`date`]"
