## Distributed Ticketing System.

An online booking system, that allows user to view available seats, select and book them. 
The application also supports viewing booked tickets and delete a booking. The application is design
to scale and handle huge number of users, while also supporting events in multiple locations 
efficiently.

### Components
* Load Balancer - The main application server the client interacts with, that orchestrates requests
to other components of the system.
* Lock Store - A distributed locking store that helps providing mutual exclusion property for the system.
* Data Store - A partitioned, replicated data store which contains the seating, booking information.
* Data Store Manager - A manager that performs booking operation on a particular data store based on 
the theater information passed. Since the data store information is partitioned by theater name. 

### How to run
* To compile all the files and create required stubs,
  * `cd src/`
  * `./shell.sh`
#### To run lock server
  * Go to src directory
    * `cd src/`
  * Start the individual lock server
    * `java lock.LockServer <port> <coordinator port> <possible sources for recovery>`
    * eg: `java lock.LockServer 3000 5000`
  * Start the lock Coordinator
    * `java lock.LockCoordinator <coordinator port> <server ports...>`
    * eg: `java lock.LockCoordinator 5000 3000`
#### To run data store
  * Go to src directory
    * `cd src/`
  * Start the individual data store
    * `java datastore.DataStore <port> <coordinator port> <possible sources for recovery>`
    * eg: `java datastore.DataStore 3000 5000`
  * Start the data store coordinator
    * `java datastore.DataStoreCoordinator <coordinator port> <server ports...>`
    * eg: `java datastore.DataStoreCoordinator 5000 3000`
  * Start the data store manager
    * `java datastore.DataStoreManager <data store ports>`
    * eg: `java datastore.DataStoreManager 3000 3002 3003`
    * The data store manager needs a minimum of 3 individual data store, with its own data store 
    coordinator setup as clusters, to support the minimum 3 theaters the application supports.
#### The lock servers and data store can be configured to get state from an already running instance of the component.
#### To perform recovery
* When running the lock server, pass the port of a running server.
  * `java lock.LockServer <port> <coordinator port> <possible sources for recovery>`
  * eg: `java lock.LockServer 3000 5000`
* When running the data store, pass the port of a running server.
  * `java datastore.DataStore <port> <coordinator port> <possible sources for recovery>`
  * eg: `java datastore.DataStore 3000 5000 3001`
#### To run load balancer
* Go to src directory
  * `cd src/`
* Start the Load Balancer
  * `java loadbalance LoadBalancer <# of lock servers> <lock server ports> <# of data server> <data server ports>`

#### To run Client
* Go to src directory
    * `cd src/`
* Run the client
  * `java client.Client <load balancer ports>`.
  * eg: `java client.Client 6000`
* The client prompts for user input to start booking process.