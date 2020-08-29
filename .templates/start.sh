#!/bin/bash
screen -S server java -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=50 -XX:CompileThreshold=100 -Xmx512m -Xms256m -jar launcher.jar