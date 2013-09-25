package com.bryanjos.lovecouch

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global
import org.scalatest._
import play.api.libs.json.Json

class DocumentSpec extends FunSpec with BeforeAndAfterAll {
  implicit val db = Database(name = "documentspec")

  case class Guy(_id: Option[String] = None, _rev: Option[String] = None, name: String, age: Long)

  implicit val guyFmt = Json.format[Guy]
  var id = ""
  var revs = List[String]()

  info("CouchDB Document Methods")

  override def beforeAll() {
    Await.result(Database.create(), 5 seconds)
  }

  describe("Create a new document") {
    it("should be created") {
      val data = Guy(name = "Alf", age = 23)
      val result = Document.post[Guy](data) map {
        value =>
          assert(value.isSuccess)
          id = value.get.id
          revs = revs ++ List[String](value.get.rev)
          assert(value.get.ok)
      }
      Await.result(result, 5 seconds)
    }
  }


  describe("Returns the latest revision of the document") {
    it("should be return the wanted document") {
      val result = Document.get[Guy](id) map {
        value =>
          assert(value.isSuccess)
          assert(value.get.age == 23)
          assert(value.get.name == "Alf")
          assert(value.get._id.get == id)
      }
      Await.result(result, 5 seconds)
    }


  }


  describe("Inserts a new document, or new version of an existing document") {
    it("should be updated") {
      val data = Guy(_id = Some(id), _rev = Some(revs.last), name = "Alf", age = 24)

      val result = Document.put[Guy](data, id) map {
        value =>
          assert(value.isSuccess)
          revs = revs ++ List[String](value.get.rev)
          assert(value.get.ok)
      }
      Await.result(result, 5 seconds)
    }


  }


  describe("Adds an attachment of a document") {
    it("should be added") {
      val result = Document.putAttachment(id,
        revs.last,
        "README.md",
        new java.io.File("/Users/bryanjos/Projects/Personal/lovecouch/README.md"),
        "text/plain") map {
        value =>
          assert(value.isSuccess)
          revs = revs ++ List[String](value.get.rev)
          assert(value.get.ok)
      }
      Await.result(result, 5 seconds)
    }
  }


  describe("Gets the attachment of a document") {
    it("should be a byte array with non zero bytes") {
      val result = Document.getAttachment(id, "README.md") map {
        value =>
          assert(value.isSuccess)
          assert(!value.get.isEmpty)
      }

      Await.result(result, 5 seconds)
    }


  }


  describe("Returns the a revision of the document") {
    it("should be a previous revision of the document") {
      val result = Document.get[Guy](id, Some(revs.head)) map {
        value =>
          assert(value.isSuccess)


          assert(value.get.age == 23)
          assert(value.get.name == "Alf")
          assert(value.get._id.get == id)
      }

      Await.result(result, 5 seconds)
    }


  }


  describe("Deletes the document") {
    it("should be deleted") {
      val result = Document.delete(id, revs.last) map {
        value =>
          assert(value.isSuccess)
          assert(value.get.ok)
      }

      Await.result(result, 5 seconds)
    }


  }

  override def afterAll() {
    Await.result(Database.delete(), 5 seconds)
  }
}
