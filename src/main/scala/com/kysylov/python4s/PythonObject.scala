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

import scala.language.{dynamics, implicitConversions}

class PythonObject(private[python4s] var reference: PythonReference) extends Dynamic {

  import PythonObject._

  /**
    * Get item or call as a function.
    * Call as a function if more than argument is provided or object is callable, otherwise get an item.
    *
    * @param args arguments
    * @return python object
    */
  def apply(args: PythonObject*): PythonObject = args match {
    case Seq(key: PythonObject) if !libPython.pyCallableCheck(reference) =>
      PythonObject(libPython.pyObjectGetItem(reference, key.reference))
    case _ =>
      PythonObject(libPython.pyObjectCallFunctionObjArgs(reference, args.map(_.reference): _*))
  }

  /**
    * Call as a function.
    *
    * @param args   arguments
    * @param kwargs named arguments
    * @return python object
    */
  def apply(args: Seq[PythonObject] = Seq(),
            kwargs: Map[String, PythonObject] = Map()): PythonObject = {
    val argsReference = args.asPythonTuple.reference
    val kwargsReference = kwargs.map({ (key: String, value: PythonObject) =>
      PythonObject(key) -> value
    }.tupled).asPythonDict.reference

    PythonObject(libPython.pyObjectCall(reference, argsReference, kwargsReference))
  }

  /**
    * Call a method.
    *
    * @param methodName method name
    * @param args       arguments
    * @return python object
    */
  def applyDynamic(methodName: String)(args: PythonObject*): PythonObject =
    PythonObject(libPython.pyObjectCallMethodObjArgs(reference, methodName.reference, args.map(_.reference): _*))

  /**
    * Call a method with named arguments.
    *
    * @param methodName method name
    * @param args       arguments
    * @return python object
    */
  def applyDynamicNamed(methodName: String)(args: (String, PythonObject)*): PythonObject = selectDynamic(methodName)(
    args.collect { case (key, value) if key.isEmpty => value }.toSeq,
    args.filter { case (key, _) => !key.isEmpty }.toMap
  )

  /**
    * Get attribute value.
    *
    * @param attributeName attribute name
    * @return python object
    */
  def selectDynamic(attributeName: String): PythonObject =
    PythonObject(libPython.pyObjectGetAttrString(reference, attributeName))

  /**
    * Set item value.
    *
    * @param key   item name
    * @param value item value
    */
  def update(key: PythonObject, value: PythonObject): Unit =
    libPython.pyObjectSetItem(reference, key.reference, value.reference)

  /**
    * Set attribute value.
    *
    * @param attributeName attribute name
    * @param value         attribute value
    */
  def updateDynamic(attributeName: String)(value: PythonObject): Unit =
    libPython.pyObjectSetAttrString(reference, attributeName, value.reference)

  override def equals(obj: Any): Boolean = obj match {
    case that: PythonObject => libPython.pyObjectRichCompare(reference, that.reference, PythonLibrary.pyEQ)
    case _ => false
  }

  override def toString: String = libPython.pyUnicodeAsUTF8(libPython.pyObjectStr(reference))

  override def hashCode(): Int = libPython.pyObjectHash(reference)

  def +(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberAdd(reference, that.reference))

  def -(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberSubtract(reference, that.reference))

  def *(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberMultiply(reference, that.reference))

  def `@`(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberMatrixMultiply(reference, that.reference))

  def `//`(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberFloorDivide(reference, that.reference))

  def /(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberTrueDivide(reference, that.reference))

  def %(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberRemainder(reference, that.reference))

  def **(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberPower(reference, that.reference, ().reference))

  def unary_-(): PythonObject = PythonObject(libPython.pyNumberNegative(reference))

  def unary_+(): PythonObject = PythonObject(libPython.pyNumberPositive(reference))

  def unary_~(): PythonObject = PythonObject(libPython.pyNumberInvert(reference))

  def <<(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberLshift(reference, that.reference))

  def >>(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberRshift(reference, that.reference))

  def &(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberAnd(reference, that.reference))

  def ^(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberXor(reference, that.reference))

  def |(that: PythonObject): PythonObject = PythonObject(libPython.pyNumberOr(reference, that.reference))

