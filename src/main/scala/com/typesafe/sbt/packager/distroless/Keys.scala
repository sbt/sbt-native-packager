package com.typesafe.sbt.packager
package distroless

import sbt.{*, given}

trait Keys {

  val distrolessDebuggerPort =
    SettingKey[Option[Int]](
      "distrolessDebuggerPort",
      "If specified, and dockerCmd is left empty, it will add the JDWP argument using the defined port"
    )

}
