package uk.gov.hmcts.reform.fact.performance.simulations

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import uk.gov.hmcts.reform.fact.performance.scenarios._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.Environment

class Fact_Jenkins extends Simulation {

  val BaseURL = Environment.baseURL

  val httpProtocol = Environment.HttpProtocol
    .baseUrl(BaseURL)
    .doNotTrackHeader("1")
    .inferHtmlResources()
    .silentResources

  val FactSimulation = scenario( "FactSimulation")
    .exec(FactScenario.FactNameKnown)
    .exec(FactScenario.FactNameNotKnown)

  setUp(
    FactSimulation.inject(atOnceUsers(1))
  ).protocols(httpProtocol)

}