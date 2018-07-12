package com.typesafe.sbt
package packager
package docker

/**
  * a single line in a dockerfile. See subclasses for more detail
  *
  */
trait CmdLike {

  /**
    * Creates the command which can be placed inside a Dockerfile.
    *
    * @return the docker command
    */
  def makeContent: String
}

/**
  * Executable command
  *
  * @example {{{
  * ExecCmd("RUN", "chown", "-R", daemonUser, ".")
  * }}}
  *
  * @example {{{
  * ExecCmd("ENTRYPOINT", "bin/%s" format execScript),
  * }}}
  *
  * @example {{{
  * ExecCmd("CMD")
  * }}}
  *
  * @example {{{
  * ExecCmd("VOLUME", exposedVolumes: _*)
  * }}}
  */
case class ExecCmd(cmd: String, args: String*) extends CmdLike {
  def makeContent: String =
    "%s [%s]\n" format (cmd, args.map('"' + _ + '"').mkString(", "))
}

/**
  * An arbitrary command
  *
  * @example
  * {{{
  *   val add = Cmd("ADD", "src/resource/LICENSE.txt", "/opt/docker/LICENSE.txt")
  * }}}
  *
  * @example
  * {{{
  *   val copy = Cmd("COPY", "src/resource/LICENSE.txt", "/opt/docker/LICENSE.txt")
  * }}}
  *
  * @example
  * {{{
  *   val env = Cmd("ENV", "APP_SECRET", "7sdfy7s9hfisdufuusud")
  * }}}
  */
case class Cmd(cmd: String, args: String*) extends CmdLike {
  def makeContent: String = "%s %s\n" format (cmd, args.mkString(" "))
}

/**
  * A command that consists of a CMD string and an CmdLike object
  *
  * @example
  * {{{
  *   val onBuildAdd = CombinedCmd("ONBUILD", Cmd("ADD", "src/resource/LICENSE.txt", "/opt/docker/LICENSE.txt"))
  * }}}
  *
  * @example
  * {{{
  *   val onBuildEnv = CombinedCmd("ONBUILD", Cmd("ENV", "APP_SECRET", "7sdfy7s9hfisdufuusud"))
  * }}}
  */
case class CombinedCmd(cmd: String, arg: CmdLike) extends CmdLike {
  def makeContent: String = "%s %s\n" format (cmd, arg.makeContent)
}

/** Represents dockerfile used by docker when constructing packages. */
case class Dockerfile(commands: CmdLike*) {
  def makeContent: String = {
    val sb = new StringBuilder
    commands foreach { sb append _.makeContent }
    sb toString
  }
}
