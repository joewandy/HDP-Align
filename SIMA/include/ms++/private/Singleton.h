/*$Id: Singleton.h 2152 2009-07-17 14:53:35Z bkausler $*/

/*
 * Singleton.h
 *
 * Copyright (c) 2008 Thorben Kroeger <thorben.kroeger@iwr.uni-heidelberg.de>
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

#ifndef __SINGLETON_H__
#define __SINGLETON_H__
#include <ms++/config.h>

namespace ms
{

template<typename T> class Singleton
{
public:
    static T& instance() {
        static T theSingleInstance;  // assumes T has a protected default constructor
        return theSingleInstance;
    }
protected:
    Singleton(){}
 
private:
    Singleton( const Singleton& ) ;
    Singleton& operator=( const Singleton& ) ;
};

} /*namespace ms*/

#endif
