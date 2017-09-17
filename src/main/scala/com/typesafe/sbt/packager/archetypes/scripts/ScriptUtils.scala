package com.typesafe.sbt.packager.archetypes.scripts

object ScriptUtils {

  private[this] def commonPrefix(list1: List[String], list2: List[String]): List[String] = (list1, list2) match {
    case (Nil, _) => Nil
    case (_, Nil) => Nil
    case (x::xs, y::ys) =>
      if (x == y)
        x :: commonPrefix(xs, ys)
      else
        Nil
  }

  private[this] case class MainClass(fullyQualifiedClassName: String, parts: Seq[String]) {
    private val packages: Seq[String] = parts.init
    private val className: String = parts.last

    def scriptName(expansionIndex: Int): String =
      (packages.take(expansionIndex) :+ className).mkString("_")
    def asTuple(expansionIndex: Int): (String, String) =
      (fullyQualifiedClassName, scriptName(expansionIndex))
  }

  private[this] def disambiguateNames(
                                       mainClasses: Seq[MainClass],
                                       expansionIndex: Int
                                     ): Seq[(String, String)] = {
    val (duplicates, uniques) = mainClasses
      .groupBy(_.scriptName(expansionIndex))
      .partition {
        case (_, classes) => classes.length > 1
      }

    val resultsForUniques = uniques.toSeq.map {
      case (_, seqOfOneClass) => seqOfOneClass.head.asTuple(expansionIndex)
    }
    val resultsForDuplicates = duplicates.toSeq.flatMap {
      case (_, classes) =>
        disambiguateNames(classes, expansionIndex + 1)
    }
    resultsForUniques ++ resultsForDuplicates
  }

  /**
    * Generates launcher script names for the specified main class names.
    * Tries to make script names readable and unique.
    * @param discoveredMainClasses discovered qualified main class names
    * @return sequence of tuples: (passed in class name) -> (generated script name)
    */
  def createScriptNames(discoveredMainClasses: Seq[String]): Seq[(String, String)] = {
    val mainClasses = discoveredMainClasses.map { qualifiedClassName =>
      val lowerCased = toLowerCase(qualifiedClassName)
      val parts = lowerCased.split("\\.")
      MainClass(qualifiedClassName, parts)
    }
    val commonPrefixLength = mainClasses.map(_.parts.toList).reduce(commonPrefix).size

    disambiguateNames(
      mainClasses.map {
        main => main.copy(parts = main.parts.drop(commonPrefixLength))
      },
      0
    )
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
