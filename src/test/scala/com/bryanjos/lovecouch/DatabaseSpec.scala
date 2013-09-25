package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec
import play.api.libs.json.{JsArray, Json}

class DatabaseSpec extends FunSpec {
  implicit val db = Database("test")
  case class Guy(_id:Option[String] = None, _rev:Option[String] = None, name:String, age:Long)
  implicit val guyFmt = Json.format[Guy]

  info("CouchDB Database Methods")

  describe("Create a new database") {
    val result = Database.create() map { value =>
      it("should be a successful request"){
        assert(value.isSuccess)
      }

      it("should be created"){
        assert(value.get)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Returns database information") {
    val result = Database.info() map { value =>
      it("should be a successful request"){
        assert(value.isSuccess)
      }

      it("should be named test"){
        assert(value.get.dbName == "test")
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Insert multiple documents in to the database in a single request") {
    val result = Database.bulkDocs(Seq[Guy](Guy(name="Alf", age=23), Guy(name="SuperAlf", age=46))) map { value =>
      it("should be a successful request"){
        assert(value.isSuccess)
      }

      it("should be a Json Array"){
        assert(value.get.isInstanceOf[JsArray])
      }

      it("should have 2 elements"){
        assert(value.get.as[JsArray].value.size == 2)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Execute a given view function for all documents and return the result") {
    val result = Database.tempView(TempView(map = "function(doc) { if (doc.age > 30) { emit(null, doc.age); } }")) map { value =>
      it("should be a successful request"){
        assert(value.isSuccess)
      }

      it("should have one element"){
        assert((value.get \ "rows").as[JsArray].value.size == 1)
      }

      it("should have a value of 46"){
        assert(((value.get \ "rows").as[JsArray].value.head \ "value").as[Long] == 46)
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Delete an existing database") {
    Await.result(Database.delete(), 5 seconds)

    val result = CouchDb.allDbs() map { value =>
      it("should not contain a database named test"){
        assert(!value.get.contains("test"))
      }
    }

    Await.result(result, 5 seconds)
  }

}
