package com.bryanjos.lovecouch

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global
import org.scalatest.{BeforeAndAfter, FunSpec}
import scala.concurrent.Await
import play.api.libs.json.Json

class DocumentSpec extends FunSpec with BeforeAndAfter{
  implicit val db = Database(name="documentspec")
  case class Guy(_id:Option[String] = None, _rev:Option[String] = None, name:String, age:Long)
  implicit val guyFmt = Json.format[Guy]
  var id = ""
  var rev = ""
  var latestRev = ""

  //before{
  //  Await.result(Database.create(), 5 seconds)
  //}

  describe("Document"){
    it("should create a document"){

      val data = Guy(name="Alf", age=23)

      val result = Document.post[Guy](data) map { value =>
      id = value.id
      rev = value.rev
      assert(value.ok)
    }

      Await.result(result, 5 seconds)
    }

    it("should get back the document"){
      val result = Document.get[Guy](id) map { value =>
        assert(value.age == 23)
        assert(value.name == "Alf")
        assert(value._id.get == id)
      }

      Await.result(result, 5 seconds)
    }

    it("should update the document"){
      val data = Guy(_id = Some(id), _rev = Some(rev), name="Alf", age=24)

      val result = Document.put[Guy](data, id) map { value =>
        latestRev = value.rev
        assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }

    it("should get revision of document"){

      val result = Document.get[Guy](id, Some(rev)) map { value =>
        assert(value.age == 23)
        assert(value.name == "Alf")
        assert(value._id.get == id)
      }

      Await.result(result, 5 seconds)
    }

    it("should delete document"){

      val result = Document.delete(id, latestRev) map { value =>
        assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }
  }

  //after{
  //  Await.result(Database.delete(), 5 seconds)
  //}

}
