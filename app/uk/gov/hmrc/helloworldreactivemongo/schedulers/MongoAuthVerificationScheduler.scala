package uk.gov.hmrc.helloworldreactivemongo.schedulers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helloworldreactivemongo.services.AuthorizationVerificationService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

@Singleton
class MongoAuthVerificationScheduler @Inject()(service: AuthorizationVerificationService, actorSystem: ActorSystem)(
  implicit val ec: ExecutionContext) {

  private val logger = Logger(getClass)

  val interval = FiniteDuration(60, TimeUnit.SECONDS)

  logger.info(s"Initialising verification of mongo authorization $interval")

  actorSystem.scheduler.schedule(FiniteDuration(1, TimeUnit.SECONDS), interval) {
    logger.info("Scheduling a mongo authorization verifier..")
    val failedChecks = service.performChecks().filter(!_.passed)
    if (failedChecks.nonEmpty) {
      logger.error(s"MONGO_SECURITY_ALERT Failed MongoDB security checks: ${failedChecks.mkString(",")}")
    }
  }

}

class MongoAuthVerificationSchedulerModule extends AbstractModule {
  override def configure(): Unit =
    bind(classOf[MongoAuthVerificationScheduler]).asEagerSingleton()
}
