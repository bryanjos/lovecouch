package com.bryanjos.lovecouch

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import ExecutionContext.Implicits.global
import Document.documentResultFmt


case class ViewIndex(compactRunning: Boolean, updaterRunning: Boolean, language: String,
                     purgeSeq: Long, waitingCommit: Boolean,
                     waitingClients: Long, signature: String, updateSeq: Long, diskSize: Long)
case class ViewInfo(name: String, viewIndex: ViewIndex)
case class ViewRow[T](id: String, key: String, value: Vector[String], doc: Option[T])
case class ViewResult[T](totalRows: Long, rows: Vector[ViewRow[T]], offset: Long)
case class View(name:String, map:String, reduce:Option[String] = None)
case class DesignDocument(_id:String, _rev:Option[String] = None, language:String = "javascript", views:List[View] =())

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
  def get(id: String, rev: Option[String] = None)(implicit database: Database): Future[DesignDocument] = {
    for (res <- Requests.get(database.url + s"/_design/$id", parameters = Map(rev.map { r => "rev" -> r }.orElse(Some("" -> "")).get) - ""))
    yield{
      val json = Json.parse(res)

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
    }
  }

  /**
   * Upload the specified design document
   * @param designDocument
   * @param database
   * @return
   */
  def put(designDocument:DesignDocument)(implicit database: Database): Future[DocumentResult] = {
    val json = Json.obj(
      "_id" -> designDocument._id,
      "language" -> designDocument.language,
      "views" -> designDocument.views.map
      {
        view =>
          if(view.reduce.isEmpty)
            view.name -> Json.obj("map" -> view.map)
          else
            view.name -> Json.obj("map" -> view.map, "reduce" -> view.reduce.get)
      }
    )

    for (res <- Requests.put(database.url + s"/${designDocument._id}", body = Json.stringify(json),
      headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Delete an existing design document
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id: String, rev: Option[String] = None)(implicit database: Database): Future[DocumentResult] = {
    for (res <- Requests.put(database.url + s"/_design/$id",
      parameters = Map(rev.map {
        r => "rev" -> r
      }.orElse(Some("" -> "")).get) - "",
      headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Returns the file attachment attachment associated with the design document
   * @param id
   * @param attachmentName
   * @param database
   * @return
   */
  def getAttachment(id: String, attachmentName: String)(implicit database: Database): Future[Array[Byte]] = {
    Requests.getBytes(database.url + s"/_design/$id/$attachmentName")
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
  def putAttachment(id: String, rev: String, attachmentName: String, attachment: java.io.File, mimeType: String)
                   (implicit database: Database): Future[DocumentResult] = {
    for (res <- Requests.putFile(database.url + s"_design/$id/$attachmentName", file = attachment,
      parameters = Map("rev" -> rev),
      headers = Map() + ("Content-Length" -> attachment.length().toString) + ("Mime-Type" -> mimeType)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }

  /**
   * Deletes the attachment
   * @param id
   * @param rev
   * @param attachmentName
   * @param database
   * @return
   */
  def deleteAttachment(id: String, rev: String, attachmentName: String)(implicit database: Database): Future[DocumentResult] = {
    for (res <- Requests.delete(database.url + s"/_design/$id/$attachmentName", parameters = Map("rev" -> rev)))
    yield Json.fromJson[DocumentResult](Json.parse(res)).get
  }


  /**
   * Obtains information about a given design document
   * @param id
   * @param database
   * @return
   */
  def info(id: String)(implicit database: Database): Future[ViewInfo] = {
    for (res <- Requests.put(database.url + s"/_design/$id/_info", headers = Map("Content-Type" -> "application/json")))
    yield Json.fromJson[ViewInfo](Json.parse(res)).get
  }


  def executeView[T](designDocName: String, viewName: String, descending: Boolean = false, endKey: Option[String] = None,
                     endKeyDocId: Option[String] = None, group: Boolean = false,
                     groupLevel: Option[Long] = None, includeDocs: Boolean = false,
                     inclusiveEnd: Boolean = true, key: Option[String] = None,
                     limit: Option[Long] = None, reduce: Boolean = true, skip: Long = 0,
                     stale: Option[String] = None, startKey: Option[String] = None,
                     startKeyDocId: Option[String] = None, updateSeq: Boolean = false)
                    (implicit database: Database, reads: Reads[T]): Future[ViewResult[T]] = {


    val map = Map[String, String]() +
      ("descending" -> descending.toString) +
      endKey.map {
        v => "endkey" -> v
      }.orElse(Some("" -> "")).get +
      endKeyDocId.map {
        v => "endkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      ("group" -> group.toString) +
      groupLevel.map {
        v => "group_level" -> v.toString
      }.orElse(Some("" -> "")).get +
      ("include_docs" -> includeDocs.toString) +
      ("inclusive_end" -> inclusiveEnd.toString) +
      key.map {
        v => "key" -> v
      }.orElse(Some("" -> "")).get +
      limit.map {
        v => "limit" -> v.toString
      }.orElse(Some("" -> "")).get +
      ("reduce" -> reduce.toString) +
      ("skip" -> skip.toString) +
      stale.map {
        v => "stale" -> v
      }.orElse(Some("" -> "")).get +
      startKey.map {
        v => "startkey" -> v
      }.orElse(Some("" -> "")).get +
      startKeyDocId.map {
        v => "startkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      ("update_seq" -> updateSeq.toString)


    for (res <- Requests.get(database.url + s"/_design/$designDocName/_view/$viewName",
      parameters = map,
      headers = Map("Content-Type" -> "application/json")))
    yield {
      val json = Json.parse(res)

      ViewResult[T](
        (json \ "total_rows").as[Long],
        (json \ "rows").as[List[JsObject]].map {
          row =>
            ViewRow[T](
              (row \ "id").as[String],
              (row \ "key").as[String],
              (row \ "value").as[Vector[String]],
              (row \ "doc").asOpt[T]
            )
        }.toVector,
        (json \ "offset").as[Long]
      )
    }
  }

  def executeViewPost[T](designDocName: String,
                         viewName: String,
                         keys: Vector[String],
                         descending: Boolean = false,
                         endKey: Option[String] = None,
                         endKeyDocId: Option[String] = None,
                         group: Boolean = false,
                         groupLevel: Option[Long] = None,
                         includeDocs: Boolean = false,
                         inclusiveEnd: Boolean = true,
                         key: Option[String] = None,
                         limit: Option[Long] = None,
                         reduce: Boolean = true,
                         skip: Long = 0,
                         stale: Option[String] = None,
                         startKey: Option[String] = None,
                         startKeyDocId: Option[String] = None,
                         updateSeq: Boolean = false)
                        (implicit database: Database, reads: Reads[T]): Future[ViewResult[T]] = {


    val map = Map[String, String]() +
      ("descending" -> descending.toString) +
      endKey.map {
        v => "endkey" -> v
      }.orElse(Some("" -> "")).get +
      endKeyDocId.map {
        v => "endkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      ("group" -> group.toString) +
      groupLevel.map {
        v => "group_level" -> v.toString
      }.orElse(Some("" -> "")).get +
      ("include_docs" -> includeDocs.toString) +
      ("inclusive_end" -> inclusiveEnd.toString) +
      key.map {
        v => "key" -> v
      }.orElse(Some("" -> "")).get +
      limit.map {
        v => "limit" -> v.toString
      }.orElse(Some("" -> "")).get +
      ("reduce" -> reduce.toString) +
      ("skip" -> skip.toString) +
      stale.map {
        v => "stale" -> v
      }.orElse(Some("" -> "")).get +
      startKey.map {
        v => "startkey" -> v
      }.orElse(Some("" -> "")).get +
      startKeyDocId.map {
        v => "startkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      ("update_seq" -> updateSeq.toString)


    for (res <- Requests.post(database.url + s"/_design/$designDocName/_view/$viewName",
      body = Json.stringify(Json.obj("keys" -> keys)),
      parameters = map,
      headers = Map("Content-Type" -> "application/json")
    ))
    yield {
      val json = Json.parse(res)

      ViewResult[T](
        (json \ "total_rows").as[Long],
        (json \ "rows").as[List[JsObject]].map {
          row =>
            ViewRow[T](
              (row \ "id").as[String],
              (row \ "key").as[String],
              (row \ "value").as[Vector[String]],
              (row \ "doc").asOpt[T]
            )
        }.toVector,
        (json \ "offset").as[Long]
      )
    }
  }

}
