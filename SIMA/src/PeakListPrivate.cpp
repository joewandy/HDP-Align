/*$Id: PeakListPrivate.cpp 2029 2009-06-29 14:48:06Z tkroeger $*/

/*
 * PeakListPrivate.cpp
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

#include <ms++/private/PeakListPrivate.h>

#include <ms++/Error.h>
#include <ms++/Log.h>

#ifdef DATABASE
#include <ms++/private/ConstPeakListRegistry.h>
#endif

using namespace ms;

//////////////////////////////////////////////////////////////////////////////
// PeakListPrivate::Ref //////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

//
//Constructors
//

//Default Constructor
PeakListPrivate::Ref::Ref()
        : ptr(0)
{
}

//Construct as a reference to already existing data
PeakListPrivate::Ref::Ref(PeakListPrivate* p) : ptr(p)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::Ref(): ptr should not be zero");
    ++ptr->refCount_;
}

//Construct as referencing the same data as already existing reference ref
PeakListPrivate::Ref::Ref(const Ref& ref) : ptr(ref.ptr)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::Ref(): ptr should not be zero");
    ++ptr->refCount_;
}

//Assignment operator
PeakListPrivate::Ref& PeakListPrivate::Ref::operator= (const PeakListPrivate::Ref & ref)
{
    if (ptr == 0) {
        //We reference no data.
        //Just count us as another p.ptr user
        ptr = ref.ptr;
        ++ptr->refCount_;
        return *this;
    }
    if (&ref == this) {
        //test for self-assignment
        return *this;
    }

    //Make us use p's data instead,
    //delete our old data if necessary
    PeakListPrivate* const old = ptr;
    ptr = ref.ptr;
    ++ptr->refCount_;
    if (--old->refCount_ == 0) {
        delete old;
    }
    return *this;
}

//Get a reference to a unique copy of the data referenced by this object
PeakListPrivate::Ref PeakListPrivate::Ref::getRefToUniqueCopy()
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::Ref(): ptr should not be zero.");

    PeakListPrivate* p = new PeakListPrivate;
    *p = *ptr;
    p->refCount_ = 0;
#ifdef DATBASE
    p->id_ = -1; //TODO check this
#endif
    return Ref( p );
}

//
//Destructor
//

PeakListPrivate::Ref::~Ref()
{
    assert(ptr != 0);
    if (--ptr->refCount_ == 0) {
        delete ptr;
    }
}

std::vector<Peak>* PeakListPrivate::Ref::peaks() const
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::peaks(): ptr should not be zero.");
#ifdef DATABASE
    ptr->ensureListIsLoaded();
#endif /*DATABASE*/
    return &(ptr->peaks_);
}

#ifdef DATABASE
//
//General database specific functions
//

void PeakListPrivate::Ref::load(int id)
{
    return ptr->load(id);
}
void PeakListPrivate::Ref::store()
{
    ptr->store();
}
void PeakListPrivate::Ref::modify()
{
    //Mark this peak list as about to be modified.
    //If we share it's data with others, make a copy so we're
    //the single owner again.

    if (ptr->refCount_ > 1) {
        //Make a copy, so we have a copy of the data for our very own
        *this = getRefToUniqueCopy();
    }
    //The modified copy is not stored yet, give it an invalid id
    ptr->id_ = -1;
}
int PeakListPrivate::Ref::id() const
{
    return ptr->id();
}

//
//Getter for meta data
//

std::string PeakListPrivate::Ref::name()  const
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::name(): ptr should not be zero.");
    return ptr->name_;
}
std::string PeakListPrivate::Ref::owner() const
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::owner(): ptr should not be zero.");
    return ptr->owner_;
}
std::string PeakListPrivate::Ref::desc() const
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::desc(): ptr should not be zero.");
    return ptr->description_;
}
std::string PeakListPrivate::Ref::meta() const
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::meta(): ptr should not be zero.");
    return ptr->meta_;
}

//
// setter for meta data
//

