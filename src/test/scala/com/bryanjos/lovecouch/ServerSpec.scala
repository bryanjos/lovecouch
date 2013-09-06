package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class ServerSpec extends FunSpec {
  describe("Get Server with default host and port") {
    it("couchdb should equal 'Welcome'"){

      val f = Server.get()

      f onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      f onSuccess {
        case server => assert(server.couchdb == "Welcome")
      }
    }

    it("vender.name should equal 'The Apache Software Foundation'"){

      val f = Server.get()

      f onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      f onSuccess {
        case server => assert(server.vendor.name == "The Apache Software Foundation")
      }
    }
  }
}