  def +=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceAdd(reference, that.reference)
  }

  def -=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceSubtract(reference, that.reference)
  }

  def *=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceMultiply(reference, that.reference)
  }

  def @=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceMatrixMultiply(reference, that.reference)
  }

  def `//=`(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceFloorDivide(reference, that.reference)
  }

  def /=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceTrueDivide(reference, that.reference)
  }

  def %=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceRemainder(reference, that.reference)
  }

  def **=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlacePower(reference, that.reference, ().reference)
  }

  def <<=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceLshift(reference, that.reference)
  }

  def >>=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceRshift(reference, that.reference)
  }

  def &=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceAnd(reference, that.reference)
  }

  def ^=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceXor(reference, that.reference)
  }

  def |=(that: PythonObject): Unit = {
    reference = libPython.pyNumberInPlaceOr(reference, that.reference)
  }

  def <(that: PythonObject): Boolean = libPython.pyObjectRichCompare(reference, that.reference, PythonLibrary.pyLT)

  def <=(that: PythonObject): Boolean = libPython.pyObjectRichCompare(reference, that.reference, PythonLibrary.pyLE)

  def >(that: PythonObject): Boolean = libPython.pyObjectRichCompare(reference, that.reference, PythonLibrary.pyGT)

  def >=(that: PythonObject): Boolean = libPython.pyObjectRichCompare(reference, that.reference, PythonLibrary.pyGE)

  def toByte: Byte = toLong.toByte

  def toShort: Short = toLong.toShort

  def toInt: Int = toLong.toInt

  def toLong: Long = libPython.pyLongAsLongLong(reference)

  def toBoolean: Boolean = libPython.pyObjectIsTrue(reference)

  def toFloat: Float = toDouble.toFloat

  def toDouble: Double = libPython.pyFloatAsDouble(reference)

  def toSeq: Seq[PythonObject] = toIterator.toSeq

  def toSet: Set[PythonObject] = toIterator.toSet

  def toMap: Map[PythonObject, PythonObject] = PythonObject(libPython.pyMappingItems(reference))
    .toIterator
    .map { tuple =>
      val key = PythonObject(libPython.pySequenceGetItem(tuple.reference, index = 0))
      val value = PythonObject(libPython.pySequenceGetItem(tuple.reference, index = 1))
      key -> value
    }.toMap

  def toIterator: Iterator[PythonObject] = new Iterator[PythonObject] {
    private val pythonIterator = libPython.pyObjectGetIter(reference)
    private var nextReference = advance()

    override def hasNext: Boolean = nextReference.isDefined

    override def next(): PythonObject = nextReference match {
      case Some(currentReference) =>
        nextReference = advance()
        PythonObject(currentReference)
      case None =>
        throw new NoSuchElementException("Next on empty iterator.")
    }

    private def advance(): Option[PythonReference] = libPython.pyIterNext(pythonIterator)
  }
}

object PythonObject {
  /**
    * Create from python reference
    *
    * @param reference python reference
    * @return new instance
    */
  def apply(reference: PythonReference) = new PythonObject(reference)

  /**
    * Force implicit conversion (no-op).
    *
    * @param pythonObject instance
    * @return instance
    */
  def apply(pythonObject: PythonObject): PythonObject = pythonObject

  implicit def `unit asPython`(unit: Unit): PythonObject = PythonObject(libPython.pyBuildValue(""))

  implicit def `byte asPython`(byte: Byte): PythonObject = byte.toLong

  implicit def `short asPython`(short: Short): PythonObject = short.toLong

  implicit def `int asPython`(int: Int): PythonObject = int.toLong

  implicit def `long asPython`(long: Long): PythonObject = PythonObject(libPython.pyLongFromLong(long))

  implicit def `boolean asPython`(boolean: Boolean): PythonObject = PythonObject(libPython.pyBoolFromLong(if (boolean) 1 else 0))

  implicit def `float asPython`(float: Float): PythonObject = float.toDouble

  implicit def `double asPython`(double: Double): PythonObject = PythonObject(libPython.pyFloatFromDouble(double))

  implicit def `char asPython`(char: Char): PythonObject = char.toString

  implicit def `string asPython`(string: String): PythonObject = PythonObject(libPython.pyUnicodeFromString(string))

  implicit def `range asPython`(range: Range): PythonObject = PythonObject {
    val start = Some(range.start.reference)

    val stop = (range.isInclusive match {
      case true if range.end == -1 => None
      case true => Some(range.end + 1)
      case false => Some(range.end)
    }).map(_.reference)

    val step = Some(range.step.reference)

    libPython.pySliceNew(start, stop, step)
  }

  implicit class `Iterator asPython`(val iterator: Iterator[PythonObject]) extends AnyVal {
    // TODO: https://docs.python.org/3/c-api/iterator.html
  }

  implicit class `Iterable asPython`(val iterable: Iterable[PythonObject]) extends AnyVal {
    def asPythonList: PythonObject = {
      val listReference = libPython.pyListNew(length = 0)
      iterable.foreach(obj => libPython.pyListAppend(listReference, obj.reference))
      PythonObject(listReference)
    }
  }

  implicit class `Seq asPython`(val seq: Seq[PythonObject]) extends AnyVal {
    def asPythonList: PythonObject = {
      val listReference = libPython.pyListNew(seq.length)
      seq.zipWithIndex.foreach { case (obj, index) =>
        libPython.pyListSetItem(listReference, index, obj.reference)
      }
      PythonObject(listReference)
    }

    def asPythonTuple: PythonObject = {
      val tupleReference = libPython.pyTupleNew(seq.length)
      seq.zipWithIndex.foreach { case (obj, index) =>
        libPython.pyTupleSetItem(tupleReference, index, obj.reference)
      }
      PythonObject(tupleReference)
    }
  }

  implicit class `Set asPython`(val set: Set[PythonObject]) extends AnyVal {
    def asPythonSet: PythonObject = {
      val setReference = libPython.pySetNew(None)
      set.foreach(obj => libPython.pySetAdd(setReference, obj.reference))
      PythonObject(setReference)
    }
  }

  implicit class `Map asPython`(val map: Map[PythonObject, PythonObject]) extends AnyVal {
    def asPythonDict: PythonObject = {
      val dictReference = libPython.pyDictNew()
      map.foreach { case (key, value) =>
        libPython.pyDictSetItem(dictReference, key.reference, value.reference)
      }
      PythonObject(dictReference)
    }
  }

}
