# akka-schedular-data-expunge
Sample project to demo Akka Scheduler.

## Architecture

<img src="https://github.com/codingkapoor/akka-schedular-data-expunge/blob/master/architecture.jpg" alt="architecture" width="1000" height="550"/>

## Dev
```
$ cd akka-schedular-data-expunge
$ sbt> universal:stage 

$ cd ./target/universal/stage/logs
$ tail -f stdout.log data-expunge.log
```

## Build Package
```
$ cd akka-schedular-data-expunge
$ sbt> universal:packageBin

$ cd ./logs
$ tail -f stdout.log data-expunge.log
```
