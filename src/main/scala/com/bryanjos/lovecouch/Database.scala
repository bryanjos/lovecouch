package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.stream.StringsByLine


case class Database(name: String, couchDb: CouchDb = CouchDb()) {
  def url: String = couchDb.url + s"/$name"
}

object Database {

  /**
   * Gets information about the specified database.
   * @param database
   * @return
   */
  def info()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.get(database.url))
    yield Json.parse(res)
  }

  /**
   * Creates a new database.
   * @param database
   * @return
   */
  def create()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.put(database.url))
    yield Json.parse(res)
  }

  /**
   * Deletes the specified database, and all the documents and attachments contained within it.
   * @param database
   * @return
   */
  def delete()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.delete(database.url))
    yield Json.parse(res)
  }


  /**
   * Obtains a list of the changes made to the database.
   * @param docIds
   * @param feed
   * @param filter
   * @param heartBeat
   * @param includeDocs
   * @param limit
   * @param since
   * @param callBack
   * @param database
   * @return
   */
  def changes(docIds: Option[List[String]] = None, feed: FeedTypes.FeedTypes = FeedTypes.Normal,
              filter: Option[String], heartBeat: Long = 6000, includeDocs: Boolean = false,
              limit: Option[Long] = None, since: Long = 0, callBack: JsValue => Unit)
             (implicit database: Database): Object with StringsByLine[Unit] = {

    val d = docIds.map {
      ids => "doc_ids" -> Json.stringify(Json.toJson(ids))
    }.orElse(Some("" -> "")).get
    val fr = filter.map {
      fil => "filter" -> fil
    }.orElse(Some("" -> "")).get
    val lt = limit.map {
      ids => "limit" -> ids.toString
    }.orElse(Some("" -> "")).get
    val f = "feed" -> feed.toString
    val hb = "heartbeat" -> heartBeat.toString
    val id = "include_docs" -> includeDocs.toString
    val sn = "since" -> since.toString

    val map = Map() + d + fr + lt + f + hb + id + sn - ""
    Requests.getStream(database.url + "/_changes", line => callBack(Json.parse(line)), parameters = map)
  }

  /**
   * Request compaction of the specified database.
   * @param designDocName Optionally compacts the view indexes associated with the specified design document.
   * @param database
   * @return
   */
  def compact(designDocName: Option[String] = None)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_compact" + designDocName.map(d => s"/$designDocName").orElse(Some("")).get,
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Cleans up the cached view output on disk for a given view.
   * @param database
   * @return
   */
  def viewCleanUp()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_view_cleanup", headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Commits any recent changes to the specified database to disk.
   * @param database
   * @return
   */
  def ensureFullCommit()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_ensure_full_commit", headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   * Allows you to create and update multiple documents at the same time within a single request.
   * @param json
   * @param database
   * @return
   */
  def bulkDocs(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_bulk_docs", body=Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Creates (and executes) a temporary view based on the view function supplied in the JSON request.
   * @param json
   * @param database
   * @return
   */
  def tempView(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_temp_view", body=Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Permanently removes the references to deleted documents from the database.
   * @param json
   * @param database
   * @return
   */
  def purge(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + "/_purge", body=Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Returns a JSON structure of all of the documents in a given database.
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
   * @param database
   * @return
   */
  def allDocs(descending:Boolean=false, endKey:Option[String]=None,
              endKeyDocId:Option[String]=None, group:Boolean=false,
              groupLevel:Option[Long]=None, includeDocs:Boolean=false,
               inclusiveEnd:Boolean = true, key:Option[String]=None,
               limit:Option[Long]=None, reduce:Boolean=true, skip:Long=0,
               stale:Option[String]=None, startKey:Option[String]=None,
               startKeyDocId:Option[String]=None)
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
      startKeyDocId.map { v => "startkey_docid" -> v }.orElse(Some("" -> "")).get


    for(res <- Requests.get(database.url + "/_all_docs", parameters=map, headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * The POST to _all_docs allows to specify multiple keys to be selected from the database.
   * @param json
   * @param database
   * @return
   */
  def postAllDocs(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + s"/_all_docs",
      body= Json.stringify(json),
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   *
   * @param json
   * @param database
   * @return
   */
  def missingRevisions(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + s"/_missing_revs",
      body= Json.stringify(json),
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   *
   * @param json
   * @param database
   * @return
   */
  def revisionDiff(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + s"/_revs_diff",
      body= Json.stringify(json),
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   * Gets the current security object from the specified database.
   * @param database
   * @return
   */
  def security()(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.get(database.url + s"/_security"))
    yield Json.parse(res)
  }

  /**
   * Sets the security object for the given database.
   * @param json
   * @param database
   * @return
   */
  def setSecurity(json:JsValue)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.post(database.url + s"/_security",
      body= Json.stringify(json),
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   * Gets the current revs_limit (revision limit) setting.
   * @param database
   * @return
   */
  def revsLimit()(implicit database: Database): Future[Int] = {
    for(res <- Requests.get(database.url + s"/_revs_limit",
      headers = Map("Content-Type" -> "application/json")))
    yield res.toInt
  }


  /**
   * Sets the maximum number of document revisions that will be tracked by CouchDB, even after compaction has occurred.
   * @param limit
   * @param database
   * @return
   */
  def setRevsLimit(limit: Int)(implicit database: Database): Future[JsValue] = {
    for(res <- Requests.put(database.url + s"/_revs_limit",
      body= limit.toString,
      headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }
}
