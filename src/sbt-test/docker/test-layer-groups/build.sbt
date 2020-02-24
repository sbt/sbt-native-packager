
enablePlugins(JavaAppPackaging)

organization := "com.example"
name := "docker-groups"
version := "0.1.0"

dockerLayerGrouping in Docker := {
  val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
  (path: String) =>
  {
    val pathInWorkdir = path.stripPrefix(dockerBaseDirectory)
    if (pathInWorkdir.startsWith(s"/lib/${organization.value}"))
      2
    else if (pathInWorkdir.startsWith("/bin/"))
      123
    else 0
  }
}
