package com.sungjk.scala_log4j_slack.common

object Errors {
	sealed trait Error extends Exception {
		val code: Int
	}

	sealed trait ClientError extends Error
	sealed trait InvalidRequest extends ClientError

	sealed trait ResponseError extends Error

	case object InvalidResponseError extends ResponseError { val code = 999}
}
