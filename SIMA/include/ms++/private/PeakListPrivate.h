/*$Id: PeakListPrivate.h 2029 2009-06-29 14:48:06Z tkroeger $*/

/*
 * PeakListPrivate.h
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

#ifndef __PEAKLISTPRIVATE_H__
#define __PEAKLISTPRIVATE_H__

#include <ms++/config.h>

#include <cassert>
#include <vector>

#include "ms++/Peak.h"

#ifdef DATABASE
#include "ms++/PersistentObject.h"
#include "ms++/DBConnection.h"
#define DB *DBConnection::instance().db()
#endif

namespace ms
{

typedef std::vector<Peak> Peaks;

#ifdef DATABASE
class ConstPeakList;

/**
 * peak list data
 *
 * PeakListPrivate holds all the data of ms::PeakListBase and implements
 * safe Reference, ms::PeakListPrivate::Ref to this data.
 *
 * The idea is that both ms::PeakList and ms::ConstPeakList just hold a
 * reference to the actual data. This way, if you load multiple experiments
 * that all reference the same peak list from the database, the correspoding
 * ms::ConstPeakList objects will work on the same PeakListPrivate data.
 * This saves space and makes editing the meta data in the above case possible.
 * (Additionaly, ms::ConstPeakListRegistry keeps track of the PeakListPrivate
 * instances in use by ms::ConstPeakList)
 */
class MSPP_EXPORT PeakListPrivate : public PersistentObject
{
#else
class MSPP_EXPORT PeakListPrivate
{
#endif
public:
    class MSPP_EXPORT Ref
    {
    public:
#ifdef DATABASE
        friend std::ostream& operator<<(std::ostream& o, ConstPeakList& c);
        friend class ConstPeakListRegistry;
        friend class DatabaseTest;
#endif
        friend std::ostream& operator<<(std::ostream& o, Ref& r);

        ///Default constructor.
        Ref();
        ///Copy constructor. \a ref will reference the same data as this object.
        Ref(const Ref& ref);
        ///Destructor. If the last reference to a PeakListPrivate instance is destroyed,
        ///that data is deleted.
        ~Ref();
        ///Assignment operator. Reference the same data as \a ref does.
        Ref& operator= (const Ref& ref);

        std::vector<Peak>* peaks() const;

#ifdef DATABASE
        void store();
        void load(int id);
        void modify();
        bool dirty() {
            return ptr->dirty_;
        }

        std::string name()  const;
        std::string owner() const;
        std::string desc()  const;
        std::string meta()  const;
        void setName(const std::string& name);
        void setOwner(const std::string& owner);
        void setDescription(const std::string& desc);
        void setMeta(const std::string& meta);

        int id() const;
#endif /*DATABASE*/

    protected:
        ///Constructor. Reference the data pointed to by \a p as well.
        Ref(PeakListPrivate* p);

        ///Get a reference to a unique copy of the data referenced by this object
        Ref getRefToUniqueCopy();

        ///Pointer to the data this objects references
        PeakListPrivate* ptr;

        friend class PeakListPrivate;
        friend class PeakListBase;
        friend class PeakList;
#ifdef DATABASE
        friend class ConstPeakList;
#endif
    };

    ///Default constructor (public)
    static Ref create();

protected:
    PeakListPrivate();
    ~PeakListPrivate();
    void store();
    void load(int id);

    mutable Peaks peaks_;

#ifdef DATABASE
    ///whether the list of peaks is completely loaded from the database or not
    mutable bool loaded_;
    ///whether there are changes that have not yet been saved to the database
    mutable bool dirty_;

    std::string name_;
    std::string owner_;
    std::string description_;
    std::string meta_;

    ///make sure that the list of peaks is loaded completely from the database.
    ///This must be called before any modifications of the peakList_ is made.
    ///If we only want to get at the metadata though, there is no need to load all that
    ///data...
    void ensureListIsLoaded() const;
#endif /*DATABASE*/

    ///number of references to this object
    int refCount_;

    friend class Ref;
};

inline std::ostream& operator<<(std::ostream& o, PeakListPrivate::Ref& r) {
    o << "[PeakListPrivate=" << r.ptr << "]";
    return o;
}

} /*namespace ms*/

#endif /*__PEAKLISTPRIVATE_H__*/
