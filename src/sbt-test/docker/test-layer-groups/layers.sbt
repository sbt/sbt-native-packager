dockerGroupLayers in Docker := {
  val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
  (dockerGroupLayers in Docker).value.orElse {
    case (_, path) if path.startsWith(dockerBaseDirectory + "/spark/") => 54
  }
}
