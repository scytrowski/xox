package xox

import cats.effect.Resource
import zio.Task

package object server {
  type RStream[A] = Stream[Resource[Task, A]]

  type Stream[+A] = fs2.Stream[Task, A]
}
