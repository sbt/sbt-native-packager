{
  val pluginVersion = sys.props("project.version")
  if (pluginVersion == null)
    throw new RuntimeException("""|The system property 'project.version' is not defined.
               |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else
    addSbtPlugin("com.github.sbt" % "sbt-native-packager" % sys.props("project.version"))
}
