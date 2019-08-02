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

import jnr.ffi.Pointer
import jnr.ffi.byref.PointerByReference

private[python4s] class PythonLibrary(self: LibPython) {
  def pyRunSimpleString(command: String): Boolean =
    self.PyRun_SimpleString(command) == 0

  def pyIncRef(o: Pointer): Unit = self.Py_IncRef(o)

  def pyDecRef(o: Pointer): Unit = self.Py_DecRef(o)

  def pyErrOccurred: Boolean = Option(self.PyErr_Occurred()).isDefined

  def pyErrFetch(): Option[(PythonReference, PythonReference, Option[PythonReference])] = {
    val typeReference = new PointerByReference()
    val valueReference = new PointerByReference()
    val traceReference = new PointerByReference()

    self.PyErr_Fetch(typeReference, valueReference, traceReference)
    self.PyErr_NormalizeException(typeReference, valueReference, traceReference)

    val typeOption = PythonReference.receive(typeReference.getValue)
    val valueOption = PythonReference.receive(valueReference.getValue)
    val traceOption = PythonReference.receive(traceReference.getValue)

    (typeOption zip valueOption).map { case (type1, value) => (type1, value, traceOption) }
  }

  def pyImportImportModule(name: String): PythonReference =
    PythonReference.receive(self.PyImport_ImportModule(name)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyBuildValue(format: String, args: AnyRef*): PythonReference =
    PythonReference.receive(self.Py_BuildValue(format, args: _*)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyEvalGetBuiltins: PythonReference =
    PythonReference.borrow(self.PyEval_GetBuiltins()).get

  def pyObjectGetAttrString(obj: PythonReference, attrName: String): PythonReference =
    PythonReference.receive(self.PyObject_GetAttrString(obj.pointer, attrName)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyObjectSetAttrString(obj: PythonReference, attrName: String, value: PythonReference): Unit = {
    if (self.PyObject_SetAttrString(obj.pointer, attrName, value.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pyObjectRichCompare(obj1: PythonReference, obj2: PythonReference, operator: Int): Boolean = {
    val result = self.PyObject_RichCompareBool(obj1.pointer, obj2.pointer, operator)
    if (result == -1)
      PythonException.fetch().foreach(throw _)
    result > 0
  }

  def pyObjectStr(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyObject_Str(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyCallableCheck(obj: PythonReference): Boolean =
    self.PyCallable_Check(obj.pointer) > 0

  def pyObjectCall(callable: PythonReference, args: PythonReference, kwargs: PythonReference): PythonReference =
    PythonReference.receive(self.PyObject_Call(callable.pointer, args.pointer, kwargs.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyObjectCallFunctionObjArgs(callable: PythonReference, args: PythonReference*): PythonReference =
    PythonReference.receive(self.PyObject_CallFunctionObjArgs(callable.pointer, args.map(_.pointer) :+ null: _*)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyObjectCallMethodObjArgs(callable: PythonReference, name: PythonReference, args: PythonReference*): PythonReference =
    PythonReference.receive(self.PyObject_CallMethodObjArgs(callable.pointer, name.pointer, args.map(_.pointer) :+ null: _*)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyObjectHash(obj: PythonReference): Int = {
    val result = self.PyObject_Hash(obj.pointer).toInt
    if (result == -1)
      PythonException.fetch().foreach(throw _)
    result
  }

  def pyObjectIsTrue(obj: PythonReference): Boolean = {
    val result = self.PyObject_IsTrue(obj.pointer)
    if (result == -1)
      PythonException.fetch().foreach(throw _)
    result > 0
  }

  def pyObjectGetItem(obj: PythonReference, key: PythonReference): PythonReference =
    PythonReference.receive(self.PyObject_GetItem(obj.pointer, key.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyObjectSetItem(obj: PythonReference, key: PythonReference, value: PythonReference): Unit = {
    if (self.PyObject_SetItem(obj.pointer, key.pointer, value.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pyObjectGetIter(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyObject_GetIter(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberAdd(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Add(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberSubtract(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Subtract(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberMultiply(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Multiply(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberMatrixMultiply(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_MatrixMultiply(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberFloorDivide(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_FloorDivide(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberTrueDivide(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_TrueDivide(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberRemainder(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Remainder(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberPower(obj1: PythonReference, obj2: PythonReference, obj3: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Power(obj1.pointer, obj2.pointer, obj3.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberNegative(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Negative(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberPositive(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Positive(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInvert(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Invert(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberLshift(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Lshift(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberRshift(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Rshift(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberAnd(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_And(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberXor(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Xor(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberOr(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_Or(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceAdd(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceAdd(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceSubtract(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceSubtract(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceMultiply(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceMultiply(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceMatrixMultiply(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceMatrixMultiply(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceFloorDivide(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceFloorDivide(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceTrueDivide(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceTrueDivide(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceRemainder(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceRemainder(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlacePower(obj1: PythonReference, obj2: PythonReference, obj3: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlacePower(obj1.pointer, obj2.pointer, obj3.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceLshift(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceLshift(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceRshift(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceRshift(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceAnd(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceAnd(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceXor(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceXor(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyNumberInPlaceOr(obj1: PythonReference, obj2: PythonReference): PythonReference =
    PythonReference.receive(self.PyNumber_InPlaceOr(obj1.pointer, obj2.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pySequenceGetItem(obj: PythonReference, index: Long): PythonReference =
    PythonReference.receive(self.PySequence_GetItem(obj.pointer, index)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyMappingItems(obj: PythonReference): PythonReference =
    PythonReference.receive(self.PyMapping_Items(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyIterNext(obj: PythonReference): Option[PythonReference] = {
    val result = PythonReference.receive(self.PyIter_Next(obj.pointer))
    if (result.isEmpty)
      PythonException.fetch().foreach(throw _)
    result
  }

  def pyLongFromLong(value: Long): PythonReference =
    PythonReference.receive(self.PyLong_FromLong(value)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyLongAsLongLong(obj: PythonReference): Long = {
    val result = self.PyLong_AsLongLong(obj.pointer)
    if (result == -1L)
      PythonException.fetch().foreach(throw _)
    result
  }

  def pyBoolFromLong(value: Long): PythonReference =
    PythonReference.receive(self.PyBool_FromLong(value)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyFloatFromDouble(value: Double): PythonReference =
    PythonReference.receive(self.PyFloat_FromDouble(value)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyFloatAsDouble(obj: PythonReference): Double = {
    val result = self.PyFloat_AsDouble(obj.pointer)
    if (result == -1.0)
      PythonException.fetch().foreach(throw _)
    result
  }

  def pyUnicodeFromString(string: String): PythonReference =
    PythonReference.receive(self.PyUnicode_FromString(string)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyUnicodeAsUTF8(obj: PythonReference): String =
    Option(self.PyUnicode_AsUTF8(obj.pointer)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyTupleNew(length: Long): PythonReference =
    PythonReference.receive(self.PyTuple_New(length)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyTupleSetItem(tuple: PythonReference, position: Long, item: PythonReference): Unit = {
    self.Py_IncRef(item.pointer)
    if (self.PyTuple_SetItem(tuple.pointer, position, item.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pyListNew(length: Long): PythonReference =
    PythonReference.receive(self.PyList_New(length)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyListSetItem(list: PythonReference, index: Long, item: PythonReference): Unit = {
    if (self.PyList_SetItem(list.pointer, index, item.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pyListAppend(list: PythonReference, item: PythonReference): Unit = {
    if (self.PyList_Append(list.pointer, item.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pyDictNew(): PythonReference =
    PythonReference.receive(self.PyDict_New()) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyDictSetItem(dictionary: PythonReference, key: PythonReference, value: PythonReference): Unit = {
    if (self.PyDict_SetItem(dictionary.pointer, key.pointer, value.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pySetNew(iterable: Option[PythonReference]): PythonReference =
    PythonReference.receive(self.PySet_New(iterable.map(_.pointer).orNull)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pySetAdd(set: PythonReference, key: PythonReference): Unit = {
    if (self.PySet_Add(set.pointer, key.pointer) == -1)
      PythonException.fetch().foreach(throw _)
  }

  def pySliceNew(start: Option[PythonReference], stop: Option[PythonReference], step: Option[PythonReference]): PythonReference =
    PythonReference.receive(self.PySlice_New(start.map(_.pointer).orNull, stop.map(_.pointer).orNull, step.map(_.pointer).orNull)) match {
      case Some(result) => result
      case None => throw PythonException.fetch().get
    }

  def pyInitializeEx(initializeSignals: Boolean): Unit = {
    self.Py_InitializeEx(if (initializeSignals) 1 else 0)
    self.PyEval_InitThreads()
  }
}

private[python4s] object PythonLibrary {
  val pyLT = 0
  val pyLE = 1
  val pyEQ = 2
  val pyNE = 3
  val pyGT = 4
  val pyGE = 5
}