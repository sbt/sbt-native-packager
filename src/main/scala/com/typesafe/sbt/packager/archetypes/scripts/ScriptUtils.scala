package com.typesafe.sbt.packager.archetypes.scripts

object ScriptUtils {

  /**
    * Generates launcher script names for the specified main class names.
    * Tries to make script names readable and unique.
    * @param discoveredMainClasses discovered qualified main class names
    * @return sequence of tuples: (passed in class name) -> (generated script name)
    */
  def createScriptNames(discoveredMainClasses: Seq[String]): Seq[(String, String)] = {
    val names = discoveredMainClasses.map { qualifiedClassName =>
      val lowerCased = toLowerCase(qualifiedClassName)
      (qualifiedClassName, lowerCased.replace('.', '-'), lowerCased.split("\\.").last)
    }

    names.groupBy(_._3).toSeq.flatMap {
      case (shortName, Seq((clazz, _, _))) => Seq((clazz, shortName))
      case (_, conflictingNames) =>
        conflictingNames.groupBy(_._2).toSeq.flatMap {
          case (longerName, Seq((clazz, _, _))) => Seq((clazz, longerName))
          case (longerName, veryConflictingNames) =>
            veryConflictingNames.zipWithIndex.map {
              case ((clazz, _, _), index) =>
                // ... and hope it will not conflict with previously accepted names...
                (clazz, s"$longerName-${index + 1}")
            }
        }
    }
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
