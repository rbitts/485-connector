#!/bin/bash
forever stopall
netstat -nap | grep 28080
forever start /home/rbits/git/485-connector/485server/forever.json
