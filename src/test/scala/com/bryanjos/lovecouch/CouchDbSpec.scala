package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class CouchDbSpec extends FunSpec {
  describe("CouchDB") {
    it("should get info on server and couchdb should equal 'Welcome'"){

      val couchDBInfoFuture = CouchDb.info()

      val result = couchDBInfoFuture map { couchDBInfo =>
        assert(couchDBInfo.couchdb == "Welcome")
      }

      Await.result(result, 5 seconds)
    }

    it("should have vender name equal 'The Apache Software Foundation'"){

      val couchDBInfoFuture = CouchDb.info()
      val result = couchDBInfoFuture map { couchDBInfo =>
        assert( couchDBInfo.vendor.name == "The Apache Software Foundation")
      }

      Await.result(result, 5 seconds)
    }

    it("should have no currently running tasks"){

      val future = CouchDb.activeTasks()

      val result = future map { value =>
        assert(value.size == 0)
      }

      Await.result(result, 5 seconds)
    }

    it("should contain a database named _users"){

      val future = CouchDb.allDbs()

      val result = future map { value =>
        assert(value.contains("_users"))
      }

      Await.result(result, 5 seconds)
    }

    it("should get stats"){
      val future = CouchDb.stats()
      val result = future map { value => assert(value != null) }
      Await.result(result, 5 seconds)
    }

    it("should get at least one UUID"){
      val future = CouchDb.uuids()
      val result = future map { value => assert(value.size > 0) }
      Await.result(result, 5 seconds)
    }

    it("should get 3 UUIDs"){
      val future = CouchDb.uuids(3)
      val result = future map { value => assert(value.size == 3) }
      Await.result(result, 5 seconds)
    }
  }
}

