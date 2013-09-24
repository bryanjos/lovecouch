package com.bryanjos.lovecouch

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global
import org.scalatest._
import play.api.libs.json.Json
import scala.Some

class DocumentSpec extends FunSpec with BeforeAndAfterAll{
  implicit val db = Database(name="documentspec")
  case class Guy(_id:Option[String] = None, _rev:Option[String] = None, name:String, age:Long)
  implicit val guyFmt = Json.format[Guy]
  var id = ""
  var revs = List[String]()

  override def beforeAll() {
    Await.result(Database.create(), 5 seconds)
  }

  describe("Document"){
    it("should create a document"){

      val data = Guy(name="Alf", age=23)

      val result = Document.post[Guy](data) map { value =>
      id = value.id
      revs = revs ++ List[String](value.rev)
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
      val data = Guy(_id = Some(id), _rev = Some(revs.last), name="Alf", age=24)

      val result = Document.put[Guy](data, id) map { value =>
        revs = revs ++ List[String](value.rev)
        assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }


    it("should attach a file"){

      val result = Document.putAttachment(id,
        revs.last,
        "README.md",
        new java.io.File("/Users/bryanjos/Projects/Personal/lovecouch/README.md"),
        "text/plain") map { value =>
        revs = revs ++ List[String](value.rev)
        assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }

    it("should get attachment"){

      val result = Document.getAttachment(id, "README.md") map { value =>
        assert(value.size > 0)
      }

      Await.result(result, 5 seconds)
    }

    it("should get revision of document"){

      val result = Document.get[Guy](id, Some(revs.head)) map { value =>
        assert(value.age == 23)
        assert(value.name == "Alf")
        assert(value._id.get == id)
      }

      Await.result(result, 5 seconds)
    }

    it("should delete document"){

      val result = Document.delete(id, revs.last) map { value =>
        assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }
  }

  override def afterAll() {
    Await.result(Database.delete(), 5 seconds)
  }
}
