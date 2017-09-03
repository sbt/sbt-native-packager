name := "test-project-windows"
version := "0.2.0"
libraryDependencies ++= Seq("com.typesafe" % "config" % "1.2.1")

mainClass in Compile := Some("ExampleApp")

enablePlugins(JavaAppPackaging)


maintainer := "some-company <someofus@some-company.com>"
packageSummary := "some application"
packageDescription := """ Some useful description here """
version := "0.0.0.0"

wixProductId := "ce07be71-510d-414a-92d4-dff47631848b"
wixProductUpgradeId := "4552fb0e-e257-4dbd-9ecb-dba9dbacf425"
wixMajorVersion := 3
lightOptions := Seq("-ext", "WixUiExtension")


// these settings are conflicting
javaOptions in Universal ++= Seq("-J-Xmx64m", "-J-Xms64m", "-jvm-debug 12345")

//bashScriptConfigLocation := Some("${app_home}/../conf/jvmopts")
