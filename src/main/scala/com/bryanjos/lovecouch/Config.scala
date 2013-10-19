package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import akka.actor.ActorSystem

object Config {

  /**
   * Returns the entire CouchDB server configuration as a JSON structure.
   * The structure is organized by different configuration sections, with individual values.
   * @param couchDb
   * @return
   */
  def get()(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem, context:ExecutionContext): Future[JsValue] = {
    Requests.get(couchDb.url + "/_config").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets the configuration structure for a single section.
   * @param section
   * @param couchDb
   * @return
   */
  def getSection(section:String)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem, context:ExecutionContext): Future[JsValue] = {
    Requests.get(couchDb.url + s"/_config/$section").map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Gets a single configuration value from within a specific configuration section.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def getSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem, context:ExecutionContext): Future[String] = {
    Requests.get(couchDb.url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Updates a configuration value.
   * @param section
   * @param key
   * @param value
   * @param couchDb
   * @return
   */
  def putSectionKey(section:String, key:String, value:String)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem, context:ExecutionContext): Future[String] = {
    Requests.put(couchDb.url + s"/_config/$section/$key", body = value).map{
      response =>
        Requests.processStringResponse(response)
    }
  }

  /**
   * Deletes a configuration value.
   * @param section
   * @param key
   * @param couchDb
   * @return
   */
  def deleteSectionKey(section:String, key:String)(implicit couchDb: CouchDb = CouchDb(), system:ActorSystem, context:ExecutionContext): Future[String] = {
    Requests.get(couchDb.url + s"/_config/$section/$key").map{
      response =>
        Requests.processStringResponse(response)
    }
  }

}
