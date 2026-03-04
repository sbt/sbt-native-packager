package com.typesafe.sbt.packager

import java.util.jar.Attributes
import sbt.*
import xsbti.{HashedVirtualFileRef, VirtualFileRef}

object PluginCompat {
  type IncludeArtifact = Any => Boolean

  private[packager] def getName(ref: HashedVirtualFileRef): String =
    ref.name()
  private[packager] def getArtifactPathName(ref: VirtualFileRef): String =
    ref.name()
  private[packager] def classpathAttr: String = Attributes.Name.CLASS_PATH.toString()
  private[packager] def mainclassAttr: String = Attributes.Name.MAIN_CLASS.toString()
}
