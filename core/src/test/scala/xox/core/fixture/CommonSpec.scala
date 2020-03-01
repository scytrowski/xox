package xox.core.fixture

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._

trait CommonSpec extends AnyWordSpecLike with Matchers with ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(1.second), interval = scaled(50.millis))
}
