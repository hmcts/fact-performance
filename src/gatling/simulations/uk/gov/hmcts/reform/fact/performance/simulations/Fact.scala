package uk.gov.hmcts.reform.fact.performance.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import uk.gov.hmcts.reform.fact.performance.scenarios._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.{Environment, Common}

class Fact extends Simulation {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .basicAuth("hmctsfact", "fact")

  val FactSimulation = scenario( "FactSimulation")
    .exec(FactScenario.FactJourney)

  setUp(
    FactSimulation.inject(atOnceUsers(1))
  ).protocols(httpProtocol)

}