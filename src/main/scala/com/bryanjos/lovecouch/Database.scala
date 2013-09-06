package com.bryanjos.lovecouch

import com.codahale.jerkson.JsonSnakeCase
import scala.concurrent._
import ExecutionContext.Implicits.global
import com.codahale.jerkson.Json._
import dispatch.{url, Http, as}


@JsonSnakeCase
case class Database(dbName:String, docCount:Int, docDelCount:Long,
                    updateSeq:Long, purgeSeq:Long, compactRunning:Boolean,
                    diskSize:Long, dataSize:Long, instanceStartTime:String,
                    diskFormatVersion:Int, commitedUpdateSeq:Long)

private case class DatabaseCreateResult(ok:Boolean)

object Database {
  def create(databaseName:String, server:Server=Server()): Future[Boolean] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").PUT
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield parse[DatabaseCreateResult](createResult).ok
    result
  }

  def get(databaseName:String, server:Server=Server()): Future[Database] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").GET
    val response = Http(request OK as.String)
    val result = for (database <- response) yield parse[Database](database)
    result
  }
}
