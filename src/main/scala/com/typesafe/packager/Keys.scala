package com.typesafe.packager

object Keys extends linux.Keys 
  with debian.DebianKeys 
  with rpm.RpmKeys 
  with windows.WindowsKeys
  with universal.UniversalKeys {}