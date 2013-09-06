package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class ServerSpec extends FunSpec {
  describe("Server info with default host and port") {
    it("couchdb should equal 'Welcome'"){

      val futureServerInfo = Server.info()

      futureServerInfo onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      futureServerInfo onSuccess {
        case serverInfo => assert(serverInfo.couchdb == "Welcome")
      }
    }

    it("vender.name should equal 'The Apache Software Foundation'"){

      val futureServer = Server.info()

      futureServer onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      futureServer onSuccess {
        case serverInfo => assert(serverInfo.vendor.name == "The Apache Software Foundation")
      }
    }
  }


  describe("Get Database by name") {
    it("dbName should be '_users'"){
      val futureDatabase = Server.getDatabase("_users")

      futureDatabase onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      futureDatabase onSuccess {
        case database => assert(database.dbName == "_users")
      }
    }
  }
}

