import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

packageArchetype.java_application

name := "docker-test"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

dockerUpdateLatest := true
