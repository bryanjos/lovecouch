package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.stream.StringsByLine
import scala.util.Try


case class Database(name: String, couchDb: CouchDb = CouchDb()) {
  def url: String = couchDb.url + s"/$name"
}

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

object Database {
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
    implicit val viewFmt = Json.format[TempView]

  /**
   * Gets information about the specified database.
   * @param database
   * @return
   */
  def info()(implicit database: Database): Future[Try[DatabaseInfo]] = {
    for(res <- Requests.get(database.url))
    yield Try(Json.fromJson[DatabaseInfo](Json.parse(res.get)).get)
  }

  /**
   * Creates a new database.
   * @param database
   * @return
   */
  def create()(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.put(database.url))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }

  /**
   * Deletes the specified database, and all the documents and attachments contained within it.
   * @param database
   * @return
   */
  def delete()(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.delete(database.url))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }


  /**
   * Obtains a list of the changes made to the database.
   * @param docIds
   * @param feed
   * @param filter
   * @param heartBeat
   * @param includeDocs
   * @param limit
   * @param since
   * @param callBack
   * @param database
   * @return
   */
  def changes(docIds: Option[List[String]] = None, feed: FeedTypes.FeedTypes = FeedTypes.Normal,
              filter: Option[String], heartBeat: Long = 6000, includeDocs: Boolean = false,
              limit: Option[Long] = None, since: Long = 0, callBack: JsValue => Unit)
             (implicit database: Database): Object with StringsByLine[Unit] = {

    val d = docIds.map {
      ids => "doc_ids" -> Json.stringify(Json.toJson(ids))
    }.orElse(Some("" -> "")).get
    val fr = filter.map {
      fil => "filter" -> fil
    }.orElse(Some("" -> "")).get
    val lt = limit.map {
      ids => "limit" -> ids.toString
    }.orElse(Some("" -> "")).get
    val f = "feed" -> feed.toString
    val hb = "heartbeat" -> heartBeat.toString
    val id = "include_docs" -> includeDocs.toString
    val sn = "since" -> since.toString

    val map = Map() + d + fr + lt + f + hb + id + sn - ""
    Requests.getStream(database.url + "/_changes", line => callBack(Json.parse(line)), parameters = map)
  }

  /**
   * Request compaction of the specified database.
   * @param designDocName Optionally compacts the view indexes associated with the specified design document.
   * @param database
   * @return
   */
  def compact(designDocName: Option[String] = None)(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.post(database.url + "/_compact" + designDocName.map(d => s"/$designDocName").orElse(Some("")).get,
      headers = Map("Content-Type" -> "application/json")))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }

  /**
   * Cleans up the cached view output on disk for a given view.
   * @param database
   * @return
   */
  def viewCleanUp()(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.post(database.url + "/_view_cleanup", headers = Map("Content-Type" -> "application/json")))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }
  /**
   * Commits any recent changes to the specified database to disk.
   * @param database
   * @return
   */
  def ensureFullCommit()(implicit database: Database): Future[Try[EnsureFullCommitResult]] = {
    for(res <- Requests.post(database.url + "/_ensure_full_commit", headers = Map("Content-Type" -> "application/json")))
    yield Try(Json.fromJson[EnsureFullCommitResult](Json.parse(res.get)).get)
  }

  /**
   * Allows you to create and update multiple documents at the same time within a single request.
   * @param docs
   * @param database
   * @return
   */
  def bulkDocs[T](docs:Seq[T])(implicit database: Database, writes: Writes[T]): Future[Try[JsValue]] = {
    val json = Json.obj("docs" -> Json.toJson(docs))
    for(res <- Requests.post(database.url + "/_bulk_docs", body=Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Try(Json.parse(res.get))
  }

  /**
   * Creates (and executes) a temporary view based on the view function supplied in the JSON request.
   * @param view
   * @param database
   * @return
   */
  def tempView(view:TempView)(implicit database: Database): Future[Try[JsValue]] = {
    for(res <- Requests.post(database.url + "/_temp_view", body=Json.stringify(Json.toJson(view)),
      headers = Map("Content-Type" -> "application/json")))
    yield Try(Json.parse(res.get))
  }

  /**
   * Returns a JSON structure of all of the documents in a given database.
   * @param descending
   * @param endKey
   * @param endKeyDocId
   * @param group
   * @param groupLevel
   * @param includeDocs
   * @param inclusiveEnd
   * @param key
   * @param limit
   * @param reduce
   * @param skip
   * @param stale
   * @param startKey
   * @param startKeyDocId
   * @param database
   * @return
   */
  def allDocs(descending:Boolean=false,
              endKey:Option[String]=None,
              endKeyDocId:Option[String]=None,
              group:Boolean=false,
              groupLevel:Option[Long]=None,
              includeDocs:Boolean=false,
              inclusiveEnd:Boolean = true,
              key:Option[String]=None,
              limit:Option[Long]=None,
              reduce:Boolean=true,
              skip:Long=0,
              stale:Option[String]=None,
              startKey:Option[String]=None,
              startKeyDocId:Option[String]=None)
             (implicit database: Database): Future[Try[AllDocsResult]] = {


    val map = Map[String, String]() +
      ("descending" -> descending.toString) +
      endKey.map { v => "endkey" -> v }.orElse(Some("" -> "")).get +
      endKeyDocId.map { v => "endkey_docid" -> v }.orElse(Some("" -> "")).get +
      ("group" -> group.toString) +
      groupLevel.map { v => "group_level" -> v.toString }.orElse(Some("" -> "")).get +
      ("include_docs" -> includeDocs.toString) +
      ("inclusive_end" -> inclusiveEnd.toString) +
      key.map { v => "key" -> v }.orElse(Some("" -> "")).get +
      limit.map { v => "limit" -> v.toString }.orElse(Some("" -> "")).get +
      ("reduce" -> reduce.toString) +
      ("skip" -> skip.toString) +
      stale.map { v => "stale" -> v }.orElse(Some("" -> "")).get +
      startKey.map { v => "startkey" -> v }.orElse(Some("" -> "")).get +
      startKeyDocId.map { v => "startkey_docid" -> v }.orElse(Some("" -> "")).get


    for(res <- Requests.get(database.url + "/_all_docs", parameters=map, headers = Map("Content-Type" -> "application/json")))
    yield Try(Json.fromJson[AllDocsResult](Json.parse(res.get)).get)
  }

  /**
   * The POST to _all_docs allows to specify multiple keys to be selected from the database.
   * @param keys
   * @param database
   * @return
   */
  def allDocs(keys:Vector[String])(implicit database: Database): Future[Try[AllDocsResult]] = {
    for(res <- Requests.post(database.url + s"/_all_docs",
      body= Json.stringify(Json.obj("keys" -> keys)),
      headers = Map("Content-Type" -> "application/json")))
    yield Try(Json.fromJson[AllDocsResult](Json.parse(res.get)).get)
  }


  /**
   * Gets the current security object from the specified database.
   * @param database
   * @return
   */
  def security()(implicit database: Database): Future[Try[Security]] = {
    for(res <- Requests.get(database.url + s"/_security"))
    yield Try(Json.fromJson[Security](Json.parse(res.get)).get)
  }

  /**
   * Sets the security object for the given database.
   * @param security
   * @param database
   * @return
   */
  def setSecurity(security: Security)(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.post(database.url + s"/_security",
      body= Json.stringify(Json.toJson(security)),
      headers = Map("Content-Type" -> "application/json")))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }


  /**
   * Gets the current revs_limit (revision limit) setting.
   * @param database
   * @return
   */
  def revsLimit()(implicit database: Database): Future[Try[Int]] = {
    for(res <- Requests.get(database.url + s"/_revs_limit",
      headers = Map("Content-Type" -> "application/json")))
    yield Try(res.get.toInt)
  }


  /**
   * Sets the maximum number of document revisions that will be tracked by CouchDB, even after compaction has occurred.
   * @param limit
   * @param database
   * @return
   */
  def setRevsLimit(limit: Int)(implicit database: Database): Future[Try[Boolean]] = {
    for(res <- Requests.put(database.url + s"/_revs_limit",
      body= limit.toString,
      headers = Map("Content-Type" -> "application/json")))
    yield Try((Json.parse(res.get) \ "ok").as[Boolean])
  }
}
