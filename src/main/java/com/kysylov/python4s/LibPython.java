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

package com.kysylov.python4s;

import jnr.ffi.Pointer;
import jnr.ffi.annotations.LongLong;
import jnr.ffi.annotations.Out;
import jnr.ffi.byref.NumberByReference;
import jnr.ffi.byref.PointerByReference;
import jnr.ffi.types.ssize_t;

public interface LibPython {
    // The Very High Level Layer

    /**
     * Executes the Python source code from command in the __main__ module.
     * If __main__ does not already exist, it is created.
     * Returns 0 on success or -1 if an exception was raised.
     * If there was an error, there is no way to get the exception information.
     *
     * @param command const char*
     * @return int
     */
    int PyRun_SimpleString(String command);

    // Reference Counting

    /**
     * Increment the reference count for object o. The object may be NULL, in which case the function has no effect.
     *
     * @param o PyObject*
     */
    void Py_IncRef(Pointer o);

    /**
     * Decrement the reference count for object o. The object may be NULL, in which case the macro has no effect.
     *
     * @param o PyObject*
     */
    void Py_DecRef(Pointer o);

    // Exception Handling

    /**
     * Test whether the error indicator is set.
     * If set, return the exception type. If not set, return NULL.
     *
     * @return PyObject* (borrowed reference)
     */
    Pointer PyErr_Occurred();

    /**
     * Retrieve the error indicator into three variables whose addresses are passed.
     * If the error indicator is not set, set all three variables to NULL.
     * If it is set, it will be cleared and you own a reference to each object retrieved.
     * The value and traceback object may be NULL even when the type object is not.
     *
     * @param ptype      PyObject** (received reference)
     * @param pvalue     PyObject** (received reference)
     * @param ptraceback PyObject** (received reference)
     */
    void PyErr_Fetch(@Out PointerByReference ptype, @Out PointerByReference pvalue, @Out PointerByReference ptraceback);

    /**
     * Under certain circumstances, the values returned by PyErr_Fetch() below can be “unnormalized”,
     * meaning that *exc is a class object but *val is not an instance of the same class.
     * This function can be used to instantiate the class in that case.
     * If the values are already normalized, nothing happens.
     * The delayed normalization is implemented to improve performance.
     *
     * @param exc PyObject**
     * @param val PyObject**
     * @param tb  PyObject**
     */
    void PyErr_NormalizeException(PointerByReference exc, PointerByReference val, PointerByReference tb);

    // Operating System Utilities

    /**
     * Decode a byte string from the locale encoding.
     * Return a pointer to a newly allocated wide character string, use PyMem_RawFree() to free the memory.
     * If size is not NULL, write the number of wide characters excluding the null character into *size.
     * Return NULL on decoding error or memory allocation error.
     * If size is not NULL, *size is set to (size_t)-1 on memory error or set to (size_t)-2 on decoding error.
     *
     * @param arg  const char*
     * @param size size_t*
     * @return wchar_t*
     */
    Pointer Py_DecodeLocale(String arg, @Out NumberByReference size);

    // Importing Modules

    /**
     * Import a module.
     * Return a new reference to the imported module, or NULL with an exception set on failure.
     * A failing import of a module doesn’t leave the module in sys.modules.
     * This function always uses absolute imports.
     *
     * @param name const char*
     * @return PyObject* (received reference)
     */
    Pointer PyImport_ImportModule(String name);

    // Parsing arguments and building values

    /**
     * Create a new value based on a format string.
     * Returns the value or NULL in the case of an error; an exception will be raised if NULL is returned.
     * If the format string is empty, it returns None;
     * if it contains exactly one format unit, it returns whatever object is described by that format unit.
     *
     * @param format const char*
     * @param va     ...
     * @return PyObject* (received reference)
     */
    Pointer Py_BuildValue(String format, Object... va);

    // Reflection

