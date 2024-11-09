package com.typesafe.sbt.packager

import java.nio.file.{Path => NioPath}
import java.util.jar.Attributes
import sbt.*
import xsbti.{FileConverter, HashedVirtualFileRef, VirtualFile}

private[packager] object PluginCompat {
  type FileRef = java.io.File
  type ArtifactPath = java.io.File
  type Out = java.io.File

  def toNioPath(a: Attributed[File])(implicit conv: FileConverter): NioPath =
    a.data.toPath()
  def toFile(a: Attributed[File])(implicit conv: FileConverter): File =
    a.data
  def artifactPathToFile(ref: File)(implicit conv: FileConverter): File =
    ref
  def toNioPaths(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[NioPath] =
    cp.map(_.data.toPath()).toVector
  def toFiles(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[File] =
    cp.map(_.data).toVector

  def classpathAttr = Attributes.Name.CLASS_PATH
  def mainclassAttr = Attributes.Name.MAIN_CLASS
}
