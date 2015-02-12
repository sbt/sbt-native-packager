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
  def makeContent = "%s [%s]\n" format (cmd, args.map('"' + _ + '"').mkString(", "))
}

/**
 * An arbitrary command
 *
 * @example
 * {{{
 *   val add = Cmd("ADD", "src/resource/LICENSE.txt /opt/docker/LICENSE.txt")
 * }}}
 */
case class Cmd(cmd: String, arg: String) extends CmdLike {
  def makeContent = "%s %s\n" format (cmd, arg)
}

/**
 * An raw command
 *
 * @example
 * {{{
 *   val raw = Cmd("""
 *                 | RUN [ "grep newguy /etc/passwd && echo user exist || useradd newguy" ]
 *                 | # a comment
 *                 | ADD /a/b  /c/d
 *                 """.stripMargin)
 * }}}
 */
case class Raw(script: String) extends CmdLike {
  def makeContent = script + "\n"
}

/** Represents dockerfile used by docker when constructing packages. */
case class Dockerfile(commands: CmdLike*) {
  def makeContent: String = {
    val sb = new StringBuilder
    commands foreach { sb append _.makeContent }
    sb toString
  }
}