    /**
     * Return a dictionary of the builtins in the current execution frame,
     * or the interpreter of the thread state if no frame is currently executing.
     *
     * @return PyObject* (borrowed reference)
     */
    Pointer PyEval_GetBuiltins();

    // Object Protocol

    /**
     * Retrieve an attribute named attr_name from object o.
     * Returns the attribute value on success, or NULL on failure.
     *
     * @param o         PyObject*
     * @param attr_name const char*
     * @return PyObject* (received reference)
     */
    Pointer PyObject_GetAttrString(Pointer o, String attr_name);

    /**
     * Set the value of the attribute named attr_name, for object o, to the value v.
     * Raise an exception and return -1 on failure; return 0 on success.
     *
     * @param o         PyObject*
     * @param attr_name const char*
     * @param v         PyObject*
     * @return int
     */
    int PyObject_SetAttrString(Pointer o, String attr_name, Pointer v);

    /**
     * Compare the values of o1 and o2 using the operation specified by opid,
     * which must be one of Py_LT, Py_LE, Py_EQ, Py_NE, Py_GT, or Py_GE,
     * corresponding to <, <=, ==, !=, >, or >= respectively.
     * Returns -1 on error, 0 if the result is false, 1 otherwise.
     *
     * @param o1   PyObject*
     * @param o2   PyObject*
     * @param opid int
     * @return int
     */
    int PyObject_RichCompareBool(Pointer o1, Pointer o2, int opid);

    /**
     * Compute a string representation of object o.
     * Returns the string representation on success, NULL on failure.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyObject_Str(Pointer o);

    /**
     * Compute and return the hash value of an object o.
     * On failure, return -1.
     *
     * @param o PyObject*
     * @return Py_hash_t
     */
    @ssize_t
    long PyObject_Hash(Pointer o);

    /**
     * Returns 1 if the object o is considered to be true, and 0 otherwise.
     * On failure, return -1.
     *
     * @param o PyObject*
     * @return int
     */
    int PyObject_IsTrue(Pointer o);

    /**
     * Return element of o corresponding to the object key or NULL on failure.
     *
     * @param o   PyObject*
     * @param key PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyObject_GetItem(Pointer o, Pointer key);

    /**
     * Map the object key to the value v.
     * Raise an exception and return -1 on failure; return 0 on success.
     *
     * @param o   PyObject*
     * @param key PyObject*
     * @param v   PyObject*
     * @return PyObject* (received reference)
     */
    int PyObject_SetItem(Pointer o, Pointer key, Pointer v);

    /**
     * It returns a new iterator for the object argument, or the object itself if the object is already an iterator.
     * Raises TypeError and returns NULL if the object cannot be iterated.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyObject_GetIter(Pointer o);

    // Call Protocol

    /**
     * Call a callable Python object callable,
     * with arguments given by the tuple args, and named arguments given by the dictionary kwargs.
     * args must not be NULL, use an empty tuple if no arguments are needed.
     * If no named arguments are needed, kwargs can be NULL.
     * Return the result of the call on success, or raise an exception and return NULL on failure.
     *
     * @param callable PyObject*
     * @param args     PyObject*
     * @param kwargs   PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyObject_Call(Pointer callable, Pointer args, Pointer kwargs);

    /**
     * Call a callable Python object callable, with a variable number of PyObject* arguments.
     * The arguments are provided as a variable number of parameters followed by NULL.
     * Return the result of the call on success, or raise an exception and return NULL on failure.
     *
     * @param callable PyObject*
     * @param args     ..., NULL
     * @return PyObject* (received reference)
     */
    Pointer PyObject_CallFunctionObjArgs(Pointer callable, Pointer... args);

    /**
     * Calls a method of the Python object obj, where the name of the method is given as a Python string object in name.
     * It is called with a variable number of PyObject* arguments.
     * The arguments are provided as a variable number of parameters followed by NULL.
     * Return the result of the call on success, or raise an exception and return NULL on failure.
     *
     * @param callable PyObject*
     * @param name     PyObject*
     * @param args     ..., NULL
     * @return PyObject* (received reference)
     */
    Pointer PyObject_CallMethodObjArgs(Pointer callable, Pointer name, Pointer... args);

