import sbt._
object PluginDef extends Build {
  override def projects = Seq(root)
  lazy val root = Project("plugins", file(".")) dependsOn(ghpages)
  // move back to josh's repo after pull requests for git branch key
  // support goes through
  lazy val ghpages = uri("git://github.com/jsuereth/xsbt-ghpages-plugin.git")
}