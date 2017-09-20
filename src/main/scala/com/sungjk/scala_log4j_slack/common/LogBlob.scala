package com.sungjk.scala_log4j_slack.common

import java.io.{PrintWriter, StringWriter}
import java.util.concurrent.atomic.AtomicLong

import org.apache.logging.log4j.{Level, LogManager}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._

object LogBlob {
    private val logIdCounter = new AtomicLong(System.currentTimeMillis())
    def newLogId(): String = {
        f"${logIdCounter.incrementAndGet()}%x"
    }

    def apply(logStorage: Class[_], blobInfo: => JValue) = new LogBlob(logStorage.getName, newLogId(), Some(blobInfo))
    def apply(logStorage: String, blobInfo: => JValue) = new LogBlob(logStorage, newLogId(), Some(blobInfo))
    def apply(logStorage: Class[_]) = new LogBlob(logStorage.getName, newLogId(), None)
    def apply(logStorage: String) = new LogBlob(logStorage, newLogId(), None)
}

class LogBlob(logStorage: String, logId: String, blobInfo: => Option[JValue]) {
    val startTime: Timestamp = Timestamp.current

    private var initial: Boolean = true
    private var logLevel: Level = Level.TRACE
    private var logs = List[(Timestamp, () => JValue)]()

    def elevateLevel(level: Level): LogBlob = {
        if (level.isLessSpecificThan(logLevel)) {
            logLevel = level
        }
        this
    }

    def log(content: => JValue): LogBlob = {
        logs +:= (Timestamp.current, () => content)
        this
    }

    def log(level: Level, content: => JValue): LogBlob =
        elevateLevel(level).log(content)

    def error(content: => JValue): LogBlob =
        log(Level.ERROR, content)

    def trace(content: => JValue): LogBlob =
        log(Level.TRACE, content)

    def flush(): Unit = {
        val logger = LogManager.getLogger(logStorage)
        if (logger.isEnabled(logLevel)) {
            val logItems: Seq[JField] = logs.reverse.zipWithIndex map { logIdx =>
                val ((timestamp, jvalue), idx) = logIdx
                JField(s"$idx:${timestamp.timestamp - startTime.timestamp}", jvalue())
            }
            val logJson: JObject =
                if (initial) {
                    ("id" -> logId) ~
                        ("start" -> startTime.timestamp) ~
                        ("info" -> blobInfo) ~
                        ("logs" -> JObject(logItems: _*))
                } else {
                    ("id" -> logId) ~ ("logs" -> JObject(logItems: _*))
                }
            logs = List()
            initial = false
            logger.log(logLevel, compact(render(logJson)))
        }
    }
}

object LogUtil {
    def stackTrace(error: Throwable): String = {
        val stringWriter = new StringWriter()
        val printWriter = new PrintWriter(stringWriter)
        error.printStackTrace(printWriter)
        printWriter.flush()
        stringWriter.toString
    }
}
