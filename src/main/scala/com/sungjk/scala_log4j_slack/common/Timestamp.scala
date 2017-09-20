package com.sungjk.scala_log4j_slack.common

import java.util.{Calendar, Date, TimeZone}

import scala.concurrent.duration.{Duration, _}

case class Timestamp(timestamp: Long) extends AnyVal {
	def +(duration: Duration): Timestamp = Timestamp(timestamp + duration.toMillis)
	def -(duration: Duration): Timestamp = Timestamp(timestamp - duration.toMillis)
	def -(other: Timestamp): Duration = (timestamp - other.timestamp).millis
	def >(other: Timestamp): Boolean = this.timestamp > other.timestamp
	def <(other: Timestamp): Boolean = this.timestamp < other.timestamp
	def >=(other: Timestamp): Boolean = this.timestamp >= other.timestamp
	def <=(other: Timestamp): Boolean = this.timestamp <= other.timestamp
	def stringRepr: String = {
		val cal = Calendar.getInstance()
		cal.setTimeInMillis(timestamp)

		val timezone = TimeZone.getDefault
		cal.setTimeZone(timezone)

		val year = cal.get(Calendar.YEAR)
		val month = cal.get(Calendar.MONTH) + 1
		val day = cal.get(Calendar.DAY_OF_MONTH)
		val hour24 = cal.get(Calendar.HOUR_OF_DAY)
		val min = cal.get(Calendar.MINUTE)
		val sec = cal.get(Calendar.SECOND)

		val tz = timezone.getID

		f"$year%04d-$month%02d-$day%02d $hour24%02d:$min%02d:$sec%02d $tz"
	}
	def toDate: Date = new Date(timestamp)
	def toCalendar: Calendar = {
		val cal = Calendar.getInstance()
		cal.setTime(toDate)
		cal
	}
}

object Timestamp {
	def current = Timestamp(System.currentTimeMillis())
	val zero = Timestamp(0)
}
