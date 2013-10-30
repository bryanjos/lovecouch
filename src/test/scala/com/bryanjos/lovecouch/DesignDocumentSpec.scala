package com.bryanjos.lovecouch

import scala.concurrent.duration._
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import scala.concurrent.Await
import play.api.libs.json.Json
import akka.actor.ActorSystem

class DesignDocumentSpec extends FunSpec with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val context = system.dispatcher
  val couchDB = CouchDb()
  val db = Database(name = "designdocumentspec", couchDB.url)
  var id = ""
  var revs = List[String]()

  case class Guy(_id: Option[String] = None, _rev: Option[String] = None, name: String, age: Long)

  implicit val guyFmt = Json.format[Guy]

  val dd = DesignDocument(_id = Some("_design/ages"),
    views = List[View](View(name = "by_age", map = "function(doc) { if (doc.age > 30) { emit(null, doc.age); } }")))

  info("CouchDB Design Document Methods")

  override def beforeAll() {
    Await.result(couchDB.createDatabase("designdocumentspec"), 5 seconds)
  }

  describe("Upload the specified design document") {
    it("should be ok") {
      val result = db.addOrUpdateDesignDocument(dd) map {
        value =>

          id = value.id
          revs = revs ++ List[String](value.rev)
          assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }
  }

  describe("Returns the specified design document") {
    it("should return the design document") {
      val result = db.getDesignDocument(id) map {
        value =>

          assert(value._id.get == id)
          assert(value._rev.get == revs.head)
          assert(value.views.head.name == "by_age")
      }

      Await.result(result, 5 seconds)
    }


  }

  describe("Executes the specified view-name from the specified design-doc design document.") {
    it("should return one result") {
      Await.result(db.bulkDocs(Seq[Guy](Guy(name = "Alf", age = 23), Guy(name = "SuperAlf", age = 46))), 5 seconds)
      val result = db.executeView(dd._id.get, "by_age") map {
        value =>

          assert(value.rows.size == 1)
      }

      Await.result(result, 5 seconds)
    }
  }

  describe("Delete an existing design document") {
    it("should be deleted successfully") {
      val result = db.deleteDesignDocument(id, revs.head) map {
        value =>

          assert(value.ok)
      }
      Await.result(result, 5 seconds)
    }
  }

  override def afterAll() {
    Await.result(couchDB.deleteDatabase("designdocumentspec"), 5 seconds)
  }
}
