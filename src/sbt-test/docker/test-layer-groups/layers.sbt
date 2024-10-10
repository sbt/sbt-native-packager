(Docker / dockerGroupLayers) := {
  val dockerBaseDirectory = ((Docker / defaultLinuxInstallLocation)).value
  ((Docker / dockerGroupLayers)).value.orElse {
    case (_, path) if path.startsWith(dockerBaseDirectory + "/spark/") => 54
  }
}
