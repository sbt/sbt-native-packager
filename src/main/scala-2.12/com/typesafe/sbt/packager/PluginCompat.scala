package com.typesafe.sbt.packager

import java.nio.file.{Path => NioPath}
import java.util.jar.Attributes
import sbt.*
import xsbti.FileConverter

private[packager] object PluginCompat {
  type FileRef = java.io.File
  type ArtifactPath = java.io.File
  type Out = java.io.File
  type IncludeArtifact = Artifact => Boolean

  val artifactStr = sbt.Keys.artifact.key
  val moduleIDStr = sbt.Keys.moduleID.key
  def parseModuleIDStrAttribute(m: ModuleID): ModuleID = m
  def moduleIDToStr(m: ModuleID): ModuleID = m
  def parseArtifactStrAttribute(a: Artifact): Artifact = a
  def artifactToStr(art: Artifact): Artifact = art

  def toNioPath(a: Attributed[File])(implicit conv: FileConverter): NioPath =
    a.data.toPath()
  def toNioPath(ref: File)(implicit conv: FileConverter): NioPath =
    ref.toPath()
  def toFile(a: Attributed[File])(implicit conv: FileConverter): File =
    a.data
  def toFile(ref: File)(implicit conv: FileConverter): File =
    ref
  def artifactPathToFile(ref: File)(implicit conv: FileConverter): File =
    ref
  def toArtifactPath(f: File)(implicit conv: FileConverter): ArtifactPath = f
  def toNioPaths(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[NioPath] =
    cp.map(_.data.toPath()).toVector
  def toFiles(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[File] =
    cp.map(_.data).toVector
  def toFileRef(x: File)(implicit conv: FileConverter): FileRef =
    x
  def getName(ref: File): String =
    ref.getName()
  def getArtifactPathName(ref: File): String =
    ref.getName()
  def classpathAttr = Attributes.Name.CLASS_PATH
  def mainclassAttr = Attributes.Name.MAIN_CLASS
}
