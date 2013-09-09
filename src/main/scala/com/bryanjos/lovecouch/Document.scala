package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class DocumentResult(ok:Boolean, id:String, rev:String)

//case class Revisions(ids:Vector[String], start:Int = 1)
//case class RevsionInfo(status:String, rev:String)
//case class Attachment(contentType:String, length:Long, revpos:String, stub:Boolean)
//case class Attachments(_attachments:Vector[Attachment])
case class Document(_id:Option[String], _rev:Option[String])

object Document {
  implicit val documentResultFmt = Json.format[DocumentResult]

  /**
   * Create a new document in the specified database.
   * If @data includes the _id field, then the document will be created with the specified document ID.
   * If the _id field is not specified, a new unique ID will be generated.
   * @param data
   * @param database
   * @param writes
   * @tparam T
   * @return
   */
  def post[T <: Document](data:T)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    for(res <- Requests.post(database.url, body = Json.stringify(Json.toJson[T](data)), headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Returns the specified doc
   * @param id
   * @param rev
   * @param database
   * @param reads
   * @tparam T
   * @return
   */
  def get[T <: Document](id:String, rev:Option[String] = None)(implicit database:Database, reads: Reads[T]): Future[T] = {
    for(res <- Requests.get(database.url + s"/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.fromJson[T](Json.parse(res)).get
  }

  /**
   * creates a new named document, or creates a new revision of the existing document.
   * @param data
   * @param database
   * @param writes
   * @tparam T
   * @return
   */
  def put[T <: Document](data:T)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    for(res <- Requests.put(database.url, body = Json.stringify(Json.toJson[T](data)), headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Deletes the specified document from the database.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id:String, rev:Option[String] = None)(implicit database:Database): Future[DocumentResult] = {
    for(res <- Requests.delete(database.url + s"/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }
}
