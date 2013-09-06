package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class DocumentResult(ok:Boolean, id:String, rev:String)

object Document {
  implicit val databaseResultFmt = Json.format[DocumentResult]

  def put[T](data:T, database:Database)(implicit writes: Writes[T]): Future[DocumentResult] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}").POST << Json.stringify(Json.toJson[T](data))
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }

  def get[T](id:String, database:Database)(implicit reads: Reads[T]): Future[T] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}/$id").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield Json.fromJson[T](Json.parse(document)).get
    result
  }

  def get[T](id:String, rev:String, database:Database)(implicit reads: Reads[T]): Future[T] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}/$id?rev=$rev").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield Json.fromJson[T](Json.parse(document)).get
    result
  }

  def delete[T](id:String, database:Database): Future[DocumentResult] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}/$id").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }

  def delete[T](id:String, rev:String, database:Database): Future[DocumentResult] = {
    val request = url(s"http://${database.server.host}:${database.server.port}/${database.name}/$id?rev=$rev").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }
}
