package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import akka.actor.ActorSystem
import spray.http.HttpEntity

case class CouchDb(host: String = "127.0.0.1", port: Int = 5984, username:Option[String] = None,
                   password:Option[String] = None)(implicit system:ActorSystem, context:ExecutionContext) {
  import Implicits._

  def url: String = s"http://$host:$port"
  def config:Config = Config(url)

  /**
   * Meta information about the instance
   * @return
   */
  def info(): Future[CouchDbInfo] = {
    Requests.get(url).map{
      response =>
        Requests.processObjectResponse[CouchDbInfo](response)
    }
  }

  def login: Future[Session] = {
    val json = Json.obj(
      "name" -> username.get,
      "password" -> password.get
    )

    Requests.post(url + s"/_session",
      body = Json.stringify(json)).map{
      response =>
        Requests.processObjectResponse[Session](response)
    }
  }


  def getSession: Future[Session] = {
    Requests.get(url + s"/_session").map{
        response =>
          Requests.processObjectResponse[Session](response)
      }
  }


  def logOut: Future[Boolean] = {
    Requests.delete(url + s"/_session").map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Currently running tasks
   * @return
   */
  def activeTasks(): Future[Vector[ActiveTask]] = {
    Requests.get(url + "/_active_tasks").map{
      response =>
        Requests.processObjectResponse[Vector[ActiveTask]](response)
    }
  }

  /**
   * Returns a list of all the databases in the CouchDB instance

   * @return
   */
  def allDbs(): Future[Vector[String]] = {
    Requests.get(url + "/_all_dbs").map{
      response =>
        Requests.processObjectResponse[Vector[String]](response)
    }
  }

  /**
   * Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.

   * @param bytes
   * @param offset
   * @return
   */
  def log(bytes: Long = 1000, offset: Long = 0): Future[String] = {
    Requests.get(url + s"/_log?bytes=$bytes&offset=$offset").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Request, configure, or stop, a replication operation.
   * http://docs.couchdb.org/en/latest/api/misc.html#post-replicate
   * @param bytes
   * @param offset
   * @param replicationSpecification

   * @return
   */
  def replicate(replicationSpecification: ReplicationSpecification, bytes: Long = 1000, offset: Long = 0)
               : Future[ReplicationResponse] = {


    Requests.post(url + s"/_replicate?bytes=$bytes&offset=$offset",
      body = Json.stringify(Json.toJson(replicationSpecification))).map{
      response =>
        Requests.processObjectResponse[ReplicationResponse](response)
    }
  }

  /**
   * Restarts CouchDB

   * @return
   */
  def restart(): Future[Boolean] = {
    Requests.post(url + s"/_restart").map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Returns statistics for the running server

   * @return
   */
  def stats(): Future[Stats] = {
    Requests.get(url + "/_stats").map{
      response =>
        Requests.processObjectResponse[Stats](response)
    }
  }

  /**
   * Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance

   * @param count
   * @return
   */
  def uuids(count: Int = 1): Future[Vector[String]] = {
    Requests.get(url + s"/_uuids?count=$count").map{
      response =>
        Requests.processResponse[Vector[String]](response,
          (e:HttpEntity) => (Json.parse(e.asString) \ "uuids").as[Vector[String]])
    }
  }

  /**
   * Creates a new database.
   * @return
   */
  def createDatabase(name:String): Future[Database] = {
    Requests.put(url + s"/$name").map{
      response =>
        Requests.processBooleanResponse(response)
        Database(name, this.url)
    }
  }

  /**
   * Returns true if the database exists.
   * @return
   */
  def getOrCreateDatabase(name:String): Future[Database] = {
    doesDatabaseExist(name).flatMap[Database]{
      exist => {
        if(exist)
          Future.successful(Database(name, this.url))
        else{
          createDatabase(name)
        }
      }
    }
  }

  /**
   * Returns true if the database exists.
   * @return
   */
  def doesDatabaseExist(name:String): Future[Boolean] = {
    allDbs().map{
      ds => {
        ds.exists((s) => s == name)
      }
    }
  }

  /**
   * Deletes the specified database, and all the documents and attachments contained within it.
   * @return
   */
  def deleteDatabase(name:String): Future[Boolean] = {
    Requests.delete(url + s"/$name").map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }


}
