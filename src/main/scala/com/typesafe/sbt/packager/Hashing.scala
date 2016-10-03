package com.typesafe.sbt
package packager

import java.io.File

object Hashing {
  def sha1Sum(t: File): String =
    messageDigestHex(java.security.MessageDigest.getInstance("SHA-1"))(t)

  def sha512(t: File): String =
    messageDigestHex(java.security.MessageDigest.getInstance("SHA-512"))(t)

  def md5Sum(t: File): String =
    messageDigestHex(java.security.MessageDigest.getInstance("MD5"))(t)

  def messageDigestHex(md: java.security.MessageDigest)(file: File): String = {
    val in = new java.io.FileInputStream(file);
    val buffer = new Array[Byte](8192)
    try {
      def read(): Unit = in.read(buffer) match {
        case x if x <= 0 => ()
        case size => md.update(buffer, 0, size); read()
      }
      read()
    } finally in.close()
    convertToHex(md.digest)
  }

  def convertToHex(data: Array[Byte]): String = {
    //TODO - use java.lang.Integer.toHexString()  ?
    val buf = new StringBuffer
    def byteToHex(b: Int) =
      if ((0 <= b) && (b <= 9)) ('0' + b).toChar
      else ('a' + (b - 10)).toChar
    for (i <- 0 until data.length) {
      buf append byteToHex((data(i) >>> 4) & 0x0F)
      buf append byteToHex(data(i) & 0x0F)
    }
    buf.toString
  }
}
