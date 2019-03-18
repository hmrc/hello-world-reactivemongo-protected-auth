package uk.gov.hmrc.helloworldreactivemongo.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helloworldreactivemongo.repositories.{HelloWorld, HelloWorldRepository, NotAuthorizedHelloWorldRepositoryHolder}

import scala.concurrent.{ExecutionContext, Future}

trait HelloWorldService {

  val repo : HelloWorldRepository

  implicit val ec : ExecutionContext

  private val logger = Logger(getClass)

  def addObjectAndCountAll(): Future[Int] =
    repo.insert(HelloWorld.random).flatMap { _ =>
      repo.count.map { count =>
        logger.info(s"count of objects = $count")
        count
      }
    }

}

@Singleton
class AuthorizedHelloWorldService @Inject()(val repo: HelloWorldRepository)(implicit val ec: ExecutionContext) extends HelloWorldService

@Singleton
class NotAuthorizedHelloWorldService @Inject()(val nonAuthorizedRepositoryHolder : NotAuthorizedHelloWorldRepositoryHolder)(implicit val ec: ExecutionContext)
  extends HelloWorldService {
  override val repo: HelloWorldRepository = nonAuthorizedRepositoryHolder.repositoryAccessingUnauthorizedDatabase
}
