package uk.gov.hmcts.reform.fact.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.Environment

import scala.concurrent.duration._

object FactScenario {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val FactJourney =

    exec(http("Fact_010_Homepage")
      .get(BaseURL + "/")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("Use this service to find a court")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Fact_020_Start")
      .get(BaseURL + "/search-option")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("<form action=.(.+?). method=.(post|get). novalidate>").ofType[(String, String)].find.optional.saveAs("action"))
      .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
      .check(regex("Do you know the name")))

    .exec { session =>
      println(session)
      session
        .set("paramName", session("radioInput").as[(String, String)]._1)
        .set("paramValue", session("radioInput").as[(String, String)]._2)
        .set("actionURL", session("action").as[(String, String)]._1)
        .set("actionMethod", session("action").as[(String, String)]._2)
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .doWhile(session => (session.contains("radioInput") || session.contains("textInput")) && session("count").as[String].toInt < 10, "count") {

      exec(_.remove("action"))
      .exec(_.remove("radioInput"))
      .exec(_.remove("textInput"))
      .exec(_.remove("courtURL"))
      .exec(_.remove("sorryCantHelp"))

      .exec(http("Fact_03${count}_${actionURL}:${paramValue}")
        .post(BaseURL + "${actionURL}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("${paramName}", "${paramValue}")
        .check(regex("<form action=.(.+?). method=.(post|get). novalidate>").ofType[(String, String)].find.optional.saveAs("action"))
        .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
        .check(regex("""govuk-input.+\" id=\".+?\" name=\"(.+?)\" type=\"text\" (?:value=""|aria-describedby)""").find.optional.saveAs("textInput"))
        .check(regex("search-postcode").find.optional.saveAs("postcodeInput"))
        .check(regex("""class="govuk-heading-m"><a href="?..(.+?)"? class="govuk-link"""").findRandom.transform(str => str.replace("&amp;","&")).optional.saveAs("courtURL"))
        .check(regex("Sorry, we couldn't help you").find.optional.saveAs("sorryCantHelp")))

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

      .doIfOrElse("${radioInput.exists()}") {
        exec {
          session => session
            .set("paramName", session("radioInput").as[(String, String)]._1)
            .set("paramValue", session("radioInput").as[(String, String)]._2)
            .set("actionURL", session("action").as[(String, String)]._1)
            .set("actionMethod", session("action").as[(String, String)]._2)
        }
      } {
        doIf("${textInput.exists()}") {
          doIfOrElse("${postcodeInput.exists()}"){
            exec {
              session => session
                .set("paramName", session("textInput").as[String])
                .set("paramValue", "EH1 9SP")
                .set("actionURL", session("action").as[(String, String)]._1)
                .set("actionMethod", session("action").as[(String, String)]._2)
            }
          }
          {
            exec {
              session => session
                .set("paramName", session("textInput").as[String])
                .set("paramValue", "Blackburn")
                .set("actionURL", session("action").as[(String, String)]._1)
                .set("actionMethod", session("action").as[(String, String)]._2)
            }
          }
        }
      }

    }

    .doIf("${courtURL.exists()}"){

      exec(http("Fact_040_LoadCourtDetailsPage")
        .get(BaseURL + "${courtURL}")
        .headers(CommonHeader)
        .headers(GetHeader)
        .check(regex("Telephone")))

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

    }

}