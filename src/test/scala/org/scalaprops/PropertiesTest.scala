package org.scalaprops

import java.lang.IllegalArgumentException
import org.scalatest.FunSuite
import org.scalaprops.exporter.{BeanExporter, JsonBeanExporter}
import org.scalaprops.parser.{JsonBeanParser, BeanParser}

class PropertiesTest extends FunSuite {


  class Bonus extends Bean {
    val amount = p('amount, 1)
    val kind = p('kind, "apples")
  }

  class Orc extends Bean {
    var liveChangedCalled = false
    def liveChanged() { liveChangedCalled = true }
    var listenerCalledWith: (Int, Int) = (0,0)

    val name       = p('name, "Igor") translate(n => "Mr. " + n)
    val surname    = p('surname, "Orc") require(!_.isEmpty, "Surname should not be empty")
    val hitPoints  = p('hitPoints, 100) require(_ >= 0) onValueChange{ (o: Int, n: Int) =>  listenerCalledWith = (o,n) }
    val floating   = p('floating, 0.53f)
    val bonus      = p('bonus, new Bonus)
    val alive      = p('alive, true) onChange liveChanged
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

  test("Listener") {
    val orc = new Orc()
    orc.hitPoints := 40
    assert(orc.listenerCalledWith === (100, 40), "Listener should be called with correct values")
    orc.hitPoints := 20
    assert(orc.listenerCalledWith === (40, 20), "Listener should be called with correct values")
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

  test("deepListeners") {
    val igor = new Orc()
    val deeperImaginaryFriend = new Orc()
    val imaginaryFriend = new Orc()
    imaginaryFriend.name := "Mist"
    deeperImaginaryFriend.name := "Freud"
    igor.addProperty('imaginaryFriend, imaginaryFriend)
    imaginaryFriend.addProperty('deeperImaginaryFriend, deeperImaginaryFriend)


    var changedProp: Symbol = null
    var deepChangedProp: Symbol = null
    igor.addListener(new BeanListener {
      def onPropertyAdded(bean: Bean, property: Property[ _ ]) {}
      def onPropertyRemoved(bean: Bean, property: Property[ _ ]) {}
      def onPropertyChanged(bean: Bean, property: Property[ _ ]) {
        changedProp = property.name
      }
    })
    igor.addDeepListener(new BeanListener {
      def onPropertyAdded(bean: Bean, property: Property[ _ ]) {}
      def onPropertyRemoved(bean: Bean, property: Property[ _ ]) {}
      def onPropertyChanged(bean: Bean, property: Property[ _ ]) {
        deepChangedProp= property.name
      }
    })

    assert(changedProp === null)
    assert(deepChangedProp === null)

    deeperImaginaryFriend.hitPoints := 3

    assert(changedProp === null)
    assert(deepChangedProp === 'hitPoints)

    igor.occupation := 'tester

    assert(changedProp === 'occupation)
    assert(deepChangedProp === 'occupation)
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
               "  beanType: Orc,\n" +
               "  smell: 0.56" +
               "  \"hitPoints\": 98," +
               "  \"floating\": 0.3" +
               "  bonus: { " +
               "     beanType: Bonus, "+
               "     amount: 10  "+
               "     kind: \"oranges\" "+
               "  } " +
               "  name : \"Olaf\",\n" +
               "  occupation: \"Warrior\"\n," +
               "  alive:false " +
               "  \"badges\": [" +
               "     \"Elf Bashing\", \"Pig Farming\", \"Nose Picking\"" +
               "   ]" +
               "}"

    val factory = new BeanFactory
    factory.registerBeanType('Orc, () => new Orc())
    factory.registerBeanType('Bonus, () => new Bonus())
    val element: Bean = Bean.loadFromString(text, "test source", beanFactory = factory)

    val orc: Orc = element.asInstanceOf[Orc]
    assert(orc.name() === "Mr. Olaf")
    assert(orc.hitPoints() === 98)
    assert(orc.floating() === 0.3f)
    assert(orc.bonus().amount() === 10)
    assert(orc.bonus().kind() === "oranges")
    assert(orc.surname() === "Orc")
    assert(orc.occupation() === 'Warrior)
    assert(orc.get('badges, Nil).size === 3)
  }

  test("Generate JSON like syntax") {
    val bean = new Orc()
    bean.name := "Igor"
    bean.addProperty('badges, List("Funny Hat", "Wooden Stick"))
    bean.addProperty(Symbol("Imaginary Friend"), new Orc())

    val exported = bean.saveToString()

    val expected =
"""{
  "beanType": "Orc",
  "name": "Igor",
  "surname": "Orc",
  "hitPoints": 100,
  "floating": 0.53,
  "bonus": {
    "beanType": "Bonus",
    "amount": 1,
    "kind": "apples"
  },
  "alive": true,
  "occupation": null,
  "badges": [
    "Funny Hat",
    "Wooden Stick"
  ],
  "Imaginary Friend": {
    "beanType": "Orc",
    "name": "Igor",
    "surname": "Orc",
    "hitPoints": 100,
    "floating": 0.53,
    "bonus": {
      "beanType": "Bonus",
      "amount": 1,
      "kind": "apples"
    },
    "alive": true,
    "occupation": null
  }
}
"""

    assert(exported === expected)
  }

}