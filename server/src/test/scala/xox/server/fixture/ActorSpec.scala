package xox.server.fixture

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

abstract class ActorSpec(name: String)
    extends TestKit(ActorSystem(name))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {
  override def afterAll(): Unit = system.terminate()
}
