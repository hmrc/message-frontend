import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {
  private val bootstrapVersion = "10.7.0"

  val appDependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "bootstrap-test-play-30"     % bootstrapVersion % Test
  )
}
