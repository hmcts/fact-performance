package uk.gov.hmcts.reform.fact.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.Environment

import scala.concurrent.duration._
import scala.util.Random

object FactScenario {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val PercStraightToDetailsPage = Environment.percStraightToDetailsPage
  val PercToSearchAPI = Environment.percToSearchAPI

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val randomFeeder = Iterator.continually( Map( "perc" -> Random.nextInt(100)))

  val courtURLFeeder = csv("courtURLs.csv").random
  val postcodeFeeder = csv("postcodes.csv").random
  val searchTermFeeder = csv("searchTerms.csv").random

  val FactJourney =

    feed(randomFeeder)
      .doIfOrElse(session => session("perc").as[Int] < PercStraightToDetailsPage) {

        //Some users will be linked directly to a random court or tribunal details page
        feed(courtURLFeeder)

      } {

        doIfOrElse(session => session("perc").as[Int] < (PercStraightToDetailsPage + PercToSearchAPI)) {

          //Some users will hit the postcode search API then a random resulting court or tribunal details page

          feed(postcodeFeeder)
            .group("Fact_050_PostcodeSearchAPI") {
              exec(http("Postcode Search API")
                .get(BaseURL + "/search/results.json?postcode=${postcode}")
                .headers(CommonHeader)
                .headers(GetHeader)
                .check(jsonPath("$[*].slug").findRandom.saveAs("courtURL"))
                .check(regex("areas_of_law")))
            }

            .pause(MinThinkTime seconds, MaxThinkTime seconds)

        } { //The remaining users will be follow the entire FaCT journey

          group("Fact_010_Homepage") {
            exec(http("Load Homepage")
              .get(BaseURL + "/")
              .headers(CommonHeader)
              .headers(GetHeader)
              .check(regex("Use this service to find a court")))
          }

            .pause(MinThinkTime seconds, MaxThinkTime seconds)

            //Do you know the name of the court or tribunal?
            .group("Fact_020_Start") {
              exec(http("Load Start Page")
                .get(BaseURL + "/search-option")
                .headers(CommonHeader)
                .headers(GetHeader)
                .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
                .check(regex("Do you know the name")))
            }

            //set the initial values for the first post call (knowLocation=yes or knowLocation=no)
            .exec { session =>
              session
                .set("paramName", session("radioInput").as[(String, String)]._1) //knowLocation
                .set("paramValue", session("radioInput").as[(String, String)]._2) //yes or no
                //.set("paramValue", "no") //yes or no
                .set("actionMethod", "POST")
                .set("actionURL", "/search-option")
                .set("transactionName", "search-option")
            }

            .pause(MinThinkTime seconds, MaxThinkTime seconds)

            /*Keep looping whilst radio buttons or text input box is found on the next page (or the loop goes on too long)
          Once the loop completes, the user must be on a page either:
          - With a link to one or more courts
          - Displaying "Sorry, we couldn't help you"*/
            .doWhile(session => (session.contains("radioInput") || session.contains("textInput")) && session("count").as[String].toInt < 10, "count") {

              //clear the session variables first
              exec(_.remove("action"))
                .exec(_.remove("radioInput"))
                .exec(_.remove("textInput"))
                .exec(_.remove("courtURL"))
                .exec(_.remove("sorryCantHelp"))

                //Keep making post requests and capture whether the following page contains radio buttons, text boxes or court URLs
                //Each capture group is optional so the resulting page's contents can be evaluated.
                //Where there are multiple options (e.g. 5 radio buttons), one is chosen at random
                .group("Fact_03x_${transactionName}") {

                  doIfOrElse(session => session("actionMethod").as[String].equals("POST")) {

                    //POST calls
                    exec(http("Progress Through Journey")
                      .post(BaseURL + "${actionURL}")
                      .headers(CommonHeader)
                      .headers(PostHeader)
                      .formParam("${paramName}", "${paramValue}")
                      .check(currentLocationRegex(BaseURL + "(.+)").saveAs("currentPageUrl"))
                      .check(currentLocationRegex(BaseURL + """.*\/(.+)?""").saveAs("transactionName"))
                      .check(regex("<form method=.GET. action=.(.+?).>").find.optional.saveAs("action"))
                      .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
                      .check(regex("""govuk-input.+\" id=\".+?\" name=\"(.+?)\" type=\"text\" (?:value=\"\">|aria-describedby)""").find.optional.saveAs("textInput"))
                      .check(regex("id=.postcode.").find.optional.saveAs("postcodeInput"))
                      .check(regex("""govuk-heading-m">\n +?<a class="govuk-link" href="/courts/(.+?)">""").findRandom.transform(str => str.replace("&amp;", "&")).optional.saveAs("courtURL"))
                      .check(regex("Sorry, we couldn't help you").find.optional.saveAs("sorryCantHelp")))
                  } {

                    doIf(session => session("actionMethod").as[String].equals("GET")) {

                      //GET calls
                      exec(http("Progress Through Journey")
                        .get(BaseURL + "${actionURL}?${paramName}=${paramValue}")
                        .headers(CommonHeader)
                        .headers(GetHeader)
                        .check(currentLocationRegex(BaseURL + "(.+)").saveAs("currentPageUrl"))
                        .check(currentLocationRegex(BaseURL + """.*\/(.+)?""").saveAs("transactionName"))
                        .check(regex("<form method=.GET. action=.(.+?).>").find.optional.saveAs("action"))
                        .check(regex("""govuk-radios__input\" id=\".+?\" name=\"(.+?)\" type=\"radio\" value="(.+?)"""").ofType[(String, String)].findRandom.optional.saveAs("radioInput"))
                        .check(regex("""govuk-input.+\" id=\".+?\" name=\"(.+?)\" type=\"text\" (?:value=\"\">|aria-describedby)""").find.optional.saveAs("textInput"))
                        .check(regex("id=.postcode.").find.optional.saveAs("postcodeInput"))
                        .check(regex("""govuk-heading-m">\n +?<a class="govuk-link" href="/courts/(.+?)">""").findRandom.transform(str => str.replace("&amp;", "&")).optional.saveAs("courtURL"))
                        .check(regex("Sorry, we couldn't help you").find.optional.saveAs("sorryCantHelp")))

                    }
                  }
                }

                .pause(MinThinkTime seconds, MaxThinkTime seconds)

                //If the page has radio buttons, set the session variables for the next page request
                .doIfOrElse("${radioInput.exists()}") {
                  exec {
                    session =>
                      session
                        .set("paramName", session("radioInput").as[(String, String)]._1)
                        .set("paramValue", session("radioInput").as[(String, String)]._2)
                        .set("actionMethod", "POST")
                        .set("actionURL", session("currentPageUrl").as[String])
                  }
                } {
                  //If the page has a postcode text box, set the session variables for the next page request
                  doIf("${textInput.exists()}") {
                    doIfOrElse("${postcodeInput.exists()}") {
                      feed(postcodeFeeder)
                        .exec {
                          session =>
                            session
                              .set("paramName", session("textInput").as[String])
                              .set("paramValue", session("postcode").as[String])
                              .set("actionMethod", "GET")
                              .set("actionURL", session("action").as[String])
                        }
                    } {
                      //If the page has a non-postcode empty text box, set the session variables for the next page request
                      feed(searchTermFeeder)
                        .exec {
                          session =>
                            session
                              .set("paramName", session("textInput").as[String])
                              .set("paramValue", session("searchTerm").as[String])
                              .set("actionMethod", "GET")
                              .set("actionURL", session("action").as[String])
                        }
                    }
                  }
                }

            }

        }

      }

      //If the page (or API) contains one or more court URLs, get the randomly chosen URL
      .doIf("${courtURL.exists()}") {

        group("Fact_040_LoadCourtDetailsPage") {
          exec(http("Load Court Page")
            .get(BaseURL + "/courts/${courtURL}")
            .headers(CommonHeader)
            .headers(GetHeader)
            .check(regex("Telephone|Make a complaint:")))
        }

        .pause(MinThinkTime seconds, MaxThinkTime seconds)

      }

}