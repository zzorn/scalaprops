package org.scalaprops.parser

import java.io.Reader
import org.scalaprops.Bean
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parses property beans from Json files
 * (with optional string quoting for non-whitespace identifiers and comma separation between elements)
 */
class JsonBeanParser extends JavaTokenParsers with BeanParser {


  def parse(reader: Reader, sourceName: String): Bean = {
    parseAll(obj, reader) match {
      case s: Success[Map[Symbol, AnyRef]] =>  createBean(s.result)
      case f: NoSuccess=> throw new ParseError(f.msg, f.next.pos, sourceName)
    }
  }

  private def obj: Parser[Map[Symbol, AnyRef]] = "{"~> repsep(member, opt(",")) <~"}" ^^ (Map() ++ _)

  private def arr: Parser[List[AnyRef]] = "["~> repsep(value, opt(",")) <~"]"

  private def fieldName: Parser[Symbol] = (
          stringLiteral  ^^ (x => Symbol(stripQuotes(x)))
          | ident ^^ (x => Symbol(x))
          )

  private def member: Parser[(Symbol, AnyRef)] =
          fieldName ~":"~value ^^ { case name~":"~value => (name, value) }


  private def value: Parser[AnyRef] = (
          obj
          | arr
          | stringLiteral ^^ (x => stripQuotes(x))
          | floatingPointNumber ^^ (x => stringToNumber(x))
          | "null" ^^ (x => null)
          | "true" ^^ (x => java.lang.Boolean.TRUE)
          | "false" ^^ (x => java.lang.Boolean.FALSE)
          | ident
          )

  private def stringToNumber(s: String): AnyRef = {
    if (s.contains(".") ||
        s.contains("e") ||
        s.contains("E")) new java.lang.Double(s)
    else if (s.length <= 9) new java.lang.Integer(s)
    else new java.lang.Long(s)
  }

  private def stripQuotes(x: String): String = x.substring(1, x.length - 1)

}