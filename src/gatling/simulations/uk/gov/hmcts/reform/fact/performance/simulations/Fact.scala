package uk.gov.hmcts.reform.fact.performance.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import uk.gov.hmcts.reform.fact.performance.scenarios.Fact
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.Environment
import uk.gov.hmcts.reform.fact.performance.scenarios._

import scala.concurrent.duration._
import scala.util.Random

class Fact extends Simulation {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")

  val rampUpDurationMins = 2
  val rampDownDurationMins = 2
  val testDurationMins = 60

  //Must be doubles to ensure the calculations result in doubles not rounded integers
  val probateHourlyTarget:Double = 88
  val intestacyHourlyTarget:Double = 12
  val caveatHourlyTarget:Double = 53

  val continueAfterEligibilityPercentage = 58

  val probateRatePerSec = probateHourlyTarget / 3600
  val intestacyRatePerSec = intestacyHourlyTarget / 3600
  val caveatRatePerSec = caveatHourlyTarget / 3600

  val randomFeeder = Iterator.continually( Map( "perc" -> Random.nextInt(100)))

  before{
    println(s"Total Test Duration: ${testDurationMins} minutes")
  }

  val ProbateNewApplication = scenario( "ProbateNewApplication")
    .feed(randomFeeder)
      .exitBlockOnFail {
        Fact.ProbateApplication
      }

  setUp(
    ProbateNewApplication.inject(
      rampUsersPerSec(0.00) to (probateRatePerSec) during (rampUpDurationMins minutes),
      constantUsersPerSec(probateRatePerSec) during (testDurationMins minutes),
      rampUsersPerSec(probateRatePerSec) to (0.00) during (rampDownDurationMins minutes)
    )
  )
    .protocols(httpProtocol)

}