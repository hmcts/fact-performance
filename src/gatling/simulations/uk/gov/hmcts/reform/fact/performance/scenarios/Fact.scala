package uk.gov.hmcts.reform.fact.performance.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.{Common, Environment}
import java.io.{BufferedWriter, FileWriter}

import uk.gov.hmcts.reform.fact.performance.scenarios.checks.CsrfCheck
import uk.gov.hmcts.reform.fact.performance.scenarios.utils.{Common, Environment}

import scala.concurrent.duration._
import scala.util.Random

object Fact {

  val BaseURL = Environment.baseURL

  val MinThinkTime = Environment.minThinkTime
  val MaxThinkTime = Environment.maxThinkTime

  val CommonHeader = Environment.commonHeader
  val GetHeader = Environment.getHeader
  val PostHeader = Environment.postHeader

  val rnd = new Random()

  val ProbateEligibility =

    exec(http("Probate_010_StartEligibility")
      .get(BaseURL + "/death-certificate")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(CsrfCheck.save)
      .check(regex("Do you have a death certificate")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_020_DeathCertificateSubmit")
      .post(BaseURL + "/death-certificate")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("deathCertificate", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Did the person who died live permanently")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_030_DomicileSubmit")
      .post(BaseURL + "/deceased-domicile")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("domicile", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Has an Inheritance Tax")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_040_IHTSubmit")
      .post(BaseURL + "/iht-completed")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("completed", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Did the person who died leave a will")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_050_WillLeftSubmit")
      .post(BaseURL + "/will-left")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("left", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Do you have the original will")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_060_WillOriginalSubmit")
      .post(BaseURL + "/will-original")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("original", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Are you named as an executor")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_070_NamedExecutorSubmit")
      .post(BaseURL + "/applicant-executor")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("executor", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Are all the executors able to make their own decisions")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_080_MentalCapacitySubmit")
      .post(BaseURL + "/mental-capacity")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("mentalCapacity", "optionYes")
      .check(regex("a href=./get-case/([0-9]+).probateType=PA").find.saveAs("appId"))
      .check(regex("In progress"))
      .check(status.saveAs("statusValue")))
    //Write out the email address, case id and type to a csv file
    .doIf(session => session("statusValue").as[String].contains("200")) {
      exec {
        session =>
          val fw = new BufferedWriter(new FileWriter("EmailAndCaseID.csv", true))
          try {
            fw.write(session("emailAddress").as[String] + "," + session("appId").as[String] + ",PA\r\n")
          }
          finally fw.close()
          session
      }
    }

    .exec {
      session =>
        println("EXEC1 EMAIL: " + session("emailAddress").as[String])
        println("CASE ID: " + session("appId").as[String])
        println("APPLICATION TYPE: PA")
        session
    }

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    //At this point, the user will be redirected to their dashboard, listing the new application as 'In progress'

  val ProbateApplication =

    exec(http("Probate_090_ContinueApplication")
      .get(BaseURL + "/get-case/${appId}?probateType=PA")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("Complete these steps")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_100_SectionOneStart")
      .get(BaseURL + "/bilingual-gop")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(CsrfCheck.save)
      .check(regex("Do you require a bilingual grant")))

      .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_110_BilingualGrantSubmit")
      .post(BaseURL + "/bilingual-gop")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("bilingual", "optionNo")
      .check(CsrfCheck.save)
      .check(regex("full name of the person who died")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_120_DeceasedNameSubmit")
      .post(BaseURL + "/deceased-name")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("firstName", "Perf" + Common.randomString(5))
      .formParam("lastName", "Test" + Common.randomString(5))
      .check(CsrfCheck.save)
      .check(regex("What was their date of birth")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_130_DeceasedDOBSubmit")
      .post(BaseURL + "/deceased-dob")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("dob-day", Common.getDay())
      .formParam("dob-month", Common.getMonth())
      .formParam("dob-year", Common.getDobYear())
      .check(CsrfCheck.save)
      .check(regex("What was the date that they died")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_140_DeceasedDODSubmit")
      .post(BaseURL + "/deceased-dod")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("dod-day", Common.getDay())
      .formParam("dod-month", Common.getMonth())
      .formParam("dod-year", Common.getDodYear())
      .check(CsrfCheck.save)
      .check(regex("What was the permanent address")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_150_DeceasedAddressSubmit")
      .post(BaseURL + "/deceased-address")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("addressLine1", rnd.nextInt(1000).toString + " Perf" + Common.randomString(5)  + " Road")
      .formParam("addressLine2", "")
      .formParam("addressLine3", "")
      .formParam("postTown", "Perf " + Common.randomString(5) + " Town")
      .formParam("newPostCode", Common.getPostcode())
      .formParam("country", "")
      .check(CsrfCheck.save)
      .check(regex("Upload the death certificate")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_160_DocumentUpload")
      .post(BaseURL + "/document-upload")
      .header("Accept", "application/json")
      .header("Accept-Encoding", "gzip, deflate, br")
      .header("Accept-Language", "en-GB,en;q=0.5")
      .header("Content-Type", "multipart/form-data")
      .header("TE", "Trailers")
      .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36")
      .header("x-csrf-token", "${csrf}")
      .header("X-Requested-With", "XMLHttpRequest")
      .formParam("isUploadingDocument", "true")
      .bodyPart(RawFileBodyPart("file", "2MB.pdf")
        .fileName("2MB.pdf")
        .transferEncoding("binary"))
      .asMultipartForm
      .check(status.is(200))
      .check(currentLocation.is(BaseURL + "/document-upload"))
      .check(regex("/document-upload/remove/0.>Remove</a>"))
      .check(regex("Upload the death certificate")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_170_DocumentUploadSubmit")
      .post(BaseURL + "/document-upload")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .check(CsrfCheck.save)
      .check(regex("How was the Inheritance Tax")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_180_IHTMethodSubmit")
      .post(BaseURL + "/iht-method")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("method", "optionPaper")
      .check(CsrfCheck.save)
      .check(regex("Which paper form was filled in")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_190_IHTPaperSubmit")
      .post(BaseURL + "/iht-paper")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("form", "optionIHT205")
      .formParam("grossValueFieldIHT205", "8000")
      .formParam("netValueFieldIHT205", "8000")
      .formParam("grossValueFieldIHT207", "")
      .formParam("netValueFieldIHT207", "")
      .formParam("grossValueFieldIHT400421", "")
      .formParam("netValueFieldIHT400421", "")
      .check(CsrfCheck.save)
      .check(regex("have assets in another name")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_200_DeceasedAliasSubmit")
      .post(BaseURL + "/deceased-alias")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("alias", "optionNo")
      .check(CsrfCheck.save)
      .check(regex("get married or enter into a civil partnership")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_210_DeceasedMarriedSubmit")
      .post(BaseURL + "/deceased-married")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("married", "optionNo")
      .check(CsrfCheck.save)
      .check(regex("Were any updates")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_220_WillCodicilsSubmit")
      .post(BaseURL + "/will-codicils")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("codicils", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("How many updates")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_230_WillNumberSubmit")
      .post(BaseURL + "/codicils-number")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("codicilsNumber", "1")
      .check(regex("Complete these steps"))
      .check(regex("""1.</span> Tell us about the person who has died\n    </h2>\n    \n        <span class="govuk-tag task-completed">Completed</span>""")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_240_SectionTwoStart")
      .get(BaseURL + "/applicant-name")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(CsrfCheck.save)
      .check(regex("What is your full name")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_250_ApplicantNameSubmit")
      .post(BaseURL + "/applicant-name")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("firstName", "Perf" + Common.randomString(5))
      .formParam("lastName", "ExecOne" + Common.randomString(5))
      .check(CsrfCheck.save)
      .check(regex("exactly what appears on the will")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_260_ApplicantNameAsOnWillSubmit")
      .post(BaseURL + "/applicant-name-as-on-will")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("nameAsOnTheWill", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("What is your phone number")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_270_ApplicantPhoneSubmit")
      .post(BaseURL + "/applicant-phone")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("phoneNumber", "07000000000")
      .check(CsrfCheck.save)
      .check(regex("What is your address")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_280_ApplicantAddressSubmit")
      .post(BaseURL + "/applicant-address")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("addressLine1", rnd.nextInt(1000).toString + " Perf" + Common.randomString(5)  + " Road")
      .formParam("addressLine2", "")
      .formParam("addressLine3", "")
      .formParam("postTown", "Perf " + Common.randomString(5) + " Town")
      .formParam("newPostCode", Common.getPostcode())
      .formParam("country", "")
      .check(CsrfCheck.save)
      .check(regex("How many past and present executors")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_290_ExecutorsNumberSubmit")
      .post(BaseURL + "/executors-number")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("executorsNumber", "2")
      .check(CsrfCheck.save)
      .check(regex("What are the executors")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_300_ExecutorsNamesSubmit")
      .post(BaseURL + "/executors-names")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("executorName[0]", "Perf Exec Two")
      .check(CsrfCheck.save)
      .check(regex("Are all the executors alive")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_310_ExecutorsAllAliveSubmit")
      .post(BaseURL + "/executors-all-alive")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("allalive", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Will any of the other executors be dealing with the estate")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_320_OtherExecutorsSubmit")
      .post(BaseURL + "/other-executors-applying")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("otherExecutorsApplying", "optionYes")
      .check(CsrfCheck.save)
      .check(regex("Which executors will be dealing with the estate")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_330_ExecutorsDealingSubmit")
      .post(BaseURL + "/executors-dealing-with-estate")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("executorsApplying[]", "Perf Exec Two")
      .check(CsrfCheck.save)
      .check(regex("Do any of these executors now have a different name")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_340_ExecutorsAliasSubmit")
      .post(BaseURL + "/executors-alias")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("alias", "optionNo")
      .check(CsrfCheck.save)
      .check(regex("email address and mobile number")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_350_ExecTwoContactDetailsSubmit")
      .post(BaseURL + "/executor-contact-details/1")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("email", "exec-two@perftest" + Common.randomString(8) + ".com")
      .formParam("mobile", "07000000001")
      .check(CsrfCheck.save)
      .check(regex("permanent address")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_360_ExecTwoAddressSubmit")
      .post(BaseURL + "/executor-address/1")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("addressLine1", rnd.nextInt(1000).toString + " Perf" + Common.randomString(5) + " Road")
      .formParam("addressLine2", "")
      .formParam("addressLine3", "")
      .formParam("postTown", "Perf " + Common.randomString(5) + " Town")
      .formParam("newPostCode", Common.getPostcode())
      .formParam("country", "")
      //PCQ (Equality/diversity survey) might pop up at this point, so cater for either outcome in the text check
      .check(regex("2.</span> Give details about the executors(?s).*?<span class=.govuk-tag task-completed.>Completed</span>|Equality and diversity questions")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_370_SectionThreeStart")
      .get(BaseURL + "/summary/declaration")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("Check your answers")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_380_Declaration")
      .get(BaseURL + "/declaration")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(CsrfCheck.save)
      .check(regex("Check the legal statement and make your declaration")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_390_DeclarationSubmit")
      .post(BaseURL + "/declaration")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .formParam("declarationCheckbox", "true")
      .check(CsrfCheck.save)
      .check(regex("Notify the other executors")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    .exec(http("Probate_400_ExecutorsInviteSubmit")
      .post(BaseURL + "/executors-invite")
      .headers(CommonHeader)
      .headers(PostHeader)
      .formParam("_csrf", "${csrf}")
      .check(regex("Complete these steps"))
      .check(regex("Not declared")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

    //Get the invite ID associated with the second executor

    .exec(http("Probate_Util_InviteIdList")
      .get(BaseURL + "/inviteIdList")
      .headers(CommonHeader)
      .headers(GetHeader)
      .check(regex("\\\"ids\\\":\\[\\\"(.+?)\\\"").saveAs("inviteId")))

    .pause(MinThinkTime seconds, MaxThinkTime seconds)

}