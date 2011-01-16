package org.scalaprops.parser

import java.io.Reader
import org.scalaprops.Bean
import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Parses property beans from Json files
 * (with optional string quoting for non-whitespace identifiers and comma separation between elements)
 */
class JsonBeanParser extends JavaTokenParsers with BeanParser {


  def parse(reader: Reader) = null

  def parse(reader: Reader, sourceName: String): Bean = {
    parseAll(obj, reader) match {
      case s: Success[Map[Symbol, AnyRef]] =>  createBean(s.result)
      case f: NoSuccess=> throw new ParseError(f.msg, f.next.pos, sourceName)
    }
  }

  private def obj: Parser[Map[Symbol, AnyRef]] = "{"~> repsep(member, opt(",")) <~"}" ^^ (Map() ++ _)

  private def arr: Parser[List[AnyRef]] = "["~> repsep(value, opt(",")) <~"]"

  private def fieldName: Parser[Symbol] = (
          stringLiteral  ^^ (x => Symbol(x))
          | ident ^^ (x => Symbol(x))
          )

  private def member: Parser[(Symbol, AnyRef)] =
          fieldName ~":"~value ^^ { case name~":"~value => (name, value) }

  private def value: Parser[AnyRef] = (
          obj
          | arr
          | stringLiteral
          | floatingPointNumber
          | "null" ^^ (x => null)
          | "true"
          | "false"
          | ident
          )


}