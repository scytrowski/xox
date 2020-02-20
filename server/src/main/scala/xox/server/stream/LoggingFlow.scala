package xox.server.stream

import akka.NotUsed
import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.stream.scaladsl.{Flow, Sink}

object LoggingFlow {
  def info[T](
      logName: String
  )(f: T => String)(implicit adapter: LoggingAdapter): Flow[T, T, NotUsed] =
    create(logName, Logging.InfoLevel)(f)

  def debug[T](
      logName: String
  )(f: T => String)(implicit adapter: LoggingAdapter): Flow[T, T, NotUsed] =
    create(logName, Logging.DebugLevel)(f)

  private def create[T](logName: String, logLevel: LogLevel)(
      f: T => String
  )(implicit adapter: LoggingAdapter): Flow[T, T, NotUsed] =
    Flow[T].alsoTo(
      Sink.foreach(t => adapter.log(logLevel, s"[$logName] ${f(t)}"))
    )
}
