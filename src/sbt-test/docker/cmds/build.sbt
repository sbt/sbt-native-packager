import NativePackagerKeys._

packagerSettings

name := "simple-test"

version := "0.1.0"

dockerCmd in Docker := Seq("--debug", "-i", "123")

TaskKey[Unit]("check-dockerfile") <<= (target, streams) map { (target, out) =>
  val docker = IO.read(target / "docker" / "Dockerfile")
  assert(docker contains "CMD [\"--debug\", \"-i\", \"123\"]", "Wrong CMD section:\n" + docker)
  ()
}

