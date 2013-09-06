package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.{url, Http, as}


case class Database(name:String, server:Server=Server())
case class DatabaseInfo(dbName:String, docCount:Long, docDelCount:Long,
                    updateSeq:Long, purgeSeq:Long, compactRunning:Boolean,
                    diskSize:Long, dataSize:Long, instanceStartTime:String,
                    diskFormatVersion:Long, committedUpdateSeq:Long)

case class DatabaseCreateResult(ok:Boolean)

object Database {
  implicit val databaseCreateResultFmt = Json.format[DatabaseCreateResult]

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


  def info(database:Database): Future[DatabaseInfo] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}").GET
    val response = Http(request OK as.String)
    val result = for (database <- response) yield Json.fromJson[DatabaseInfo](Json.parse(database)).get
    result
  }

  def create(database:Database): Future[Boolean] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}").PUT
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield Json.fromJson[DatabaseCreateResult](Json.parse(createResult)).get.ok
    result
  }
}
