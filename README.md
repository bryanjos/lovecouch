# lovecouch

## An asynchronous CouchDB Library for Scala


The goal is to implement all relevant API from the [CouchDB API Reference]

Current dependencies are the Plat 2 Json Library for Json Parsing, Spray for Http Client as well as Akka

All methods will return a Future[U] where U is whatever object is wanted whenever everything is done.

If an error occurs, than a CouchDBException will be thrown


#### All methods need an implicit ActorSystem and ExecutionContext along with either an implicit instance of either the CouchDB or Database class.

#### Everything is in the com.bryanjos.lovecouch package
```scala
import com.bryanjos.lovecouch._
import akka.actor.ActorSystem

implicit val system = ActorSystem()
implicit val context = system.dispatcher



implicit val couchDB = CouchDB() //Uses default host and port
CouchDb.info()
//or

implicit val database = Database("test")
Database.info()
```


There are 5 Objects to interact with: Config, CouchDB, Database, DesignDocument, Document. These correspond to [CouchDB API Reference] sections.



[CouchDB API Reference]: http://docs.couchdb.org/en/latest/api/reference.html

