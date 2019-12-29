package xox

import zio.{Managed, Task}

package object server {
  type MStream[A] = Stream[Managed[Throwable, A]]

  type Stream[+A] = fs2.Stream[Task, A]
}