void PeakListPrivate::Ref::setName(const std::string& name)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::setName(): ptr should not be zero.");
    ptr->name_ = name;
    ptr->dirty_ = true;
}
void PeakListPrivate::Ref::setOwner(const std::string& owner)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::setOwner(): ptr should not be zero.");
    ptr->owner_ = owner;
    ptr->dirty_ = true;
}
void PeakListPrivate::Ref::setDescription(const std::string& desc)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::setDescription(): ptr should not be zero.");
    ptr->description_ = desc;
    ptr->dirty_ = true;
}
void PeakListPrivate::Ref::setMeta(const std::string& meta)
{
    mspp_invariant(ptr != 0, "PeakListPrivate::Ref::setMeta(): ptr should not be zero.");
    ptr->meta_ = meta;
    ptr->dirty_ = true;
}
#endif /*DATABASE*/

//////////////////////////////////////////////////////////////////////////////
// PeakListPrivate      //////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////

//
// Constructor
//

//Default constructor (public)
PeakListPrivate::Ref PeakListPrivate::create()
{
    return Ref( new PeakListPrivate );
}

//Default constructor (protected)
PeakListPrivate::PeakListPrivate()
#ifdef DATABASE
        :
        PersistentObject(), loaded_(false), dirty_(false), refCount_(0)
#else
        :
        refCount_(0)
#endif /*DATABASE*/
{
}

//
// Destructor
//

PeakListPrivate::~PeakListPrivate()
{
#ifdef DATABASE
    ConstPeakListRegistry::instance().unregisterList(id());
#endif
}

//
// Loading and saving
//

#ifdef DATABASE
void PeakListPrivate::load(int id)
{
    otl_stream i(1, "SELECT name, owner, description, meta FROM peaklists WHERE ID=:<int>", DB);
    i << id;
    i >> name_ >> owner_ >> description_ >> meta_;
    id_ = id;
    loaded_ = false;
    dirty_ = false;

    mspp_invariant(peaks_.size() == 0, "PeakListPrivate::load(): peaks_.size() has to be zero.");
}
void PeakListPrivate::store()
{
    if ( id_ == -1 ) {
        otl_stream insert(1, "INSERT INTO peaklists (name, owner, description, meta) VALUES (:name<char[512]>, :owner<char[256]>, :description<char[4096]>, :meta<char[8192]>)", DB);
        insert << name_ << owner_ << description_ << meta_;
        id_ = DBConnection::lastInsertID();

        otl_stream o(1,
                "INSERT INTO peaks (peaklistID, mz, charge, abundance) VALUES(:peaklistID<int>, :mz<double>, :charge<int>, :abundance<double>)", DB
                    );

        for (Peaks::iterator it = peaks_.begin(); it != peaks_.end(); ++it) {
            o << id_ << it->getMz() << it->getCharge() << it->getAbundance();
        }
        loaded_ = true; //we have stored our current state, so we must be loaded
    }
    else if (dirty_) {
        otl_stream u(1, "UPDATE peaklists SET name=:name<char[512]>, owner=:owner<char[256]>, description=:description<char[4096]>, meta=:meta<char[8192]> WHERE ID=:ID<int>", DB);
        u << name_ << owner_ << description_ << meta_ << id_;
    }
    dirty_ = false;
}
void PeakListPrivate::ensureListIsLoaded() const
{
    if (id_ == -1) {
        return;
    }

    if (!loaded_) {
        mspp_invariant(peaks_.size() == 0, "PeakListPrivate::store(): peaks_.size() has to be zero.");
        MSPP_LOG(logINFO) << "lazy loading in " << this;
        otl_stream i(1, "SELECT mz, charge, abundance FROM peaks WHERE peaklistID=:peaklistID<int>", DB);
        i << id_;

        double mz0, abundance = 0;
        int charge = 0;
        while (!i.eof()) {
            i >> mz0 >> charge >> abundance;
            peaks_.push_back( Peak(mz0, charge, abundance) );
        }
        loaded_ = true;
    }
}
#endif /*DATABASE*/
