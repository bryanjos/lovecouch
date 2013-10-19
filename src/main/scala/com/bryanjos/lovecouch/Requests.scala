package com.bryanjos.lovecouch

import scala.concurrent._
import play.api.libs.json.{JsValue, Reads, Json}
import spray.http._
import spray.client.pipelining._
import akka.actor.ActorSystem

object Requests {

  private def sendRequest(request:HttpRequest)(implicit system:ActorSystem, context:ExecutionContext) = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive(system, context)
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
        e => HttpHeaders.RawHeader(e._1,e._2)
      }.toList
    }
  }

  def get(url:String, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
         (implicit system:ActorSystem, context:ExecutionContext):Future[HttpResponse] = {
    val request = Get(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers))
    sendRequest(request)
  }

  def post(url:String, body:String = "", queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
          (implicit system:ActorSystem, context:ExecutionContext):Future[HttpResponse] = {
    val request = Post(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(contentType = ContentTypes.`application/json`, string = body))
    sendRequest(request)
  }

  def put(url:String, body:String = "", queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
         (implicit system:ActorSystem, context:ExecutionContext):Future[HttpResponse] = {
    val request = Put(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(contentType = ContentTypes.`application/json`, string = body))
    sendRequest(request)
  }

  def putFile(url:String, file:java.io.File, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
             (implicit system:ActorSystem, context:ExecutionContext):Future[HttpResponse] = {
    val request = Put(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers)).withEntity(HttpEntity(data = HttpData(file)))
    sendRequest(request)
  }

  def delete(url:String, queryParameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]())
            (implicit system:ActorSystem, context:ExecutionContext):Future[HttpResponse] = {
    val request = Delete(buildUrl(url, queryParameters)).withHeaders(buildHeaders(headers))
    sendRequest(request)
  }

  def processObjectResponse[T](response:HttpResponse)(implicit reads: Reads[T]):T = {
    processResponse[T](response,
      (e:HttpEntity) => Json.fromJson[T](Json.parse(e.asString)).get)
  }

  def processJsonResponse(response:HttpResponse):JsValue = {
      Requests.processResponse[JsValue](response,
        (e:HttpEntity) => Json.parse(e.asString))
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

  def processIntResponse(response:HttpResponse):Int = {
    processResponse[Int](response,
      (e:HttpEntity) => e.asString.toInt)
  }

  def processResponse[T](response:HttpResponse, transform:HttpEntity => T):T = {
    if(response.status.isSuccess){
      transform(response.entity)
    }else
      throw new CouchDBException(response.entity.asString)
  }

}
