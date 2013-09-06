package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
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

      Await.ready(futureServerInfo, 5 seconds)
    }

    it("vender.name should equal 'The Apache Software Foundation'"){

      val futureServerInfo = Server.info()

      futureServerInfo onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      futureServerInfo onSuccess {
        case serverInfo => assert(serverInfo.vendor.name == "The Apache Software Foundation")
      }

      Await.ready(futureServerInfo, 5 seconds)
    }
  }
}

