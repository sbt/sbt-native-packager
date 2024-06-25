package com.typesafe.sbt.packager

import com.typesafe.sbt.packager.archetypes.scripts._
import org.scalatest._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StartScriptMainClassConfigSpec extends AnyWordSpec with Matchers {

  "StartScriptMainClassConfig" should {

    "handle a single explicit main class" when {

      "main class is not discovered" in {
        StartScriptMainClassConfig.from(Some("mainClass"), Nil) should ===(SingleMain("mainClass"))
      }

      "main class is discovered" in {
        StartScriptMainClassConfig.from(Some("mainClass"), Seq("mainClass")) should ===(SingleMain("mainClass"))
      }
    }

    "handle an explicit main class with alternatives" when {

      "main class is not discovered" in {
        StartScriptMainClassConfig.from(Some("mainClass"), Seq("alternate")) should ===(
          ExplicitMainWithAdditional("mainClass", Seq("alternate"))
        )
      }

      "main class is discovered" in {
        StartScriptMainClassConfig.from(Some("mainClass"), Seq("mainClass", "alternate")) should ===(
          ExplicitMainWithAdditional("mainClass", Seq("alternate"))
        )
      }
    }

    "handle discovered main classes" when {

      "a single main class is discovered" in {
        StartScriptMainClassConfig.from(None, Seq("mainClass")) should ===(SingleMain("mainClass"))
      }

      "multiple main classes are discovered" in {
        StartScriptMainClassConfig.from(None, Seq("mainClass", "alternate")) should ===(
          MultipleMains(Seq("mainClass", "alternate"))
        )
      }
    }

    "handle no main classes" in {
      StartScriptMainClassConfig.from(None, Seq.empty) should ===(NoMain)
    }
  }
}
