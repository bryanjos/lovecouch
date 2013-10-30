package com.bryanjos.lovecouch

import play.api.libs.json._
import play.api.libs.functional.syntax._

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


class CouchDBException(msg: String) extends RuntimeException(msg)


case class DatabaseInfo(dbName: String, docCount: Long, docDelCount: Long,
                        updateSeq: Long, purgeSeq: Long, compactRunning: Boolean,
                        diskSize: Long, dataSize: Long, instanceStartTime: String,
                        diskFormatVersion: Long, committedUpdateSeq: Long)

case class EnsureFullCommitResult(ok: Boolean, instanceStartTime: String)

case class SecurityGroup(roles: Vector[String], names: Vector[String])

case class Security(admins: Option[SecurityGroup], readers: Option[SecurityGroup])

case class RowValue(rev:String)
case class Row(id:String, key:String, value:RowValue)
case class AllDocsResult(offset:Long, rows:Vector[Row])

case class TempView(map:String, reduce:Option[String] = None)


case class DocumentResult(ok:Boolean, id:String, rev:String)

case class ViewIndex(compactRunning: Boolean, updaterRunning: Boolean, language: String, purgeSeq: Long,
                     waitingCommit: Boolean, waitingClients: Long, signature: String, updateSeq: Long, diskSize: Long)
case class ViewInfo(name: String, viewIndex: ViewIndex)
case class ViewRow(id: String, key: Option[String] = None, value: JsValue)
case class ViewResult(totalRows: Long, rows: Vector[ViewRow], offset: Long)
case class View(name:String, map:String, reduce:Option[String] = None)

case class DesignDocument(_id:Option[String], _rev:Option[String] = None, language:String = "javascript", views:List[View] = List[View]())


case class SessionInfo(authenticationDB:String, authenticationHandlers:Vector[String], authenticated:String)
case class SessionContext(name:String, roles:Vector[String])
case class Session(ok:Boolean, userCtx: SessionContext, info:SessionInfo)


object Implicits {
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



  implicit val databaseInfoReads = (
    (__ \ "db_name").read[String] ~
      (__ \ "doc_count").read[Long] ~
      (__ \ "doc_del_count").read[Long] ~
      (__ \ "update_seq").read[Long] ~
      (__ \ "purge_seq").read[Long] ~
      (__ \ "compact_running").read[Boolean] ~
      (__ \ "disk_size").read[Long] ~
      (__ \ "data_size").read[Long] ~
      (__ \ "instance_start_time").read[String] ~
      (__ \ "disk_format_version").read[Long] ~
      (__ \ "committed_update_seq").read[Long]
    )(DatabaseInfo.apply _)

  implicit val ensureFullCommitResultReads = (
    (__ \ "ok").read[Boolean] ~
      (__ \ "instance_start_time").read[String]
    )(EnsureFullCommitResult.apply _)

  implicit val securityGroupFmt = Json.format[SecurityGroup]
  implicit val securityFmt = Json.format[Security]
  implicit val rowValueFmt = Json.format[RowValue]
  implicit val rowFmt = Json.format[Row]
  implicit val allDocsResultFmt = Json.format[AllDocsResult]
  implicit val tempViewFmt = Json.format[TempView]
  implicit val viewFmt = Json.format[View]
  implicit val documentResultFmt = Json.format[DocumentResult]


  implicit val viewIndexReads = (
    (__ \ "compact_running").read[Boolean] ~
      (__ \ "updater_running").read[Boolean] ~
      (__ \ "language").read[String] ~
      (__ \ "purge_seq").read[Long] ~
      (__ \ "waiting_commit").read[Boolean] ~
      (__ \ "waiting_clients").read[Long] ~
      (__ \ "signature").read[String] ~
      (__ \ "update_seq").read[Long] ~
      (__ \ "disk_size").read[Long]
    )(ViewIndex.apply _)


  implicit val viewInfoReads = (
    (__ \ "name").read[String] ~
      (__ \ "view_index").read[ViewIndex]
    )(ViewInfo.apply _)

  implicit val designDocumentFmt = Json.format[DesignDocument]



  implicit val sessionInfoReads = (
    (__ \ "authentication_db").read[String] ~
      (__ \ "authentication_handlers").read[Vector[String]] ~
      (__ \ "authenticated").read[String]

    )(SessionInfo.apply _)


  implicit val sessionContextFmt = Json.format[SessionContext]

  implicit val sessionFmt = Json.format[Session]
}
