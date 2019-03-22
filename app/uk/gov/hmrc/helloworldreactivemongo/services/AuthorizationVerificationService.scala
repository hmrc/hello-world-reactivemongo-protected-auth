package uk.gov.hmrc.helloworldreactivemongo.services

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoComponentImpl
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.helloworldreactivemongo.repositories.HelloWorld
import uk.gov.hmrc.helloworldreactivemongo.utils.MongoUrl

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

case class Scenario(mongoDbUrl : String, shouldSucceed : Boolean, description : String)

case class ValidationResult(description : String, passed : Boolean, error : Option[String])


@Singleton()
class AuthorizationVerificationService @Inject() (configuration: Configuration,
                                       environment: Environment,
                                       lifecycle: ApplicationLifecycle)(implicit ec : ExecutionContext) {

  private lazy val baseMongodbUri = configuration.getString("mongodb.uri").getOrElse("mongodb.uri not found")

  private lazy val parsedUri = MongoUrl.parse(baseMongodbUri)

  private lazy val parsedUriWithLessChannels = parsedUri.copy(params = Some(parsedUri.params
    .map(_ + "&rm.nbChannelsPerNode=3").getOrElse("&rm.nbChannelsPerNode=3")))

  lazy val scenarios = List(
    Scenario(parsedUriWithLessChannels.print, shouldSucceed = true, "Valid connection"),
    Scenario(parsedUriWithLessChannels.copy(database = Some("invalidDatabase")).print, shouldSucceed = false, "Unauthorised database"),
    Scenario(parsedUriWithLessChannels.copy(authPart = parsedUri.authPart.map(_.copy(login = "invalid"))).print, shouldSucceed = false, "Invalid username"),
    Scenario(parsedUriWithLessChannels.copy(authPart = parsedUri.authPart.map(_.copy(password = Some("invalid")))).print, shouldSucceed = false, "Invalid password")
  )

  def performChecks(): Seq[ValidationResult] = scenarios.map(runScenario)

  private def runScenario(scenario: Scenario) = {
    val reactiveMongo = new ReactiveMongoComponentImpl(
      configuration = configuration ++ Configuration("mongodb.uri" -> scenario.mongoDbUrl),
      environment = environment,
      lifecycle = lifecycle)

    val tryDb = Try {
      reactiveMongo.mongoConnector.db()
    }
    val outcome = tryDb.flatMap {
       db => Try {
         Await.result(db.collection[JSONCollection]("test").insert(HelloWorld.random), 30 seconds)
       }
    }

    reactiveMongo.mongoConnector.close()

    outcome match {
      case Success(_) => ValidationResult(scenario.description,  scenario.shouldSucceed, None)
      case Failure(exception) => ValidationResult(scenario.description, !scenario.shouldSucceed, Some(exception.getMessage))
    }

  }

}

