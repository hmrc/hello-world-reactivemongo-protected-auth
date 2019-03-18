package uk.gov.hmrc.helloworldreactivemongo.controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.helloworldreactivemongo.services.{AuthorizedHelloWorldService, NotAuthorizedHelloWorldService}

import scala.concurrent.ExecutionContext.Implicits.global

class MicroserviceHelloWorldControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  private val fakeRequest = FakeRequest("GET", "/")
  private val repo        = mock[AuthorizedHelloWorldService]
  private val nonAuthRepo = mock[NotAuthorizedHelloWorldService]

  "GET /" should {
    "return 200" in {
      val controller = new MicroserviceHelloWorld(repo, nonAuthRepo)
      val result     = controller.hello()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

}
