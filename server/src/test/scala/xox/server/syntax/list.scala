package xox.server.syntax

object list {
  final implicit class SingleElement[A](list: List[A]) {
    def single: Option[A] = list match {
      case s :: Nil => Some(s)
      case _        => None
    }
  }
}
