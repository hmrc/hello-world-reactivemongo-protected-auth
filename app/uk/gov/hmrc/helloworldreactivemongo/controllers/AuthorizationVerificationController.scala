package uk.gov.hmrc.helloworldreactivemongo.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat, Writes}
import play.api.mvc._
import uk.gov.hmrc.helloworldreactivemongo.services.{AuthorizationVerificationService, ValidationResult}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext

@Singleton()
class AuthorizationVerificationController @Inject()(authorizedHelloService: AuthorizationVerificationService)(implicit val ec: ExecutionContext)
    extends BaseController {

  implicit val validationErrorFormat: Writes[ValidationResult] = Json.format[ValidationResult]

  def verifyAuthorization() = Action {
    Ok(Json.toJson(authorizedHelloService.performChecks()))
  }

}
