package com.bryanjos.lovecouch

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import Document.documentResultFmt
import spray.http.HttpEntity
import akka.actor.ActorSystem


case class ViewIndex(compactRunning: Boolean, updaterRunning: Boolean, language: String, purgeSeq: Long,
                     waitingCommit: Boolean, waitingClients: Long, signature: String, updateSeq: Long, diskSize: Long)
case class ViewInfo(name: String, viewIndex: ViewIndex)
case class ViewRow(id: String, key: Option[String] = None, value: JsValue)
case class ViewResult(totalRows: Long, rows: Vector[ViewRow], offset: Long)
case class View(name:String, map:String, reduce:Option[String] = None)
case class DesignDocument(_id:String, _rev:Option[String] = None, language:String = "javascript", views:List[View] = List[View]())

object DesignDocument {
  implicit val viewIndexReads = (
    (__ \ "compact_running").read[Boolean] ~
      (__ \ "updater_running").read[Boolean] ~
      (__ \ "language").read[String] ~
      (__ \ "purge_seq").read[Long] ~
      (__ \ "waiting_commit").read[Boolean] ~
      (__ \ "waiting_clients").read[Long] ~
      (__ \ "signature").read[String] ~
      (__ \ "update_seq").read[Long] ~
      (__ \ "disk_size").read[Long]
    )(ViewIndex.apply _)


  implicit val viewInfoReads = (
    (__ \ "name").read[String] ~
      (__ \ "view_index").read[ViewIndex]
    )(ViewInfo.apply _)

  implicit val viewFmt = Json.format[View]
  implicit val designDocumentFmt = Json.format[DesignDocument]

