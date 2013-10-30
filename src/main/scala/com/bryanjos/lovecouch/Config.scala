package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import akka.actor.ActorSystem

case class Config(url:String)(implicit system:ActorSystem, context:ExecutionContext) {

  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @return
   */
  def get(): Future[JsValue] = {
    Requests.get(url + "/_config").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @return
   */
  def getSection(section:String): Future[JsValue] = {
    Requests.get(url + s"/_config/$section").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @return
   */
  def getSectionKey(section:String, key:String): Future[String] = {
    Requests.get(url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Updates a configuration value.
   * @param section
   * @param key
   * @param value
   * @return
   */
  def putSectionKey(section:String, key:String, value:String): Future[String] = {
    Requests.put(url + s"/_config/$section/$key", body = value).map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @return
   */
  def deleteSectionKey(section:String, key:String): Future[String] = {
    Requests.get(url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

}
