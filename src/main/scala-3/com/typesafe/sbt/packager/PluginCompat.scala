package com.typesafe.sbt.packager

import java.io.File
import java.nio.file.{Path => NioPath}
import java.util.jar.Attributes
import sbt.*
import xsbti.{FileConverter, HashedVirtualFileRef, VirtualFile, VirtualFileRef}
import sbt.internal.RemoteCache

private[packager] object PluginCompat:
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

  def parseArtifactStrAttribute(str: String): Artifact =
    import sbt.librarymanagement.LibraryManagementCodec.ArtifactFormat
    import sjsonnew.support.scalajson.unsafe.*
    Converter.fromJsonUnsafe[Artifact](Parser.parseUnsafe(str))
  def artifactToStr(art: Artifact): String =
    import sbt.librarymanagement.LibraryManagementCodec.ArtifactFormat
    import sjsonnew.support.scalajson.unsafe.*
    CompactPrinter(Converter.toJsonUnsafe(art))

  def toNioPath(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): NioPath =
    conv.toPath(a.data)
  def toNioPath(ref: HashedVirtualFileRef)(using conv: FileConverter): NioPath =
    conv.toPath(ref)
  inline def toFile(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): File =
    toNioPath(a).toFile()
  inline def toFile(ref: HashedVirtualFileRef)(using conv: FileConverter): File =
    toNioPath(ref).toFile()
  def artifactPathToFile(ref: VirtualFileRef)(using conv: FileConverter): File =
    conv.toPath(ref).toFile()
  def toArtifactPath(f: File)(using conv: FileConverter): ArtifactPath =
    conv.toVirtualFile(f.toPath())
  def toNioPaths(cp: Seq[Attributed[HashedVirtualFileRef]])(using conv: FileConverter): Vector[NioPath] =
    cp.map(toNioPath).toVector
  inline def toFiles(cp: Seq[Attributed[HashedVirtualFileRef]])(using conv: FileConverter): Vector[File] =
    toNioPaths(cp).map(_.toFile())
  def toFileRef(x: File)(using conv: FileConverter): FileRef =
    conv.toVirtualFile(x.toPath())
  def getName(ref: FileRef): String =
    ref.name()
  def getArtifactPathName(ref: ArtifactPath): String =
    ref.name()
  def classpathAttr: String = Attributes.Name.CLASS_PATH.toString()
  def mainclassAttr: String = Attributes.Name.MAIN_CLASS.toString()
end PluginCompat
