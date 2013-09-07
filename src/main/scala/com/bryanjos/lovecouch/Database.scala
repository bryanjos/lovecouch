package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.{url, Http, as}
import dispatch.stream.StringsByLine


case class Database(name:String, couchDb:CouchDb=CouchDb()) { def url:String = couchDb.url + s"/$name" }
case class DatabaseInfo(dbName:String, docCount:Long, docDelCount:Long,
                    updateSeq:Long, purgeSeq:Long, compactRunning:Boolean,
                    diskSize:Long, dataSize:Long, instanceStartTime:String,
                    diskFormatVersion:Long, committedUpdateSeq:Long)

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


  /**
   * Gets information about the specified database.
   * @param database
   * @return
   */
  def info()(implicit database:Database): Future[DatabaseInfo] = {
    val request = url(database.url).GET
    val response = Http(request OK as.String)
    val result = for (database <- response) yield Json.fromJson[DatabaseInfo](Json.parse(database)).get
    result
  }

  /**
   * Creates a new database.
   * @param database
   * @return
   */
  def create()(implicit database:Database): Future[Boolean] = {
    val request = url(database.url).PUT
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield (Json.parse(createResult) \ "ok").as[Boolean]
    result
  }

  /**
   * Deletes the specified database, and all the documents and attachments contained within it.
   * @param database
   * @return
   */
  def delete()(implicit database:Database): Future[Boolean] = {
    val request = url(database.url).DELETE
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield (Json.parse(createResult) \ "ok").as[Boolean]
    result
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
  def changes(docIds:Option[List[String]] = None, feed:FeedTypes.FeedTypes=FeedTypes.Normal,
              filter:Option[String], heartBeat:Long=6000, includeDocs:Boolean=false,
              limit:Option[Long]= None, since:Long=0, callBack: JsValue => Unit)
             (implicit database:Database): Object with StringsByLine[Unit] = {

    val d = docIds.map{ids => "doc_ids"-> Json.stringify(Json.toJson(ids))}.orElse(Some(""->"")).get
    val fr = filter.map{fil => "filter"-> fil}.orElse(Some(""->"")).get
    val lt = limit.map{ids => "limit"-> ids.toString}.orElse(Some(""->"")).get
    val f = "feed" -> feed.toString
    val hb = "heartbeat" -> heartBeat.toString
    val id = "include_docs" -> includeDocs.toString
    val sn = "since" -> since.toString

    val map = Map() + d + fr + lt + f + hb + id + sn - ""
    val request = url(database.url + s"/_changes").GET <<? map
    val callBacker = as.stream.Lines(line => callBack(Json.parse(line)))
    Http(request > callBacker)
    callBacker
  }
}
