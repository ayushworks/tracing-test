package com.ayushworks

import cats._
import cats.data._
import cats.implicits._

abstract class EitherHelper[Error] extends FlexibleErrorHandling[Error, Either]

abstract class ValidatedHelper[Error] extends FlexibleErrorHandling[Error, Validated]

trait FlexibleErrorHandling[Error, Mechanism[+_, +_]] {
  type MultipleErrorsOr[+A] = Mechanism[NonEmptyChain[Error], A]

  final implicit protected class FixedApplicativeIdOps[A](private val a: A) {
    final def good(implicit F: Applicative[MultipleErrorsOr]): MultipleErrorsOr[A] =
      F.pure(a)
  }

  final implicit protected class FixedApplicativeErrorIdOps(private val e: Error) {
    final def bad[A](implicit F: ApplicativeError[MultipleErrorsOr, _ >: NonEmptyChain[Error]]): MultipleErrorsOr[A] =
      F.raiseError(e.pure[NonEmptyChain])
  }

  final implicit protected class FixedApplicativeEitherOps[A](private val e: Either[Error, A]) {
    final def toMultipleError(implicit F: ApplicativeError[MultipleErrorsOr, _ >: NonEmptyChain[Error]]): MultipleErrorsOr[A] =
      e match {
        case Left(error) => F.raiseError(error.pure[NonEmptyChain])
        case Right(a) =>  F.pure(a)
      }
  }
}
