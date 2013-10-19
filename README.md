# lovecouch

## A CouchDB Library for Scala


The goal is to implement all relevant API from the [CouchDB API Reference]

Currently uses the Play2 Json Library for Json parsing. Currently using Dispatch for the http client, but switching over to Spray

All methods will return a Future[U] where U is whatever object is wanted whenever everything is done.


When the spray integration is completed, you will have to make an implicit ActorSystem to use it


```scala
import akka.actor.ActorSystem
implicit val system = ActorSystem()
```


### Examples

#### Everything is in the com.bryanjos.lovecouch package
```scala
import com.bryanjos.lovecouch._
```


### CouchDB functions


#### def info()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[CouchDbInfo]
##### Get the welcome message and version information
```scala
val info = CouchDB.info()
```

#### def activeTasks()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[Vector[ActiveTask]]
##### Currently running tasks
```scala
val activeTasks = CouchDB.activeTasks()
```


#### def allDbs()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[Vector[String]]
##### Returns a list of all the databases in the CouchDB instance
```scala
val dbs = CouchDB.allDbs()
```

#### def log(bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[String]
##### Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
```scala
val log = CouchDB.log(bytes = 1000, offset = 50)
```

#### def replicate(replicationSpecification: ReplicationSpecification, bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[ReplicationResponse]
##### Request, configure, or stop, a replication operation.
```scala
val response = CouchDB.replicate(ReplicationSpecification(false, source="", target ="")
```


#### def restart()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[Boolean]
##### Restarts CouchDB
```scala
val response = CouchDB.restart()
```


#### def stats()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[Stats]
##### Returns statistics for the running server
```scala
val stats = CouchDB.stats()
```


#### def uuids(count: Int = 1)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem): Future[Vector[String]]
##### Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
```scala
val uuids = CouchDB.uuids()
```


### Config functions

#### def get()(implicit couchDb: CouchDb = CouchDb()): Future[Try[JsValue]]
##### Returns the entire CouchDB server configuration as a JSON structure.
```scala
val config = Config.get()
```

#### getSection(section:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[JsValue]]
##### Gets the configuration structure for a single section.
```scala
val section = Config.getSection("section_name")
```

#### getSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]]
##### Gets a single configuration value from within a specific configuration section.
```scala
val sectionKey = Config.getSectionKey("section_name", "section_key")
```

#### putSectionKey(section:String, key:String, value:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]]
##### Updates a configuration value.
```scala
val sectionKeyResponse = Config.putSectionKey("section_name", "section_key", "section_value")
```


#### deleteSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]]
##### Deletes a configuration value.
```scala
val sectionKeyResponse = Config.deleteSectionKey("section_name")
```



[CouchDB API Reference]: http://docs.couchdb.org/en/latest/api/reference.html

