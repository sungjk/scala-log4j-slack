package com.sungjk.scala_log4j_slack

import com.sungjk.scala_log4j_slack.common.{LogBlob, Resource}
import com.sungjk.scala_log4j_slack.services.NettySlackService
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object Cei {
	implicit class TypedJObject(val jvalue: JValue) extends AnyVal {
		def asString: Option[String] = jvalue.toOption match {
			case Some(JString(v)) => Some(v)
			case _ => None
		}
	}
}

object Main {
	def main(args: Array[String]): Unit = {
		import Cei._
		implicit val ec: ExecutionContext = ExecutionContext.global

		val confJson = parse(Resource.fromResourceAsString("/classified/slack.json", "/classified").get)
		val log = LogBlob(classOf[NettySlackService])

		val responseFuture = ((confJson \ "host").asString, (confJson \ "endpoint").asString) match {
			case (Some(host), Some(endpoint)) =>
				val nettySlackService = new NettySlackService()
				val logJson: JValue = "text" -> "Start log4j slack appender!!"
				log.log(logJson)

				nettySlackService.writeMessage(host, endpoint, logJson) flatMap {
					case Right(_) =>
						Future.successful(SuccessResponse)
					case Left(error) =>
						Future.successful(ErrorResponse(error))
				}
			case _ =>
				Future.successful(NoResponse)
		}

		responseFuture onComplete {
			case Success(NoResponse) =>
				println("NoResponse")
				log.log("NoResponse").flush()
			case Success(response) =>
				println(response)
				log.log(response.toJson).flush()
			case _ =>
				println("Noop")
				log.log("Noop").flush()
		}
	}
}
