package uk.gov.hmrc.helloworldreactivemongo.repositories

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.{Json, OFormat}
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.{ReactiveMongoComponent, ReactiveMongoComponentImpl}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext

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

  val newConfiguration = configuration ++ Configuration("mongodb.uri" -> configuration.getString("mongodb.notAuthorizedUri").getOrElse(
    throw new RuntimeException("mongodb.notAuthorizedUri key not found")
  ))

  val reactiveMongoComponent = new ReactiveMongoComponentImpl(configuration, environment, lifecycle)

  lazy val unathorizedRepository = new HelloWorldRepository(reactiveMongoComponent)

}