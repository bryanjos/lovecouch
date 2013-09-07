package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

object Config {

  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @param couchDb
   * @return
   */
  def get()(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    val request = url(couchDb.url + s"/_config").GET
    val response = Http(request OK as.String)
    val result = for (res <- response) yield Json.parse(res)
    result
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @param couchDb
   * @return
   */
  def getSection(section:String)(implicit couchDb: CouchDb = CouchDb()): Future[JsValue] = {
    val request = url(couchDb.url + s"/_config/$section").GET
    val response = Http(request OK as.String)
    val result = for (res <- response) yield Json.parse(res)
    result
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def getSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    val request = url(couchDb.url + s"/_config/$section/$key").GET
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
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
    val request = url(couchDb.url + s"/_config/$section/$key").PUT << value
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def deleteSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb()): Future[String] = {
    val request = url(couchDb.url + s"/_config/$section/$key").DELETE
    val response = Http(request OK as.String)
    val result = for (res <- response) yield res
    result
  }

}
