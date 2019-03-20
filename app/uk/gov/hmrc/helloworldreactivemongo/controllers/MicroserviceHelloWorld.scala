package uk.gov.hmrc.helloworldreactivemongo.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.helloworldreactivemongo.services.HelloWorldService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext

@Singleton()
class MicroserviceHelloWorld @Inject()(helloService: HelloWorldService)(implicit val ec: ExecutionContext)
    extends BaseController {

  def hello() = Action {
    Ok("hello-world")
  }

  def testMongo() = Action.async {
    helloService.addObjectAndCountAll().map { count =>
      Ok(Json.obj("count" -> count))
    }
  }

}
