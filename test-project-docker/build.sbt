enablePlugins(JavaAppPackaging)

name := "docker-test"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

dockerRawWithOriginalUser := """
RUN [ "grep newguy /etc/passwd && echo user exist || useradd newguy" ]
RUN [ "echo $USER > /tmp/test" ]
"""

daemonUser := "newguy"

dockerRawWithDaemonUser := """
RUN [ "touch", "/tmp/newguy-testfile" ]
"""