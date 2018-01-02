package com.typesafe.sbt.packager.archetypes.scripts

object ScriptUtils {

  private[this] case class MainClass(fullyQualifiedClassName: String) {
    private val lowerCased = toLowerCase(fullyQualifiedClassName)
    val simpleName: String = lowerCased.split("\\.").last

    def asSimpleTuple: (String, String) = (fullyQualifiedClassName, simpleName)
    def asQualifiedTuple: (String, String) = (fullyQualifiedClassName, lowerCased.replace('.', '_'))
  }

  /**
    * Generates launcher script names for the specified main class names.
    * @param discoveredMainClasses discovered qualified main class names
    * @return sequence of tuples: (passed in class name) -> (generated script name)
    */
  def createScriptNames(discoveredMainClasses: Seq[String]): Seq[(String, String)] = {
    val mainClasses = discoveredMainClasses.map { fullyQualifiedClassName =>
      MainClass(fullyQualifiedClassName)
    }
    val (duplicates, uniques) = mainClasses
      .groupBy(_.simpleName)
      .partition {
        case (_, classes) => classes.length > 1
      }

    val resultsForUniques = uniques.toSeq.map {
      case (_, seqOfOneClass) => seqOfOneClass.head.asSimpleTuple
    }
    val resultsForDuplicates = duplicates.toSeq.flatMap {
      case (_, classes) => classes.map(_.asQualifiedTuple)
    }
    resultsForUniques ++ resultsForDuplicates
  }

  /**
    * Converts class name to lower case, applying some heuristics
    * to guess the word splitting.
    * @param qualifiedClassName a class name
    * @return lower cased name with '-' between words. Dots ('.') are left as is.
    */
  def toLowerCase(qualifiedClassName: String): String = {
    // suppose list is not very huge, so no need in tail recursion
    def split(chars: List[Char]): List[Char] = chars match {
      case c1 :: c2 :: cs if c1.isLower && c2.isUpper =>
        //  aClass   ->  a-Class
        // anUITest  -> an-UITest
        //  ^
        c1 :: '-' :: split(c2 :: cs)
      case c1 :: c2 :: c3 :: cs if c1.isUpper && c2.isUpper && c3.isLower =>
        // UITest -> UI-Test
        //  ^
        c1 :: '-' :: split(c2 :: c3 :: cs)
      case c1 :: c2 :: cs if c1.isLetter && c2.isDigit =>
        // Test1 -> Test-1
        //    ^
        c1 :: '-' :: split(c2 :: cs)
      case c1 :: c2 :: cs if c1.isDigit && c2.isLetter =>
        // Test1Class -> Test-1-Class
        //     ^              ^
        // _not_ pkg1.Test
        //          ^
        c1 :: '-' :: split(c2 :: cs)
      case c :: cs =>
        c :: split(cs)
      case Nil => Nil
    }
    val sb = new StringBuilder
    sb ++= split(qualifiedClassName.toList).map(_.toLower)
    sb.result()
  }
}
