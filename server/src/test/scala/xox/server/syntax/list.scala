package xox.server.syntax

object list {
  implicit final class SingleElement[A](list: List[A]) {
    def single: Option[A] = list match {
      case s :: Nil => Some(s)
      case _        => None
    }
  }
}
