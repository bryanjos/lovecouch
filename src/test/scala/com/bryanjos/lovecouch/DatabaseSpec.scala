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

  describe("Database") {

    it("should create new database named test"){
      val future = Database.create()

      val result = future map { value =>
        assert(value)
      }

      Await.result(result, 5 seconds)
    }

    it("should get info on test database"){
      val future = Database.info()

      val result = future map { value =>
        assert(value.dbName == "test")
      }

      Await.result(result, 5 seconds)
    }

    it("should import mulitple objects"){
      val future = Database.bulkDocs(Seq[Guy](Guy(name="Alf", age=23), Guy(name="SuperAlf", age=46)))

      val result = future map { value =>
        assert(value.isInstanceOf[JsArray])
        assert(value.as[JsArray].value.size == 2)
      }

      Await.result(result, 5 seconds)
    }

    it("should run temp view"){
      val future = Database.tempView(View(map = "function(doc) { if (doc.age > 30) { emit(null, doc.age); } }"))

      val result = future map { value =>
        assert((value \ "rows").as[JsArray].value.size == 1)
        assert(((value \ "rows").as[JsArray].value.head \ "value").as[Long] == 46)
      }

      Await.result(result, 5 seconds)
    }

    it("should delete test database"){
      Await.result(Database.delete(), 5 seconds)

      val future = CouchDb.allDbs()

      val result = future map { value =>
        assert(!value.contains("test"))
      }

      Await.result(result, 5 seconds)
    }
  }

}
