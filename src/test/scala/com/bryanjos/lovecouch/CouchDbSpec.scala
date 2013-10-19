package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec
import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask

class CouchDbSpec extends FunSpec {
  implicit val system = ActorSystem()

  info("CouchDB Miscellaneous Methods")

  describe("Get the welcome message and version information") {

    val result = CouchDb.info() map { value =>

      it("couchdb should equal 'Welcome'"){
          assert(value.couchdb == "Welcome")
      }

      it("should have vender name equal 'The Apache Software Foundation'"){
        assert(value.vendor.name == "The Apache Software Foundation")
      }
    }

    Await.result(result, 5 seconds)
  }

  describe("Obtain a list of the tasks running in the server") {
    val result = CouchDb.activeTasks() map { value =>
      it("should have no currently running tasks"){
        assert(value.size == 0)
      }
    }

    Await.result(result, 5 seconds)
  }


  describe("Get a list of all the DBs") {
    val result = CouchDb.allDbs() map { value =>
      it("should contain a database named _users"){
        assert(value.contains("_users"))
      }
    }

    Await.result(result, 5 seconds)
  }


  describe("Return server statistics") {
    val result = CouchDb.stats() map { value =>
      it("should contain stats"){
        assert(value != null)
      }
    }

    Await.result(result, 5 seconds)
  }


  describe("Get generated UUIDs from the server"){

    it("should get at least one UUID"){
      val future = CouchDb.uuids()
      val result = future map { value => assert(value.size == 1) }
      Await.result(result, 5 seconds)
    }

    it("should get 3 UUIDs"){
      val future = CouchDb.uuids(3)
      val result = future map { value => assert(value.size == 3) }
      Await.result(result, 5 seconds)
    }
  }
}