    /**
     * Determine if the object o is callable.
     * Return 1 if the object is callable and 0 otherwise.
     *
     * @param o PyObject*
     * @return int
     */
    int PyCallable_Check(Pointer o);

    // Number Protocol

    /**
     * Returns the result of adding o1 and o2, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Add(Pointer o1, Pointer o2);

    /**
     * Returns the result of subtracting o2 from o1, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Subtract(Pointer o1, Pointer o2);

    /**
     * Returns the result of multiplying o1 and o2, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Multiply(Pointer o1, Pointer o2);

    /**
     * Returns the result of matrix multiplication on o1 and o2, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_MatrixMultiply(Pointer o1, Pointer o2);

    /**
     * Return the floor of o1 divided by o2, or NULL on failure.
     * This is equivalent to the “classic” division of integers.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_FloorDivide(Pointer o1, Pointer o2);

    /**
     * Return a reasonable approximation for the mathematical value of o1 divided by o2, or NULL on failure.
     * The return value is “approximate” because binary floating point numbers are approximate;
     * it is not possible to represent all real numbers in base two.
     * This function can return a floating point value when passed two integers.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_TrueDivide(Pointer o1, Pointer o2);

    /**
     * Returns the remainder of dividing o1 by o2, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Remainder(Pointer o1, Pointer o2);

    /**
     * Return o1 to the power o2; if o3 is present, return o1 to the power o2, modulo o3. Returns NULL on failure.
     * If o3 is to be ignored, pass Py_None in its place (passing NULL for o3 would cause an illegal memory access).
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @param o3 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Power(Pointer o1, Pointer o2, Pointer o3);

    /**
     * Returns the negation of o on success, or NULL on failure.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Negative(Pointer o);

    /**
     * Returns o on success, or NULL on failure.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Positive(Pointer o);

    /**
     * Returns the bitwise negation of o on success, or NULL on failure.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Invert(Pointer o);

    /**
     * Returns the result of left shifting o1 by o2 on success, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Lshift(Pointer o1, Pointer o2);

    /**
     * Returns the result of right shifting o1 by o2 on success, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Rshift(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise and” of o1 and o2 on success and NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_And(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise exclusive or” of o1 by o2 on success, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Xor(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise or” of o1 and o2 on success, or NULL on failure.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_Or(Pointer o1, Pointer o2);

    /**
     * Returns the result of adding o1 and o2, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceAdd(Pointer o1, Pointer o2);

    /**
     * Returns the result of subtracting o2 from o1, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceSubtract(Pointer o1, Pointer o2);

    /**
     * Returns the result of multiplying o1 and o2, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceMultiply(Pointer o1, Pointer o2);

    /**
     * Returns the result of matrix multiplication on o1 and o2, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceMatrixMultiply(Pointer o1, Pointer o2);

    /**
     * Returns the mathematical floor of dividing o1 by o2, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceFloorDivide(Pointer o1, Pointer o2);

    /**
     * Return a reasonable approximation for the mathematical value of o1 divided by o2, or NULL on failure.
     * The return value is “approximate” because binary floating point numbers are approximate;
     * it is not possible to represent all real numbers in base two.
     * This function can return a floating point value when passed two integers.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceTrueDivide(Pointer o1, Pointer o2);

    /**
     * Returns the remainder of dividing o1 by o2, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceRemainder(Pointer o1, Pointer o2);

    /**
     * Return o1 to the power o2; if o3 is present, return o1 to the power o2, modulo o3. Returns NULL on failure.
     * If o3 is to be ignored, pass Py_None in its place (passing NULL for o3 would cause an illegal memory access).
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @param o3 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlacePower(Pointer o1, Pointer o2, Pointer o3);

    /**
     * Returns the result of left shifting o1 by o2 on success, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceLshift(Pointer o1, Pointer o2);

    /**
     * Returns the result of right shifting o1 by o2 on success, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceRshift(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise and” of o1 and o2 on success and NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceAnd(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise exclusive or” of o1 by o2 on success, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceXor(Pointer o1, Pointer o2);

    /**
     * Returns the “bitwise or” of o1 and o2 on success, or NULL on failure.
     * The operation is done in-place when o1 supports it.
     *
     * @param o1 PyObject*
     * @param o2 PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyNumber_InPlaceOr(Pointer o1, Pointer o2);

    // Sequence Protocol

    /**
     * Return the ith element of o, or NULL on failure.
     *
     * @param o PyObject*
     * @param i Py_ssize_t
     * @return PyObject* (received reference)
     */
    Pointer PySequence_GetItem(Pointer o, @ssize_t long i);

