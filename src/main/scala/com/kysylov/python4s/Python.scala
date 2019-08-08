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

import jnr.ffi.LibraryLoader

import scala.language.dynamics
import scala.sys.process._

object Python extends Dynamic {
  // locate executable and shared library
  val Array(executable, libraryDirectory, libraryName) =
    s"""${sys.env.getOrElse("PYTHON4S_EXECUTABLE", "python")} -c "
       |import sys
       |from distutils.sysconfig import get_config_var
       |
       |print(sys.executable)
       |print(get_config_var('LIBDIR'))
       |print('python{version}{abiflags}'.format(
       |  version=get_config_var('VERSION'),
       |  abiflags=(get_config_var('ABIFLAGS') or '')
       |))
       |"
    """.stripMargin.!!.split('\n')

  private[python4s] val libPython = {
    // load shared library
    val library = new PythonLibrary(
      LibraryLoader
        .create(classOf[LibPython])
        .search(libraryDirectory)
        .load(libraryName)
    )

    // program name is required in order to set sys.exec_path
    library.pySetProgramName("python")

    // initialize interpreter
    library.pyInitializeEx(false)

    // execute runtime fixes
    library.pyRunSimpleString(
      s"""import sys
         |
         |# Some Python modules expect to have at least one argument in sys.argv.
         |sys.argv = ['']
         |
         |# Some Python modules require sys.executable to return the path to the Python interpreter executable.
         |sys.executable = '$executable'
         |
         |# Add working directory to module search path.
         |sys.path.insert(0, '')
      """.stripMargin)

    library
  }

  private val builtins = PythonObject(libPython.pyEvalGetBuiltins)

  /**
    * Call a built-in function.
    *
    * @param methodName function name
    * @param args       arguments
    * @return python object
    */
  def applyDynamic(methodName: String)(args: PythonObject*): PythonObject = builtins(methodName)(args: _*)

  /**
    * Call a built-in function with named arguments.
    *
    * @param methodName function name
    * @param args       arguments
    * @return python object
    */
  def applyDynamicNamed(methodName: String)(args: (String, PythonObject)*): PythonObject = builtins(methodName)(
    args.collect { case (key, value) if key.isEmpty => value }.toSeq,
    args.filter { case (key, _) => !key.isEmpty }.toMap
  )

  /**
    * Get a built-in function.
    *
    * @param attributeName function name
    * @return python function
    */
  def selectDynamic(attributeName: String): PythonObject = builtins(attributeName)

  /**
    * Import python module
    *
    * @param name module name
    * @return python module
    */
  def importModule(name: String) = PythonObject(libPython.pyImportImportModule(name))
}
