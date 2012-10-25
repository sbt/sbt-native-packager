package com.typesafe.sbt
package packager
package windows

import Keys._
import sbt._

trait WindowsPlugin extends Plugin {
  val Windows = config("windows")
  
  def windowsSettings: Seq[Setting[_]] = Seq(
      sourceDirectory in Windows <<= sourceDirectory(_ / "windows"),
      target in Windows <<= target apply (_ / "windows"),
      name in Windows <<= name,
      lightOptions := Seq.empty,
      candleOptions := Seq.empty,
      wixFile <<= (wixConfig in Windows, name in Windows, target in Windows) map { (c, n, t) =>
        val f = t / (n + ".wxs")
        IO.write(f, c.toString)
        f
      }
  ) ++ inConfig(Windows)(Seq(
      // Disable windows generation by default.
      wixConfig := <wix/>,
      mappings := Seq.empty,
      mappings in packageMsi <<= mappings,
      packageMsi <<= (mappings in packageMsi, wixFile, name, target, candleOptions, lightOptions, streams) map {(m, f, n, t, co, lo, s) =>
        val msi = t / (n + ".msi")
        // First we have to move everything (including the wix file) to our target directory.
        val wix = t / (n + ".wix")
        if(f.getAbsolutePath != wix.getAbsolutePath) IO.copyFile(f, wix)
        IO.copy(for((f, to) <- m) yield (f, t / to)) 
        // Now compile WIX
        val wixdir = Option(System.getenv("WIX")) getOrElse sys.error("WIX environemnt not found.  Please ensure WIX is installed on this computer.")
        val candleCmd = Seq(wixdir + "\\bin\\candle.exe", wix.getAbsolutePath) ++ co
        s.log.debug(candleCmd mkString " ")
        Process(candleCmd, Some(t)) ! s.log match {
          case 0 => ()
          case x => sys.error("Unable to run WIX compilation to wixobj...")
        }
        // Now create MSI
        val wixobj = t / (n + ".wixobj")
        val lightCmd = Seq(wixdir + "\\bin\\light.exe", wixobj.getAbsolutePath) ++ lo
        s.log.debug(lightCmd mkString " ")
        Process(lightCmd, Some(t)) ! s.log match {
          case 0 => ()
          case x => sys.error("Unable to run build msi...")
        }
        msi
      }
  ))
}