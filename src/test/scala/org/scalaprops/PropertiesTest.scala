package org.scalaprops

import org.scalatest.FunSuite
import java.lang.IllegalArgumentException

class PropertiesTest extends FunSuite {
  

  class Orc extends Bean {
    var liveChangedCalled = false
    def liveChanged() = liveChangedCalled = true

    val name      = p('name, "Igor") translate(n => "Mr. " + n)
    val surname   = p('surname, "Orc") require(!_.isEmpty, "Surname should not be empty")
    val hitPoints = p('hitPoints, 100) require(_ >= 0)
    val alive     = p('alive, true) onChange liveChanged _
  }

  test("properties") {

    val orc = new Orc()

    assert(orc.name() === "Igor")

    orc.name := "Gertrud"

    assert(orc.name() === "Mr. Gertrud")

    orc.alive := false

    assert(orc.liveChangedCalled)

    try {
      orc.hitPoints := -1
      fail("-1 should not be allowed")
    } catch {
      case e: IllegalArgumentException => // Success
    }

    try {
      orc.surname := ""
      fail("empty string should not be allowed")
    } catch {
      case e: IllegalArgumentException => // Success
    }
  }

  test("Optional get") {
    val orc = new Orc()
    assert(orc.get('knitting) === None)
    assert(orc.get('hitPoints) === Some(100))
  }

  test("accessing properties through names") {
    val orc = new Orc()
    orc('hitPoints) = 40
    assert(orc[Int]('hitPoints) === 40)

    try {
      orc.set('knitting, true)
      fail("should not allow setting non-added property")
    } catch {
      case e: IllegalArgumentException => // Success
    }
  }

  test("Getting all properties") {
    val orc = new Orc()
    val props = orc.properties

    assert(props.contains('hitPoints))
    assert(props.contains('alive))
    assert(!props.contains('knitting))
  }

}