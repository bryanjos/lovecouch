package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.FunSpec
import play.api.libs.json.{JsArray, Json}
import akka.actor.ActorSystem

class DatabaseSpec extends FunSpec {
  implicit val system = ActorSystem()
  implicit val context = system.dispatcher
  val couchDB = CouchDb()
  val db = Database("test", couchDB.url)
  case class Guy(_id:Option[String] = None, _rev:Option[String] = None, name:String, age:Long)
  implicit val guyFmt = Json.format[Guy]

  info("CouchDB Database Methods")

  describe("Create a new database") {
    val result = couchDB.createDatabase("test") map { value =>
      it("should be created"){
        assert(true)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Returns database information") {
    val result = db.info() map { value =>
      it("should be named test"){
        assert(value.dbName == "test")
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Insert multiple documents in to the database in a single request") {
    val result = db.bulkDocs(Seq[Guy](Guy(name="Alf", age=23), Guy(name="SuperAlf", age=46))) map { value =>
      it("should be a Json Array"){
        assert(value.isInstanceOf[JsArray])
      }

      it("should have 2 elements"){
        assert(value.as[JsArray].value.size == 2)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Execute a given view function for all documents and return the result") {
    val result = db.tempView(TempView(map = "function(doc) { if (doc.age > 30) { emit(null, doc.age); } }")) map { value =>

      it("should have one element"){
        assert((value \ "rows").as[JsArray].value.size == 1)
      }

      it("should have a value of 46"){
        assert(((value \ "rows").as[JsArray].value.head \ "value").as[Long] == 46)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Delete an existing database") {
    Await.result(couchDB.deleteDatabase("test"), 5 seconds)

    val result = couchDB.allDbs() map { value =>
      it("should not contain a database named test"){
        assert(!value.contains("test"))
      }
    }

    Await.result(result, 5 seconds)
  }

}
