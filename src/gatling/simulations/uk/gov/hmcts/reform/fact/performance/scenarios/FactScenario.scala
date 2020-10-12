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

  var paramName = ""
  var paramValue = ""

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
      .check(regex("<form action=.(.+). method=.(post|get). novalidate>").ofType[(String, String)].find.optional.saveAs("action1"))
      .check(regex("""govuk-radios__input\" id=\".+\" name=\"(.+)\" type=\"radio\" value="(.+)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput1")))

      //.check(bodyString.saveAs("body"))
      //.check(checkIf(regex("govuk-radios__input").exists){regex("govuk-radios__input").find.saveAs("radio")})
      //.check(checkIf((response: Response, session: Session) => session("body").as[String].contains("govuk-radios__input"))(regex("govuk-radios__input").count.is(2)))

    .exec { session =>
      println(session)
      println(session("action1").as[(String, String)]._1)
      println(session("action1").as[(String, String)]._2)
      println(session("radioInput1").as[(String, String)]._1)
      println(session("radioInput1").as[(String, String)]._2)
      session
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Fact_030_KnowLocation")
      .post(BaseURL + "${action1._1}")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("${radioInput1._1}", "${radioInput1._2}")
      .check(regex("<form action=.(.+). method=.(post|get). novalidate>").ofType[(String, String)].find.optional.saveAs("action"))
      .check(regex("""govuk-radios__input\" id=\".+\" name=\"(.+)\" type=\"radio\" value="(.+)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
      .check(regex("""govuk-input.+\" id=\".+\" name=\"(.+)\" type=\"text\"""").find.optional.saveAs("textInput")))

    .doIfOrElse("${radioInput.exists()}") {
      exec {
        session =>
          paramName = session("radioInput").as[(String, String)]._1
          println("RADIO VALUE = " + paramName)
          session
      }
    }
    {
      doIf("${textInput.exists()}") {
        exec {
          session =>
            paramName = session("textInput").as[String]
            println("TEXT VALUE = " + paramName)
            session
        }
      }
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