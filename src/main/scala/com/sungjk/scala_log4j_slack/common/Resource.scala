package com.sungjk.scala_log4j_slack.common

import java.io.InputStream
import java.net.URL
import java.nio.file.Paths

import org.json4s._
import org.json4s.native.JsonMethods._

object Resource {
	def inputStreamToString(inputStream: InputStream): String = {
		scala.io.Source.fromInputStream(inputStream, "UTF-8").mkString
	}

	def checkPath(pathStr: String, rootStr: String): Option[String] = {
		val path = Paths.get(pathStr, "").normalize()
		val root = Paths.get(rootStr, "").normalize()
		if (path.startsWith(root)) {
			Some(path.toString)
		} else {
			None
		}
	}

	def fromResource(pathStr: String, rootStr: String): Option[InputStream] = {
		checkPath(pathStr, rootStr) map { path =>
			getClass.getResourceAsStream(path)
		}
	}

	def fromResourceAsString(path: String, root: String): Option[String] = {
		fromResource(path, root) map inputStreamToString
	}

	def urlFromResource(pathStr: String, root: String): Option[URL] =
		checkPath(pathStr, root) flatMap { path =>
			Option(getClass.getResource(path))
		}

	def fromResourceAsJson(pathStr: String, root: String): Option[JValue] = {
		fromResourceAsString(pathStr, root) map { parse(_) }
	}
}
