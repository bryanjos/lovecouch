package com.bryanjos.lovecouch

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import org.scalatest.FunSpec

class DatabaseSpec extends FunSpec {

  describe("Get Database by name") {
    it("dbName should be '_users'"){
      val futureDatabase = Database.info(Database("_users"))

      futureDatabase onFailure {
        case t => fail("An error has occured: " + t.getMessage)
      }
      futureDatabase onSuccess {
        case database => assert(database.dbName == "_users")
      }

      Await.ready(futureDatabase, 5 seconds)
    }
  }

}
