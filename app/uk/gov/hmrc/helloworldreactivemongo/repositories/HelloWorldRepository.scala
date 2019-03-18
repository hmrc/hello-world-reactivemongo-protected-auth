package uk.gov.hmrc.helloworldreactivemongo.repositories

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{Json, OFormat}
import play.api.{Configuration, Environment, Logger}
import play.modules.reactivemongo.{ReactiveMongoComponent, ReactiveMongoComponentImpl}
import reactivemongo.ReactiveMongoHelper
import reactivemongo.api.MongoConnection
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

final case class HelloWorld(foo: String)

object HelloWorld {

  def random = HelloWorld(UUID.randomUUID().toString)

  implicit val format: OFormat[HelloWorld] = Json.format[HelloWorld]

}

@Singleton
class HelloWorldRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit val ec: ExecutionContext)
    extends ReactiveRepository("hello-world", reactiveMongoComponent.mongoConnector.db, HelloWorld.format)

@Singleton
class NotAuthorizedHelloWorldRepositoryHolder @Inject()(configuration: Configuration,
                                   environment: Environment,
                                   lifecycle: ApplicationLifecycle)(implicit val ec: ExecutionContext) {
  val configurationWithUnauthorizedDatabase = configuration ++ Configuration("mongodb.uri" -> configuration.getString("mongodb.notAuthorizedUri").getOrElse(
    throw new RuntimeException("mongodb.notAuthorizedUri key not found")
  ))



  lazy val repositoryAccessingUnauthorizedDatabase = new HelloWorldRepository(
    new ReactiveMongoComponentImpl(configurationWithUnauthorizedDatabase, environment, lifecycle))

  lazy val mongoDbUriString = configuration.getString("mongodb.uri").getOrElse("mongodb.uri not found")


  val parsedUri = MongoConnection.parseURI(mongoDbUriString) match {
    case Success(uri) =>
      uri
    case Success(MongoConnection.ParsedURI(_, _, _, None, _)) =>
      throw new Exception(s"Missing database name in mongodb.uri '$mongoDbUriString'")
    case Failure(e) => throw new Exception(s"Invalid mongodb.uri '$mongoDbUriString'", e)
  }

  if (parsedUri.authenticate.isDefined) {
    Logger.warn(s"Uri with authentication ${parsedUri.db} / ${parsedUri.authenticate.get.user}")
  } else {
    Logger.warn(s"Uri without authentication ${parsedUri.db}")
  }

}