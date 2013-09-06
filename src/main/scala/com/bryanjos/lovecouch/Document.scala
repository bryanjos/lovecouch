package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class DocumentResult(ok:Boolean, id:String, rev:String)

object Document {
  implicit val databaseResultFmt = Json.format[DocumentResult]

  def put[T](data:T)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    val request = url(database.url).POST << Json.stringify(Json.toJson[T](data))
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }

  def get[T](id:String, rev:Option[String] = None)(implicit database:Database, reads: Reads[T]): Future[T] = {
    val seq: Seq[(String,String)] = rev.map{r => "rev"-> r}.orElse(Some(""->"")).get::Nil
    val request = url(database.url + s"/$id").GET <<? Map(seq: _*) - ""
    val response = Http(request OK as.String)
    val result = for (document <- response) yield Json.fromJson[T](Json.parse(document)).get
    result
  }

  def delete[T](id:String, rev:Option[String] = None)(implicit database:Database): Future[DocumentResult] = {
    val seq: Seq[(String,String)] = rev.map{r => "rev"-> r}.orElse(Some(""->"")).get::Nil
    val request = url(database.url + s"/$id").DELETE <<? Map(seq: _*) - ""
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield Json.fromJson[DocumentResult](Json.parse(documentResult)).get
    result
  }
}