    // Mapping Protocol

    /**
     * On success, return a list of the items in object o, where each item is a tuple containing a key-value pair.
     * On failure, return NULL.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyMapping_Items(Pointer o);

    // Iterator Protocol

    /**
     * Return the next value from the iteration o.
     * The object must be an iterator (it is up to the caller to check this).
     * If there are no remaining values, returns NULL with no exception set.
     * If an error occurs while retrieving the item, returns NULL and passes along the exception.
     *
     * @param o PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PyIter_Next(Pointer o);

    // Integer Objects

    /**
     * Return a new PyLongObject object from v, or NULL on failure.
     *
     * @param v long
     * @return PyObject* (received reference)
     */
    Pointer PyLong_FromLong(long v);

    /**
     * Return a C long long representation of obj.
     * If obj is not an instance of PyLongObject, first call its __int__() method to convert it to a PyLongObject.
     * Raise OverflowError if the value of obj is out of range for a long.
     * Returns -1 on error. Use PyErr_Occurred() to disambiguate.
     *
     * @param obj PyObject*
     * @return long long
     */
    @LongLong
    Long PyLong_AsLongLong(Pointer obj);

    // Boolean Objects

    /**
     * Return a new reference to Py_True or Py_False depending on the truth value of v.
     *
     * @param v long
     * @return PyObject* (received reference)
     */
    Pointer PyBool_FromLong(long v);

    // Floating Point Objects

    /**
     * Create a PyFloatObject object from v, or NULL on failure.
     *
     * @param v double
     * @return PyObject* (received reference)
     */
    Pointer PyFloat_FromDouble(double v);

    /**
     * Return a C double representation of the contents of pyfloat.
     * If pyfloat is not a Python floating point object but has a __float__() method,
     * this method will first be called to convert pyfloat into a float.
     * This method returns -1.0 upon failure, so one should call PyErr_Occurred() to check for errors.
     *
     * @param pyfloat PyObject*
     * @return double
     */
    Double PyFloat_AsDouble(Pointer pyfloat);

    // Unicode Objects

    /**
     * Create a Unicode object from a UTF-8 encoded null-terminated char buffer u.
     *
     * @param u const char*
     * @return PyObject* (received reference)
     */
    Pointer PyUnicode_FromString(String u);

    /**
     * Return a pointer to the UTF-8 encoding of the Unicode object.
     * The returned buffer always has an extra null byte appended (not included in size),
     * regardless of whether there are any other null code points.
     * In the case of an error, NULL is returned with an exception set and no size is stored.
     * This caches the UTF-8 representation of the string in the Unicode object,
     * and subsequent calls will return a pointer to the same buffer.
     * The caller is not responsible for deallocating the buffer.
     *
     * @param unicode PyObject*
     * @return const char*
     */
    String PyUnicode_AsUTF8(Pointer unicode);

    // Tuple Objects

