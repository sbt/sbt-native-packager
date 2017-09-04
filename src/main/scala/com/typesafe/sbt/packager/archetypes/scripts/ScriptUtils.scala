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

  /**
    * Recursive function that disambiguates classes with equal short names
    * according to their packages.
    * @param classesAndPrefixParts sequence of tuples
    *     (qualified class name, not yet processed suffix of packages in which this class resides)
    * @param shortName short class name to append at the end of generate name
    * @param accumulatedPrefixes prefix parts accumulated above on the call stack
    * @return sequence of tuples (passed in class name, disambiguated name)
    */
  private[this] def disambiguateNames(
                      classesAndPrefixParts: Seq[(String, List[String])],
                      shortName: String,
                      accumulatedPrefixes: Seq[String] = Seq()
                                  ): Seq[(String, String)] = {

    if (classesAndPrefixParts.size <= 1) {
      classesAndPrefixParts.map {
        case (clazz, _) => (
          clazz,
          (accumulatedPrefixes :+ shortName)
            .filter { str => str.nonEmpty }
            .mkString("_")
        )
      }
    } else {
      val commonPrefixLength = classesAndPrefixParts
        .map {
          case (_, candidatePrefixes) => candidatePrefixes
        }
        .reduce(commonPrefix)
        .size

      classesAndPrefixParts
        .map {
          case (clazz, candidatePrefixes) =>
            (clazz, candidatePrefixes.drop(commonPrefixLength))
        }
        .groupBy {
          case (_, prefixes) => prefixes.headOption.getOrElse("")
        }
        .toSeq
        .flatMap {
          case (nextPrefix, nextConflictingNames) =>
            disambiguateNames(
              nextConflictingNames
                .map {
                  case (clazz, Nil) => (clazz, Nil)
                  case (clazz, _ :: otherPrefixes) => (clazz, otherPrefixes)
                },
              shortName,
              accumulatedPrefixes :+ nextPrefix
            )
        }
    }
  }

  /**
    * Generates launcher script names for the specified main class names.
    * Tries to make script names readable and unique.
    * @param discoveredMainClasses discovered qualified main class names
    * @return sequence of tuples: (passed in class name) -> (generated script name)
    */
  def createScriptNames(discoveredMainClasses: Seq[String]): Seq[(String, String)] = {
    val names = discoveredMainClasses.map { qualifiedClassName =>
      val lowerCased = toLowerCase(qualifiedClassName)
      val parts = lowerCased.split("\\.")
      (qualifiedClassName, parts.init.toList, parts.last)
    }

    names
      .groupBy {
        case (_, _, shortName) => shortName
      }
      .toSeq
      .flatMap {
        case (shortName, conflictingNames) =>
          disambiguateNames(
            conflictingNames.map {
              case (clazz, prefixes, _) => (clazz, prefixes)
            },
            shortName
          )
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
