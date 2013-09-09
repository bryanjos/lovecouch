package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import ExecutionContext.Implicits.global

object Config {

  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @param couchDb
   * @return
   */
  def get()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url + s"/_config"))
    yield Json.parse(res)
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @param couchDb
   * @return
   */
  def getSection(section:String)(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    for(res <- Requests.get(couchDb.url + s"/_config/$section"))
    yield Json.parse(res)
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def getSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    for(res <- Requests.get(couchDb.url + s"/_config/$section/$key"))
    yield res
  }

  /**
   * Updates a configuration value.
   * @param section
   * @param key
   * @param value
   * @param couchDb
   * @return
   */
  def putSectionKey(section:String, key:String, value:String)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    for(res <- Requests.put(couchDb.url + s"/_config/$section/$key", body = value))
    yield res
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def deleteSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    for(res <- Requests.delete(couchDb.url + s"/_config/$section/$key"))
    yield res
  }

}
