/*$Id: Error.h 2253 2009-08-19 11:43:25Z bkausler $*/

/*
 * Error.h
 *
 * Copyright (c) 2009 Bernhard Kausler <bernhard.kausler@iwr.uni-karlsruhe.de>
 * Copyright (c) 2009 Marc Kirchner <marc.kirchner@childrens.harvard.edu>
 *
 * This file is part of ms++.
 *
 * ms++ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ms++ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ms++. If not, see <http://www.gnu.org/licenses/>.
 *
 */

#ifndef __ERROR_H__
#define __ERROR_H__

#include <exception>
#include <string>

namespace ms
{

//////////////////
// base classes //
//////////////////

/**
*   Base class for all ms++ exceptions.
*
*/
class Exception : public std::exception
{
public:
    /** Constructor to pass C-style strings.
     *  @param message C-style string describing the error condition.
     *                 Callee is responsible for freeing the memory.
     */
    explicit Exception(const char* message) {
        what_ = message;
    }
    /** Constructor taking C++-style strings.
     *  @param message String describing the error condition.
     */
    explicit Exception(const std::string& message) {
        what_ = message;
    }
    /** Virtual destructor. Not abstract for convenience.
     */
    virtual ~Exception() throw() {}
    /** C-string-style access to the description of the error condition.
     *  @return A pointer to a C-string. The memory is under possession
     *          of the Exception object. Callees should NEVER try to
     *          destroy/free the returned pointer.
     */
    virtual const char * what() const throw() {
        return what_.c_str();
    }

protected:
    /** Stores the string that describes the error condition.
     *
     * The implementation leaves the variable as 'protected' to
     * simplify reimplentations of 'what()' in derived classes.
     */
    std::string what_;
}; /* class Exception */


/**
*   Base class for all logic errors.
*
*   Use LogicError for defects, which in principle could be detected by codeflow analysis.
*   In general logic errors are originated from invalid external input or code bugs, which are
*   causing the ms++ to run outside expected parameter ranges.
*/
class LogicError : public ms::Exception
{
public:
    explicit LogicError(const char * message) : Exception(message) {}
    explicit LogicError(const std::string& message) : Exception(message) {}
    virtual ~LogicError() throw() {}
}; /* class LogicError */

/**
*   Base class for all runtime errors.
*
*   Use RuntimeError for defects, which could only happen or be detected during runtime.
*   Runtime errors are caused by not acquirable system resources (like memory, file handles, network etc.),
*   race conditions of different threads or processes and other unforseeable failures.
*/
class RuntimeError : public ms::Exception
{
public:
    explicit RuntimeError(const char* message) : Exception(message) {}
    explicit RuntimeError(const std::string& message) : Exception(message) {}
    virtual ~RuntimeError() throw() {}
}; /* class RuntimeError */

//////////////////////////////////
// now, some general subclasses //
//////////////////////////////////

class PreconditionViolation : public LogicError
{
public:
    explicit PreconditionViolation(const char* message) : LogicError(message) {}
    explicit PreconditionViolation(const std::string& message) : LogicError(message) {}
    virtual ~PreconditionViolation() throw() {}
}; /* class PreconditionViolation */


class PostconditionViolation : public LogicError
{
public:
    explicit PostconditionViolation(const char* message) : LogicError(message) {}
    explicit PostconditionViolation(const std::string& message) : LogicError(message) {}
    virtual ~PostconditionViolation() throw() {}
}; /* class PostconditionViolation */


class InvariantViolation : public LogicError
{
public:
    explicit InvariantViolation(const char* message) : LogicError(message) {}
    explicit InvariantViolation(const std::string& message) : LogicError(message) {}
    virtual ~InvariantViolation() throw() {}
}; /* class InvariantViolation */

//////////////////////////////
// here the specific errors //
//////////////////////////////

/**
* Error for out-of-range access.
*
* This exception is used if an operation attempts to access a container
* beyond its boudaries. This is a logic error because it could be
* avoided if range checks were in place before calling a function.
*/
class OutOfRange : public LogicError
{
public:
    explicit OutOfRange(const char* message) : LogicError(message) {}
    explicit OutOfRange(const std::string& message) : LogicError(message) {}
    virtual ~OutOfRange() throw() {}
}; /* class OutOfRange */

// Starvation
/**
 * Insufficient amount of data to finish a calculation.
 *
 * During data analysis an algorithm may be forced to give up a calculation because of
 * too few or worse input data. In this case, a starvation error might be thrown.
 */
class Starvation : public LogicError
{
public:
    explicit Starvation(const char* message) : LogicError(message) {}
    explicit Starvation(const std::string& message) : LogicError(message) {}
    virtual ~Starvation() throw() {}
};

// NumericalInstability
/**
 * Exceptional termination of an algorithm due to numerical instability.
 *
 * Some algorithms aren't robust and may become numerical instable for specific
 * inputs. If this is detected by the program, a NumericalInstability may be thrown.
 */
class NumericalInstability : public LogicError
{
public:
    explicit NumericalInstability(const char* message) : LogicError(message) {}
    explicit NumericalInstability(const std::string& message) : LogicError(message) {}
    virtual ~NumericalInstability() throw() {}
};

/**
* Runtime error regarding insufficient RAM.
*
* This exception is used if:
*  - an operation would need more memory than available
*  - the allocation of memory failed
*
* In this sense it is more general than a std::bad_alloc,
* because it is used before and after trying to allocate memory.
* For example, a memory estimation could be done before allocating. If
* the estimation predicts to much needed memory, an InsufficientMemory could
* be thrown.
*/
class InsufficientMemory : public RuntimeError
{
public:
    explicit InsufficientMemory(const char* message) : RuntimeError(message) {}
    explicit InsufficientMemory(const std::string& message) : RuntimeError(message) {}
    virtual ~InsufficientMemory() throw() {}
}; /* class InsufficientMemory */

/**
* Error if a bad cast happens.
*
* This exception is used if a cast fails.
*/
class BadCast : public LogicError
{
public:
    explicit BadCast(const char* message) : LogicError(message) {}
    explicit BadCast(const std::string& message) : LogicError(message) {}
    virtual ~BadCast() throw() {}
}; /* class BadCast */


//////////////////////////////////////////////////////
// some helper functions to throw exceptions easily //
// dont't use them directly, use the macros below   //
//////////////////////////////////////////////////////

inline
void throw_invariant_error(bool predicate, const char* message)
{
    if (!predicate)
        throw ms::InvariantViolation(message);
}

inline
void throw_precondition_error(bool predicate, const char* message)
{
    if (!predicate)
        throw ms::PreconditionViolation(message);
}

inline
void throw_postcondition_error(bool predicate, const char* message)
{
    if (!predicate)
        throw ms::PostconditionViolation(message);
}

inline
void throw_invariant_error(bool predicate, const std::string& message)
{
    if (!predicate)
        throw ms::InvariantViolation(message);
}

inline
void throw_precondition_error(bool predicate, const std::string& message)
{
    if (!predicate)
        throw ms::PreconditionViolation(message);
}

inline
void throw_postcondition_error(bool predicate, const std::string& message)
{
    if (!predicate)
        throw ms::PostconditionViolation(message);
}

///////////////////////////////////////////////
// Macros to write quick throwing statements //
///////////////////////////////////////////////

/**
* Throws a ms::PreconditionViolation, if the PREDICATE is false.
*/
#define mspp_precondition(PREDICATE, MESSAGE) ms::throw_precondition_error((PREDICATE), MESSAGE)
/**
* Throws a ms::PostconditionViolation, if the PREDICATE is false.
*/
#define mspp_postcondition(PREDICATE, MESSAGE) ms::throw_postcondition_error((PREDICATE), MESSAGE)
/**
* Throws a ms::InvariantViolation, if the PREDICATE is false.
*/
#define mspp_invariant(PREDICATE, MESSAGE) ms::throw_invariant_error((PREDICATE), MESSAGE)
/**
* Throws a RuntimeError.
*/
#define mspp_fail(MESSAGE) throw ms::RuntimeError(MESSAGE)

} /* namespace ms */
#endif /* __ERROR_H__ */
