package org.scalaprops

import org.scalatest.FunSuite
import java.lang.IllegalArgumentException

class PropertiesTest extends FunSuite {
  
  test("properties") {
    var liveChangedCalled = false
    def liveChanged() = liveChangedCalled = true

    class Orc extends Bean {
      val name      = property("Igor") translate(n => "Mr. " + n)
      val surname   = property("Orc") require(!_.isEmpty, "Surname should not be empty")
      val hitPoints = property(100) require(_ >= 0)
      val alive     = property(true) onChange liveChanged _
    }


    val orc = new Orc()

    assert(orc.name() === "Igor")

    orc.name := "Gertrud"

    assert(orc.name() === "Mr. Gertrud")

    orc.alive := false

    assert(liveChangedCalled)

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

}