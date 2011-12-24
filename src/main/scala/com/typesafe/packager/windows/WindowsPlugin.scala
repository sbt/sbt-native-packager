package com.typesafe.packager.windows

import Keys._
import sbt._

trait WindowsPlugin extends Plugin {
  val Windows = config("windows")
  
  def windowsSettings: Seq[Setting[_]] = Seq(
      sourceDirectory in Windows <<= sourceDirectory(_ / "windows"),
      target in Windows <<= target apply (_ / "windows"),
      name in Windows <<= name,
      wixFile <<= (wixConfig in Windows, name in Windows, target in Windows) map { (c, n, t) =>
        val f = t / (n + ".wxs")
        IO.write(f, c.toString)
        f
      }
  ) ++ inConfig(Windows)(Seq(
      mappings := Seq.empty,
      mappings in packageMsi <<= mappings.identity,
      packageMsi <<= (mappings in packageMsi, wixFile, name, target, streams) map {(m, f, n, t, s) =>
        val msi = t / (n + ".msi")
        // First we have to move everything (including the wix file) to our target directory.
        val wix = t / (n + ".wix")
        if(f.getAbsolutePath != wix.getAbsolutePath) IO.copyFile(f, wix)
        IO.copy(for((f, to) <- m) yield (f, t / to)) 
        // Now compile WIX
        Process(Seq("cmd", "/c", "candle", wix.getAbsolutePath), Some(t)) ! s.log match {
          case 0 => ()
          case x => error("Unable to run WIX compilation to wixobj...")
        }
        // Now create MSI
        val wixobj = t / (n + ".wixobj")
        Process(Seq("cmd", "/c", "light", wixobj.getAbsolutePath), Some(t)) ! s.log match {
          case 0 => ()
          case x => error("Unable to run build msi...")
        }
        msi
      }
  ))
}