    /**
     * Return a new tuple object of size len, or NULL on failure.
     *
     * @param len Py_ssize_t
     * @return PyObject* (received reference)
     */
    Pointer PyTuple_New(@ssize_t long len);

    /**
     * Insert a reference to object o at position pos of the tuple pointed to by p. Return 0 on success.
     * This function “steals” a reference to o.
     *
     * @param p   PyObject*
     * @param pos Py_ssize_t
     * @param o   PyObject*
     * @return int
     */
    int PyTuple_SetItem(Pointer p, @ssize_t long pos, Pointer o);

    // List Objects

    /**
     * Return a new list of length len on success, or NULL on failure.
     * If len is greater than zero, the returned list object’s items are set to NULL.
     * Thus you cannot use abstract API functions such as PySequence_SetItem() or expose the object to Python code
     * before setting all items to a real object with PyList_SetItem().
     *
     * @param len Py_ssize_t
     * @return PyObject* (received reference)
     */
    Pointer PyList_New(@ssize_t long len);

    /**
     * Set the item at index index in list to item. Return 0 on success or -1 on failure.
     * This function “steals” a reference to item
     * and discards a reference to an item already in the list at the affected position.
     *
     * @param list  PyObject*
     * @param index Py_ssize_t
     * @param item  PyObject*
     * @return int
     */
    int PyList_SetItem(Pointer list, @ssize_t long index, Pointer item);

    /**
     * Append the object item at the end of list list.
     * Return 0 if successful; return -1 and set an exception if unsuccessful.
     *
     * @param list PyObject*
     * @param item PyObject*
     * @return int
     */
    int PyList_Append(Pointer list, Pointer item);

    // Dictionary Objects

    /**
     * Return a new empty dictionary, or NULL on failure.
     *
     * @return PyObject* (received reference)
     */
    Pointer PyDict_New();

    /**
     * Insert value into the dictionary p with a key of key.
     * key must be hashable; if it isn’t, TypeError will be raised.
     * Return 0 on success or -1 on failure.
     *
     * @param p   PyObject*
     * @param key PyObject*
     * @param val PyObject*
     * @return int
     */
    int PyDict_SetItem(Pointer p, Pointer key, Pointer val);

    // Set Objects

    /**
     * Return a new set containing objects returned by the iterable. The iterable may be NULL to create a new empty set.
     * Return the new set on success or NULL on failure. Raise TypeError if iterable is not actually iterable.
     *
     * @param iterable PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PySet_New(Pointer iterable);

    /**
     * Add key to a set instance. Return 0 on success or -1 on failure. Raise a TypeError if the key is unhashable.
     * Raise a MemoryError if there is no room to grow.
     * Raise a SystemError if set is not an instance of set or its subtype.
     *
     * @param set PyObject*
     * @param key PyObject*
     * @return int
     */
    int PySet_Add(Pointer set, Pointer key);

    // Slice Objects

    /**
     * Return a new slice object with the given values.
     * Any of the values may be NULL, in which case the None will be used for the corresponding attribute.
     * Return NULL if the new object could not be allocated.
     *
     * @param start PyObject*
     * @param stop  PyObject*
     * @param step  PyObject*
     * @return PyObject* (received reference)
     */
    Pointer PySlice_New(Pointer start, Pointer stop, Pointer step);

    // Initialization, Finalization, and Threads

    /**
     * This function should be called before Py_Initialize() is called for the first time, if it is called at all.
     * It tells the interpreter the value of the argv[0] argument to the main() function of the program.
     *
     * @param name const wchar_t*
     */
    void Py_SetProgramName(Pointer name);

    /**
     * Initialize the Python interpreter.
     * If initsigs is 0, it skips initialization registration of signal handlers.
     *
     * @param initsigs int
     */
    void Py_InitializeEx(int initsigs);

    /**
     * Initialize and acquire the global interpreter lock.
     * Since 3.7 this function is called by Py_Initialize().
     */
    void PyEval_InitThreads();
}
