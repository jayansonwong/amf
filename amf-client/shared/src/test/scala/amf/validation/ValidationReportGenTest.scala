package amf.validation

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.remote.{Hint, Oas, Raml, RamlYamlHint}
import amf.core.validation.AMFValidationReport
import amf.facades.{AMFCompiler, Validation}
import amf.io.FileAssertionTest
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.Future

trait ValidationReportGenTest extends AsyncFunSuite with FileAssertionTest {

  val basePath: String
  val reportsPath: String
  val hint: Hint

  private lazy val defaultProfile: ProfileName = hint.vendor match {
    case Raml => ProfileNames.RAML
    case Oas  => ProfileNames.OAS
    case _    => ProfileNames.AMF
  }

  protected def generate(report: AMFValidationReport): String = {
    report.toString
  }

  protected def handleReport(report: AMFValidationReport, golden: Option[String]): Future[Assertion] =
    golden match {
      case Some(g) =>
        writeTemporaryFile(golden.get)(generate(report)).flatMap(assertDifferences(_, reportsPath + golden.get))
      case None =>
        Future.successful({
          if (!report.conforms) fail("Report not conforms:\n" + report.toString)
          if (report.results.nonEmpty)
            fail("Report conforms but there is results, probably some warnings\n:" + report.toString)
          succeed
        })
      // i have to check results in order to check warnings. If you pass none, and you are expeting some warnings, you should use the file assertion to check the warnings messages.
    }

  protected def validate(api: String,
                         golden: Option[String] = None,
                         profile: ProfileName = defaultProfile,
                         profileFile: Option[String] = None): Future[Assertion] = {
    for {
      validation <- Validation(platform)
      _          <- if (profileFile.isDefined) validation.loadValidationProfile(basePath + profileFile.get) else Future.unit
      model      <- AMFCompiler(basePath + api, platform, RamlYamlHint, validation).build()
      report     <- validation.validate(model, profile)
      r          <- handleReport(report, golden)
    } yield {
      r
    }
  }
}

trait ValidModelTest extends ValidationReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = ""

  protected def checkValid(api: String, profile: ProfileName = ProfileNames.RAML): Future[Assertion] =
    super.validate(api, None, profile, None)

}