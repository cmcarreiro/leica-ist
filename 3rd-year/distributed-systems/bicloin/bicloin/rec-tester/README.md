# Silo client


## Authors

Group A52

### Lead developer 

92438 [Catarina Carreiro](https://git.rnl.tecnico.ulisboa.pt/ist192438)

### Contributors

92440 [Cristiano Clemente](https://git.rnl.tecnico.ulisboa.pt/ist192440)

## About

This is a gRPC client that performs integration tests on a running server.
The integration tests verify the responses of the server to a set of requests.


## Instructions for using Maven

You must start the servers first.

To compile and run integration tests:

```
mvn verify -DzooHost="localhost" -DzooPort="2181"
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

