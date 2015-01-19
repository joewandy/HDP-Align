/*$Id: Peak.h 2718 2009-11-26 17:26:49Z anna.kreshuk@iwr.uni-heidelberg.de $*/

/*
 * Peak.h
 *
 * Copyright (c) 2008 Marc Kirchner <marc.kirchner@iwr.uni-heidelberg.de>
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

#ifndef __PEAK_H__
#define __PEAK_H__
#include <ms++/config.h>

#include <functional>
#include <iostream>
#include <iomanip>

namespace ms
{

/**
 * A peak in a spectrum, as characterized by charge, abundance and \f$mz_0\f$
 */
class MSPP_EXPORT Peak
{
public:
    /** compares two ms::Peak by their \f$mz_0\f$ value
     */
    struct LessThanMz {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const Peak& lhs, const Peak& rhs) const {
            return lhs.mz0_ < rhs.mz0_;
        }
        bool operator()(const Peak& lhs, const double rhs) const {
            return lhs.mz0_ < rhs;
        }
        bool operator()(const double lhs, const Peak& rhs) const {
            return lhs < rhs.mz0_;
        }
    };

    /** compares two ms::Peak by their \f$mz_0\f$ value
     */
    struct LessThanMzWithIndex {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const std::pair<Peak, int>& lhs, const std::pair<Peak, int>& rhs) const {
            return lhs.first.mz0_ < rhs.first.mz0_;
        }
        bool operator()(const std::pair<Peak, int>& lhs, const double rhs) const {
            return lhs.first.mz0_ < rhs;
        }
        bool operator()(const double lhs, const std::pair<Peak, int>& rhs) const {
            return lhs < rhs.first.mz0_;
        }
    };

    /** compares two ms::Peak by their \f$mz_at_max\f$ value
     */
    struct LessThanMzAtMaxWithIndex {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const std::pair<Peak, int>& lhs, const std::pair<Peak, int>& rhs) const {
            return lhs.first.mz_at_max_ < rhs.first.mz_at_max_;
        }
        bool operator()(const std::pair<Peak, int>& lhs, const double rhs) const {
            return lhs.first.mz_at_max_ < rhs;
        }
        bool operator()(const double lhs, const std::pair<Peak, int>& rhs) const {
            return lhs < rhs.first.mz_at_max_;
        }
    };

    /** compares two ms::Peak by their charge value
     */
    struct LessThanCharge {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const Peak& lhs, const Peak& rhs) const {
            return lhs.charge_ < rhs.charge_;
        }
        bool operator()(const Peak& lhs, const int rhs) const {
            return lhs.charge_ < rhs;
        }
        bool operator()(const double lhs, const Peak& rhs) const {
            return lhs < rhs.charge_;
        }
    };

    /** compares two ms::Peak by their retention time
     */
    struct LessThanRt {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const Peak& lhs, const Peak& rhs) const {
            return lhs.rt_ < rhs.rt_;
        }
        bool operator()(const Peak& lhs, const int rhs) const {
            return lhs.rt_ < rhs;
        }
        bool operator()(const double lhs, const Peak& rhs) const {
            return lhs < rhs.rt_;
        }
    };

    /** compares two ms::Peak by their abundance value
     */
    struct LessThanAbundance {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const Peak& lhs, const Peak& rhs) const {
            return lhs.abundance_ < rhs.abundance_;
        }
        bool operator()(const Peak& lhs, const double rhs) const {
            return lhs.abundance_ < rhs;
        }
        bool operator()(const double lhs, const Peak& rhs) const {
            return lhs < rhs.abundance_;
        }
    };

    /** compares two ms::Peak by their abundance value
     */
    struct LessThanAbundanceWithIndex {
        // multiple operator()s make the functor unadaptable. Oh well.
        bool operator()(const std::pair<Peak, int>& lhs, const std::pair<Peak, int>& rhs) const {
            return lhs.first.abundance_ < rhs.first.abundance_;
        }
        bool operator()(const std::pair<Peak, int>& lhs, const double rhs) const {
            return lhs.first.abundance_ < rhs;
        }
        bool operator()(const double lhs, const std::pair<Peak, int>& rhs) const {
            return lhs < rhs.first.abundance_;
        }
    };

    /** test for a ms::Peak \f$mz_0\f$ value to be in a certain range
     */
    struct MzInRange
                : std::unary_function<bool, double> {
        MzInRange(const double b, const double e) : begin(b), end(e) {}
        bool operator()(const Peak& p) {
            return begin <= p.getMz() && p.getMz() <= end;
        }
private:
        double begin;
        double end;
    };
    Peak() : mz0_(0.0), mz_at_max_(0), charge_(0), abundance_(0.0), rt_(0.0), annotationId_(-1), regionId_(-1) {}
    Peak(const double mz0, const int charge, const double abundance, int annotationId) :
    mz0_(mz0), mz_at_max_(0), charge_(charge), abundance_(abundance), rt_(0.0), annotationId_(annotationId), regionId_(-1) {}

    Peak(const double mz0, const double mz_at_max, const int charge, const double abundance, int annotationId) :
    mz0_(mz0), mz_at_max_(mz_at_max), charge_(charge), abundance_(abundance), rt_(0.0), annotationId_(annotationId), regionId_(-1) {}

    Peak(const double mz0, const int charge, const double abundance, double rt = -1) :
    mz0_(mz0), mz_at_max_(0), charge_(charge), abundance_(abundance), rt_(rt), annotationId_(-1), regionId_(-1) {}


    bool operator==(const Peak& p) const {
        return mz0_ == p.mz0_ && charge_ == p.charge_ && abundance_ == p.abundance_ && rt_ == p.rt_ && annotationId_ == p.annotationId_;
    }
    ~Peak(void) {}

    /** Get the mz value
    *   @return The mz value
    */
    double getMz(void) const {
        return mz0_;
    }

    double getMzAtMax() const {
        return mz_at_max_;
    }

    /** Get the charge value
    *   @return The charge value
    */
    int getCharge(void) const {
        return charge_;
    }

    /** Get the abundance value
    *   @return The abundance value
    */
    double getAbundance(void) const {
        return abundance_;
    }

    /** Get the retention time value
    *   @return The retention time value
    */
    double getRt(void) const {
        return rt_;
    }

    /** Get the annotation id
    *   @return The annotation id
    */
    int getAnnotationId() const {
        return annotationId_;
    }

    /** Get the region id
    *   @return The region id
    */
    int getRegionId() const {
        return regionId_;
    }

    /** Set the mz value
    *   @param mz0 The monoisotopic mz value
    */
    void setMz(const double mz0) {
        mz0_ = mz0;
    }

    /** Set the charge value
    *   @param charge The charge value
    */
    void setCharge(const int charge) {
        charge_ = charge;
    }

    /** Set the abundance value
    *   @param abundance The abundance value
    */
    void setAbundance(const double abundance) {
        abundance_ = abundance;
    }

    /** Set the retention time value
    *   @param rt The retention time value
    */
    void setRt(const double rt) {
        rt_ = rt;
    }

    /** Set the annotation id
    *   @param annotationId The annotation id to set
    */
    void setAnnotationId(const int annotationId) {
        annotationId_ = annotationId;
    }

    /** Set the retion id
    *   @param regionId The retion id to set
    */
    void setRegionId(const int regionId) {
        regionId_ = regionId;
    }

protected:
    double mz0_;        //mz position of the monoisotopic peak
    double mz_at_max_;  //mz position of the max. peak, explained by the model
    int charge_;
    double abundance_;
    double rt_;
    int annotationId_;  // annotation id indicates the correspondence between peak and the stoichiometry
    int regionId_;      // region id indicates the segment region this peak belongs to
};

inline std::ostream& operator<<(std::ostream& o, const Peak& p)
{
    o << "mz= " << p.getMz() << "\t"
    << "rt= " << p.getRt() << "\t"
    << "chg= " << p.getCharge() << "\t"
    << "abd= " << p.getAbundance() << "\t"
    << "annId= " << p.getAnnotationId() << "\t"
    << "rgnId= " << p.getRegionId();
    return o;
}

} /*namespace ms*/

#endif /*__PEAK_H__*/
