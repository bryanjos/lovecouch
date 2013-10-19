package com.bryanjos.lovecouch

import scala.concurrent._
import ExecutionContext.Implicits.global
import dispatch.{Http, as}
import dispatch.stream.StringsByLine
import scala.util.Try
import akka.actor.ActorSystem
import play.api.libs.json.{Reads, Json}


object Requests {
  def buildQueryParameters(req:dispatch.Req, parameters:Seq[(String,String)]):dispatch.Req = {
    if(parameters.size == 0)
      return req

    buildQueryParameters(req.addQueryParameter(parameters.head._1, parameters.head._2), parameters.tail)
  }

  def buildHeaders(req:dispatch.Req, headers:Seq[(String,String)]):dispatch.Req = {
    if(headers.size == 0)
      return req

    buildHeaders(req.addHeader(headers.head._1, headers.head._2), headers.tail)
  }

  def buildBody(req:dispatch.Req, body:String):dispatch.Req = {
    if(body.size == 0)
      return req

    req << body
  }



  def get(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[String]] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).GET, parameters.toSeq), headers.toSeq)
    val response = dispatch.Http(request OK as.String)
    for (res <- response) yield Try(res)
  }

  def getBytes(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[Array[Byte]]] = {
      val request = buildHeaders(buildQueryParameters(dispatch.url(url).GET, parameters.toSeq), headers.toSeq)
      val response = dispatch.Http(request OK as.Bytes)
      for (res <- response) yield Try(res)
  }

  def getStream(url:String, callback:String => Unit, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Object with StringsByLine[Unit] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).GET, parameters.toSeq), headers.toSeq)
    val callBacker = as.stream.Lines(callback)
    Http(request > callBacker)
    callBacker
  }

  def post(url:String, body:String = "", parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[String]] = {
    val request = buildBody(buildHeaders(buildQueryParameters(dispatch.url(url).POST, parameters.toSeq), headers.toSeq), body)
    val response = dispatch.Http(request OK as.String)
    for (res <- response) yield Try(res)
  }

  def put(url:String, body:String = "", parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[String]] = {
    val request = buildBody(buildHeaders(buildQueryParameters(dispatch.url(url).PUT, parameters.toSeq), headers.toSeq), body)
    val response = dispatch.Http(request OK as.String)
    for (res <- response) yield Try(res)
  }

  def putFile(url:String, file:java.io.File, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[String]] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).PUT, parameters.toSeq), headers.toSeq) <<< file
    val response = dispatch.Http(request OK as.String)
    for (res <- response) yield Try(res)
  }

  def delete(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[Try[String]] = {
    val request = buildHeaders(buildQueryParameters(dispatch.url(url).DELETE, parameters.toSeq), headers.toSeq)
    val response = dispatch.Http(request OK as.String)
    for (res <- response) yield Try(res)
  }

}



object SprayRequests {
  import spray.http._
  import spray.client.pipelining._
  import akka.actor.ActorSystem


  private def sendRequest(request:HttpRequest)(implicit system:ActorSystem) = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive(system, system.dispatcher)
    pipeline(request)
  }

  private def buildUrl(url:String, queryParameters:Map[String,String]):String = {
    if(queryParameters.isEmpty)
      url
    else if(!url.contains("?"))
      buildUrl(s"$url?${queryParameters.head._1 }=${queryParameters.head._2}", queryParameters.tail)
    else
      buildUrl(s"$url&${queryParameters.head._1 }=${queryParameters.head._2}", queryParameters.tail)
  }

  private def buildHeaders(headers:Map[String, String]):List[HttpHeader] = {
    if(headers.isEmpty)
      List[HttpHeader]()
    else{
      headers.map{
        e => {
          HttpHeaders.RawHeader(e._1,e._2)
        }
      }.toList
    }
  }

  def get(url:String, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
         (implicit system:ActorSystem):Future[HttpResponse] = {
    val request = Get(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers))
    sendRequest(request)
  }

  def post(url:String, body:String = "", queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
          (implicit system:ActorSystem):Future[HttpResponse] = {
    val request = Post(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(body))
    sendRequest(request)
  }

  def put(url:String, body:String = "", queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
         (implicit system:ActorSystem):Future[HttpResponse] = {
    val request = Put(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(body))
    sendRequest(request)
  }

  def putFile(url:String, file:java.io.File, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
             (implicit system:ActorSystem):Future[HttpResponse] = {
    val source = scala.io.Source.fromFile(file)
    val byteArray = source.map(_.toByte).toArray
    source.close()

    val request = Put(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(bytes = byteArray))
    sendRequest(request)
  }

  def delete(url:String, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
            (implicit system:ActorSystem):Future[HttpResponse] = {
    val request = Get(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers))
    sendRequest(request)
  }


  def processJsonResponse[T](response:HttpResponse)(implicit reads: Reads[T]):T = {
    processResponse[T](response,
      (e:HttpEntity) => Json.fromJson[T](Json.parse(e.asString)).get)
  }

  def processStringResponse(response:HttpResponse):String = {
    processResponse[String](response,
      (e:HttpEntity) => e.asString)
  }

  def processBinaryResponse(response:HttpResponse):Array[Byte] = {
    processResponse[Array[Byte]](response,
      (e:HttpEntity) => e.data.toByteArray)
  }

  def processBooleanResponse(response:HttpResponse):Boolean = {
    processResponse[Boolean](response,
      (e:HttpEntity) => (Json.parse(e.asString) \ "ok").as[Boolean])
  }

  def processResponse[T](response:HttpResponse, transform:HttpEntity => T):T = {
    if(response.status.isSuccess)
      transform(response.entity)
    else
      throw new CouchDBException(response.entity.asString)
  }

}
