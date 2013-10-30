# lovecouch

## An asynchronous CouchDB Library for Scala


The goal is to implement all relevant API from the [CouchDB API Reference]

Current dependencies are the Plat 2 Json Library for Json Parsing, Spray for Http Client as well as Akka

All methods will return a Future[U] where U is whatever object is wanted whenever everything is done.

If an error occurs, than a CouchDBException will be thrown

#### Everything is in the com.bryanjos.lovecouch package
```scala
import com.bryanjos.lovecouch._
import akka.actor.ActorSystem

implicit val system = ActorSystem()
implicit val context = system.dispatcher

val couchDB = CouchDB() //Uses default host and port
val database = Database("test", couchDB.url)

case class Guy(_id: Option[String] = None, _rev: Option[String] = None, name: String, age: Long) //Make sure _id and _rev are defined
implicit val guyFmt = Json.format[Guy] //Json formatter

val guy = Guy(name = "Alf", age = 23)
database.createDocument[Guy](guy) map {
result =>
    //A DocumentResult is returned
}

```



[CouchDB API Reference]: http://docs.couchdb.org/en/latest/api/reference.html

