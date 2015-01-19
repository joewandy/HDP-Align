/*$Id: PeakListBase.h 2070 2009-07-06 12:03:46Z tkroeger $*/

/*
 * PeakListBase.h
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

#ifndef __PEAKLISTBASE_H__
#define __PEAKLISTBASE_H__
#include <ms++/config.h>

#include <iostream>
#include <iomanip>
#include <vector>

#include "ms++/Peak.h"

#include "PeakListPrivate.h"

namespace ms
{

typedef std::vector<Peak> Peaks;

/**
 * base class for peak lists
 */
class MSPP_EXPORT PeakListBase
{
public:
    PeakListBase();
    PeakListBase(PeakListPrivate::Ref ref);

    typedef Peaks::value_type value_type;
    typedef Peaks::const_iterator const_iterator;
    typedef Peaks::const_reverse_iterator const_reverse_iterator;
    typedef Peaks::const_reference const_reference;
    typedef Peaks::size_type size_type;
    typedef Peaks::difference_type difference_type;

    bool operator==(const PeakListBase& p) {
        bool equal =  (*d.peaks() == *p.d.peaks());
        #ifdef DATABASE
        equal = equal && p.d.name_() == d.name()
        && p.d.owner() == d.owner()
        && p.d.description() == d.desc()
        && p.d.meta() == d.meta();
        #endif
        return equal;
    }
    
#ifdef DATABASE
    void setName(const std::string& name) {
        d.setName(name);
    }
    void setDescription(const std::string& desc) {
        d.setDescription(desc);
    }
    void setOwner(const std::string& owner) {
        d.setOwner(owner);
    }
    void setMeta(const std::string& meta) {
        d.setMeta(meta);
    }

    std::string getName() const {
        return d.name();
    }
    std::string getDescription() const {
        return d.desc();
    }
    std::string getOwner() const {
        return d.owner();
    }
    std::string getMeta() const {
        return d.meta();
    }

    int id() {
        return d.id();
    }
#endif /*DATABASE*/

    //CONST
    size_t size(void) const {
        return d.peaks()->size();
    }
    size_type capacity() const {
        return d.peaks()->capacity();
    }
    const_iterator begin() const {
        return d.peaks()->begin();
    }
    const_iterator end() const {
        return d.peaks()->end();
    }
    const_iterator cbegin() const {
        return d.peaks()->begin();
    }
    const_iterator cend() const {
        return d.peaks()->end();
    }
    const_reference operator[](size_type n) const {
        return d.peaks()->operator[](n);
    }
    const_reference back() const {
        return d.peaks()->back();
    }

protected:
    PeakListPrivate::Ref d;

#ifdef DATABASE
    friend std::ostream& operator<<(std::ostream& o, ConstPeakList& c);
    friend class DatabaseTest;
#endif
    friend class PeakListPrivate::Ref;

};

inline std::ostream& operator<<(std::ostream& o, const PeakListBase& p)
{
    o << "-------------------------------------------------------------------" << std::endl;
    o << "|    mz    |    rt    |   chg    |   abd    |  annId   |  rgnId   |" << std::endl;
    o << "-------------------------------------------------------------------" << std::endl;
    for (unsigned int i = 0; i < p.size(); ++i) {
        o << "|"
        << std::setw(10) << p[i].getMz() << "|"
        << std::setw(10) << p[i].getRt() << "|"
        << std::setw(10) << p[i].getCharge() << "|"
        << std::setw(10) << p[i].getAbundance() << "|"
        << std::setw(10) << p[i].getAnnotationId() << "|"
        << std::setw(10) << p[i].getRegionId() << "|"
        << std::endl;
    }
    o << "-------------------------------------------------------------------" << std::endl;
    return o;
}

} /*namespace ms*/

#endif /*__PEAKLISTBASE_H__*/
