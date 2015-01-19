/*$Id: PeakList.cpp 2223 2009-08-11 12:53:09Z bkausler $*/

/*
 * PeakList.cpp
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


#include "ms++/PeakList.h"
#include "ms++/Error.h"

#include <fstream>
#include <iostream>
#include <iterator>
#include <sstream>
#include <string>
#include <algorithm>

#ifndef _MSC_VER
#include <ext/algorithm>
#endif

using namespace ms;

PeakList::PeakList(PeakListPrivate::Ref& r)
        : PeakListBase(r)
{
}

PeakList::PeakList()
        : PeakListBase()
{
}

PeakList::PeakList(const std::string& filename)
{
    // call default constructor
    PeakList();

    double mz, abundance, rt;
    int charge;
    std::ifstream ifs(filename.c_str());
    if (ifs) {
        while (!ifs.eof()) {
            ifs >> mz >> charge >> abundance >> rt;
            push_back(PeakList::value_type(mz, charge, abundance, rt));
        }
    }
}

PeakList::PeakList(size_type size)
        : PeakListBase()
{
    d.peaks()->resize(size);
}

void PeakList::resize(size_type n)
{
#ifdef DATABASE
    d.modify();
#endif
    d.peaks()->resize(n);
}

#ifdef DATABASE
ConstPeakList PeakList::fromID(int id)
{
    return ConstPeakListRegistry::instance().requestList(id);
}

ConstPeakList PeakList::store()
{
    d.store();
    return ConstPeakList(d);
}
#endif

PeakList::iterator PeakList::begin()
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->begin();
}

PeakList::iterator PeakList::end()
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->end();
}

PeakList::reference PeakList::operator[](size_type n)
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->operator[](n);
}

PeakList::reference PeakList::back()
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->back();
}

void PeakList::push_back(const value_type& p)
{
#ifdef DATABASE
    d.modify();
#endif
    d.peaks()->push_back(p);
}

void PeakList::pop_back()
{
#ifdef DATABASE
    d.modify();
#endif
    d.peaks()->pop_back();
}

PeakList::iterator PeakList::erase(PeakList::iterator pos)
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->erase(pos);
}

PeakList::iterator PeakList::erase(PeakList::iterator first, PeakList::iterator last)
{
#ifdef DATABASE
    d.modify();
#endif
    return d.peaks()->erase(first, last);
}

void PeakList::clear()
{
#ifdef DATABASE
    d.modify();
#endif
    d.peaks()->clear();
}


struct ms::PeakList::ConcatItems : public std::unary_function<std::string, ms::PeakList::value_type> {
    std::string operator()(const ms::PeakList::value_type& p) {
        std::ostringstream os;
        os << p.getRt() << '\t' << p.getMz() << '\t' << p.getCharge() << '\t' << p.getAbundance() << std::endl;
        return(os.str());
    }
};

std::ostream& operator<<(std::ostream& os, ms::PeakList& p)
{
    transform(p.begin(), p.end(), std::ostream_iterator<std::string>(os), ms::PeakList::ConcatItems());
    return(os);
}

PeakList PeakList::subset(const int charge) const
{
    mspp_precondition(is_sorted(cbegin(), cend(), LessThanChargeMz<Peak, Peak>()), "PeakList must be sorted by charge and mz before trying to get a charge subset");

    // use upper_bound to locate the upper bound
    Peaks::iterator low = lower_bound(d.peaks()->begin(), d.peaks()->end(), charge, LessThanCharge<Peak, int>());
    Peaks::iterator up  = upper_bound(low, d.peaks()->end(), charge, LessThanCharge<int, Peak>());

    // get the subset
    PeakList newPeakList;
    for (PeakList::const_iterator peak = low; peak != up; peak++)
        newPeakList.push_back(*peak);

    return newPeakList;
}

PeakList PeakList::subset(const std::pair<double, double>& mzRange) const
{
    mspp_precondition(is_sorted(cbegin(), cend(), LessThanMz<Peak, Peak>()), "PeakList must be sorted by mz before trying to get a mz subset");

    // use upper_bound to locate the upper bound
    Peaks::iterator low = lower_bound(d.peaks()->begin(), d.peaks()->end(), mzRange.first, LessThanMz<Peak, double>());
    Peaks::iterator up  = upper_bound(low, d.peaks()->end(), mzRange.second, LessThanMz<double, Peak>());

    // get the subset
    PeakList newPeakList;
    for (PeakList::const_iterator peak = low; peak != up; peak++)
        newPeakList.push_back(*peak);

    return newPeakList;
}

PeakList PeakList::subset(const double mass, const double massError) const
{
    mspp_precondition(is_sorted(cbegin(), cend(), LessThanMass<Peak, Peak>()), "PeakList must be sorted by mass before trying to get a mass subset");

    // use upper_bound to locate the upper bound
    Peaks::iterator low = lower_bound(d.peaks()->begin(), d.peaks()->end(), mass - massError, LessThanMass<Peak, double>());
    Peaks::iterator up  = upper_bound(low, d.peaks()->end(), mass + massError, LessThanMass<double, Peak>());

    // get the subset
    PeakList newPeakList;
    for (PeakList::const_iterator peak = low; peak != up; peak++)
        newPeakList.push_back(*peak);

    return newPeakList;
}

PeakList PeakList::find(const Peak& peak, const double mzError) const
{
    mspp_precondition(is_sorted(cbegin(), cend(), LessThanChargeMz<Peak, Peak>()), "PeakList must be sorted by charge and mz before trying to find");

    Peak pLow = Peak(peak.getMz() - mzError, peak.getCharge(), 1);
    Peak pUp = Peak(peak.getMz() + mzError, peak.getCharge(), 1);
    // use upper_bound to locate the upper bound
    Peaks::iterator low = std::lower_bound(d.peaks()->begin(), d.peaks()->end(), pLow, LessThanChargeMz<Peak, Peak>());
    Peaks::iterator up  = std::upper_bound(low, d.peaks()->end(), pUp, LessThanChargeMz<Peak, Peak>());

    // get the subset
    PeakList newPeakList;
    for (PeakList::const_iterator peak = low; peak != up; peak++)
        newPeakList.push_back(*peak);

    return newPeakList;
}

PeakList PeakList::find(PeakList& peakList, const double mzError) const
{
    // get the subset
    PeakList results;
    for (PeakList::iterator peak = peakList.begin(); peak != peakList.end(); peak++) {
        PeakList newPeakList = PeakList::find(*peak, mzError);
        std::copy(newPeakList.begin(), newPeakList.end(), std::back_inserter(results));
    }

    return results;
}
