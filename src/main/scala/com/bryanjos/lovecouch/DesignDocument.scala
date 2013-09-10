package com.bryanjos.lovecouch

import scala.concurrent.Future
import play.api.libs.json.{Json, JsValue}

object DesignDocument {

  /**
   * Returns the specified design document
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def get(id:String, rev:Option[String] = None)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.get(database.url + s"/_design/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.parse(res)
  }

  /**
   * Upload the specified design document
   * @param json
   * @param id
   * @param database
   * @return
   */
  def put(json:JsValue, id:String)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.put(database.url + s"/_design/$id", body = Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Delete an existing design document
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id:String, rev:Option[String] = None)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.put(database.url + s"/_design/$id",
      parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - "",
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   * Returns the file attachment attachment associated with the design document
   * @param id
   * @param attachmentName
   * @param database
   * @return
   */
  def getAttachment(id:String, attachmentName:String)(implicit database:Database): Future[Array[Byte]] = {
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
  def putAttachment(id:String, rev:String, attachmentName:String, attachment:java.io.File, mimeType:String)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.putFile(database.url + s"_design/$id/$attachmentName", file=attachment,
      parameters = Map("rev"-> rev),
      headers = Map() + ("Content-Length" -> attachment.length().toString) + ("Mime-Type" -> mimeType)))
    yield Json.parse(res)
  }

  /**
   * Deletes the attachment
   * @param id
   * @param rev
   * @param attachmentName
   * @param database
   * @return
   */
  def deleteAttachment(id:String, rev:String, attachmentName:String)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.delete(database.url + s"/_design/$id/$attachmentName", parameters = Map("rev"-> rev)))
    yield Json.parse(res)
  }


  /**
   * Obtains information about a given design document
   * @param id
   * @param database
   * @return
   */
  def info(id:String)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.put(database.url + s"/_design/$id/_info", headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  def executeView(designDocName:String, viewName:String, descending:Boolean=false, endKey:Option[String]=None,
              endKeyDocId:Option[String]=None, group:Boolean=false,
              groupLevel:Option[Long]=None, includeDocs:Boolean=false,
              inclusiveEnd:Boolean = true, key:Option[String]=None,
              limit:Option[Long]=None, reduce:Boolean=true, skip:Long=0,
              stale:Option[String]=None, startKey:Option[String]=None,
              startKeyDocId:Option[String]=None, updateSeq:Boolean=false)
             (implicit database: Database): Future[JsValue] = {


    val map = Map[String, String]() +
      ("descending" -> descending.toString) +
      endKey.map { v => "endkey" -> v }.orElse(Some("" -> "")).get +
      endKeyDocId.map { v => "endkey_docid" -> v }.orElse(Some("" -> "")).get +
      ("group" -> group.toString) +
      groupLevel.map { v => "group_level" -> v.toString }.orElse(Some("" -> "")).get +
      ("include_docs" -> includeDocs.toString) +
      ("inclusive_end" -> inclusiveEnd.toString) +
      key.map { v => "key" -> v }.orElse(Some("" -> "")).get +
      limit.map { v => "limit" -> v.toString }.orElse(Some("" -> "")).get +
      ("reduce" -> reduce.toString) +
      ("skip" -> skip.toString) +
      stale.map { v => "stale" -> v }.orElse(Some("" -> "")).get +
      startKey.map { v => "startkey" -> v }.orElse(Some("" -> "")).get +
      startKeyDocId.map { v => "startkey_docid" -> v }.orElse(Some("" -> "")).get +
      ("update_seq" -> updateSeq.toString)


    for(res <- Requests.get(database.url + s"/_design/$designDocName/_view/$viewName", parameters=map, headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  def executeViewPost(designDocName:String, viewName:String, json:JsValue, descending:Boolean=false, endKey:Option[String]=None,
                  endKeyDocId:Option[String]=None, group:Boolean=false,
                  groupLevel:Option[Long]=None, includeDocs:Boolean=false,
                  inclusiveEnd:Boolean = true, key:Option[String]=None,
                  limit:Option[Long]=None, reduce:Boolean=true, skip:Long=0,
                  stale:Option[String]=None, startKey:Option[String]=None,
                  startKeyDocId:Option[String]=None, updateSeq:Boolean=false)
                 (implicit database: Database): Future[JsValue] = {


    val map = Map[String, String]() +
      ("descending" -> descending.toString) +
      endKey.map { v => "endkey" -> v }.orElse(Some("" -> "")).get +
      endKeyDocId.map { v => "endkey_docid" -> v }.orElse(Some("" -> "")).get +
      ("group" -> group.toString) +
      groupLevel.map { v => "group_level" -> v.toString }.orElse(Some("" -> "")).get +
      ("include_docs" -> includeDocs.toString) +
      ("inclusive_end" -> inclusiveEnd.toString) +
      key.map { v => "key" -> v }.orElse(Some("" -> "")).get +
      limit.map { v => "limit" -> v.toString }.orElse(Some("" -> "")).get +
      ("reduce" -> reduce.toString) +
      ("skip" -> skip.toString) +
      stale.map { v => "stale" -> v }.orElse(Some("" -> "")).get +
      startKey.map { v => "startkey" -> v }.orElse(Some("" -> "")).get +
      startKeyDocId.map { v => "startkey_docid" -> v }.orElse(Some("" -> "")).get +
      ("update_seq" -> updateSeq.toString)


    for(res <- Requests.post(database.url + s"/_design/$designDocName/_view/$viewName", body=Json.stringify(json), parameters=map, headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

}