  /**
   * Returns the specified design document
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def get(id: String, rev: Option[String] = None)
         (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DesignDocument] = {
    Requests.get(database.url + s"/$id", queryParameters = Map(rev.map { r => "rev" -> r }.orElse(Some("" -> "")).get) - "").map{
      response =>
        Requests.processResponse[DesignDocument](response,
          (e:HttpEntity) => {
            val json = Json.parse(e.asString)

            DesignDocument(
              _id = (json \ "_id").as[String],
              _rev = Some((json \ "_rev").as[String]),
              language = (json \ "language").as[String],
              views = (json \ "views").as[JsObject].fields.map{
                field =>
                  View(
                    name = field._1,
                    map = (field._2 \ "map").as[String],
                    reduce = (field._2 \ "map").asOpt[String]
                  )
              }.toList
            )
        })
    }
  }

  /**
   * Upload the specified design document
   * @param designDocument
   * @param database
   * @return
   */
  def addOrUpdate(designDocument:DesignDocument)
                 (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    val json = Json.obj(
      "_id" -> designDocument._id,
      "language" -> designDocument.language,
      "views" -> Json.toJson(designDocument.views.map
      {
        view =>
          if(view.reduce.isEmpty)
            view.name -> Json.obj("map" -> view.map)
          else
            view.name -> Json.obj("map" -> view.map, "reduce" -> view.reduce.get)
      }.toMap)
    )


    Requests.put(database.url + s"/${designDocument._id}", body = Json.stringify(json)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Delete an existing design document
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id: String, rev: String)
            (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.delete(database.url + s"/$id",
      queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Returns the file attachment attachment associated with the design document
   * @param id
   * @param attachmentName
   * @param database
   * @return
   */
  def getAttachment(id: String, attachmentName: String)
                   (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[Array[Byte]] = {
    Requests.get(database.url + s"/$id/$attachmentName").map{
      response =>
        Requests.processBinaryResponse(response)
    }
  }


  /**
   * Upload the supplied content as an attachment to the specified design document
   * @param id
   * @param rev
   * @param attachmentName
   * @param attachment
   * @param mimeType
   * @param database
   * @return
   */
  def addAttachment(id: String, rev: String, attachmentName: String, attachment: java.io.File, mimeType: String)
                   (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.putFile(database.url + s"$id/$attachmentName", file=attachment,
      queryParameters = Map("rev"-> rev),
      headers = Map() + ("Mime-Type" -> mimeType)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Deletes the attachment
   * @param id
   * @param rev
   * @param attachmentName
   * @param database
   * @return
   */
  def deleteAttachment(id: String, rev: String, attachmentName: String)
                      (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[DocumentResult] = {
    Requests.delete(database.url + s"/$id/$attachmentName", queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Obtains information about a given design document
   * @param id
   * @param database
   * @return
   */
  def info(id: String)
          (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[ViewInfo] = {
    Requests.put(database.url + s"/$id/_info").map{
      response =>
        Requests.processObjectResponse[ViewInfo](response)
    }
  }


  /**
   * Executes the specified view-name from the specified design-doc design document.
   * @param designDocName
   * @param viewName
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
   * @param updateSeq
   * @param database
   * @return
   */
  def executeView(designDocName: String, viewName: String,
                     descending: Option[Boolean] = None,
                     endKey: Option[String] = None,
                     endKeyDocId: Option[String] = None,
                     group: Option[Boolean] = None,
                     groupLevel: Option[Long] = None,
                     includeDocs: Option[Boolean] = None,
                     inclusiveEnd: Option[Boolean] = None,
                     key: Option[String] = None,
                     limit: Option[Long] = None,
                     reduce: Option[Boolean] = None,
                     skip: Option[Long] = None,
                     stale: Option[String] = None,
                     startKey: Option[String] = None,
                     startKeyDocId: Option[String] = None,
                     updateSeq: Option[Boolean] = None)
                    (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[ViewResult] = {

    val map = Map[String, String]() +
      descending.map {
        v => "descending" -> v.toString
      }.orElse(Some("" -> "")).get +
      endKey.map {
        v => "endkey" -> v
      }.orElse(Some("" -> "")).get +
      endKeyDocId.map {
        v => "endkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      group.map {
        v => "group" -> v.toString
      }.orElse(Some("" -> "")).get +
      groupLevel.map {
        v => "group_level" -> v.toString
      }.orElse(Some("" -> "")).get +
      includeDocs.map {
        v => "include_docs" -> v.toString
      }.orElse(Some("" -> "")).get +
      inclusiveEnd.map {
      v => "inclusive_end" -> v.toString
      }.orElse(Some("" -> "")).get +
      key.map {
        v => "key" -> v
      }.orElse(Some("" -> "")).get +
      limit.map {
        v => "limit" -> v.toString
      }.orElse(Some("" -> "")).get +
      reduce.map {
        v => "reduce" -> v.toString
      }.orElse(Some("" -> "")).get +
      skip.map {
        v => "skip" -> v.toString
      }.orElse(Some("" -> "")).get +
      stale.map {
        v => "stale" -> v
      }.orElse(Some("" -> "")).get +
      startKey.map {
        v => "startkey" -> v
      }.orElse(Some("" -> "")).get +
      startKeyDocId.map {
        v => "startkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      updateSeq.map {
        v => "update_seq" -> v.toString
      }.orElse(Some("" -> "")).get - ""

    Requests.get(database.url + s"/$designDocName/_view/$viewName", queryParameters = map).map{
      response =>
        Requests.processResponse[ViewResult](response,
          (e:HttpEntity) => {
            val json = Json.parse(e.asString)

            ViewResult(
              (json \ "total_rows").as[Long],
              (json \ "rows").as[List[JsObject]].map {
                row =>
                  ViewRow(
                    (row \ "id").as[String],
                    (row \ "key").asOpt[String],
                    (row \ "value").as[JsValue]
                  )
              }.toVector,
              (json \ "offset").as[Long]
            )
          })
    }
  }

  /**
   * Executes the specified view-name from the specified design-doc design document.
   * Unlike the GET method for accessing views, the POST method supports the specification
   * of explicit keys to be retrieved from the view results.
   * @param designDocName
   * @param viewName
   * @param keys
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
   * @param updateSeq
   * @param database
   * @return
   */
  def executeViewPost(designDocName: String,
                         viewName: String,
                         keys: Vector[String],
                         descending: Option[Boolean] = None,
                         endKey: Option[String] = None,
                         endKeyDocId: Option[String] = None,
                         group: Option[Boolean] = None,
                         groupLevel: Option[Long] = None,
                         includeDocs: Option[Boolean] = None,
                         inclusiveEnd: Option[Boolean] = None,
                         key: Option[String] = None,
                         limit: Option[Long] = None,
                         reduce: Option[Boolean] = None,
                         skip: Option[Long] = None,
                         stale: Option[String] = None,
                         startKey: Option[String] = None,
                         startKeyDocId: Option[String] = None,
                         updateSeq: Option[Boolean] = None)
                        (implicit database: Database, system:ActorSystem, context:ExecutionContext): Future[ViewResult] = {


    val map = Map[String, String]() +
      descending.map {
        v => "descending" -> v.toString
      }.orElse(Some("" -> "")).get +
      endKey.map {
        v => "endkey" -> v
      }.orElse(Some("" -> "")).get +
      endKeyDocId.map {
        v => "endkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      group.map {
        v => "group" -> v.toString
      }.orElse(Some("" -> "")).get +
      groupLevel.map {
        v => "group_level" -> v.toString
      }.orElse(Some("" -> "")).get +
      includeDocs.map {
        v => "include_docs" -> v.toString
      }.orElse(Some("" -> "")).get +
      inclusiveEnd.map {
        v => "inclusive_end" -> v.toString
      }.orElse(Some("" -> "")).get +
      key.map {
        v => "key" -> v
      }.orElse(Some("" -> "")).get +
      limit.map {
        v => "limit" -> v.toString
      }.orElse(Some("" -> "")).get +
      reduce.map {
        v => "reduce" -> v.toString
      }.orElse(Some("" -> "")).get +
      skip.map {
        v => "skip" -> v.toString
      }.orElse(Some("" -> "")).get +
      stale.map {
        v => "stale" -> v
      }.orElse(Some("" -> "")).get +
      startKey.map {
        v => "startkey" -> v
      }.orElse(Some("" -> "")).get +
      startKeyDocId.map {
        v => "startkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      updateSeq.map {
        v => "update_seq" -> v.toString
      }.orElse(Some("" -> "")).get - ""


    Requests.post(database.url + s"/$designDocName/_view/$viewName",
      body = Json.stringify(Json.obj("keys" -> keys)),
      queryParameters = map
    ).map{
      response =>
        Requests.processResponse[ViewResult](response,
          (e:HttpEntity) => {
            val json = Json.parse(e.asString)

            ViewResult(
              (json \ "total_rows").as[Long],
              (json \ "rows").as[List[JsObject]].map {
                row =>
                  ViewRow(
                    (row \ "id").as[String],
                    (row \ "key").asOpt[String],
                    (row \ "value").as[JsValue]
                  )
              }.toVector,
              (json \ "offset").as[Long]
            )
          })
    }
  }

}
