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
				val logJson: JValue = "text" -> "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
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
