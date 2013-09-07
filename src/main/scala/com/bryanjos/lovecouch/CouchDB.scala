package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.{url, Http, as}
import dispatch.stream.StringsByLine

case class CouchDb(host: String = "127.0.0.1", port: Int = 5984) {
  def url: String = s"http://$host:$port"
}

case class CouchDbInfo(couchdb: String, version: String, uuid: String, vendor: Vendor)

case class Vendor(version: String, name: String)

case class ActiveTask(pid: String, status: String, task: String, taskType: String)

object DatabaseEvents extends Enumeration {
  type DatabaseEvents = Value
  val Created = Value("created")
  val Updated = Value("updated")
  val Deleted = Value("deleted")
}

case class DatabaseEvent(dbName: String, event: DatabaseEvents.DatabaseEvents)

object FeedTypes extends Enumeration {
  type FeedTypes = Value
  val LongPoll = Value("longpoll")
  val Continuous = Value("continuous")
  val EventSource = Value("eventsource")
  val Normal = Value("normal")
}

object CouchDb {
  implicit val vendorFmt = Json.format[Vendor]
  implicit val couchDbInfoFmt = Json.format[CouchDbInfo]

  implicit val activeTaskReads = (
    (__ \ "pid").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "task").read[String] ~
      (__ \ "type").read[String]
    )(ActiveTask.apply _)


  implicit val databaseEventReads = (
    (__ \ "dbname").read[String] ~
      (__ \ "type").read[DatabaseEvents.DatabaseEvents]
    )(DatabaseEvent.apply _)

  /**
   * Meta information about the instance
   * @param couchDb
   * @return
   */
  def info()(implicit couchDb: CouchDb = CouchDb()): Future[CouchDbInfo] = {
    val request = url(couchDb.url).GET
    val response = Http(request OK as.String)
    val result = for (serverInfo <- response) yield Json.fromJson[CouchDbInfo](Json.parse(serverInfo)).get
    result
  }

  /**
   * Currently running tasks
   * @param couchDb
   * @return
   */
  def activeTasks()(implicit couchDb: CouchDb = CouchDb()): Future[List[ActiveTask]] = {
    val request = url(couchDb.url + "/_active_tasks").GET
    val response = Http(request OK as.String)
    val result = for (activeTasks <- response) yield Json.fromJson[List[ActiveTask]](Json.parse(activeTasks)).get
    result
  }

  /**
   * Returns a list of all the databases in the CouchDB instance
   * @param couchDb
   * @return
   */
  def allDbs()(implicit couchDb: CouchDb = CouchDb()): Future[List[String]] = {
    val request = url(couchDb.url + "/_all_dbs").GET
    val response = Http(request OK as.String)
    val result = for (allDbs <- response) yield Json.parse(allDbs).as[List[String]]
    result
  }

  /**
   * Returns a list of all database events in the CouchDB instance.
   * @param feed
   * @param timeout
   * @param heartbeat
   * @param couchDb
   * @return
   */
  def dbUpdates(feed: FeedTypes.FeedTypes = FeedTypes.LongPoll,
                timeout: Long = 60,
                heartbeat: Boolean = true,
                callBack: DatabaseEvent => Unit)
               (implicit couchDb: CouchDb = CouchDb()): Object with StringsByLine[Unit] = {
    val request = url(couchDb.url + s"/_db_updates?feed=${feed.toString}&timeout=$timeout&heartbeat=$heartbeat").GET
    val callBacker = as.stream.Lines(line => callBack(Json.fromJson[DatabaseEvent](Json.parse(line)).get))
    Http(request > callBacker)
    callBacker
  }

  /**
   * Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
   * @param couchDb
   * @param bytes
   * @param offset
   * @return
   */
  def log(bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    val request = url(couchDb.url + s"/_log?bytes=$bytes&offset=$offset").GET
    val response = Http(request OK as.String)
    val result = for (log <- response) yield log
    result
  }

  /**
   * Request, configure, or stop, a replication operation.
   * http://docs.couchdb.org/en/latest/api/misc.html#post-replicate
   * @param bytes
   * @param offset
   * @param replicationSpecification
   * @param couchDb
   * @return
   */
  def replicate(bytes: Long = 1000, offset: Long = 0, replicationSpecification: JsValue)
               (implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    val request = url(couchDb.url + s"/_replicate?bytes=$bytes&offset=$offset").POST <<
      Json.stringify(replicationSpecification)
    val response = Http(request OK as.String)
    val result = for (res <- response) yield Json.parse(res)
    result
  }

  /**
   * Restarts CouchDB
   * @param couchDb
   * @return
   */
  def restart()(implicit couchDb: CouchDb = CouchDb()): Future[Boolean] = {
    val request = url(couchDb.url + s"/_restart").POST.addHeader("Content-Type", "application/json")
    val response = Http(request OK as.String)
    val result = for (res <- response) yield (Json.parse(res) \ "ok").as[Boolean]
    result
  }

  /**
   * Returns a JSON object containing the statistics for the running server
   * @param couchDb
   * @return
   */
  def stats()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    val request = url(couchDb.url + s"/_stats").GET
    val response = Http(request OK as.String)
    val result = for (res <- response) yield Json.parse(res)
    result
  }

  /**
   * Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
   * @param couchDb
   * @param count
   * @return
   */
  def uuids(count: Int = 1)(implicit couchDb: CouchDb = CouchDb()): Future[List[String]] = {
    val request = url(couchDb.url + s"/_uuids?count=$count").GET
    val response = Http(request OK as.String)
    val result = for (uuids <- response) yield (Json.parse(uuids) \ "uuids").as[List[String]]
    result
  }
}
