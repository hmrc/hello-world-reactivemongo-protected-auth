package uk.gov.hmrc.helloworldreactivemongo.services

import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoComponentImpl
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

  lazy val scenarios = List(
    Scenario(parsedUri.print, shouldSucceed = true, "Valid connection"),
    Scenario(parsedUri.copy(database = Some("invalidDatabase")).print, shouldSucceed = false, "Unauthorised database"),
    Scenario(parsedUri.copy(authPart = parsedUri.authPart.map(_.copy(login = "invalid"))).print, shouldSucceed = false, "Invalid username"),
    Scenario(parsedUri.copy(authPart = parsedUri.authPart.map(_.copy(password = Some("invalid")))).print, shouldSucceed = false, "Invalid username")
  )

  def performChecks(): Seq[ValidationResult] = scenarios.map(runScenario)

  private def runScenario(scenario: Scenario) = {
    val reactiveMongo = new ReactiveMongoComponentImpl(
      configuration = configuration ++ Configuration("mongodb.uri" -> scenario.mongoDbUrl),
      environment = environment,
      lifecycle = lifecycle)
    Try {
      val db = reactiveMongo.mongoConnector.db()
      Await.result(db.connection.askClose()(30 seconds), 30 seconds)
    } match {
      case Success(_) => ValidationResult(scenario.description + scenario.mongoDbUrl + "original uri: " + baseMongodbUri,  scenario.shouldSucceed, None)
      case Failure(exception) => ValidationResult(scenario.description + scenario.mongoDbUrl + "original uri: " + baseMongodbUri, !scenario.shouldSucceed, Some(exception.getMessage))
    }

  }

}

