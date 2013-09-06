package com.bryanjos.lovecouch

import com.codahale.jerkson.JsonSnakeCase
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import play.api.libs.functional.syntax._
import dispatch.{url, Http, as}


@JsonSnakeCase
case class Database(dbName:String, docCount:Long, docDelCount:Long,
                    updateSeq:Long, purgeSeq:Long, compactRunning:Boolean,
                    diskSize:Long, dataSize:Long, instanceStartTime:String,
                    diskFormatVersion:Long, committedUpdateSeq:Long)

case class DatabaseCreateResult(ok:Boolean)
case class DocumentResult(ok:Boolean, id:String, rev:String)

object Database {
  implicit val databaseCreateResultFmt = Json.format[DatabaseCreateResult]
  implicit val databaseResultFmt = Json.format[DocumentResult]

  implicit val databaseReads = (
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
    )(Database.apply _)


  def info(databaseName:String, server:Server=Server()): Future[Database] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").GET
    val response = Http(request OK as.String)
    val result = for (database <- response) yield Json.fromJson[Database](Json.parse(database)).get
    result
  }

  def create(databaseName:String, server:Server=Server()): Future[Boolean] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").PUT
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield Json.fromJson[DatabaseCreateResult](Json.parse(createResult)).get.ok
    result
  }

  def putDocument[T](data:T, databaseName:String, server:Server=Server())(implicit writes: Writes[T]): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").POST << Json.stringify(Json.toJson[T](data))
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }

  def getDocument[T](id:String, databaseName:String, server:Server=Server())(implicit reads: Reads[T]): Future[T] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield Json.fromJson[T](Json.parse(document)).get
    result
  }

  def getDocumentRevision[T](id:String, rev:String, databaseName:String, server:Server=Server())(implicit reads: Reads[T]): Future[T] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id?rev=$rev").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield Json.fromJson[T](Json.parse(document)).get
    result
  }

  def deleteDocument[T](id:String, databaseName:String, server:Server=Server()): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }

  def deleteDocument[T](id:String, rev:String, databaseName:String, server:Server=Server()): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id?rev=$rev").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }
}
