package com.typesafe.sbt

import org.scalatest._

package object packager {

  object UniversalTag extends Tag("universal")
  object LinuxTag extends Tag("linux")
  object WindowsTag extends Tag("windows")
}
