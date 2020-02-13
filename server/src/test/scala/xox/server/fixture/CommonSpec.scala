package xox.server.fixture

import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._

trait CommonSpec extends ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(1.second), interval = scaled(50.millis))
}
