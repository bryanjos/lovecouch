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
case class DocumentResult(ok:Boolean, id:String, rev:String)

object Database {
  def info(databaseName:String, server:Server=Server()): Future[Database] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").GET
    val response = Http(request OK as.String)
    val result = for (database <- response) yield parse[Database](database)
    result
  }

  def create(databaseName:String, server:Server=Server()): Future[Boolean] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").PUT
    val response = Http(request OK as.String)
    val result = for (createResult <- response) yield parse[DatabaseCreateResult](createResult).ok
    result
  }

  def putDocument[T](data:T, databaseName:String, server:Server=Server()): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName").PUT << generate(data)
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield parse[DocumentResult](documentResult)
    result
  }

  def getDocument[T](id:String, databaseName:String, server:Server=Server()): Future[T] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield parse[T](document)
    result
  }

  def getDocumentRevision[T](id:String, rev:String, databaseName:String, server:Server=Server()): Future[T] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id?rev=$rev").GET
    val response = Http(request OK as.String)
    val result = for (document <- response) yield parse[T](document)
    result
  }

  def deleteDocument[T](id:String, databaseName:String, server:Server=Server()): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield parse[DocumentResult](documentResult)
    result
  }

  def deleteDocument[T](id:String, rev:String, databaseName:String, server:Server=Server()): Future[DocumentResult] = {
    val request = url(s"http://${server.host}:${server.port}/$databaseName/$id?rev=$rev").DELETE
    val response = Http(request OK as.String)
    val result = for (documentResult <- response) yield parse[DocumentResult](documentResult)
    result
  }
}
