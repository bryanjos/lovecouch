package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import ExecutionContext.Implicits.global
import scala.util.Try

object Config {

  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @param couchDb
   * @return
   */
  def get()(implicit couchDb: CouchDb = CouchDb()): Future[Try[JsValue]] = {
    for(res <- Requests.get(couchDb.url + s"/_config"))
    yield Try(Json.parse(res.get))
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @param couchDb
   * @return
   */
  def getSection(section:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[JsValue]] = {
    for(res <- Requests.get(couchDb.url + s"/_config/$section"))
    yield Try(Json.parse(res.get))
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def getSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]] = {
    for(res <- Requests.get(couchDb.url + s"/_config/$section/$key"))
    yield Try(res.get)
  }

  /**
   * Updates a configuration value.
   * @param section
   * @param key
   * @param value
   * @param couchDb
   * @return
   */
  def putSectionKey(section:String, key:String, value:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]] = {
    for(res <- Requests.put(couchDb.url + s"/_config/$section/$key", body = value))
    yield Try(res.get)
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def deleteSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[Try[String]] = {
    for(res <- Requests.delete(couchDb.url + s"/_config/$section/$key"))
    yield Try(res.get)
  }

}
