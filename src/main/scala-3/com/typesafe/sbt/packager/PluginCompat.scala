package com.typesafe.sbt.packager

import java.io.File
import java.nio.file.{Path => NioPath}
import java.util.jar.Attributes
import sbt.*
import xsbti.{FileConverter, HashedVirtualFileRef, VirtualFile, VirtualFileRef}
import sbt.internal.RemoteCache

object PluginCompat {
  type FileRef = HashedVirtualFileRef
  type ArtifactPath = VirtualFileRef
  type Out = VirtualFile
  type IncludeArtifact = Any => Boolean

  val artifactStr = Keys.artifactStr
  val moduleIDStr = Keys.moduleIDStr
  def parseModuleIDStrAttribute(str: String): ModuleID =
    Classpaths.moduleIdJsonKeyFormat.read(str)
  def moduleIDToStr(m: ModuleID): String =
    Classpaths.moduleIdJsonKeyFormat.write(m)

  private[packager] def parseArtifactStrAttribute(str: String): Artifact =
    import sbt.librarymanagement.LibraryManagementCodec.ArtifactFormat
    import sjsonnew.support.scalajson.unsafe.*
    Converter.fromJsonUnsafe[Artifact](Parser.parseUnsafe(str))
  def artifactToStr(art: Artifact): String =
    import sbt.librarymanagement.LibraryManagementCodec.ArtifactFormat
    import sjsonnew.support.scalajson.unsafe.*
    CompactPrinter(Converter.toJsonUnsafe(art))

  private[packager] def toNioPath(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): NioPath =
    conv.toPath(a.data)
  private[packager] def toNioPath(ref: HashedVirtualFileRef)(using conv: FileConverter): NioPath =
    conv.toPath(ref)
  def toFile(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): File =
    toNioPath(a).toFile()
  def toFile(ref: HashedVirtualFileRef)(using conv: FileConverter): File =
    toNioPath(ref).toFile()
  private[packager] def artifactPathToFile(ref: VirtualFileRef)(using conv: FileConverter): File =
    conv.toPath(ref).toFile()
  private[packager] def toArtifactPath(f: File)(using conv: FileConverter): ArtifactPath =
    conv.toVirtualFile(f.toPath())
  private[packager] def toNioPaths(cp: Seq[Attributed[HashedVirtualFileRef]])(using
    conv: FileConverter
  ): Vector[NioPath] =
    cp.map(toNioPath).toVector
  private[packager] def toFiles(cp: Seq[Attributed[HashedVirtualFileRef]])(using conv: FileConverter): Vector[File] =
    toNioPaths(cp).map(_.toFile())
  def toFileRefsMapping(mappings: Seq[(File, String)])(using conv: FileConverter): Seq[(FileRef, String)] =
    mappings.map { case (f, name) => toFileRef(f) -> name }
  def toFileRef(x: File)(using conv: FileConverter): FileRef =
    conv.toVirtualFile(x.toPath())
  private[packager] def getName(ref: FileRef): String =
    ref.name()
  private[packager] def getArtifactPathName(ref: ArtifactPath): String =
    ref.name()
  private[packager] def classpathAttr: String = Attributes.Name.CLASS_PATH.toString()
  private[packager] def mainclassAttr: String = Attributes.Name.MAIN_CLASS.toString()
}
