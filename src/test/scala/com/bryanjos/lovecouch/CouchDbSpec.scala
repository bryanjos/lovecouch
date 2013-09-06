package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class CouchDbSpec extends FunSpec {
  describe("Server info with default host and port") {
    it("couchdb should equal 'Welcome'"){

      val couchDBInfoFuture = CouchDb.info()

      couchDBInfoFuture onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      couchDBInfoFuture onSuccess {
        case couchDBInfo => assert(couchDBInfo.couchdb == "Welcome")
      }

      Await.ready(couchDBInfoFuture, 5 seconds)
    }

    it("vender.name should equal 'The Apache Software Foundation'"){

      val couchDBInfoFuture = CouchDb.info()

      couchDBInfoFuture onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      couchDBInfoFuture onSuccess {
        case  couchDBInfo => assert( couchDBInfo.vendor.name == "The Apache Software Foundation")
      }

      Await.ready(couchDBInfoFuture, 5 seconds)
    }
  }
}

