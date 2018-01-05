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
    ScriptUtils.toLowerCase("package.Test1") should be("package.test-1")
    ScriptUtils.toLowerCase("package.Test11") should be("package.test-11")
    ScriptUtils.toLowerCase("package.Test1Class") should be("package.test-1-class")
    ScriptUtils.toLowerCase("package.Test11Class") should be("package.test-11-class")
  }

  private[this] def testMapping(testCase: (String, String)*): Unit =
    ScriptUtils.createScriptNames(testCase.map(_._1)) should contain theSameElementsAs testCase

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
      "pkg1.ui.TestClass" -> "pkg-1_ui_test-class",
      "pkg1.AnotherTestClass" -> "another-test-class",
      "pkg2.TestClass" -> "pkg-2_test-class"
    )
  }

  it should "handle single main class" in {
    testMapping("pkg1.Test" -> "test")
  }

  it should "be consistent with the docs" in {
    // see src/sphinx/archetypes/java_app/index.rst
    testMapping(
      "pkg1.TestClass" -> "pkg-1_test-class",
      "pkg2.AnUIMainClass" -> "an-ui-main-class",
      "pkg2.SomeXMLLoader" -> "some-xml-loader",
      "pkg3.TestClass" -> "pkg-3_test-class"
    )
  }

  "duplicated script name detector" should "work" in {
    ScriptUtils.describeDuplicates(
      Seq(
        "Test11" -> "dup1",
        "Test12" -> "dup1",
        "Test21" -> "dup2",
        "Test22" -> "dup2",
        "Test23" -> "dup2",
        "Test3" -> "nodup"
      )
    ) should contain theSameElementsAs Seq("dup1 (Test11, Test12)", "dup2 (Test21, Test22, Test23)")
  }
}
