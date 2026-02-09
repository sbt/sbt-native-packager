package com.typesafe.sbt.packager

import java.util.jar.Attributes
import sbt.*

object PluginCompat {
  type IncludeArtifact = Artifact => Boolean

  private[packager] def getName(ref: java.io.File): String =
    ref.getName()
  private[packager] def getArtifactPathName(ref: java.io.File): String =
    ref.getName()
  private[packager] def classpathAttr = Attributes.Name.CLASS_PATH
  private[packager] def mainclassAttr = Attributes.Name.MAIN_CLASS
}
