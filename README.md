# lovecouch

## A CouchDB Library for Scala


The goal is to implement all relevant API from the [CouchDB API Reference]

Currently uses the Play2 Json Library and Dispatch

All methods return a Future[Try[U]] where U is whatever object is wanted


### Examples

#### Everything is in the com.bryanjos.lovecouch package
```scala
import com.bryanjos.lovecouch._
```


### CouchDB object methods (Miscellaneous Methods from CouchDB API)


#### def info()(implicit couchDb: CouchDb = CouchDb()): Future[Try[CouchDbInfo]]
##### Get the welcome message and version information
```scala
val info = CouchDB.info()
```

#### def activeTasks()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[ActiveTask]]]
##### Currently running tasks
```scala
val activeTasks = CouchDB.activeTasks()
```


#### def allDbs()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[String]]]
##### Returns a list of all the databases in the CouchDB instance
```scala
val dbs = CouchDB.allDbs()
```

#### def updates(callBack: DatabaseEvent => Unit, feed: FeedTypes.FeedTypes = FeedTypes.LongPoll, timeout: Long = 60, heartbeat: Boolean = true)(implicit couchDb: CouchDb = CouchDb()): Object with StringsByLine[Unit]
##### Returns a list of all database events in the CouchDB instance.
```scala
//Still a WIP
```

#### def log(bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]]
##### Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
```scala
val log = CouchDB.log(bytes = 1000, offset = 50)
```

#### def replicate(replicationSpecification: ReplicationSpecification, bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb()): Future[Try[ReplicationResponse]]
##### Request, configure, or stop, a replication operation.
```scala
val response = CouchDB.replicate(ReplicationSpecification(false, source="", target ="")
```


#### def restart()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Boolean]]
##### Restarts CouchDB
```scala
val response = CouchDB.restart()
```


#### def stats()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Stats]]
##### Returns statistics for the running server
```scala
val stats = CouchDB.stats()
```


#### def uuids(count: Int = 1)(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[String]]]
##### Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
```scala
val uuids = CouchDB.uuids()
```



[CouchDB API Reference]: http://docs.couchdb.org/en/latest/api/reference.html

