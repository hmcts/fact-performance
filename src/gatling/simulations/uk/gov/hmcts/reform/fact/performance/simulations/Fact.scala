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

  val rampUpDurationMins = 2
  val rampDownDurationMins = 2
  val testDurationMins = 5

  //Must be doubles to ensure the calculations result in doubles not rounded integers
  val factHourlyTarget:Double = 500

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