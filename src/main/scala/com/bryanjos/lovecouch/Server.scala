package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json._
import dispatch.{url, Http, as}

case class Server(host:String = "127.0.0.1", port:Int = 5984)
case class ServerInfo(couchdb:String, version:String, uuid:String, vendor:Vendor)

case class Vendor(version:String, name:String)


case class UUIDs(uuids: List[String])

object Server {
      implicit val serverFmt = Json.format[Server]
      implicit val vendorFmt = Json.format[Vendor]
      implicit val serverInfofmt = Json.format[ServerInfo]
      implicit val uuidsFmt = Json.format[UUIDs]

     def info(server:Server=Server()): Future[ServerInfo] = {
       val request = url(s"http://${server.host}:${server.port}").GET
       val response = Http(request OK as.String)
       val result = for (serverInfo <- response) yield Json.fromJson[ServerInfo](Json.parse(serverInfo)).get
       result
     }

    def uuids(server:Server=Server(), count:Int=1): Future[UUIDs] = {
      val request = url(s"http://${server.host}:${server.port}/_uuids?count=$count").GET
      val response = Http(request OK as.String)
      val result = for (uuids <- response) yield Json.fromJson[UUIDs](Json.parse(uuids)).get
      result
    }
}
