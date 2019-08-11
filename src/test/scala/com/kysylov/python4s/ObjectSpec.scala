/*
 * Copyright 2019 Maksym Kysylov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kysylov.python4s

import org.scalatest.{FlatSpec, Matchers}

class ObjectSpec extends FlatSpec with Matchers {

  "An object wrapper" should "support callable objects" in {
    val capWords = Python.importModule("string").capwords

    capWords("hello world").toString shouldEqual "Hello World"
    capWords("hello_world", '_').toString shouldEqual "Hello_World"
    capWords(
      args = Seq[PythonObject]("hello_world"),
      kwargs = Map[String, PythonObject]("sep" -> '_')
    ).toString shouldEqual "Hello_World"

    PythonObject("foo,bar,baz")
      .split(',', 1)
      .toIterator
      .map(_.toString)
      .toSeq shouldEqual Seq("foo", "bar,baz")

    PythonObject("foo,bar,baz")
      .split(',', maxsplit = 1)
      .toIterator
      .map(_.toString)
      .toSeq shouldEqual Seq("foo", "bar,baz")
  }

  it should "support subscriptable objects" in {
    val dict = Map(PythonObject("foo") -> PythonObject("bar")).asPythonDict
    dict("foo").toString shouldEqual "bar"

    dict("foo") = "baz"
    dict("foo").toString shouldEqual "baz"
  }

  it should "support object attributes" in {
    val complex = Python.complex(1, 2)
    complex.real.toDouble shouldEqual 1.0
    complex.imag.toDouble shouldEqual 2.0

    val obj = Python.importModule("types").SimpleNamespace()
    obj.foo = "bar"
    obj.foo.toString shouldEqual "bar"
  }

  it should "support checking equality" in {
    PythonObject("foo") shouldEqual PythonObject("foo")
    PythonObject("foo") shouldNot equal(PythonObject("bar"))
    PythonObject("foo") shouldNot equal("foo")

    PythonObject(100) + PythonObject(200) shouldEqual PythonObject(300)
  }

  it should "convert to string" in {
    PythonObject("str").toString shouldEqual "str"
    PythonObject(1.0).toString shouldEqual "1.0"
    Seq[PythonObject]("foo", "bar", "baz").asPythonList.toString shouldEqual "['foo', 'bar', 'baz']"
  }

  it should "calculate hash" in {
    val math = Python.importModule("math")
    math.e.hashCode() shouldEqual 1656245132797518850L.toInt
  }

  it should "support + operator" in {
    val a = PythonObject(1)
    val b = PythonObject(2)
    (a + b).toInt shouldEqual 3

    a += b
    a.toInt shouldEqual 3
  }

  it should "support - operator" in {
    val a = PythonObject(3)
    val b = PythonObject(5)
    (a - b).toInt shouldEqual -2

    a -= b
    a.toInt shouldEqual -2
  }

  it should "support * operator" in {
    val a = PythonObject(2)
    val b = PythonObject(3)
    (a * b).toInt shouldEqual 6

    a *= b
    a.toInt shouldEqual 6
  }

  ignore should "support @ operator" in {
    ???
  }

  it should "support // operator" in {
    val a = PythonObject(7)
    val b = PythonObject(2)
    (a `//` b).toDouble shouldEqual 3

    a `//=` b
    a.toDouble shouldEqual 3
  }

  it should "support / operator" in {
    val a = PythonObject(7)
    val b = PythonObject(2)
    (a / b).toDouble shouldEqual 3.5

    a /= b
    a.toDouble shouldEqual 3.5
  }

  it should "support % operator" in {
    val a = PythonObject(11)
    val b = PythonObject(3)
    (a % b).toInt shouldEqual 2

    a %= b
    a.toInt shouldEqual 2
  }

  it should "support ** operator" in {
    val a = PythonObject(3)
    val b = PythonObject(4)
    (a ** b).toInt shouldEqual 81

    a **= b
    a.toInt shouldEqual 81
  }

  it should "support unary - operator" in {
    (-PythonObject(2)).toInt shouldEqual -2
  }

  it should "support unary + operator" in {
    val decimal = Python.importModule("decimal")
    decimal.Decimal("1").copy_sign(+decimal.Decimal("-0")).toInt shouldEqual 1
  }

  it should "support unary ~ operator" in {
    (~PythonObject(123)).toInt shouldEqual -124
  }

  it should "support << operator" in {
    val a = PythonObject(7)
    val b = PythonObject(2)
    (a << b).toInt shouldEqual 28

    a <<= b
    a.toInt shouldEqual 28
  }

  it should "support >> operator" in {
    val a = PythonObject(170)
    val b = PythonObject(2)
    (a >> b).toInt shouldEqual 42

    a >>= b
    a.toInt shouldEqual 42
  }

  it should "support & operator" in {
    val a = PythonObject(10)
    val b = PythonObject(3)
    (a & b).toInt shouldEqual 2

    a &= b
    a.toInt shouldEqual 2
  }

  it should "support ^ operator" in {
    val a = PythonObject(10)
    val b = PythonObject(3)
    (a ^ b).toInt shouldEqual 9

    a ^= b
    a.toInt shouldEqual 9
  }

  it should "support | operator" in {
    val a = PythonObject(10)
    val b = PythonObject(3)
    (a | b).toInt shouldEqual 11

    a |= b
    a.toInt shouldEqual 11
  }

  it should "support < operator" in {
    (PythonObject(1) < PythonObject(2)) shouldEqual true
    (PythonObject(1) < PythonObject(1)) shouldEqual false
    (PythonObject(1) < PythonObject(0)) shouldEqual false
  }

  it should "support <= operator" in {
    (PythonObject(1) <= PythonObject(2)) shouldEqual true
    (PythonObject(1) <= PythonObject(1)) shouldEqual true
    (PythonObject(1) <= PythonObject(0)) shouldEqual false
  }

  it should "support > operator" in {
    (PythonObject(1) > PythonObject(2)) shouldEqual false
    (PythonObject(1) > PythonObject(1)) shouldEqual false
    (PythonObject(1) > PythonObject(0)) shouldEqual true
  }

  it should "support >= operator" in {
    (PythonObject(1) >= PythonObject(2)) shouldEqual false
    (PythonObject(1) >= PythonObject(1)) shouldEqual true
    (PythonObject(1) >= PythonObject(0)) shouldEqual true
  }

}
