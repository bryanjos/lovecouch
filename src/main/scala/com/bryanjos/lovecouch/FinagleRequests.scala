package com.bryanjos.lovecouch

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.{Http, RequestBuilder}
import com.twitter.util.{Future => TwFuture}
import org.jboss.netty.handler.codec.http._
import scala.concurrent.{Future, Promise}
import java.net.URL
import org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer
import com.twitter.finagle.Service

object FinagleRequests {
  implicit def twFutureToScala[T](twFuture: TwFuture[T]): Future[T] = {
    val prom = Promise[T]()
    twFuture.onSuccess { res: T =>
      prom.success(res)
    }
    twFuture.onFailure { t: Throwable =>
      prom.failure(t)
    }
    prom.future
  }

  def buildQueryParameters(url:String, parameters:Seq[(String,String)]):String = {
    if(parameters.size == 0) return url

    val parameterSeparator = if(url.contains("?")){ "&" }else{"?"}
    buildQueryParameters(url + s"$parameterSeparator${parameters.head._1}=${parameters.head._2}", parameters.tail)
  }

  def buildService(url:URL): Service[HttpRequest, HttpResponse] = {
    ClientBuilder()
      .codec(Http())
      .hosts(s"${url.getHost}:${url.getPort}")
      .hostConnectionLimit(1)
      .build()
  }


  def get(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[HttpResponse] = {
    val u = new URL(buildQueryParameters(url, parameters.toSeq))
    val request = RequestBuilder().http10().url(u).addHeaders(headers).buildGet()
    for (res <- buildService(u)(request)) yield res
  }

  def post(url:String, body:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[HttpResponse] = {
    val u = new URL(buildQueryParameters(url, parameters.toSeq))
    val request = RequestBuilder().http10().url(u).addHeaders(headers).buildPost(wrappedBuffer(body.getBytes("UTF-8")))
    for (res <- buildService(u)(request)) yield res
  }

  def put(url:String, body:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[HttpResponse] = {
    val u = new URL(buildQueryParameters(url, parameters.toSeq))
    val request = RequestBuilder().http10().url(u).addHeaders(headers).buildPut(wrappedBuffer(body.getBytes("UTF-8")))
    for (res <- buildService(u)(request)) yield res
  }

  def delete(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[HttpResponse] = {
    val u = new URL(buildQueryParameters(url, parameters.toSeq))
    val request = RequestBuilder().http10().url(u).addHeaders(headers).buildDelete()
    for (res <- buildService(u)(request)) yield res
  }

  def head(url:String, parameters:Map[String,String] = Map[String,String](), headers:Map[String, String] = Map[String,String]()):Future[HttpResponse] = {
    val u = new URL(buildQueryParameters(url, parameters.toSeq))
    val request = RequestBuilder().http10().url(u).addHeaders(headers).buildHead()
    for (res <- buildService(u)(request)) yield res
  }

}
