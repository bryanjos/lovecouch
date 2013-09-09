package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class DatabaseSpec extends FunSpec {

  describe("Get Database info") {
    it("dbName should be '_users'"){
      implicit val db = Database("_users")

      val future = Database.info()

      val result = future map { value =>
        assert(value.dbName == "_users")
      }

      Await.result(result, 5 seconds)
    }
  }

  describe("Database CRUD") {
    implicit val db = Database("test")
    it("should create new database named test"){
      val future = Database.create()

      val result = future map { value =>
        assert(value)
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
