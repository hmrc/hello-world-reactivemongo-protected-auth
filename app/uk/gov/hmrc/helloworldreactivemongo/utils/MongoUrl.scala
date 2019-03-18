package uk.gov.hmrc.helloworldreactivemongo.utils

case class AuthPart(login : String, password : Option[String]) {
  def print: String = login + password.map(value => ":" + value).getOrElse("")
}

case class MongoUrl(hostPart : String, database : Option[String], params : Option[String], authPart: Option[AuthPart]) {

  def print: String = {

    val printedAuthPart = authPart.map(_.print + "@").getOrElse("")
    val printedDatabasePart = database.map("/" + _).getOrElse("")
    val printedQueryPart = params.map("?" + _).getOrElse("")

    s"mongodb://$printedAuthPart$hostPart$printedDatabasePart$printedQueryPart"

  }

}

object MongoUrl {
  def parse(input : String): MongoUrl = {
    val MongoUrlRegex = "mongodb://(?:([^@]+)@)?([^/]+)(?:/([^\\?]+))?(?:\\?(.+))?".r
    val AuthRegex = "([^:]+)(?::(.+))?".r

    input match {
      case MongoUrlRegex(authPart, hostPart, dbPart, optionPart) =>
        val parsedAuth = Option(authPart) match {
          case Some(AuthRegex(login, password)) => Some(AuthPart(login, Option(password)))
          case None => None
        }
        MongoUrl(hostPart, Option(dbPart), Option(optionPart), parsedAuth)
    }
  }
}