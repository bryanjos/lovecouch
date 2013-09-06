package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import com.codahale.jerkson.Json._
import dispatch.{url, Http, as}

case class Vendor(version:String, name:String)
case class Server(host:String, port:Int, couchdb:String, version:String, uuid:String, vendor:Vendor )

object Server {
     def get(host:String="127.0.0.1", port:Int=5984): Future[Server] = {
       val response = Http(url(s"http://$host:$port") OK as.String)
       val futureServer = for (c <- response) yield parse[Server](c).copy(host=host, port=port)
       futureServer
     }



}
