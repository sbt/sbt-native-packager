import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader

// controls the name of the bash script
executableScriptName := "play-demo-run"

maintainer := "Maintainer <maintainer@example.org>"

packageSummary := "A demo RPM package of Play"

packageDescription := "A demonstration of using sbt-native-packager to package a Play app as an RPM"

// controls the logical name of the linux package
Linux / packageName := "play-demo"

Linux / daemonUser := "play-demo-user"

Linux / daemonGroup := "play-demo-group"

Linux / daemonShell := "/bin/bash"

// RPM settings

Rpm / serverLoading := Some(ServerLoader.SystemV)

rpmRelease := "1"

// Name of the vendor for this RPM.
rpmVendor := "DemoVendor"

rpmLicense := Some("Apache-2.0")

rpmEpoch := Some(1)
