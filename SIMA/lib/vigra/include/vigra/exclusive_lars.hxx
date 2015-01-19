/************************************************************************/
/*                                                                      */
/*                  Copyright 2007 by Ullrich Koethe                    */
/*                                                                      */
/*    This file is part of the VIGRA computer vision library.           */
/*    The VIGRA Website is                                              */
/*        http://kogs-www.informatik.uni-hamburg.de/~koethe/vigra/      */
/*    Please direct questions, bug reports, and contributions to        */
/*        koethe@informatik.uni-hamburg.de          or                  */
/*        vigra@kogs1.informatik.uni-hamburg.de                         */
/*                                                                      */
/*    Permission is hereby granted, free of charge, to any person       */
/*    obtaining a copy of this software and associated documentation    */
/*    files (the "Software"), to deal in the Software without           */
/*    restriction, including without limitation the rights to use,      */
/*    copy, modify, merge, publish, distribute, sublicense, and/or      */
/*    sell copies of the Software, and to permit persons to whom the    */
/*    Software is furnished to do so, subject to the following          */
/*    conditions:                                                       */
/*                                                                      */
/*    The above copyright notice and this permission notice shall be    */
/*    included in all copies or substantial portions of the             */
/*    Software.                                                         */
/*                                                                      */
/*    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND    */
/*    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES   */
/*    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND          */
/*    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT       */
/*    HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,      */
/*    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING      */
/*    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR     */
/*    OTHER DEALINGS IN THE SOFTWARE.                                   */
/*                                                                      */
/************************************************************************/


#ifndef VIGRA_EXCLUSIVE_REGRESSION_HXX
#define VIGRA_EXCLUSIVE_REGRESSION_HXX

#ifdef _MSC_VER
#include <float.h>
#define isnan _isnan
#define isinf !_finite
#endif

#include "matrix.hxx"
#include "linear_solve.hxx"
#include "singular_value_decomposition.hxx"
#include "numerictraits.hxx"
#include "functorexpression.hxx"

#include "regression.hxx"

#include <cassert>
#include <cstdlib>

