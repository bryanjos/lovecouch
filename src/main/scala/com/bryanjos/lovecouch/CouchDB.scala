package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.stream.StringsByLine
import scala.util.Try

case class CouchDb(host: String = "127.0.0.1", port: Int = 5984) {
  def url: String = s"http://$host:$port"
}

case class CouchDbInfo(couchdb: String, version: String, uuid: String, vendor: Vendor)

case class Vendor(version: String, name: String)

case class ActiveTask(pid: String, status: String, task: String, taskType: String)

case class DatabaseEvent(dbName: String, event:String)

case class ReplicationSpecification(createTarget:Boolean = false, source:String, target:String)

case class ReplicationHistory(docsRead:Long, sessionId:String, recordedSeq:Long,
                              endLastSeq:Long, docWriteFailures:Long, startTime:String,
                               startLastSeq:Long, endTime:String, missingChecked:Long,
                               docsWritten:Long, missingFound:Long)


case class ReplicationResponse(ok:Boolean, history:Vector[ReplicationHistory], sessionId:String, sourceLastSeq:Long)

case class StatItem(description:String, current:Option[Long], sum:Option[Long], mean:Option[Long], stddev:Option[Long],
                    min:Option[Long], max:Option[Long])


case class CouchDBStats(authCacheMisses:StatItem, databaseWrites:StatItem, openDatabases:StatItem,
                        authCacheHits:StatItem, requestTime:StatItem, databaseReads:StatItem, openOSFiles:StatItem)


case class HttpRequestMethodStats(PUT:StatItem, GET:StatItem, COPY:StatItem,
                        DELETE:StatItem, POST:StatItem, HEAD:StatItem)


case class HttpStatusCodeStats( _403:StatItem, _202:StatItem, _401:StatItem,
                                _409:StatItem, _200:StatItem, _405:StatItem,
                                _400:StatItem, _201:StatItem, _404:StatItem,
                                _500:StatItem, _412:StatItem, _301:StatItem,
                                _304:StatItem)


case class HttpdStats(clientsRequestingChanges:StatItem, temporaryViewReads:StatItem, requests:StatItem,
                                  bulkRequests:StatItem, viewReads:StatItem)

