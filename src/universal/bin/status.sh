#!/bin/bash

CHECK="com.codingkapoor.dataexpunge.core.DataExpunge"
PID=`jps | grep DataExpunge | awk '{print $1}'`
STATUS=$(ps aux | grep -v grep | grep ${CHECK})

if [ "${#STATUS}" -gt 0 ] && [ -n ${PID} ]; then
    echo "`date`: Data Expunge is running"
else
    echo "`date`: Data Expunge is not running"
    exit 1
fi
