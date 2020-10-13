package uk.gov.hmcts.reform.fact.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.{Common, Environment}

import uk.gov.hmcts.reform.fact.performance.scenarios.checks.CsrfCheck
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.{Common, Environment}

import scala.concurrent.duration._
import scala.util.Random

object FactScenario {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val rnd = new Random()

  //var paramName = ""
  //var paramValue = ""
  //var actionMethod = ""
  //var actionURL = ""
  //var count = 0

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

      //.check(bodyString.saveAs("body"))
      //.check(checkIf(regex("govuk-radios__input").exists){regex("govuk-radios__input").find.saveAs("radio")})
      //.check(checkIf((response: Response, session: Session) => session("body").as[String].contains("govuk-radios__input"))(regex("govuk-radios__input").count.is(2)))

    .exec { session =>
      //paramName = session("radioInput").as[(String, String)]._1
      //paramValue = session("radioInput").as[(String, String)]._2
      //actionURL = session("action").as[(String, String)]._1
      //actionMethod = session("action").as[(String, String)]._2
      println(session)
      session
        .set("paramName", session("radioInput").as[(String, String)]._1)
        .set("paramValue", session("radioInput").as[(String, String)]._2)
        .set("actionURL", session("action").as[(String, String)]._1)
        .set("actionMethod", session("action").as[(String, String)]._2)
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .doWhile(session => session.contains("radioInput") || session.contains("textInput")) {
      //.doWhile("${radioInput.exists()}|${textInput.exists()}") {

      exec(_.remove("action"))
      .exec(_.remove("radioInput"))
      .exec(_.remove("textInput"))
      .exec(_.remove("courtURL"))
      .exec(_.remove("sorryCantHelp"))

      .exec(http("Fact_030_${actionURL}")
        .post(BaseURL + "${actionURL}")
        .headers(CommonHeader)
        .headers(PostHeader)
        .formParam("${paramName}", "${paramValue}")
        .check(regex("<form action=.(.+?). method=.(post|get). novalidate>").ofType[(String, String)].find.optional.saveAs("action"))
        .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
        .check(regex("""govuk-input.+\" id=\".+?\" name=\"(.+?)\" type=\"text\"""").find.optional.saveAs("textInput"))
        .check(regex("search-postcode").find.optional.saveAs("postcodeInput"))
        .check(regex("""class="govuk-heading-m"><a href="..(.+?)" class="govuk-link"""").findRandom.optional.saveAs("courtURL"))
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
          exec {
            session => session
              .set("paramName", session("textInput"))
              .set("actionURL", session("action").as[(String, String)]._1)
              .set("actionMethod", session("action").as[(String, String)]._2)
          }
          doIfOrElse("${postcodeInput.exists()}"){
            exec {
              session => session
                .set("paramValue", "EH1 9SP")
            }
          }
          {
            exec {
              session => session
                .set("paramValue", "Blackburn")
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

    }

/*
    .exec { session =>
      println(session)
      println(session("action").as[(String, String)]._1)
      println(session("action").as[(String, String)]._2)
      println(session("radioInput").as[(String, String)]._1)
      println(session("radioInput").as[(String, String)]._2)
      println(session("textInput").as[String])
      session
    }
*/




/*



    .pause(MinThinkTime seconds, MaxThinkTime seconds)


    .exec(http("Fact_030_KnowLocation")
      .post(BaseURL + "/search-option")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("knowLocation", "yes")
      .check(regex("What is the name or address")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Fact_040_SearchForLocation")
      .post(BaseURL + "/search-for-location")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("location-search-value", "Blackburn")
      .check(regex("matching your search")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Fact_050_LoadCourtDetailsPage")
      .get(BaseURL + "/individual-location-pages/generic?ctsc=no&courtname=blackburn-family-court")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("Visit or contact us")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)
*/

}