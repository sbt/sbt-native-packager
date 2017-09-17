package com.typesafe.sbt.packager.archetypes.scripts

import org.scalatest.{FlatSpec, Matchers}

class ScriptUtilsTest extends FlatSpec with Matchers {
  "toLowerCase()" should "convert regular names" in {
    ScriptUtils.toLowerCase("package.TestClass") should be("package.test-class")
  }

  it should "convert regular names with single-lettered words" in {
    ScriptUtils.toLowerCase("package.ATestClass") should be("package.a-test-class")
    ScriptUtils.toLowerCase("package.FindAClass") should be("package.find-a-class")
  }

  it should "convert names with abbreviations" in {
    ScriptUtils.toLowerCase("package.XMLParser") should be("package.xml-parser")
    ScriptUtils.toLowerCase("package.AnXMLParser") should be("package.an-xml-parser")
  }

  it should "convert names with numbers" in {
    ScriptUtils.toLowerCase("package.Test1")  should be("package.test-1")
    ScriptUtils.toLowerCase("package.Test11") should be("package.test-11")
    ScriptUtils.toLowerCase("package.Test1Class")  should be("package.test-1-class")
    ScriptUtils.toLowerCase("package.Test11Class") should be("package.test-11-class")
  }

  private[this] def testMapping(testCase: (String, String)*): Unit = {
    ScriptUtils.createScriptNames(testCase.map(_._1)) should contain theSameElementsAs testCase
  }

  "createScriptNames()" should "generate short names when no conflicts" in {
    testMapping(
      "pkg1.TestClass" -> "test-class",
      "pkg1.AnotherTestClass" -> "another-test-class",
      "pkg2.ThirdTestClass" -> "third-test-class"
    )
  }

  it should "generate long names only when necessary" in {
    testMapping(
      "pkg1.TestClass" -> "pkg-1_test-class",
      "pkg1.AnotherTestClass" -> "another-test-class",
      "pkg2.TestClass" -> "pkg-2_test-class"
    )
  }

  it should "properly strip common packages when disambiguating names" in {
    testMapping(
      "com.company.feature1.Test" -> "feature-1_test",
      "com.company.feature1.AnotherTest" -> "another-test",
      "com.company.feature2.subpkg.core.Test" -> "feature-2_subpkg_core_test",
      "com.company.feature2.subpkg.ui.Test"   -> "feature-2_subpkg_ui_test"
    )
    testMapping(
      "pkg1.Test" -> "test",
      "pkg1.ui.Test" -> "ui_test"
    )
  }
}
