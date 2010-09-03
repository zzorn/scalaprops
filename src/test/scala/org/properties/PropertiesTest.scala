package org.properties

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.lang.IllegalArgumentException

/**
 * 
 */
@RunWith(classOf[JUnitRunner])
class PropertiesTest extends FunSuite {
  

  test("properties") {
    var liveChangedCalled = false
    def liveChanged() = liveChangedCalled = true

    class Orc extends Bean {
      val name      = stringField('name, "Igor") translate(n => "Mr. " + n)
      val hitPoints = doubleField('hitPoints, 100) require(_ >= 0)
      val alive     = boolField('alive, true) onChange liveChanged _
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
  }

}