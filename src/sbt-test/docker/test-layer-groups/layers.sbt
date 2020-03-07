dockerLayerGrouping in Docker := {
  val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
  (path: String) =>
    {
      val pathInWorkdir = path.stripPrefix(dockerBaseDirectory)
      if (pathInWorkdir.startsWith(s"/lib/${organization.value}"))
        Some(2)
      else if (pathInWorkdir.startsWith("/bin/"))
        Some(123)
      else if (pathInWorkdir.startsWith("/spark/"))
        Some(54)
      else None
    }
}
