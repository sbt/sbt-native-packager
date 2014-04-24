package com.typesafe.sbt
package packager
package docker

trait CmdLike {
  def makeContent: String
}

case class ExecCmd(cmd: String, args: String*) extends CmdLike {
  def makeContent = "%s [%s]\n" format (cmd, args.map('"' + _ + '"').mkString(", "))
}

case class Cmd(cmd: String, arg: String) extends CmdLike {
  def makeContent = "%s %s\n" format (cmd, arg)
}

/** Represents dockerfile used by docker when constructing packages. */
case class Dockerfile(commands: CmdLike*) {
  def makeContent: String = {
    val sb = new StringBuilder
    commands foreach { sb append _.makeContent }
    sb toString
  }
}
