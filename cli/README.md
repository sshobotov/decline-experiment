Tickets4Sale CLI tool
---------------------

### Usage

Build fat JAR with all dependencies
```
sbt cli/assembly
```

and run JAR with your arguments
```
java -jar <your-path-to-built-jar>/tickets4sale-cli-assembly-0.1.jar <path-to-csv-file> 2019-08-01 2019-08-07
```

or check for a help output
```
java -jar <your-path-to-built-jar>/tickets4sale-cli-assembly-0.1.jar --help
```

Run tests

```
sbt cli/test
```