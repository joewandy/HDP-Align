/*$Id: PeakList.h 2237 2009-08-13 12:46:42Z xlou $*/

/*
 * PeakList.h
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

#ifndef __PEAKLIST_H__
#define __PEAKLIST_H__
#include <ms++/config.h>

#include <vector>
#include <cassert>
#include <string>
#include <map>
#include <numeric>

#ifdef DATABASE
#include <ms++/ConstPeakList.h>
#include "ConstPeakListRegistry.h"
#endif

#include <ms++/Peak.h>

#include <ms++/private/PeakListBase.h>
#include <ms++/private/Singleton.h>

namespace ms
{

/**
 * a set of ms::Peak
 *
 * You can use this class like a std::vector<Peak>.
 */
class MSPP_EXPORT PeakList : public PeakListBase
{
public:
    typedef Peak value_type;
    typedef std::vector<PeakList> PeakListSet;

    PeakList();
    PeakList(size_type size);
    PeakList(const std::string& filename);
    
#ifdef DATABASE
public:
    ConstPeakList store();
    void load(int id);
    static ConstPeakList fromID(int id);
#endif /*DATABASE*/

    template<class A, class B>
    struct LessThanCharge : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** order two ms::Peak based on their \f$charge\f$ and \f$mz\f$ values
     */
    template<class A, class B>
    struct LessThanChargeMz : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** order two ms::Peak by their \f$retention time\f$
    */
    template<class A, class B>
    struct LessThanRt : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** order two ms::Peak based on their \f$mz\f$s
    */
    template<class A, class B>
    struct LessThanMz : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };
    
    /** order two ms::Peak based on their \f$mass\f$ values
    */
    template<class A, class B>
    struct LessThanMass : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** order two ms::Peak based on their \f$abundance\f$
    */
    template<class A, class B>
    struct LessThanAbundance : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** order two ms::Peak based on their \f$regionId\f$
    */
    template<class A, class B>
    struct LessThanRegionId : std::binary_function<A, B, bool> {
        bool operator()(const A& lhs, const B& rhs);
    };

    /** Concat the content of a PeakList item
     */
    struct ConcatItems;

    typedef std::vector<value_type>::iterator iterator;
    typedef std::vector<value_type>::const_iterator const_iterator;
    typedef std::vector<value_type>::reverse_iterator reverse_iterator;
    typedef std::vector<value_type>::const_reverse_iterator const_reverse_iterator;
    typedef std::vector<value_type>::pointer pointer;
    typedef std::vector<value_type>::reference reference;
    typedef std::vector<value_type>::const_reference const_reference;
    typedef std::vector<value_type>::size_type size_type;
    typedef std::vector<value_type>::difference_type difference_type;

    iterator begin();
    iterator end();
    reference operator[](size_type n);
    reference back();
    void push_back(const value_type& p);
    void pop_back();
    iterator erase(iterator pos);
    iterator erase(iterator first, iterator last);
    void clear();
    void resize(size_type n);
    bool empty() const {
        return d.peaks()->empty();
    }

    /** Get a subset of peaklist by given charge
    *  @param charge Charge
    *  @return A new PeakList object
    *  @throws ms::PreconditionViolation peak list not sorted correctly
    */
    PeakList subset(const int charge) const;

    /** Get a subset of peaklist by mz range
    *  @param mzRange The mz range
    *  @return A new PeakList object
    *  @throws ms::PreconditionViolation peak list not sorted correctly
    */
    PeakList subset(const std::pair<double, double>& mzRange) const;

    /** Get a subset of peaklist by mass
    *  @param mass The mass
    *  @param massError The mass error tolerance
    *  @return A new PeakList object
    *  @throws ms::PreconditionViolation peak list not sorted correctly
    */
    PeakList subset(const double mass, const double massError) const;

    /** add the abundance of an element @e to value @a val
    */
    struct PlusAbundance : std::binary_function<double, PeakList::value_type, double> {
        double operator()(const double& val, const PeakList::value_type& e) {
            return val + e.getAbundance();
        }
    };

    /** Find peaks closest to a given sample peak
    *   @param peak A ms::Peak object as the reference
    *   @param mzError The mz error tolerance
    *   @return A new PeakList object
    *   @throws ms::PreconditionViolation peak list not sorted correctly
    *
    * In addition to mz error tolerance, this function also considers
    * the equality of charge.
    */
    PeakList find(const Peak& peak, const double mzError) const;

    /** Find peaks closest to a list of sample peaks
    *   @param peak A ms::PeakList object as the reference samples
    *   @param mzError The mz error tolerance
    *   @return A new PeakList object
    *   @throws ms::PreconditionViolation peak list not sorted correctly
    *
    * An extension to the find(const Peak& peak, const double mzError)
    * function.
    */
    PeakList find(PeakList& peakList, const double mzError) const;

