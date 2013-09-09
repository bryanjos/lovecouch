package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class CouchDbSpec extends FunSpec {
  describe("Info") {
    it("couchdb should equal 'Welcome'"){

      val couchDBInfoFuture = CouchDb.info()

      val result = couchDBInfoFuture map { couchDBInfo =>
        assert(couchDBInfo.couchdb == "Welcome")
      }

      Await.result(result, 5 seconds)
    }

    it("vender.name should equal 'The Apache Software Foundation'"){

      val couchDBInfoFuture = CouchDb.info()
      val result = couchDBInfoFuture map { couchDBInfo =>
        assert( couchDBInfo.vendor.name == "The Apache Software Foundation")
      }

      Await.result(result, 5 seconds)
    }
  }

  describe("Active Tasks") {
    it("should have no currently running tasks"){

      val future = CouchDb.activeTasks()

      val result = future map { value =>
        assert(value.size == 0)
      }

      Await.result(result, 5 seconds)
    }
  }

  describe("All DBs") {
    it("should contain a database named _users"){

      val future = CouchDb.allDbs()

      val result = future map { value =>
        assert(value.contains("_users"))
      }

      Await.result(result, 5 seconds)
    }
  }


  describe("Stats") {
    it("should not fail"){

      val future = CouchDb.stats()
      val result = future map { value => }
      Await.result(result, 5 seconds)
    }
  }
}

