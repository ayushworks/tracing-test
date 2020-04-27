package com.ayushworks

import cats.data.Kleisli
import cats.effect.{IO, Resource}
import com.ayushworks.Test.Span
import cats.implicits._
/**
  * @author Ayush Mittal
  */
object TracingTest extends App {

  println("─" * 50)

  type Span = String
  type Tracer = String

  type User = String
  type UserNetwork = String
  type UserProfile = String


  def createSpan(operationName: String, parentSpan: Span): Resource[IO, Span] = {
    //Resource.make(IO(tracer.buildSpan(operationName).asChildOf(parentSpan).start()))(s => IO(s.finish()))
    Resource.make(IO(s"$parentSpan --> $operationName"))(str => IO(println(str)))
  }

  case class UserData(userProfile: UserProfile, userNetwork: UserNetwork)

  case class UserSearchResult(user: User, userData: UserData)

  /*def getUser(userId: Int): IO[User] = {
    // query database and get user
    IO("user")
  }


  def getUserNetwork(user: User): IO[UserNetwork] = {
    // get user network data from another service
    IO("user network")
  }

  def getUserProfile(user: User): IO[UserProfile] = {
    // fetch user profile data from another service
    IO("user profile information")
  }


  def getUserData(user: User): IO[UserData] = for {
    userNetwork <- getUserNetwork(user)
    userProfile <- getUserProfile(user)
  } yield UserData(userNetwork, userProfile)


  def program(userId: Int): IO[UserSearchResult] = for {
    user <- getUser(userId)
    userData <- getUserData(user)
  } yield UserSearchResult(user, userData)


  program(1).unsafeRunSync()

  */
  def getUser(userId: Int, parentSpan: Span): IO[User] = createSpan("get user from database", parentSpan).use {
    _ =>
    // query database and get user
    IO("user")
  }


  def getUserNetwork(user: User, parentSpan: Span): IO[UserNetwork] = createSpan("get user network", parentSpan).use {
    _ =>
    // get user network data from another service
    IO("user network")
  }

  def getUserProfile(user: User, parentSpan: Span): IO[UserProfile] = createSpan("get user profile", parentSpan).use {
    _ =>
    // fetch user profile data from another service
    IO("user profile information")
  }


  def getUserData(user: User, parentSpan: Span): IO[UserData] = createSpan("get user data", parentSpan).use {
    userDataSpan =>
    for {
      userNetwork <- getUserNetwork(user, userDataSpan)
      userProfile <- getUserProfile(user, userDataSpan)
    } yield UserData(userNetwork, userProfile)
  }


  def program(userId: Int, parentSpan: Span): IO[UserSearchResult] = createSpan("program span", parentSpan).use{
    programSpan =>
    for {
      user <- getUser(userId, programSpan)
      userData <- getUserData(user, programSpan)
    } yield UserSearchResult(user, userData)
  }

  val applicationRootSpan : Span = "root span"

  program(1, applicationRootSpan).unsafeRunSync()

  type Trace[A] = Kleisli[IO, Span, A]

  def createSpan[A](name: String)(k: Trace[A]): Trace[A] = {
    Kleisli(x => {
      val io = k.run(s"$x --> $name")
      io.flatTap(_ => IO(println(s"$x --> $name")))
    })
  }

  def getUser(userId: Int): Trace[User] = createSpan("get user from database"){
    // query database and get user
    Kleisli.liftF(IO("user"))
  }


  def getUserNetwork(user: User): Trace[UserNetwork] = createSpan("get user network"){
    // get user network data from another service
    Kleisli.liftF(IO("user network"))
  }

  def getUserProfile(user: User): Trace[UserProfile] = createSpan("get user profile"){
    // fetch user profile data from another service
    Kleisli.liftF(IO("user profile information"))
  }


  def getUserData(user: User): Trace[UserData] = createSpan("get user data"){
      for {
        userNetwork <- getUserNetwork(user)
        userProfile <- getUserProfile(user)
      } yield UserData(userNetwork, userProfile)
  }


  def program(userId: Int): Trace[UserSearchResult] = createSpan("program span"){
      for {
        user <- getUser(userId)
        userData <- getUserData(user)
      } yield UserSearchResult(user, userData)
  }

  program(1).run(applicationRootSpan).unsafeRunSync()
}
