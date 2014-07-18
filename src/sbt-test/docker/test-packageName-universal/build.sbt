import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._

packageArchetype.java_application

name := "docker-test"

packageName := "docker-package" // sets the executable script, too

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

TaskKey[Unit]("check-dockerfile") <<= (target, streams) map { (target, out) =>
  val dockerfile = IO.read(target / "docker" / "Dockerfile")
  assert(dockerfile.contains("ENTRYPOINT [\"bin/docker-package\"]\n"), "dockerfile doesn't contain ENTRYPOINT [\"docker-package\"]\n" + dockerfile)
  out.log.success("Successfully tested control script")
  ()
}