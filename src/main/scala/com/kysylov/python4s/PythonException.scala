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

class PythonException private(message: String) extends Exception(message)

private[python4s] object PythonException {
  private lazy val traceback = Python.importModule("traceback")

  /**
    * Retrieve and clear Python error indicator. Convert Python error to JVM exception.
    *
    * @return Some(exception) if error occurred otherwise None.
    */
  def fetch(): Option[PythonException] = {
    if (libPython.pyErrOccurred)
      libPython.pyErrFetch().map { case (errorType, errorValue, errorTrace) =>
        PythonException(PythonObject(errorType), PythonObject(errorValue), errorTrace.map(PythonObject(_)))
      }
    else
      None
  }

  /**
    * Convert python error to JVM exception.
    * Modify stacktrace to include JVM and Python calls.
    *
    * @param errorType  class
    * @param errorValue message
    * @param errorTrace traceback
    * @return JVM exception
    */
  private def apply(errorType: PythonObject,
                    errorValue: PythonObject,
                    errorTrace: Option[PythonObject]): PythonException = {
    val exception = new PythonException(s"[${errorType.__name__}] $errorValue")

    // convert FrameSummary (Python) to StackTraceElement (JVM)
    val pythonStackTrace = errorTrace.toSeq
      .flatMap(tb => traceback.extract_tb(tb).toIterator)
      .map(frame => new StackTraceElement(
        s"<${Python.libraryName}>",
        frame.name.toString,
        frame.filename.toString,
        frame.lineno.toInt
      ))
      .reverse
      .toArray

    // remove supporting calls from JVM stacktrace
    val jvmStackTrace = exception.getStackTrace
      .reverse
      .takeWhile(_.getClassName != this.getClass.getName)
      .reverse

    exception.setStackTrace(pythonStackTrace ++ jvmStackTrace)
    exception
  }

}