namespace vigra
{

namespace linalg
{

namespace detail {

template<class T, class C1, class C2, class C3, class C4, class C5, class Array>
typename NormTraits<T>::SquaredNormType
writeLARSSolution(bool least_squares_solutions, bool enforce_positive, int activeSetSize, 
                  MultiArrayView<2, T, C1> const & lars_solution,
                  MultiArrayView<2, T, C2> const & lsq_solution,
                  MultiArrayView<2, T, C3> const & lars_prediction,
                  MultiArrayView<2, T, C4> const & lsq_prediction,
                  MultiArrayView<2, T, C5> const & b,
                  Array & solutions)
{
    using namespace vigra::functor;

    std::cerr << "writeLARSSolution activeSetSize=" << activeSetSize << std::endl;

    if(least_squares_solutions)
    {
        if(enforce_positive)
        {
            // find dimensions whose weight changes sign below the LSQ solution
            ArrayVector<T> d(activeSetSize, NumericTraits<T>::max());
            for(MultiArrayIndex k=0; k<activeSetSize; ++k)
            {
                if(lsq_solution(k,0) < 0.0) {
                    std::cout << "lsq_solution( " << k << ",0) < 0.0 : " << lsq_solution(k,0) << std::endl;
                    std::cout << "lars_solution(" << k << ",0)       : " << lars_solution(k,0) << std::endl;
                    d[k] = lars_solution(k,0) / (lars_solution(k,0) - lsq_solution(k,0));
                }
            }
            typename ArrayVector<T>::iterator changesSign = argMinIf(d.begin(), d.end(), Arg1() < Param(1.0));
            if(changesSign != d.end())
            {
                T g = *changesSign;
                solutions.push_back(g*lsq_solution + (1.0 - g)*lars_solution);
                assert(!isnan(g));
                assert(!isinf(g));
                for(int i=0; i<rowCount(lsq_solution); ++i) {
                    assert(!isnan(lsq_solution(i,0)));
                    assert(!isinf(lsq_solution(i,0)));
                    assert(!isnan(lars_solution(i,0)));
                    assert(!isinf(lsq_solution(i,0)));
                }
                for(int i=0; i<rowCount(lsq_solution); ++i) {
                    assert(!isnan((g*lsq_solution)(i,0)));
                    assert(!isinf((g*lsq_solution)(i,0)));
                }
                for(int i=0; i<rowCount(lsq_solution); ++i) {
                    assert(!isnan(((1.0 - g)*lars_solution)(i,0)));
                    assert(!isinf(((1.0 - g)*lars_solution)(i,0)));
                }
                for(int i=0; i<rowCount(lsq_solution); ++i) {
                    assert(!isnan((g*lsq_solution + (1.0 - g)*lars_solution)(i,0)));
                    assert(!isinf((g*lsq_solution + (1.0 - g)*lars_solution)(i,0)));
                }
                std::cerr << "About to return squaredNorm(b - g*lsq_prediction - (1.0 - g)*lars_prediction)" << std::endl;
                for(int i=0; i<rowCount(solutions[solutions.size()-1]); ++i) {
                  assert(!isnan(solutions[solutions.size()-1](i,0)));
                }
                return squaredNorm(b - g*lsq_prediction - (1.0 - g)*lars_prediction);
            }
            else
            {
                solutions.push_back(lsq_solution);
                std::cerr << "About to return squaredNorm(b - lsq_prediction) 1" << std::endl;
                for(int i=0; i<solutions[solutions.size()-1].size(); ++i) {
                  assert(!isnan(solutions[solutions.size()-1](i,0)));
                }
                return squaredNorm(b - lsq_prediction);
            }
        }
        else
        {
            solutions.push_back(lsq_solution);
            std::cerr << "About to returnsquaredNorm(b - lsq_prediction) 2" << std::endl;
            for(int i=0; i<solutions[solutions.size()-1].size(); ++i) {
                  assert(!isnan(solutions[solutions.size()-1](i,0)));
                }
            return squaredNorm(b - lsq_prediction);
        }
    }
    else
    {
        solutions.push_back(lars_solution);
        std::cerr << "About to return squaredNorm(b - lars_prediction) 3" << std::endl;
        for(int i=0; i<solutions[solutions.size()-1].size(); ++i) {
                  assert(!isnan(solutions[solutions.size()-1](i,0)));
                }
        return squaredNorm(b - lars_prediction);
    }
}

} // namespace detail

template <class T, class C1, class C2, class Array1, class Array2>
unsigned int
exclusiveLeastAngleRegression(MultiArrayView<2, T, C1> const & A, MultiArrayView<2, T, C2> const &b, 
                     Array1 & solutions, Array2 & activeSets,
                     LeastAngleRegressionOptions const & options = LeastAngleRegressionOptions())
{
    using namespace vigra::functor;
    using namespace vigra::linalg;
    
    typedef typename MultiArrayShape<2>::type Shape;
    typedef typename Matrix<T>::view_type Subarray;

    typedef typename Array2::value_type Permutation;
    typedef typename Permutation::view_type ColumnSet;
    
    if(options.enforce_positive && !options.lasso_modification)
        vigra_precondition(false,
              "leastAngleRegression(): Positive solutions can only be enforced whan LASSO modification is active.");

    const MultiArrayIndex rows = rowCount(A);
    const MultiArrayIndex cols = columnCount(A);
    const MultiArrayIndex maxRank = std::min(rows, cols);
    const unsigned int ucols = (unsigned int)cols;

#ifdef EFA_DEBUG
    std::cerr << "Got matrix of size " << rows << "x" << cols << std::endl;
#endif

    vigra_precondition(rowCount(b) == rows && columnCount(b) == 1,
       "leastAngleRegression(): Shape mismatch between matrices A and b.");
       
    MultiArrayIndex maxSolutionCount = options.max_solution_count;
    if(maxSolutionCount == 0)
        maxSolutionCount = options.lasso_modification
                                ? 10*maxRank
                                : maxRank;
    
    bool use_bic = (options.bic_variance > 0.0);
    
    Matrix<T> R(A), qtb(b);

    // the first activeSetSize entries will hold the active set indices,
    // the other entries are the inactive set, all permuted in the same way as the
    // columns of the matrix R
    Permutation columnPermutation(ucols);
    for(int k=0; k<cols; ++k)
        columnPermutation[k] = k;
        
    // find dimension with largest correlation
    Matrix<T> c = transpose(A)*b;
    MultiArrayIndex initialColumn;
    if(options.enforce_positive)
        initialColumn = argMaxIf(c, Arg1() > Param(0.0));
    else
        initialColumn = argMax(abs(c));
    if(initialColumn == -1)
        return 0; // no solution found

    #ifdef EFA_DEBUG
    std::cerr << std::endl;
    std::cerr << "Adding " << columnPermutation[initialColumn] << " to active set" <<std::endl;
    std::cerr << "swapping cols " << columnPermutation[0] << ", " << columnPermutation[initialColumn] <<std::endl;
    std::cerr << "swapping cols " << columnPermutation[ucols-1]<< ", "<<columnPermutation[ucols-1-initialColumn] <<std::endl;
    std::cerr << "Adding " << columnPermutation[ucols-1-initialColumn] << " to forbidden set" <<std::endl;
    std::cerr << std::endl;
    #endif

    // prepare initial active set and search direction etc.
    MultiArrayIndex activeSetSize = 1;
    //add initialColumn to the active set
    std::swap(columnPermutation[0], columnPermutation[initialColumn]);
    columnVector(R, 0).swapData(columnVector(R, initialColumn));
    //add ucols-1-initialColumn to the forbidden set
    std::swap(columnPermutation[ucols-1], columnPermutation[ucols-1-initialColumn]);
    columnVector(R, ucols-1).swapData(columnVector(R, ucols-1-initialColumn));

    detail::qrColumnHouseholderStep(0, R, qtb);

    Matrix<T> lsq_solution(cols, 1), lars_solution(cols,1), lsq_prediction(rows,1), lars_prediction(rows,1); // initially zero
    
    Matrix<T> next_lsq_solution(cols, 1);
    next_lsq_solution(0,0) = qtb(0,0) / R(0,0);
    Matrix<T> searchVector = 
         next_lsq_solution(0,0) * columnVector(A, (MultiArrayIndex)columnPermutation[0]);
    
    double minimal_bic = NumericTraits<double>::max();
    double minimal_bic_0 = b.squaredNorm()/options.bic_variance;

    int minimal_bic_solution = -1;
    
    Permutation columnsToBeRemoved;
    MultiArrayIndex currentSolutionCount = 0;
    while(currentSolutionCount < maxSolutionCount)
    {
        #ifdef EFA_DEBUG
        std::cerr << std::endl << "* Iteration: " << currentSolutionCount
                  << " (" << currentSolutionCount << "<" << maxSolutionCount << ")" << std::endl;
        #endif

        ColumnSet activeSet = columnPermutation.subarray(0, (unsigned int)activeSetSize);
        ColumnSet inactiveSet = columnPermutation.subarray((unsigned int)activeSetSize, ucols-(unsigned int)activeSetSize);
        
        // find next dimension to be activated (in the inactive set)
        Matrix<T> ac(cols - 2*activeSetSize, 1, NumericTraits<T>::min());
        Matrix<T> lars_residual = b - lars_prediction;

        T C = dot(columnVector(A, activeSet[0]), lars_residual);
        if(!options.enforce_positive)
            C = abs(C);

        for(MultiArrayIndex k = 0; k<cols-2*activeSetSize; ++k) //go through the inactive set
        {
            // perform permutation on A explicitly, so that we need not store a permuted copy of A
            T c  = dot(columnVector(A, inactiveSet[k]), lars_residual),
              a  = dot(columnVector(A, inactiveSet[k]), searchVector),
              a1 = (C - c) / (C - a),
              a2 = (C + c) / (C + a);

            ac(k, 0) = options.enforce_positive
                          ? a1
                          : std::min(a1, a2);
        }

        MultiArrayIndex columnToBeAdded = argMinIf(ac, Arg1() < Param(1.0) && Arg1() > Param(0.0));
        if(columnToBeAdded == -1)
        {
            #ifdef EFA_DEBUG
            std::cerr << "no feasible solutions in active set => output the last LSQ solution" << std::endl;
            #endif
            ++currentSolutionCount;
            if(minimal_bic_solution == -1)
                minimal_bic_solution = currentSolutionCount;
            activeSets.push_back(Permutation(columnPermutation.subarray(0, (unsigned int)activeSetSize)));
            Subarray current_lars_solution  = lars_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
            current_lars_solution(activeSetSize-1, 0) = 0.0;
            Subarray current_lsq_solution  = next_lsq_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
            detail::writeLARSSolution(true, options.enforce_positive,
                           activeSetSize, 
                           current_lars_solution, current_lsq_solution, lars_prediction, lsq_prediction, b,
                           solutions);
            break;
        }
        
        T gamma = ac(columnToBeAdded, 0);
        
        // adjust columnToBeAdded: we skipped the active set
        columnToBeAdded += activeSetSize; 
        
        // check whether we have to remove columns from the active set
        bool needToRemoveColumns = false;
        if(options.lasso_modification)
        {
            // find dimensions whose weight changes sign below gamma*searchDirection
            Matrix<T> d(Shape(activeSetSize, 1), NumericTraits<T>::max());
            for(MultiArrayIndex k=0; k<activeSetSize; ++k)
            {
                if(lars_solution(k,0) != 0.0 && sign(lars_solution(k,0))*sign(next_lsq_solution(k,0)) == -1.0)
                    d(k,0) = lars_solution(k,0) / (lars_solution(k,0) - next_lsq_solution(k,0));
            }
            int changesSign = argMinIf(d, Arg1() < Param(gamma));
            if(changesSign >= 0)
            {
                needToRemoveColumns = true;
                gamma = d(changesSign, 0);
            }
        }

        // compute the current solution
        Subarray current_lsq_solution;
        Subarray current_lars_solution;
        if(needToRemoveColumns)
        {
            T tolerance = NumericTraits<T>::epsilon();  // FIXME: adjust tolerance to problem?
            lars_solution = gamma * next_lsq_solution + (1.0 - gamma) * lars_solution;
            
            columnsToBeRemoved.clear();
            for(MultiArrayIndex k=0; k<activeSetSize; ++k)
                if((options.enforce_positive && lars_solution(k,0) <= tolerance) ||
                   abs(lars_solution(k,0)) <= tolerance)
                    columnsToBeRemoved.push_back(k);
            
            for(int k=columnsToBeRemoved.size()-1; k>=0; --k)
            {
                --activeSetSize;
                if(columnsToBeRemoved[k] != activeSetSize)
                {
                    #ifdef EFA_DEBUG
                    std::cerr << std::endl;
                    std::cerr << "Remove " << columnPermutation[columnsToBeRemoved[k]] << " from active set" <<std::endl;
                    std::cerr << "swapping cols upper triangular " << columnPermutation[columnsToBeRemoved[k]]
                              << ", " << columnPermutation[activeSetSize] << std::endl;
                    std::cerr << "swapping cols " << columnPermutation[ucols-1-columnsToBeRemoved[k]]
                              << ", " << columnPermutation[ucols-1-activeSetSize] << std::endl;
                    #endif
                    // remove column 'columnsToBeRemoved[k]' and restore triangular from of R
                    // note: columnPermutation is automatically swapped here
                    detail::upperTriangularSwapColumns(columnsToBeRemoved[k], activeSetSize, R, qtb, columnPermutation);

                    std::swap(columnPermutation[ucols-1-columnsToBeRemoved[k]],
                              columnPermutation[ucols-1-activeSetSize]);
                    columnVector(R, columnPermutation[ucols-1-columnsToBeRemoved[k]])
                        .swapData(columnVector(R, columnPermutation[ucols-1-activeSetSize]));

                    // remove entries from the solution
                    lars_solution(columnsToBeRemoved[k], 0) = lars_solution(activeSetSize,0);
                    lsq_solution(columnsToBeRemoved[k], 0) = lsq_solution(activeSetSize,0);
                    lars_solution(ucols-1-columnsToBeRemoved[k], 0) = lars_solution(ucols-1-activeSetSize,0);
                    lsq_solution(ucols-1-columnsToBeRemoved[k], 0) = lsq_solution(ucols-1-activeSetSize,0);
                }
                else {
                    #ifdef EFA_DEBUG
                    std::cerr << "Removing rightmost column from active set (now #" << activeSetSize << ")" << std::endl;
                    #endif
                }
                lars_solution(activeSetSize,0) = 0.0;
                lsq_solution(activeSetSize,0) = 0.0;
                lars_solution(ucols-1-activeSetSize,0) = 0.0;
                lsq_solution(ucols-1-activeSetSize,0) = 0.0;

            }
            
            // remember the active subvector of the solutions
            current_lars_solution = lars_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
            current_lsq_solution = lsq_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));

            // compute the LSQ solution of the reduced active set
            Subarray Ractive = R.subarray(Shape(0,0), Shape(activeSetSize, activeSetSize));
            Subarray qtbactive = qtb.subarray(Shape(0,0), Shape(activeSetSize, 1));
            linearSolveUpperTriangular(Ractive, qtbactive, current_lsq_solution);

            // compute the predictions of the reduced active set
            lsq_prediction.init(NumericTraits<T>::zero()); 
            lars_prediction.init(NumericTraits<T>::zero()); 
            for(MultiArrayIndex k=0; k<activeSetSize; ++k)
            {
               lsq_prediction += current_lsq_solution(k,0)*columnVector(A, columnPermutation[k]);
               lars_prediction += current_lars_solution(k,0)*columnVector(A, columnPermutation[k]);
            }
        }
        else
        {
            lars_solution = gamma * next_lsq_solution + (1.0 - gamma) * lars_solution;
            current_lars_solution = lars_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
            current_lsq_solution = next_lsq_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));

