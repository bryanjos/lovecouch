package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import akka.actor.ActorSystem
import spray.http.{BasicHttpCredentials, HttpEntity}
import play.api.libs.functional.syntax._

case class Vendor(version: String, name: String)
case class CouchDbInfo(couchdb: String, version: String, uuid: String, vendor: Vendor)

case class ActiveTask(pid: String, status: String, task: String, taskType: String)

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


case class CouchDb(host: String = "127.0.0.1", port: Int = 5984,
                   username:Option[String] = None, password:Option[String] = None)
                  (implicit system:ActorSystem, context:ExecutionContext)
{

  implicit val vendorFmt = Json.format[Vendor]
  implicit val couchDbInfoFmt = Json.format[CouchDbInfo]

  implicit val activeTaskReads = (
    (__ \ "pid").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "task").read[String] ~
      (__ \ "type").read[String]
    )(ActiveTask.apply _)

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


  def url: String = s"http://$host:$port"

  implicit val authorization:Option[BasicHttpCredentials] = {
    if(username.isDefined && password.isDefined)
      Some(BasicHttpCredentials(username.get, password.get))
    else
      None
  }

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



  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @return
   */
  def getConfiguration: Future[JsValue] = {
    Requests.get(url + "/_config").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @return
   */
  def getConfigurationSection(section:String): Future[JsValue] = {
    Requests.get(url + s"/_config/$section").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @return
   */
  def getConfigurationSectionKey(section:String, key:String): Future[String] = {
    Requests.get(url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Updates a configuration value.
   * @param section
   * @param key
   * @param value
   * @return
   */
  def putConfigurationSectionKey(section:String, key:String, value:String): Future[String] = {
    Requests.put(url + s"/_config/$section/$key", body = value).map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @return
   */
  def deleteConfigurationSectionKey(section:String, key:String): Future[String] = {
    Requests.get(url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }


}
