#!/bin/bash
javac */*.java
rmic lock.LockServer
rmic lock.LockCoordinator
rmic datastore.DataStore
rmic datastore.DataStoreCoordinator
rmic datastore.DataStoreManager
rmic loadbalance.LoadBalancer
