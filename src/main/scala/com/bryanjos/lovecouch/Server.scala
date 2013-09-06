package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import com.codahale.jerkson.Json._
import dispatch.{url, Http, as}
import com.codahale.jerkson.JsonSnakeCase

case class Server(host:String = "127.0.0.1", port:Int = 5984)
case class ServerInfo(couchdb:String, version:String, uuid:String, vendor:Vendor)
case class Vendor(version:String, name:String)

@JsonSnakeCase
case class Database(dbName:String, docCount:Int, docDelCount:Long, updateSeq:Long, purgeSeq:Long, compactRunning:Boolean,
                     diskSize:Long, dataSize:Long, instanceStartTime:String, diskFormatVersion:Int, commitedUpdateSeq:Long)

object Server {
     def info(server:Server=Server()): Future[ServerInfo] = {
       val request = url(s"http://${server.host}:${server.port}").GET
       val response = Http(request OK as.String)
       val futureServer = for (serverInfo <- response) yield parse[ServerInfo](serverInfo)
       futureServer
     }

    def getDatabase(databaseName:String, server:Server=Server()): Future[Database] = {
      val request = url(s"http://${server.host}:${server.port}/$databaseName").GET
      val response = Http(request OK as.String)
      val futureServer = for (database <- response) yield parse[Database](database)
      futureServer
    }






}
