# Architecture
General architecture is a client - server - database application.

## Deployment
I'm also considering different deployment stages to cope with growth.
1. _Cheapskate_ mode. Everything is in a shared server Dreamhost account. The server runs as [FCGI](https://github.com/edward3h/fcgi-with-graal). Database is MySQL. Photo uploads are files on the server filesystem.
2. VPS. Server can run standalone. Database could be anything but probably stick with MySQL.
3. Cloud. AWS or equivalent. Server can run as Lambda or dedicated container(s) depending on traffic. Migrate database to DynamoDB - requires a model change but by the time it gets to this stage there should be better understanding of access patterns.

## Server
The server is built on the Micronaut framework. 
This can work with the above deployment stages with only minor changes.

## Database
MySQL, because that's what Dreamhost includes!
In the future, DynamoDB.

## Client
The client is built on the Quasar framework, using Vue 3 and Typescript.
I only just started learning Quasar but its documentation looks good.