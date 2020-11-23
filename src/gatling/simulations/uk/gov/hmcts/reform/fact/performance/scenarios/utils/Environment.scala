package uk.gov.hmcts.reform.fact.performance.scenarios.utils

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Environment {

  //val baseURL = "https://fact.perftest.platform.hmcts.net"
  val baseURL = "https://fact.aat.platform.hmcts.net"

  val minThinkTime = 1
  val maxThinkTime = 1

  val HttpProtocol = http

  val commonHeader = Map(
    "accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    "accept-encoding" -> "gzip, deflate, br",
    "sec-fetch-dest" -> "document",
    "sec-fetch-mode" -> "navigate",
    "sec-fetch-user" -> "?1",
    "upgrade-insecure-requests" -> "1",
    "user-agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36"
  )

  val getHeader = Map(
    "accept-language" -> "en-GB,en;q=0.9",
    "sec-fetch-site" -> "same-origin"
    )
  val postHeader = Map(
    "content-type" -> "application/x-www-form-urlencoded",
    "sec-fetch-site" -> "same-origin"
  )

}