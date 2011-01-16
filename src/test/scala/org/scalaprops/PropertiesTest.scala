package org.scalaprops

import org.scalatest.FunSuite
import java.lang.IllegalArgumentException
import parser.{BeanParser, JsonBeanParser}

class PropertiesTest extends FunSuite {
  

  class Orc extends Bean {
    var liveChangedCalled = false
    def liveChanged() = liveChangedCalled = true

    val name       = p('name, "Igor") translate(n => "Mr. " + n)
    val surname    = p('surname, "Orc") require(!_.isEmpty, "Surname should not be empty")
    val hitPoints  = p('hitPoints, 100) require(_ >= 0)
    val alive      = p('alive, true) onChange liveChanged _
    val occupation = p[Symbol]('occupation, null)
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

  test("Bound properties, automatic update") {
    val igor = new Orc()
    val igorsShadow = new Orc()

    igorsShadow.hitPoints.bind(igor.hitPoints)

    igor.hitPoints := 50
    assert(igorsShadow.hitPoints() === 50)
  }

  test("Bound properties, manual update") {
    val igor = new Orc()
    val igorsShadow = new Orc()

    igorsShadow.hitPoints.bind(igor.hitPoints, automaticUpdate = false)

    igor.hitPoints := 50
    assert(igorsShadow.hitPoints() === 100)

    igorsShadow.updateBoundValues()
    assert(igorsShadow.hitPoints() === 50)
  }

  test("Bound properties, translate") {
    val igor = new Orc()
    val igorsShadow = new Orc()

    igorsShadow.hitPoints.bind(igor.hitPoints, hp => hp - 10)

    igor.hitPoints := 50
    assert(igorsShadow.hitPoints() === 40)
  }

  test("Bound properties, unbind") {
    val igor = new Orc()
    val igorsShadow = new Orc()

    igorsShadow.hitPoints.bind(igor.hitPoints)

    igor.hitPoints := 50
    assert(igorsShadow.hitPoints() === 50)

    igorsShadow.hitPoints.unbind()
    igor.hitPoints := 30
    assert(igorsShadow.hitPoints() === 50)
  }


  test("Parse JSON like syntax") {

    val text = "{" +
               "  beanType: Orc\n" +
               "  smell: 0.56" +
               "  \"hitPoints\": 98" +
               "  name : \"Olaf\"\n" +
               "  occupation: \"Warrior\"\n" +
               "  alive:false " +
               "  \"badges\": [" +
               "     \"Elf Bashing\", \"Pig Farming\", \"Nose Picking\"" +
               "   ]" +
               "}"

    val parser: BeanParser = new JsonBeanParser()
    parser.registerBeanType('Orc, () => new Orc())
    val element: Bean = parser.parse(text, "test source")

    val orc: Orc = element.asInstanceOf[Orc]
    assert(orc.name() === "Mr. Olaf")
    assert(orc.hitPoints() === 98)
    assert(orc.surname() === "Orc")
    assert(orc.occupation() === 'Warrior)
  }

}