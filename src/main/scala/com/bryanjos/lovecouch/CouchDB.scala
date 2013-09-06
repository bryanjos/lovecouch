package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class CouchDb(host:String = "127.0.0.1", port:Int = 5984) { def url:String = s"http://$host:$port" }
case class CouchDbInfo(couchdb:String, version:String, uuid:String, vendor:Vendor)
case class Vendor(version:String, name:String)

object CouchDb {
     implicit val vendorFmt = Json.format[Vendor]
     implicit val couchDbInfoFmt = Json.format[CouchDbInfo]

    /**
     * Meta information about the instance
     * @param couchDb
     * @return
     */
     def info()(implicit couchDb:CouchDb=CouchDb()): Future[CouchDbInfo] = {
       val request = url(couchDb.url).GET
       val response = Http(request OK as.String)
       val result = for (serverInfo <- response) yield Json.fromJson[CouchDbInfo](Json.parse(serverInfo)).get
       result
     }

    /**
     * Gets the CouchDB log, equivalent to accessing the local log file of the corresponding CouchDB instance.
     * @param couchDb
     * @param bytes
     * @param offset
     * @return
     */
    def log(bytes:Long=1000, offset:Long=0)(implicit couchDb:CouchDb=CouchDb()): Future[String] = {
      val request = url(couchDb.url + s"/_log?bytes=$bytes&offset=$offset").GET
      val response = Http(request OK as.String)
      val result = for (log <- response) yield log
      result
    }

    /**
     * Requests one or more Universally Unique Identifiers (UUIDs) from the CouchDB instance
     * @param couchDb
     * @param count
     * @return
     */
    def uuids(count:Int=1)(implicit couchDb:CouchDb=CouchDb()): Future[List[String]] = {
      val request = url(couchDb.url + s"/_uuids?count=$count").GET
      val response = Http(request OK as.String)
      val result = for (uuids <- response) yield (Json.parse(uuids) \ "uuids").as[List[String]]
      result
    }
}
