package com.typesafe.sbt.packager

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
import java.io.IOException

class DeleteDirectoryVisitor extends SimpleFileVisitor[Path] {

  override def visitFile(file: Path, attrs: BasicFileAttributes) = {
    Files delete file
    FileVisitResult.CONTINUE
  }

  override def postVisitDirectory(dir: Path, exc: IOException) = {
    Files delete dir
    FileVisitResult.CONTINUE
  }

}
