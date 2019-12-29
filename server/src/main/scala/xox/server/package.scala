package xox

import zio.{Task, TaskManaged}

package object server {
  type MStream[A] = Stream[TaskManaged[A]]

  type Stream[+A] = fs2.Stream[Task, A]
}
