import com.typesafe.sbt.packager.archetypes.ServerLoader

// controls the name of the bash script
executableScriptName := "play-demo-run"

maintainer := "Maintainer <maintainer@example.org>"

packageSummary := "A demo RPM package of Play"

packageDescription := "A demonstration of using sbt-native-packager to package a Play app as an RPM"

// controls the logical name of the linux package
packageName in Linux := "play-demo"

daemonUser in Linux := "play-demo-user"

daemonGroup in Linux := "play-demo-group"

daemonShell in Linux := "/bin/bash"

// RPM settings

serverLoading in Rpm := ServerLoader.SystemV

rpmRelease := "1"

// Name of the vendor for this RPM.
rpmVendor := "DemoVendor"

rpmLicense := Some("Apache-2.0")
