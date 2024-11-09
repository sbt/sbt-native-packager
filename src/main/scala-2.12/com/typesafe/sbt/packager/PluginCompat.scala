package com.typesafe.sbt.packager

import java.nio.file.{Path => NioPath}
import java.util.jar.Attributes
import sbt.*
import xsbti.FileConverter

object PluginCompat {
  type FileRef = java.io.File
  type ArtifactPath = java.io.File
  type Out = java.io.File
  type IncludeArtifact = Artifact => Boolean

  val artifactStr = sbt.Keys.artifact.key
  val moduleIDStr = sbt.Keys.moduleID.key
  def parseModuleIDStrAttribute(m: ModuleID): ModuleID = m
  def moduleIDToStr(m: ModuleID): ModuleID = m
  private[packager] def parseArtifactStrAttribute(a: Artifact): Artifact = a
  def artifactToStr(art: Artifact): Artifact = art

  private[packager] def toNioPath(a: Attributed[File])(implicit conv: FileConverter): NioPath =
    a.data.toPath()
  private[packager] def toNioPath(ref: File)(implicit conv: FileConverter): NioPath =
    ref.toPath()
  def toFile(a: Attributed[File])(implicit conv: FileConverter): File =
    a.data
  def toFile(ref: File)(implicit conv: FileConverter): File =
    ref
  private[packager] def artifactPathToFile(ref: File)(implicit conv: FileConverter): File =
    ref
  private[packager] def toArtifactPath(f: File)(implicit conv: FileConverter): ArtifactPath = f
  private[packager] def toNioPaths(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[NioPath] =
    cp.map(_.data.toPath()).toVector
  private[packager] def toFiles(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[File] =
    cp.map(_.data).toVector
  def toFileRefsMapping(mappings: Seq[(File, String)])(implicit conv: FileConverter): Seq[(FileRef, String)] =
    mappings
  def toFileRef(x: File)(implicit conv: FileConverter): FileRef =
    x
  private[packager] def getName(ref: File): String =
    ref.getName()
  private[packager] def getArtifactPathName(ref: File): String =
    ref.getName()
  private[packager] def classpathAttr = Attributes.Name.CLASS_PATH
  private[packager] def mainclassAttr = Attributes.Name.MAIN_CLASS
}
