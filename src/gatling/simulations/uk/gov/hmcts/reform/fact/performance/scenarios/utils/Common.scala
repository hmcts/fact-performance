package uk.gov.hmcts.reform.fact.performance.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object Common {

  val rnd = new Random()

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

}