private:
    PeakList(PeakListPrivate::Ref& r);

    friend class ConstPeakList;
};

template<>
struct PeakList::LessThanChargeMz<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type& rhs) const {
        if (lhs.getCharge() < rhs.getCharge())
            return true;
        
        return lhs.getMz() < rhs.getMz();
    }
};
template<>
struct PeakList::LessThanChargeMz<PeakList::value_type, const PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type& rhs) const {
        return (lhs.getCharge() - 1)*100000 + lhs.getMz() < (rhs.getCharge() - 1)*100000 + rhs.getMz();
    }
};

template<>
struct PeakList::LessThanCharge<PeakList::value_type, int> : std::binary_function<PeakList::value_type, int, bool> {
    bool operator()(const PeakList::value_type& lhs, const int& rhs) const {
        return lhs.getCharge() < rhs;
    }
};
template<>
struct PeakList::LessThanCharge<int, PeakList::value_type> : std::binary_function<int, PeakList::value_type, bool> {
    bool operator()(const int& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getCharge();
    }
};

template<>
struct PeakList::LessThanRt<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type& rhs) const {
        return lhs.getRt() < rhs.getRt();
    }
};
template<>
struct PeakList::LessThanRt<PeakList::value_type, double> : std::binary_function<PeakList::value_type, double, bool> {
    bool operator()(const PeakList::value_type& lhs, const double& rhs) const {
        return lhs.getRt() < rhs;
    }
};
template<>
struct PeakList::LessThanRt<double, PeakList::value_type> : std::binary_function<double, PeakList::value_type, bool> {
    bool operator()(const double& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getRt();
    }
};

template<>
struct PeakList::LessThanMz<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type& rhs) const {
        return lhs.getMz() < rhs.getMz();
    }
};
template<>
struct PeakList::LessThanMz<PeakList::value_type, double> : std::binary_function<PeakList::value_type, double, bool> {
    bool operator()(const PeakList::value_type& lhs, const double& rhs) const {
        return lhs.getMz() < rhs;
    }
};
template<>
struct PeakList::LessThanMz<double, PeakList::value_type> : std::binary_function<double, PeakList::value_type, bool> {
    bool operator()(const double& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getMz();
    }
};

template<>
struct PeakList::LessThanMass<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type rhs) const {
        return lhs.getCharge()*lhs.getMz() < rhs.getCharge()*rhs.getMz();
    }
};
template<>
struct PeakList::LessThanMass<PeakList::value_type, double> : std::binary_function<PeakList::value_type, double, bool> {
    bool operator()(const PeakList::value_type& lhs, const double rhs) const {
        return lhs.getCharge()*lhs.getMz() < rhs;
    }
};
template<>
struct PeakList::LessThanMass<double, PeakList::value_type> : std::binary_function<double, PeakList::value_type, bool> {
    bool operator()(const double& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getCharge()*rhs.getMz();
    }
};

template<>
struct PeakList::LessThanAbundance<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type rhs) const {
        return lhs.getAbundance() < rhs.getAbundance();
    }
};
template<>
struct PeakList::LessThanAbundance<PeakList::value_type, double> : std::binary_function<PeakList::value_type, double, bool> {
    bool operator()(const PeakList::value_type& lhs, const double rhs) const {
        return lhs.getAbundance() < rhs;
    }
};
template<>
struct PeakList::LessThanAbundance<double, PeakList::value_type> : std::binary_function<double, PeakList::value_type, bool> {
    bool operator()(const double& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getAbundance();
    }
};

template<>
struct PeakList::LessThanRegionId<PeakList::value_type, PeakList::value_type> : std::binary_function<PeakList::value_type, PeakList::value_type, bool> {
    bool operator()(const PeakList::value_type& lhs, const PeakList::value_type& rhs) const {
        return lhs.getRegionId() < rhs.getRegionId();
    }
};
template<>
struct PeakList::LessThanRegionId<PeakList::value_type, int> : std::binary_function<PeakList::value_type, int, bool> {
    bool operator()(const PeakList::value_type& lhs, const int rhs) const {
        return lhs.getRegionId() < rhs;
    }
};
template<>
struct PeakList::LessThanRegionId<int, PeakList::value_type> : std::binary_function<int, PeakList::value_type, bool> {
    bool operator()(const int& lhs, const PeakList::value_type& rhs) const {
        return lhs < rhs.getRegionId();
    }
};

} //namespace ms

MSPP_EXPORT std::ostream& operator<<(std::ostream& os, ms::PeakList& s);

#endif /*__PEAKLIST_H__*/
