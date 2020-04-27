package com.ayushworks

import cats.data.Kleisli
import cats.effect.IO


/**
  * @author Ayush Mittal
  */
object Test extends App {

  println("─"*50)

  println("hello world")

  type Span = String

  type User = String
  type UserProfile = String
  type UserNetwork = String

  def trace[A](name: String)(currentRun: Span => A): Span => A = span => {
    println(s"$span --> $name")
    currentRun(s"$span --> $name")
  }

  def getUser(userId: Int) : (Span => User) = trace[User]("get user from database") {
    _ => "worker"
  }

  def getUserProfile(user: User): (Span => UserProfile) =  trace[UserProfile]("get user profile"){
    _ => "routing attributes"
  }

  def getUserNetwork(user: User): (Span => UserNetwork) = trace[UserNetwork]("get user network"){
    _ => "non routing attributes"
  }

  def getUserData(user: User): (Span => (UserProfile, UserNetwork)) = trace("get user data ") {
    x =>
      (getUserProfile(user).apply(x), getUserNetwork(user).apply(x))
  }

  def program: (Span => (UserProfile, UserNetwork)) =
    trace("root span") {
      span =>
        val user = getUser(1).apply(span)
        getUserData(user).apply(span)
    }

  program.apply("main")


  println("─"*50)
}