case class Stats(couchdb:CouchDBStats, httpdRequestMethods:HttpRequestMethodStats,
                 httpdStatusCodes:HttpStatusCodeStats, httpd:HttpdStats)

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
      (__ \ "type").read[String]
    )(DatabaseEvent.apply _)



  implicit val replicationSpecificationWrites = (
    (__ \ "create_target").write[Boolean] ~
      (__ \ "source").write[String] ~
      (__ \ "target").write[String]
    )(unlift(ReplicationSpecification.unapply))



  implicit val replicationHistoryReads = (
    (__ \ "docs_read").read[Long] ~
      (__ \ "session_id").read[String] ~
      (__ \ "recorded_seq").read[Long] ~
      (__ \ "end_last_seq").read[Long] ~
      (__ \ "doc_write_failures").read[Long] ~
      (__ \ "start_time").read[String] ~
      (__ \ "start_last_seq").read[Long] ~
      (__ \ "end_time").read[String] ~
      (__ \ "missing_checked").read[Long] ~
      (__ \ "docs_written").read[Long] ~
      (__ \ "missing_found").read[Long]

    )(ReplicationHistory.apply _)


  implicit val replicationResponseReads = (
    (__ \ "ok").read[Boolean] ~
      (__ \ "history").read[Vector[ReplicationHistory]] ~
      (__ \ "session_id").read[String] ~
      (__ \ "source_last_seq").read[Long]

    )(ReplicationResponse.apply _)


  implicit val statItemFmt = Json.format[StatItem]

  implicit val couchDBStatsReads = (
      (__ \ "auth_cache_misses").read[StatItem] ~
      (__ \ "database_writes").read[StatItem] ~
      (__ \ "open_databases").read[StatItem] ~
      (__ \ "auth_cache_hits").read[StatItem] ~
      (__ \ "request_time").read[StatItem] ~
      (__ \ "database_reads").read[StatItem] ~
      (__ \ "open_os_files").read[StatItem]
    )(CouchDBStats.apply _)


  implicit val HttpRequestMethodStatsFmt = Json.format[HttpRequestMethodStats]


  implicit val HttpStatusCodeStatsReads = (
      (__ \ "403").read[StatItem] ~
      (__ \ "202").read[StatItem] ~
      (__ \ "401").read[StatItem] ~
      (__ \ "409").read[StatItem] ~
      (__ \ "200").read[StatItem] ~
      (__ \ "405").read[StatItem] ~
      (__ \ "400").read[StatItem] ~
      (__ \ "201").read[StatItem] ~
      (__ \ "404").read[StatItem] ~
      (__ \ "500").read[StatItem] ~
      (__ \ "412").read[StatItem] ~
      (__ \ "301").read[StatItem] ~
      (__ \ "304").read[StatItem]
    )(HttpStatusCodeStats.apply _)


  implicit val httpdStatsReads = (
    (__ \ "clients_requesting_changes").read[StatItem] ~
      (__ \ "temporary_view_reads").read[StatItem] ~
      (__ \ "requests").read[StatItem] ~
      (__ \ "bulk_requests").read[StatItem] ~
      (__ \ "view_reads").read[StatItem]
    )(HttpdStats.apply _)


  implicit val statsRead = (
    (__ \ "couchdb").read[CouchDBStats] ~
      (__ \ "httpd_request_methods").read[HttpRequestMethodStats] ~
      (__ \ "httpd_status_codes").read[HttpStatusCodeStats] ~
      (__ \ "httpd").read[HttpdStats]
    )(Stats.apply _)


  /**
   * Meta information about the instance
   * @param couchDb
   * @return
   */
  def info()(implicit couchDb: CouchDb = CouchDb()): Future[Try[CouchDbInfo]] = {
    for(res <- Requests.get(couchDb.url))
    yield Try(Json.fromJson[CouchDbInfo](Json.parse(res.get)).get)
  }

  /**
   * Currently running tasks
   * @param couchDb
   * @return
   */
  def activeTasks()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[ActiveTask]]] = {
    for(res <- Requests.get(couchDb.url + "/_active_tasks"))
    yield Try(Json.fromJson[Vector[ActiveTask]](Json.parse(res.get)).get)
  }

  /**
   * Returns a list of all the databases in the CouchDB instance
   * @param couchDb
   * @return
   */
  def allDbs()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[String]]] = {
    for(res <- Requests.get(couchDb.url + "/_all_dbs"))
    yield Try(Json.fromJson[Vector[String]](Json.parse(res.get)).get)
  }

  /**
   * Returns a list of all database events in the CouchDB instance.
   * @param feed
   * @param timeout
   * @param heartbeat
   * @param couchDb
   * @return
   */
  def updates(callBack: DatabaseEvent => Unit,
              feed: FeedTypes.FeedTypes = FeedTypes.LongPoll,
              timeout: Long = 60,
              heartbeat: Boolean = true)
              (implicit couchDb: CouchDb = CouchDb()): Object with StringsByLine[Unit] = {
    Requests.getStream(couchDb.url + s"/_db_updates?feed=${feed.toString}&timeout=$timeout&heartbeat=$heartbeat",
    line => callBack(Json.fromJson[DatabaseEvent](Json.parse(line)).get)
    )
  }

  /**
   * Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
   * @param couchDb
   * @param bytes
   * @param offset
   * @return
   */
  def log(bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]] = {
    for(res <- Requests.get(couchDb.url + s"/_log?bytes=$bytes&offset=$offset"))
    yield Try(res.get)
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
  def replicate(bytes: Long = 1000, offset: Long = 0, replicationSpecification: ReplicationSpecification)
               (implicit couchDb: CouchDb = CouchDb()): Future[Try[ReplicationResponse]] = {
    for(res <- Requests.post(couchDb.url + s"/_replicate?bytes=$bytes&offset=$offset",
      body = Json.stringify(Json.toJson(replicationSpecification))))
    yield Try(Json.fromJson[ReplicationResponse](Json.parse(res.get)).get)
  }

  /**
   * Restarts CouchDB
   * @param couchDb
   * @return
   */
  def restart()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Boolean]] = {
    for(res <- Requests.post(couchDb.url + s"/_restart", headers = Map("Content-Type" -> "application/json")))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }

  /**
   * Returns a JSON object containing the statistics for the running server
   * @param couchDb
   * @return
   */
  def stats()(implicit couchDb: CouchDb = CouchDb()): Future[Try[Stats]] = {
    for(res <- Requests.get(couchDb.url + s"/_stats"))
    yield Try(Json.fromJson[Stats](Json.parse(res.get)).get)
  }

  /**
   * Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
   * @param couchDb
   * @param count
   * @return
   */
  def uuids(count: Int = 1)(implicit couchDb: CouchDb = CouchDb()): Future[Try[Vector[String]]] = {
    for(res <- Requests.get(couchDb.url + s"/_uuids?count=$count"))
    yield Try((Json.parse(res.get) \ "uuids").as[Vector[String]])
  }
}
