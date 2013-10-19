package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import akka.actor.ActorSystem


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
  def info()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DatabaseInfo] = {
    Requests.get(database.url).map{
      response =>
        Requests.processObjectResponse[DatabaseInfo](response)
    }
  }

  /**
   * Creates a new database.
   * @param database
   * @return
   */
  def create()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.put(database.url).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Deletes the specified database, and all the documents and attachments contained within it.
   * @param database
   * @return
   */
  def delete()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.delete(database.url).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Request compaction of the specified database.
   * @param designDocName Optionally compacts the view indexes associated with the specified design document.
   * @param database
   * @return
   */
  def compact(designDocName: Option[String] = None)(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.post(database.url + "/_compact" + designDocName.map(d => s"/$designDocName").orElse(Some("")).get).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Cleans up the cached view output on disk for a given view.
   * @param database
   * @return
   */
  def viewCleanUp()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.post(database.url + "/_view_cleanup").map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }
  /**
   * Commits any recent changes to the specified database to disk.
   * @param database
   * @return
   */
  def ensureFullCommit()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[EnsureFullCommitResult] = {
    Requests.post(database.url + "/_ensure_full_commit").map{
      response =>
        Requests.processObjectResponse[EnsureFullCommitResult](response)
    }
  }

  /**
   * Allows you to create and update multiple documents at the same time within a single request.
   * @param docs
   * @param database
   * @return
   */
  def bulkDocs[T](docs:Seq[T])(implicit database: Database, system:ActorSystem, context:ExecutionContext, writes: Writes[T]): Future[JsValue] = {
    val json = Json.obj("docs" -> Json.toJson(docs))

    Requests.post(database.url + "/_bulk_docs", body=Json.stringify(json)).map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Creates (and executes) a temporary view based on the view function supplied in the JSON request.
   * @param view
   * @param database
   * @return
   */
  def tempView(view:TempView)(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[JsValue] = {
    Requests.post(database.url + "/_temp_view", body=Json.stringify(Json.toJson(view))).map{
      response =>
        Requests.processJsonResponse(response)
    }
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
             (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[AllDocsResult] = {


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


    Requests.get(database.url + "/_all_docs", queryParameters=map).map{
      response =>
        Requests.processObjectResponse[AllDocsResult](response)
    }
  }

  /**
   * The POST to _all_docs allows to specify multiple keys to be selected from the database.
   * @param keys
   * @param database
   * @return
   */
  def allDocs(keys:Vector[String])(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[AllDocsResult] = {
    Requests.post(database.url + "/_all_docs",
      body= Json.stringify(Json.obj("keys" -> keys))).map{
      response =>
        Requests.processObjectResponse[AllDocsResult](response)
    }
  }


  /**
   * Gets the current security object from the specified database.
   * @param database
   * @return
   */
  def security()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Security] = {
    Requests.get(database.url + "/_security").map{
      response =>
        Requests.processObjectResponse[Security](response)
    }
  }

  /**
   * Sets the security object for the given database.
   * @param security
   * @param database
   * @return
   */
  def setSecurity(security: Security)(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.post(database.url + "/_security",
      body= Json.stringify(Json.toJson(security))).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }


  /**
   * Gets the current revs_limit (revision limit) setting.
   * @param database
   * @return
   */
  def revsLimit()(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Int] = {
    Requests.get(database.url + "/_revs_limit").map{
      response =>
        Requests.processIntResponse(response)
    }
  }


  /**
   * Sets the maximum number of document revisions that will be tracked by CouchDB, even after compaction has occurred.
   * @param limit
   * @param database
   * @return
   */
  def setRevsLimit(limit: Int)(implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Boolean] = {
    Requests.put(database.url + s"/_revs_limit",
      body= limit.toString).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }
}
