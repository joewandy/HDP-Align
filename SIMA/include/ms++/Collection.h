/*$Id: Collection.h 2253 2009-08-19 11:43:25Z bkausler $*/

/*
 * Collection.h
 *
 * Copyright (c) 2009 Marc Kirchner <marc.kirchner@iwr.uni-heidelberg.de>
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

#ifndef __COLLECTION_H__
#define __COLLECTION_H__

#include <vector>

// for friend declaration further down
struct CollectionTestSuite;

namespace ms
{

/**
 * @brief Wrapper around std::vector
 *
 * std::vector does not have a virtual destructor, so it is not possible
 * to derive from it. As a lot of classes need to implement a vector-like
 * interface, the Collection class can be used to derive from.
 */
template < class T, class A = std::allocator<T> >
class Collection
{
public:
    friend struct ::CollectionTestSuite;
    
    // typedefs
    typedef T value_type;
    typedef A allocator_type;

    typedef typename A::size_type size_type;
    typedef typename A::difference_type difference_type;

    typedef typename std::vector<T, A>::iterator iterator;
    typedef typename std::vector<T, A>::const_iterator const_iterator;
    typedef typename std::vector<T, A>::reverse_iterator reverse_iterator;
    typedef typename std::vector<T, A>::const_reverse_iterator const_reverse_iterator;
    typedef typename std::vector<T, A>::pointer pointer;
    typedef typename std::vector<T, A>::const_pointer const_pointer;
    typedef typename std::vector<T, A>::reference reference;
    typedef typename std::vector<T, A>::const_reference const_reference;

    // constructors
    explicit Collection() {}
    Collection(const Collection& rhs) : c_(rhs.c_) {}
    explicit Collection(const std::vector<T, A>& rhs) : c_(rhs) {}
    explicit Collection(size_type n, const T& value = T()) : c_(n, value) {}
    template <class In> Collection(In begin, In end) : c_(begin, end) {}

    // virtual destructor
    virtual ~Collection() {}

    // operators
    virtual Collection& operator=(const Collection& rhs) {
        c_ = rhs.c_;
        return *this;
    }
    
    template<typename U, typename V>
    friend bool operator==(const Collection<U, V>& lhs, const Collection<U, V>& rhs);

    template <class U, class V> 
    friend bool operator<(const Collection<U, V>& lhs, const Collection<U, V>& rhs);

    template <class U, class V>
    friend void swap(Collection<U, V>& lhs, Collection<U, V>& rhs);

    // assignment
    template <class In> void assign(In begin, In end) {
        c_.assign(begin, end);
    }
    virtual void assign(size_type n, const T& value) {
        c_.assign(n, value);
    }

    // stack operations
    virtual void push_back(const T& value) {
        c_.push_back(value);
    }
    virtual void pop_back(void) {
        c_.pop_back();
    }

    // list operations
    virtual iterator insert(iterator pos, const T& value) {
        return c_.insert(pos, value);
    }
    virtual void insert(iterator pos, size_type n, const T& value) {
        return c_.insert(pos, n, value);
    }
    template <class In> void insert(iterator pos, In begin, In end) {
        c_.insert(pos, begin, end);
    }
    virtual iterator erase(iterator pos) {
        return c_.erase(pos);
    }
    virtual iterator erase(iterator begin, iterator end) {
        return c_.erase(begin, end);
    }
    virtual void clear() {
        c_.clear();
    }

    // iterators
    virtual iterator begin() {
        return c_.begin();
    }
    virtual reverse_iterator rbegin() {
        return c_.rbegin();
    }
    virtual iterator end() {
        return c_.end();
    }
    virtual reverse_iterator rend() {
        return c_.rend();
    }
    virtual const_iterator begin() const {
        return c_.begin();
    }
    virtual const_iterator end() const {
        return c_.end();
    }

    // element access
    virtual value_type& operator[](size_type pos) {
        return c_[pos];
    }
    virtual value_type& at(size_type pos) {
        return c_.at(pos);
    }
    virtual const value_type& operator[](size_type pos) const {
        return c_[pos];
    }
    virtual const value_type& at(size_type pos) const {
        return c_.at(pos);
    }

    // size and capacity
    virtual size_type size(void) const {
        return c_.size();
    }
    virtual size_type max_size() const {
        return c_.max_size();
    }
    virtual bool empty() const {
        return c_.empty();
    }
    virtual void resize(size_type sz, const T& value = T()) {
        c_.resize(sz, value);
    }
    virtual size_type capacity() const {
        return c_.capacity();
    }
    virtual void reserve(size_type n) {
        c_.reserve(n);    // throws length_error if n > max_size()
    }

    // other
    virtual void swap(Collection& rhs) {
        c_.swap(rhs.c_);
    }
    virtual allocator_type get_allocator() const {
        return c_.get_allocator();
    }

protected:
    std::vector<T, A> c_;
};

// helper functions
template <class T, class A> bool operator==(const Collection<T, A>& lhs, const Collection<T, A>& rhs)
{
    return lhs.c_ == rhs.c_;
}
template <class T, class A> bool operator<(const Collection<T, A>& lhs, const Collection<T, A>& rhs)
{
    return lhs.c_ < rhs.c_;
}
template <class T, class A> void swap(Collection<T, A>& lhs, Collection<T, A>& rhs)
{
    lhs.c_.swap(rhs.c_);
}

}

#endif
