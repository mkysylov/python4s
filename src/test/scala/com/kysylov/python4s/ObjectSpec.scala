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

import com.kysylov.python4s.Python.libPython
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

}
