Tickets4Sale Json API server
----------------------------

### Usage

Start the server locally
```
sbt api/run
```

and query it on localhost
```
curl -XPOST http://localhost:8080/inventory -d '{"show-date":"2019-08-01"}' -H 'Content-Type: application/json'
```

Run tests

```
sbt api/test
```

### TODO

- Provide logging library and its setup
- Provide configuration library to configure application on demand
- Add monitoring
- Setup proper deployment
