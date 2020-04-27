package com.ayushworks

import cats.Id
import cats.data.Kleisli

/**
  * @author Ayush Mittal
  */
object Main extends App {

  println("─"*50)

  println("hello world")

  type Worker = String
  type RoutingAttributes = String
  type NonRoutingAttributes = String

  type Span = String

  type SpanKleisli[A] = Kleisli[Id, Span, A]

  def trace[A](name: String)(k: SpanKleisli[A]): SpanKleisli[A] = {
    Kleisli(x => {
      println(s"span is $x --> $name")
      k.run(s"$x --> $name")
    })
  }

  def getWorkerFromDatabase(id: Int) : SpanKleisli[Worker] = trace("get worker from database"){
    Kleisli.liftF[Id, Span, Worker]("User")
  }

  def getWorkerRoutingAttrs(worker: Worker): SpanKleisli[RoutingAttributes] = trace("get worker routing attributes") {
    Kleisli.liftF[Id, Span, RoutingAttributes] ("routing attributes")
  }

  def getWorkerNonRoutingAttrs(worker: Worker): SpanKleisli[NonRoutingAttributes] = trace("get worker non routing attributes") {
    Kleisli.liftF[Id, Span,NonRoutingAttributes] ("non routing attributes")
  }


  def getAllWorkerAttrs(worker: Worker) = trace("get attributes") {
    for {
      routingAttributes <- getWorkerRoutingAttrs(worker)
      nonRoutingAttributes <- getWorkerNonRoutingAttrs(worker)
    } yield (routingAttributes, nonRoutingAttributes)
  }

  def program: SpanKleisli[(RoutingAttributes, NonRoutingAttributes)] =
    trace("root span") (for {
      worker <- getWorkerFromDatabase(1)
      attributes <- getAllWorkerAttrs(worker)
    } yield attributes)

  program.run("main")

  println("─"*50)
}
