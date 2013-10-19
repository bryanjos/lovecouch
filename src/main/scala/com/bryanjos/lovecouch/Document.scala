package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import akka.actor.ActorSystem

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
  def create[T](doc:T)
               (implicit database:Database, system:ActorSystem, context:ExecutionContext, writes: Writes[T]): Future[DocumentResult] = {
    Requests.post(database.url, body = Json.stringify(Json.toJson[T](doc))).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Returns the specified doc
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def get[T](id:String, rev:Option[String] = None)
            (implicit database:Database, system:ActorSystem, context:ExecutionContext, reads: Reads[T]): Future[T] = {
    Requests.get(database.url + s"/$id", queryParameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - "").map{
      response =>
        Requests.processObjectResponse[T](response)
    }
  }

  /**
   * Gets the specified local document.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def getLocal[T](id:String, rev:Option[String] = None)
                 (implicit database:Database, system:ActorSystem, context:ExecutionContext, reads: Reads[T]): Future[T] = {
    Requests.get(database.url + s"/_local/$id", queryParameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - "").map{
      response =>
        Requests.processObjectResponse[T](response)
    }
  }

  /**
   * creates a new named document, or creates a new revision of the existing document.
   * @param doc
   * @param id
   * @param database
   * @return
   */
  def updateOrCreate[T](doc:T, id:String)
                       (implicit database:Database, system:ActorSystem, context:ExecutionContext, writes: Writes[T]): Future[DocumentResult] = {
    Requests.put(database.url + s"/$id", body = Json.stringify(Json.toJson[T](doc))).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Stores the specified local document.
   * @param doc
   * @param id
   * @param database
   * @return
   */
  def updateOrCreateLocal[T](doc:T, id:String)(implicit database:Database, system:ActorSystem, context:ExecutionContext, writes: Writes[T]): Future[DocumentResult] = {
    Requests.put(database.url + s"/_local/$id", body = Json.stringify(Json.toJson[T](doc))).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Deletes the specified document from the database.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id:String, rev:String)(implicit database:Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.delete(database.url + s"/$id", queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Deletes the specified local document.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def deleteLocal(id:String, rev:String)(implicit database:Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.delete(database.url + s"/_local/$id", queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Returns the file attachment attachment associated with the document id.
   * @param id
   * @param attachmentName
   * @param database
   * @return
   */
  def getAttachment(id:String, attachmentName:String)(implicit database:Database, system:ActorSystem, context:ExecutionContext): Future[Array[Byte]] = {
    Requests.get(database.url + s"/$id/$attachmentName").map{
      response =>
        Requests.processBinaryResponse(response)
    }
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
  def addAttachment(id:String, rev:String, attachmentName:String, attachment:java.io.File, mimeType:String)
                   (implicit database:Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.putFile(database.url + s"/$id/$attachmentName", file=attachment,
      queryParameters = Map("rev"-> rev),
      headers = Map() + ("Mime-Type" -> mimeType)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Deletes the attachment attachment to the specified id.
   * @param id
   * @param rev
   * @param attachmentName
   * @param database
   * @return
   */
  def deleteAttachment(id:String, rev:String, attachmentName:String)
                      (implicit database:Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {

    Requests.delete(database.url + s"/$id/$attachmentName", queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }
}
