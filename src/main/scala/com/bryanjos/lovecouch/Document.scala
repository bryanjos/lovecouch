package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._

case class DocumentResult(ok:Boolean, id:String, rev:String)

object Document {
  implicit val documentResultFmt = Json.format[DocumentResult]

  /**
   * Create a new document in the specified database.
   * If @data includes the _id field, then the document will be created with the specified document ID.
   * If the _id field is not specified, a new unique ID will be generated.
   * @param database
   * @return
   */
  def post[T](doc:T)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    for(res <- Requests.post(database.url, body = Json.stringify(Json.toJson[T](doc)), headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Returns the specified doc
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def get[T](id:String, rev:Option[String] = None)(implicit database:Database, reads: Reads[T]): Future[T] = {
    for(res <- Requests.get(database.url + s"/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.fromJson[T](Json.parse(res)).get
  }

  /**
   * Gets the specified local document.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def getLocal[T](id:String, rev:Option[String] = None)(implicit database:Database, reads: Reads[T]): Future[T] = {
    for(res <- Requests.get(database.url + s"/_local/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.fromJson[T](Json.parse(res)).get
  }

  /**
   * creates a new named document, or creates a new revision of the existing document.
   * @param doc
   * @param id
   * @param database
   * @return
   */
  def put[T](doc:T, id:String)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    for(res <- Requests.put(database.url + s"/$id", body = Json.stringify(Json.toJson[T](doc)), headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Stores the specified local document.
   * @param doc
   * @param id
   * @param database
   * @return
   */
  def putLocal[T](doc:T, id:String)(implicit database:Database, writes: Writes[T]): Future[DocumentResult] = {
    for(res <- Requests.put(database.url  + s"/_local/$id", body = Json.stringify(Json.toJson[T](doc)), headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Deletes the specified document from the database.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id:String, rev:String)(implicit database:Database): Future[DocumentResult] = {
    for(res <- Requests.delete(database.url + s"/$id", parameters = Map("rev"-> rev)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Deletes the specified local document.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def deleteLocal(id:String, rev:String)(implicit database:Database): Future[DocumentResult] = {
    for(res <- Requests.delete(database.url + s"/_local/$id", parameters = Map("rev"-> rev)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Returns the file attachment attachment associated with the document id.
   * @param id
   * @param attachmentName
   * @param database
   * @return
   */
  def getAttachment(id:String, attachmentName:String)(implicit database:Database): Future[Array[Byte]] = {
    Requests.getBytes(database.url + s"/$id/$attachmentName")
  }

  /**
   * Upload the supplied content as an attachment to the specified document
   * @param id
   * @param rev
   * @param attachmentName
   * @param attachment
   * @param mimeType
   * @param database
   * @return
   */
  def putAttachment(id:String, rev:String, attachmentName:String, attachment:java.io.File, mimeType:String)(implicit database:Database): Future[DocumentResult] = {
    for(res <- Requests.putFile(database.url + s"/$id/$attachmentName", file=attachment,
      parameters = Map("rev"-> rev),
      headers = Map() + ("Content-Length" -> attachment.length().toString) + ("Mime-Type" -> mimeType)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Deletes the attachment attachment to the specified id.
   * @param id
   * @param rev
   * @param attachmentName
   * @param database
   * @return
   */
  def deleteAttachment(id:String, rev:String, attachmentName:String)(implicit database:Database): Future[DocumentResult] = {
    for(res <- Requests.delete(database.url + s"/$id/$attachmentName", parameters = Map("rev"-> rev)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }
}
