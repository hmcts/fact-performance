package uk.gov.hmcts.reform.fact.performance.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.Random

object Common {

  val rnd = new Random()
  val now = LocalDate.now()
  val patternYear = DateTimeFormatter.ofPattern("yyyy")
  val patternDate = DateTimeFormatter.ofPattern("yyyyMMdd")

  def randomString(length: Int) = {
    rnd.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getDate(): String = {
    now.format(patternDate)
  }

  def getDay(): String = {
    (1 + rnd.nextInt(28)).toString
  }

  def getMonth(): String = {
    (1 + rnd.nextInt(12)).toString
  }

  //Dob >= 25 years
  def getDobYear(): String = {
    now.minusYears(25 + rnd.nextInt(70)).format(patternYear)
  }
  //Dod <= 21 years
  def getDodYear(): String = {
    now.minusYears(1 + rnd.nextInt(20)).format(patternYear)
  }

  def getPostcode(): String = {
    randomString(2).toUpperCase() + rnd.nextInt(10).toString + " " + rnd.nextInt(10).toString + randomString(2).toUpperCase()
  }

  val ClearSessionVariables =
    exec(flushHttpCache)
    .exec(flushCookieJar)
    .exec(_.remove("state"))
    .exec(_.remove("emailAddress"))
    .exec(_.remove("authCode"))
    .exec(_.remove("ChargeId"))
    .exec(_.remove("rand"))
    .exec(_.remove("csrf"))
    .exec(_.remove("inviteId"))
    .exec(_.remove("currentPageUrl"))
    .exec(_.remove("appId"))
    .exec(_.remove("gatling.http.cache.baseUrl"))
    .exec(_.remove("role"))
    .exec(_.remove("gatling.http.referer"))
    .exec(_.remove("statusValue"))
    .exec(_.remove("pin"))
    .exec(_.remove("password"))
    .exec(_.remove("gatling.http.cookies"))
    .exec(_.remove("gatling.http.cache.contentCache"))
    .exec(_.remove("gatling.http.cache.redirects"))
    .exec(_.remove("gatling.http.ssl.sslContexts"))

}