package com.sungjk.scala_log4j_slack.services

import java.nio.charset.Charset

import com.sungjk.scala_log4j_slack.common.{Errors, LogBlob}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues, HttpMethod, HttpResponseStatus}
import org.json4s.JValue
import org.json4s.native.JsonMethods._

import scala.concurrent.Future

class NettySlackService extends NettyHttpsService {
	def writeMessage(host: String, endpoint: String, msgJson: JValue): Future[Either[Errors.Error, String]] = {
		val contentJson = compact(render(msgJson))

		https(
			host = host,
			port = 443,
			method = HttpMethod.POST,
			path = endpoint,
			params = Map(),
			headers = Map(HttpHeaderNames.CONTENT_TYPE -> HttpHeaderValues.APPLICATION_JSON.toString),
			Some(contentJson.getBytes("UTF-8"))
		) { message =>
			message.content().toString(Charset.defaultCharset()) match {
				case "ok" =>
					Right("ok")
				case _ =>
					Left(Errors.InvalidResponseError)
			}
		}
	}
}
