package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.stream.StringsByLine

case class CouchDb(host: String = "127.0.0.1", port: Int = 5984) {
  def url: String = s"http://$host:$port"
}

object CouchDb {
  /**
   * Meta information about the instance
   * @param couchDb
   * @return
   */
  def info()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url))
    yield Json.parse(res)
  }

  /**
   * Currently running tasks
   * @param couchDb
   * @return
   */
  def activeTasks()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url + "/_active_tasks"))
    yield Json.parse(res)
  }

  /**
   * Returns a list of all the databases in the CouchDB instance
   * @param couchDb
   * @return
   */
  def allDbs()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url + "/_all_dbs"))
    yield Json.parse(res)
  }

  /**
   * Returns a list of all database events in the CouchDB instance.
   * @param feed
   * @param timeout
   * @param heartbeat
   * @param couchDb
   * @return
   */
  def updates(feed: String, timeout: Long = 60, heartbeat: Boolean = true,
                callBack: JsValue => Unit)
               (implicit couchDb: CouchDb = CouchDb()): Object with StringsByLine[Unit] = {
    Requests.getStream(couchDb.url + s"/_db_updates?feed=${feed}&timeout=$timeout&heartbeat=$heartbeat",
    line => callBack(Json.parse(line)))
  }

  /**
   * Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
   * @param couchDb
   * @param bytes
   * @param offset
   * @return
   */
  def log(bytes: Long = 1000, offset: Long = 0)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    for(res <- Requests.get(couchDb.url + s"/_log?bytes=$bytes&offset=$offset"))
    yield res
  }

  /**
   * Request, configure, or stop, a replication operation.
   * http://docs.couchdb.org/en/latest/api/misc.html#post-replicate
   * @param bytes
   * @param offset
   * @param replicationSpecification
   * @param couchDb
   * @return
   */
  def replicate(bytes: Long = 1000, offset: Long = 0, replicationSpecification: JsValue)
               (implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.post(couchDb.url + s"/_replicate?bytes=$bytes&offset=$offset", body = Json.stringify(replicationSpecification)))
    yield Json.parse(res)
  }

  /**
   * Restarts CouchDB
   * @param couchDb
   * @return
   */
  def restart()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.post(couchDb.url + s"/_restart", headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Returns a JSON object containing the statistics for the running server
   * @param couchDb
   * @return
   */
  def stats()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url + s"/_stats"))
    yield Json.parse(res)
  }

  /**
   * Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
   * @param couchDb
   * @param count
   * @return
   */
  def uuids(count: Int = 1)(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.post(couchDb.url + s"/_uuids?count=$count"))
    yield Json.parse(res)
  }
}
