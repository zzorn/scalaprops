package org.properties

import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * 
 */
@RunWith(classOf[JUnitRunner])
class PropertiesTest extends FunSuite {
  
  test("properties") {
    class Orc extends Bean {
      val name = stringField('name, "Igor")
      val hitPoints = doubleField('hitPoints, 100)
      val alive = boolField('alive, true)
    }

    val orc = new Orc()

    assert(orc.name() === "Igor")

    orc.name := "Gertrud"

    assert(orc.name() === "Gertrud")
  }

}