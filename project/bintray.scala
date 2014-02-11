import sbt._
import sbt.Keys._

object Bintray {
  val bintrayPublishAllStaged = TaskKey[Unit]("bintray-publish-all-staged", "Publish all staged artifacts on bintray.")
  val checkBintrayCredentials = TaskKey[Unit]("bintray-check-credentials", "Checks to see if bintray credentials are configured.")
  val bintrayPluginId = "sbt-plugin-releases"
  val bintrayPluginUrl = "https://api.bintray.com/content/sbt/sbt-plugin-releases/"
  val bintrayPluginLayout = "[module]/[revision]/"+ Resolver.localBasePattern

  def bintrayCreds(creds: Seq[sbt.Credentials]): (String, String) = {
    val matching = 
      for {
        c <- creds
        if c.isInstanceOf[sbt.DirectCredentials]
        val cred = c.asInstanceOf[sbt.DirectCredentials]
        if cred.host == "api.bintray.com"
      } yield cred.userName -> cred.passwd

    matching.headOption getOrElse sys.error("Unable to find bintray credentials (api.bintray.com)")
  }

  def publishContent(pkg: String, repo: String, version: String, creds: Seq[sbt.Credentials]): Unit = {
    val subject = "sbt" // Sbt org - TODO - don't hardcode
    val uri = s"https://bintray.com/api/v1/content/$subject/$repo/$pkg/$version/publish"
  
    val (u,p) = bintrayCreds(creds)
    import dispatch.classic._
    // TODO - Log the output
    Http(url(uri).POST.as(u,p).>|)
  }

  def settings: Seq[Setting[_]] = 
    Seq(
       publishTo := {
         val resolver = Resolver.url("bintray-"+bintrayPluginId, new URL(bintrayPluginUrl))(Patterns(false, bintrayPluginLayout))
         Some(resolver)
       },
       checkBintrayCredentials := {
       	 val creds = credentials.value
       	 val (user, _) = bintrayCreds(creds)
       	 streams.value.log.info(s"Using $user for bintray login.") 
       },
       bintrayPublishAllStaged := {
       	 val creds = credentials.value
       	 publishContent(projectID.value.name, bintrayPluginId, version.value, creds)
       }
    )
}