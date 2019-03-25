package uk.gov.hmrc.helloworldreactivemongo.utils

import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.{Matchers, WordSpec}

class MongoUrlSpec extends WordSpec with Matchers {

  val uris =
    Table(
      ("URI to parse and print", "expected parsed Form"),
      ("mongodb://localhost:27017/hello-world-reactivemongo",
        MongoUrl("localhost:27017", Some("hello-world-reactivemongo"), None, None)),

      ("mongodb://localhost:27017/hello-world-reactivemongo2",
        MongoUrl("localhost:27017", Some("hello-world-reactivemongo2"), None, None)),

      ("mongodb://localhost:27017",
        MongoUrl("localhost:27017", None, None, None)),

      ("mongodb://foo:8080",
        MongoUrl("foo:8080", None, None, None)),

      ("mongodb://public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017/hmrcdeskpro",
        MongoUrl("public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017", Some("hmrcdeskpro"), None, None)),

      ("mongodb://public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017/hmrcdeskpro?par1=val1&par2=val2",
        MongoUrl("public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017", Some("hmrcdeskpro"), Some("par1=val1&par2=val2"), None)),

      ("mongodb://login:password@public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017/hmrcdeskpro",
        MongoUrl("public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017", Some("hmrcdeskpro"), None, Some(AuthPart("login", Some("password"))))),

      ("mongodb://login@public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017/hmrcdeskpro",
        MongoUrl("public-mongo-1:27017,public-mongo-2:27017,public-mongo-3:27017", Some("hmrcdeskpro"), None, Some(AuthPart("login", None))))

    )

  "MongoUrl" should {
    "properly parse all urls" in {

      forAll(uris) { (inputUrl: String, parsedUrl: MongoUrl) =>

        MongoUrl.parse(inputUrl) shouldBe parsedUrl

      }
    }

    "be able to print parsed urls" in {

      forAll(uris) { (inputUrl: String, parsedUrl: MongoUrl) =>

        parsedUrl.print shouldBe inputUrl

      }

    }
   }

}
