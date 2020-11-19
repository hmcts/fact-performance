package uk.gov.hmcts.reform.fact.performance.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import uk.gov.hmcts.reform.fact.performance.scenarios._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.Environment
import scala.concurrent.duration._

class Fact extends Simulation {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    //.basicAuth("hmctsfact", "fact")
    .inferHtmlResources()
    .silentResources

  val rampUpDurationMins = 5
  val rampDownDurationMins = 5
  val testDurationMins = 60

  //Must be doubles to ensure the calculations result in doubles not rounded integers
  val factHourlyTarget:Double = 6000 //was 5200, but not all journeys end in a court details page, so need to bulk it up to reach targets

  val factRatePerSec = factHourlyTarget / 3600


  before{
    println(s"Total Test Duration: ${testDurationMins} minutes")
  }

  val FactSimulation = scenario( "FactSimulation")
    .exec(FactScenario.FactJourney)

/*
  setUp(
    FactSimulation.inject(atOnceUsers(1))
  ).protocols(httpProtocol)
*/

  setUp(
    FactSimulation.inject(
      rampUsersPerSec(0.00) to (factRatePerSec) during (rampUpDurationMins minutes),
      constantUsersPerSec(factRatePerSec) during (testDurationMins minutes),
      rampUsersPerSec(factRatePerSec) to (0.00) during (rampDownDurationMins minutes)
    )
  )
    .protocols(httpProtocol)

}