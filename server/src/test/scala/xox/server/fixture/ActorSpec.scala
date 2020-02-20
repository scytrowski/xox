package xox.server.fixture

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll

abstract class ActorSpec(name: String)
    extends TestKit(ActorSystem(name))
    with CommonSpec
    with BeforeAndAfterAll {
  override def afterAll(): Unit = system.terminate()
}
