package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._

object Document {

  /**
   * Create a new document in the specified database.
   * If @data includes the _id field, then the document will be created with the specified document ID.
   * If the _id field is not specified, a new unique ID will be generated.
   * @param database
   * @return
   */
  def post(json:JsValue)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.post(database.url, body = Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }

  /**
   * Returns the specified doc
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def get(id:String, rev:Option[String] = None)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.get(database.url + s"/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.parse(res)
  }

  /**
   * creates a new named document, or creates a new revision of the existing document.
   * @param database
   * @return
   */
  def put(json:JsValue)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.put(database.url, body = Json.stringify(json), headers = Map("Content-Type" -> "application/json")))
    yield Json.parse(res)
  }


  /**
   * Deletes the specified document from the database.
   * @param id
   * @param rev
   * @param database
   * @return
   */
  def delete(id:String, rev:Option[String] = None)(implicit database:Database): Future[JsValue] = {
    for(res <- Requests.delete(database.url + s"/$id", parameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - ""))
    yield Json.parse(res)
  }
}
