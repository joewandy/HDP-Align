/*$Id: PeakListBase.cpp 2029 2009-06-29 14:48:06Z tkroeger $*/

/*
 * PeakListBase.cpp
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

#include <ms++/private/PeakListBase.h>

using namespace ms;

PeakListBase::PeakListBase()
{
    d = PeakListPrivate::create();
}

PeakListBase::PeakListBase(PeakListPrivate::Ref ref)
{
    d = ref;
}