            lsq_prediction = lars_prediction + searchVector;
            lars_prediction += gamma*searchVector;
        }
            
        ++currentSolutionCount;
        activeSets.push_back(Permutation(columnPermutation.subarray(0, (unsigned int)activeSetSize)));
        
        double residual = detail::writeLARSSolution(options.least_squares_solutions, options.enforce_positive,
                           activeSetSize, 
                           current_lars_solution, current_lsq_solution, lars_prediction, lsq_prediction, b,
                           solutions);

        // determine BIC and possibly stop iteration
        if(use_bic)
        {
            double bic = residual / options.bic_variance + activeSetSize*std::log((double)rows);
            if(bic < minimal_bic)
            {
                minimal_bic = bic;
                minimal_bic_solution = currentSolutionCount;
            }
            if(currentSolutionCount - minimal_bic_solution >= options.bic_delay)
            {
                if(minimal_bic > minimal_bic_0)
                    return 0;
                else
                    return (unsigned int)minimal_bic_solution;
            }
        }

        if(needToRemoveColumns)
        {
            searchVector = lsq_prediction - lars_prediction;
        }
        else
        {
            // add column 'columnToBeAdded'
            if(activeSetSize != columnToBeAdded)
            {
                if(columnToBeAdded < cols-activeSetSize) {
                    #ifdef EFA_DEBUG
                    std::cerr << std::endl;
                    std::cerr << "ADD " << columnPermutation[columnToBeAdded] << " from INACTIVE to active set (#"
                              <<activeSetSize<<"->#"<<activeSetSize+1<<")" <<std::endl;
                    std::cerr << "swapping cols " << columnPermutation[activeSetSize] << ", "
                              << columnPermutation[columnToBeAdded] <<std::endl;
                    std::cerr << "swapping cols " << columnPermutation[ucols-1-activeSetSize] << ", "
                              << columnPermutation[ucols-1-columnToBeAdded] <<std::endl;
                    #endif

                    std::swap(columnPermutation[activeSetSize], columnPermutation[columnToBeAdded]);
                    columnVector(R, activeSetSize).swapData(columnVector(R, columnToBeAdded));
                    std::swap(columnPermutation[ucols-1-activeSetSize], columnPermutation[ucols-1-columnToBeAdded]);
                    columnVector(R, ucols-1-activeSetSize)
                        .swapData(columnVector(R, ucols-1-columnToBeAdded));

                    bool singular = !detail::qrColumnHouseholderStep(activeSetSize, R, qtb);
                    if(singular || closeAtTolerance(qtb(activeSetSize,0) / R(activeSetSize, activeSetSize), 0.0)) {// FIXME: use tolerance???
                        #ifdef EFA_DEBUG
                        std::cerr << "break" << std::endl;
                        #endif
                        break; // no further solutions possible
                    }
                    ++activeSetSize;
                }
                else {
                    #ifdef EFA_DEBUG
                    std::cerr << std::endl;
                    std::cerr << "ADD " << columnPermutation[columnToBeAdded] << " from FORBIDDEN to active set (#"
                              <<activeSetSize<<"->#"<<activeSetSize<<")" <<std::endl;
                    std::cerr << "swapping cols upper triangular " << columnPermutation[columnToBeAdded] << ", "
                              << columnPermutation[ucols-1-columnToBeAdded] <<std::endl;
                    #endif
                    detail::upperTriangularSwapColumns(columnToBeAdded,
                        ucols-1-columnToBeAdded, R, qtb, columnPermutation);
                    std::cerr << "added column from forbidden set, removed corresponding active set column" << std::endl;
                }
            }
            else {
                #ifdef EFA_DEBUG
                std::cerr << "Adding a column right next to the active set" << std::endl;
                #endif
                // zero the corresponding entry of the solutions
                lsq_solution(activeSetSize,0) = 0.0;
                lars_solution(activeSetSize,0) = 0.0;
                lsq_solution(ucols-1-activeSetSize,0) = 0.0;
                lars_solution(ucols-1-activeSetSize,0) = 0.0;

                // reduce R (i.e. its newly added column) to triangular form
                bool singular = !detail::qrColumnHouseholderStep(activeSetSize, R, qtb);
                if(singular || closeAtTolerance(qtb(activeSetSize,0) / R(activeSetSize, activeSetSize), 0.0)) {// FIXME: use tolerance???
                    #ifdef EFA_DEBUG
                    std::cerr << "break" << std::endl;
                    #endif
                    break; // no further solutions possible
                }
                ++activeSetSize;
            }
 
            // compute LSQ solution of new active set
            Subarray Ractive = R.subarray(Shape(0,0), Shape(activeSetSize, activeSetSize));
            Subarray qtbactive = qtb.subarray(Shape(0,0), Shape(activeSetSize, 1));
            Subarray next_lsq_solution_k = next_lsq_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
            linearSolveUpperTriangular(Ractive, qtbactive, next_lsq_solution_k);
            
#ifdef DEBUG
            if(options.enforce_positive && next_lsq_solution_k(activeSetSize-1,0) < 0.0)
                std::cerr << "Error: leastAngleRegression(): newly added column has negative coefficient in plasso mode.\n";
#endif

            if(activeSetSize == maxRank)
            {
                // no feasible solutions in active set => output the last LSQ solution
                ++currentSolutionCount;
                activeSets.push_back(Permutation(columnPermutation.subarray(0, (unsigned int)activeSetSize)));
                Subarray current_lars_solution  = lars_solution.subarray(Shape(0,0), Shape(activeSetSize, 1));
                current_lars_solution(activeSetSize-1, 0) = 0.0;
                Subarray current_lsq_solution  = next_lsq_solution_k.subarray(Shape(0,0), Shape(activeSetSize, 1));
                detail::writeLARSSolution(true, options.enforce_positive,
                               activeSetSize, 
                               current_lars_solution, current_lsq_solution, lars_prediction, lsq_prediction, b,
                               solutions);
                #ifdef EFA_DEBUG
                std::cerr << "break because of activeSetSize == maxRank" << std::endl;
                #endif
                break;
            }

            // compute new search direction
            searchVector = -lars_prediction;
            for(MultiArrayIndex k=0; k<activeSetSize; ++k)
                searchVector += next_lsq_solution_k(k,0)*columnVector(A, columnPermutation[k]);
        }
    }
    
    if(use_bic)
    {
        if(minimal_bic > minimal_bic_0)
            return 0;
        else
            return (unsigned int)minimal_bic_solution;
    }
    else
    {
        return (unsigned int)currentSolutionCount;
    }
}

} // namespace linalg

using linalg::exclusiveLeastAngleRegression;
using linalg::LeastAngleRegressionOptions;

} // namespace vigra

#endif // VIGRA_REGRESSION_HXX
