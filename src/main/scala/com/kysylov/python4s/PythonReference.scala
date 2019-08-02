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

import java.lang.ref.Cleaner
import java.lang.ref.Cleaner.Cleanable
import java.util.concurrent.ConcurrentLinkedQueue

import com.kysylov.python4s.Python.libPython
import jnr.ffi.Pointer

import scala.annotation.tailrec

private[python4s] class PythonReference private(val pointer: Pointer) extends AutoCloseable {
  private val cleanable = PythonReference.register(this)

  override def close(): Unit = cleanable.clean()
}

private[python4s] object PythonReference {
  private val cleaner = Cleaner.create()
  private val cleanableQueue = new ConcurrentLinkedQueue[Pointer]()

  /**
    * Borrow reference from python interpreter.
    *
    * @param pointer native pointer
    * @return non-null reference or None
    */
  def borrow(pointer: Pointer): Option[PythonReference] = {
    reclaim()
    libPython.pyIncRef(pointer)
    Option(pointer).map(new PythonReference(_))
  }

  /**
    * Receive reference from python interpreter.
    *
    * @param pointer native pointer
    * @return non-null reference or None
    */
  def receive(pointer: Pointer): Option[PythonReference] = {
    reclaim()
    Option(pointer).map(new PythonReference(_))
  }

  /**
    * Release phantom reachable python references.
    */
  @tailrec
  def reclaim(): Unit = Option(cleanableQueue.poll()) match {
    case Some(pointer) =>
      libPython.pyDecRef(pointer)
      reclaim()
    case None =>
  }

  /**
    * Register python reference for clean up.
    *
    * @param reference python reference
    * @return cleanable
    */
  private def register(reference: PythonReference): Cleanable = {
    val pointer = reference.pointer
    cleaner.register(reference, () => cleanableQueue.add(pointer))
  }
}

