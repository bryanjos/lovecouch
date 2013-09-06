package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class Server(host:String = "127.0.0.1", port:Int = 5984) { def url:String = s"http://$host:$port" }
case class ServerInfo(couchdb:String, version:String, uuid:String, vendor:Vendor)
case class Vendor(version:String, name:String)

object Server {
      implicit val serverFmt = Json.format[Server]
      implicit val vendorFmt = Json.format[Vendor]
      implicit val serverInfofmt = Json.format[ServerInfo]

     def info(server:Server=Server()): Future[ServerInfo] = {
       val request = url(server.url).GET
       val response = Http(request OK as.String)
       val result = for (serverInfo <- response) yield Json.fromJson[ServerInfo](Json.parse(serverInfo)).get
       result
     }

    def uuids(server:Server=Server(), count:Int=1): Future[List[String]] = {
      val request = url(server.url + s"/_uuids?count=$count").GET
      val response = Http(request OK as.String)
      val result = for (uuids <- response) yield (Json.parse(uuids) \ "uuids").as[List[String]]
      result
    }
}
