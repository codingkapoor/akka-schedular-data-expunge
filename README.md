# akka-schedular-data-expunge
Sample project to demo Akka Scheduler.

The objective of this service is to rid data obsoleted by configured `ttl` value per databse in the `application.conf`.

## Architecture
The service is designed to be reactive to changes made to the application configuration file. In other words, changes made to the configuration file will be accomodated by the service reactively and would not require service restarts.

<br>

<img src="https://github.com/codingkapoor/akka-schedular-data-expunge/blob/master/architecture.jpg" alt="architecture" />

## Sample Configuration
The actor system is infact influenced by the application configuration itself.

```
deconf = {
    "siemens": {
      cass: { ttl: 90, freq: 432000 }
      solr: { ttl: 30, freq: 86400 }
      vertica: { ttl: 7, freq: 86400 }
    }
}
```

## Ttl Watcher
Ttl watcher actor is the one that watches for any configuration changes and notifies the same to the *Supervisor* actor.

## Supervisor
Supervisor actor creates supervisor per customer actors. This facilitates seggregation of concerns by customers. Configuration changes related to a customer will only be forwarded to the pertaining *SupervisorPerCustomer* actor. Also, this heirarchy of actors under *SupervisorPerCustomer* actor can be killed independently without affecting the other actors if that particular actor is decommissioned in the configuration file.

## Supervisor per customer
Based on which databases are commissioned in the configuration file, *SupervisorPerCustomer* actor creates a scheduler actor and a pertaining data expunge actor per db. It then forwards new configuration changes to the scheduler that then takes care of scheduling/rescheduling itself to ask the expunge actor for the job.

## Scheduler
There is a dedicated scheduler actor for every expunge actor. Scheduler is the one responsible for scheduling and rescheduling itself to ask the expunge actor for the job. It acts on the `freq` configuration for this purpose. The motivation behind having `freq` configuration per database is to be to schedule expunge jobs per database independently. For instance, cass data expunge  can be configured to be scheduled for 10 days while solr and vertica for 3 days each.

Besides rescheduling itself against every `freq` configuration change, *Scheduler* also reschedules itself based on the duration it takes to complete the job by the expunge actor. For instance, if the scheduler is scheduled for 1 day and it takes 5 days to complete the job. It would not make sense to ask the expunge actor for the job every day. In this particular case, scheduler actor would reschedule itself for every 5 days instead.

**Note** : The default `freq` configuration is set to 24 hours.

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
