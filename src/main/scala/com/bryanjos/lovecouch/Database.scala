package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json._
import spray.http.HttpEntity
import akka.actor.ActorSystem

case class Database(name: String, couchDbUrl: String)(implicit system:ActorSystem, context:ExecutionContext) {
  import Implicits._

  def url: String = s"$couchDbUrl/$name"

  /**
   * Gets information about the specified database.

   * @return
   */
  def info(): Future[DatabaseInfo] = {
    Requests.get(url).map{
      response =>
        Requests.processObjectResponse[DatabaseInfo](response)
    }
  }

  /**
   * Request compaction of the specified database.
   * @param designDocName Optionally compacts the view indexes associated with the specified design document.

   * @return
   */
  def compact(designDocName: Option[String] = None): Future[Boolean] = {
    Requests.post(url + "/_compact" + designDocName.map(d => s"/$designDocName").orElse(Some("")).get).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }

  /**
   * Cleans up the cached view output on disk for a given view.

   * @return
   */
  def viewCleanUp(): Future[Boolean] = {
    Requests.post(url + "/_view_cleanup").map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }
  /**
   * Commits any recent changes to the specified database to disk.

   * @return
   */
  def ensureFullCommit(): Future[EnsureFullCommitResult] = {
    Requests.post(url + "/_ensure_full_commit").map{
      response =>
        Requests.processObjectResponse[EnsureFullCommitResult](response)
    }
  }

  /**
   * Allows you to create and update multiple documents at the same time within a single request.
   * @param docs

   * @return
   */
  def bulkDocs[T](docs:Seq[T])(implicit writes: Writes[T]): Future[JsValue] = {
    val json = Json.obj("docs" -> Json.toJson(docs))

    Requests.post(url + "/_bulk_docs", body=Json.stringify(json)).map{
      response =>
        Requests.processJsonResponse(response)
    }
  }

  /**
   * Creates (and executes) a temporary view based on the view function supplied in the JSON request.
   * @param view

   * @return
   */
  def tempView(view:TempView): Future[JsValue] = {
    Requests.post(url + "/_temp_view", body=Json.stringify(Json.toJson(view))).map{
      response =>
        Requests.processJsonResponse(response)
    }
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

   * @return
   */
  def allDocs(descending:Boolean=false,
              endKey:Option[String]=None,
              endKeyDocId:Option[String]=None,
              group:Boolean=false,
              groupLevel:Option[Long]=None,
              includeDocs:Boolean=false,
              inclusiveEnd:Boolean = true,
              key:Option[String]=None,
              limit:Option[Long]=None,
              reduce:Boolean=true,
              skip:Long=0,
              stale:Option[String]=None,
              startKey:Option[String]=None,
              startKeyDocId:Option[String]=None)
             : Future[AllDocsResult] = {


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


    Requests.get(url + "/_all_docs", queryParameters=map).map{
      response =>
        Requests.processObjectResponse[AllDocsResult](response)
    }
  }

  /**
   * The POST to _all_docs allows to specify multiple keys to be selected from the database.
   * @param keys

   * @return
   */
  def allDocs(keys:Vector[String]): Future[AllDocsResult] = {
    Requests.post(url + "/_all_docs",
      body= Json.stringify(Json.obj("keys" -> keys))).map{
      response =>
        Requests.processObjectResponse[AllDocsResult](response)
    }
  }


  /**
   * Gets the current security object from the specified database.

   * @return
   */
  def security(): Future[Security] = {
    Requests.get(url + "/_security").map{
      response =>
        Requests.processObjectResponse[Security](response)
    }
  }

  /**
   * Sets the security object for the given database.
   * @param security

   * @return
   */
  def setSecurity(security: Security): Future[Boolean] = {
    Requests.post(url + "/_security",
      body= Json.stringify(Json.toJson(security))).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }


  /**
   * Gets the current revs_limit (revision limit) setting.

   * @return
   */
  def revsLimit(): Future[Int] = {
    Requests.get(url + "/_revs_limit").map{
      response =>
        Requests.processIntResponse(response)
    }
  }


  /**
   * Sets the maximum number of document revisions that will be tracked by CouchDB, even after compaction has occurred.
   * @param limit

   * @return
   */
  def setRevsLimit(limit: Int): Future[Boolean] = {
    Requests.put(url + s"/_revs_limit",
      body= limit.toString).map{
      response =>
        Requests.processBooleanResponse(response)
    }
  }




  /**
   * Create a new document in the specified database.
   * If @data includes the _id field, then the document will be created with the specified document ID.
   * If the _id field is not specified, a new unique ID will be generated.
   * @return
   */
  def createDocument[T](doc:T)(implicit writes: Writes[T]): Future[DocumentResult] = {
    Requests.post(url, body = Json.stringify(Json.toJson[T](doc))).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Returns the specified doc
   * @param id
   * @param rev
   * @return
   */
  def getDocument[T](id:String, rev:Option[String] = None, local:Boolean=false)
            (implicit reads: Reads[T]): Future[T] = {
    val urlA = url.concat(if(local){ s"/_local/$id" } else { s"/$id" })
    Requests.get(urlA, queryParameters = Map(rev.map{r => "rev"-> r}.orElse(Some(""->"")).get) - "").map{
      response =>
        Requests.processObjectResponse[T](response)
    }
  }


  /**
   * creates a new named document, or creates a new revision of the existing document.
   * @param doc
   * @param id
   * @return
   */
  def updateDocument[T](doc:T, id:String, local:Boolean=false)(implicit writes: Writes[T]): Future[DocumentResult] = {

    val urlA = url.concat(if(local){ s"/_local/$id" } else { s"/$id" })
    Requests.put(urlA, body = Json.stringify(Json.toJson[T](doc))).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Deletes the specified document from the database.
   * @param id
   * @param rev
   * @return
   */
  def deleteDocument(id:String, rev:String, local:Boolean=false): Future[DocumentResult] = {
    val urlA = url.concat(if(local){ s"/_local/$id" } else { s"/$id" })
    Requests.delete(urlA, queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Returns the specified design document
   * @param id
   * @param rev
   * @return
   */
  def getDesignDocument(id: String, rev: Option[String] = None): Future[DesignDocument] = {
    Requests.get(url + s"/$id", queryParameters = Map(rev.map { r => "rev" -> r }.orElse(Some("" -> "")).get) - "").map{
      response =>
        Requests.processResponse[DesignDocument](response,
          (e:HttpEntity) => {
            val json = Json.parse(e.asString)

            DesignDocument(
              _id = (json \ "_id").asOpt[String],
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
          })
    }
  }


  def getDesignDocumentInfo(id:String): Future[ViewInfo] = {
    Requests.put(url + s"/$id/_info").map{
      response =>
        Requests.processObjectResponse[ViewInfo](response)
    }
  }


  /**
   * Upload the specified design document
   * @param designDocument
   * @return
   */
  def addOrUpdateDesignDocument(designDocument:DesignDocument): Future[DocumentResult] = {
    val json = Json.obj(
      "_id" -> designDocument._id,
      "language" -> designDocument.language,
      "views" -> Json.toJson(designDocument.views.map
        {
          view =>
            if(view.reduce.isEmpty)
              view.name -> Json.obj("map" -> view.map)
            else
              view.name -> Json.obj("map" -> view.map, "reduce" -> view.reduce.get)
        }.toMap)
    )


    Requests.put(url + s"/${designDocument._id.get}", body = Json.stringify(json)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }

  /**
   * Delete an existing design document
   * @param id
   * @param rev
   * @return
   */
  def deleteDesignDocument(id: String, rev: String): Future[DocumentResult] = {
    Requests.delete(url + s"/$id",
      queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Returns the file attachment attachment associated with the document id.
   * @param id
   * @param attachmentName
   * @return
   */
  def getAttachment(id:String, attachmentName:String): Future[Array[Byte]] = {
    Requests.get(url + s"/$id/$attachmentName").map{
      response =>
        Requests.processBinaryResponse(response)
    }
  }

  /**
   * Upload the supplied content as an attachment to the specified document
   * @param id
   * @param rev
   * @param attachmentName
   * @param attachment
   * @param mimeType
   * @return
   */
  def addAttachment(id:String, rev:String, attachmentName:String, attachment:java.io.File, mimeType:String): Future[DocumentResult] = {
    Requests.putFile(url + s"/$id/$attachmentName", file=attachment,
      queryParameters = Map("rev"-> rev),
      headers = Map() + ("Mime-Type" -> mimeType)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Deletes the attachment attachment to the specified id.
   * @param id
   * @param rev
   * @param attachmentName
   * @return
   */
  def deleteAttachment(id:String, rev:String, attachmentName:String): Future[DocumentResult] = {

    Requests.delete(url + s"/$id/$attachmentName", queryParameters = Map("rev"-> rev)).map{
      response =>
        Requests.processObjectResponse[DocumentResult](response)
    }
  }


  /**
   * Executes the specified view-name from the specified design-doc design document.
   * @param viewName
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
   * @param updateSeq
   * @return
   */
  def executeView(id:String, viewName: String, keys: Vector[String] = Vector[String](),
                  descending: Option[Boolean] = None,
                  endKey: Option[String] = None,
                  endKeyDocId: Option[String] = None,
                  group: Option[Boolean] = None,
                  groupLevel: Option[Long] = None,
                  includeDocs: Option[Boolean] = None,
                  inclusiveEnd: Option[Boolean] = None,
                  key: Option[String] = None,
                  limit: Option[Long] = None,
                  reduce: Option[Boolean] = None,
                  skip: Option[Long] = None,
                  stale: Option[String] = None,
                  startKey: Option[String] = None,
                  startKeyDocId: Option[String] = None,
                  updateSeq: Option[Boolean] = None): Future[ViewResult] = {

    val map = Map[String, String]() +
      descending.map {
        v => "descending" -> v.toString
      }.orElse(Some("" -> "")).get +
      endKey.map {
        v => "endkey" -> v
      }.orElse(Some("" -> "")).get +
      endKeyDocId.map {
        v => "endkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      group.map {
        v => "group" -> v.toString
      }.orElse(Some("" -> "")).get +
      groupLevel.map {
        v => "group_level" -> v.toString
      }.orElse(Some("" -> "")).get +
      includeDocs.map {
        v => "include_docs" -> v.toString
      }.orElse(Some("" -> "")).get +
      inclusiveEnd.map {
        v => "inclusive_end" -> v.toString
      }.orElse(Some("" -> "")).get +
      key.map {
        v => "key" -> v
      }.orElse(Some("" -> "")).get +
      limit.map {
        v => "limit" -> v.toString
      }.orElse(Some("" -> "")).get +
      reduce.map {
        v => "reduce" -> v.toString
      }.orElse(Some("" -> "")).get +
      skip.map {
        v => "skip" -> v.toString
      }.orElse(Some("" -> "")).get +
      stale.map {
        v => "stale" -> v
      }.orElse(Some("" -> "")).get +
      startKey.map {
        v => "startkey" -> v
      }.orElse(Some("" -> "")).get +
      startKeyDocId.map {
        v => "startkey_docid" -> v
      }.orElse(Some("" -> "")).get +
      updateSeq.map {
        v => "update_seq" -> v.toString
      }.orElse(Some("" -> "")).get - ""


    val renderViewResult = (e:HttpEntity) => {
      val json = Json.parse(e.asString)

      ViewResult(
        (json \ "total_rows").as[Long],
        (json \ "rows").as[List[JsObject]].map {
          row =>
            ViewRow(
              (row \ "id").as[String],
              (row \ "key").asOpt[String],
              (row \ "value").as[JsValue]
            )
        }.toVector,
        (json \ "offset").as[Long]
      )
    }

    if(keys.isEmpty){
      Requests.get(url + s"/$id/_view/$viewName", queryParameters = map).map{
        response =>
          Requests.processResponse[ViewResult](response,renderViewResult)
      }
    }else{
      Requests.post(url + s"/$id/_view/$viewName",
        body = Json.stringify(Json.obj("keys" -> keys)),
        queryParameters = map
      ).map{
        response =>
          Requests.processResponse[ViewResult](response,renderViewResult)
      }
    }
  }
}
