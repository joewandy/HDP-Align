#ifndef VIGRA_RECURSIVECONVOLUTION_EXTENSIONS_HXX
#define VIGRA_RECURSIVECONVOLUTION_EXTENSIONS_HXX

#include <cmath>
#include <vector>
#include "utilities.hxx"
#include "numerictraits.hxx"
#include "imageiteratoradapter.hxx"
#include "bordertreatment.hxx"
#include "array_vector.hxx"
#include "recursiveconvolution.hxx"

namespace vigra {

//doxygen_overloaded_function(template <...> void recursiveVlietFilter)
            
/********************************************************/
/*                                                      */
/*            recursiveVlietFilterLine (sebo)           */
/*                                                      */
/********************************************************/

template <class SrcIterator, class SrcAccessor,
          class DestIterator, class DestAccessor>
void recursiveVlietFilterLine(SrcIterator is, SrcIterator isend, SrcAccessor as,
                         DestIterator id, DestAccessor ad, double sigma)
{


    // coefficoents taken out Luigi Rosa's Implemtentation for Matlab
	double q = 1.31564 * (sqrt(1 + 0.490811 * sigma*sigma) - 1);
	double qq = q*q;
	double qqq = qq*q;
	double b0 = 1.0/(1.57825 + 2.44413*q + 1.4281*qq + 0.422205*qqq);
	double b1 = (2.44413*q + 2.85619*qq + 1.26661*qqq)*b0;
	double b2 = (-1.4281*qq - 1.26661*qqq)*b0;
	double b3 = 0.422205*qqq*b0;
	double B = 1.0 - (b1 + b2 + b3);

    int w = isend - is;
 
    int x;
    
    typedef typename NumericTraits<typename SrcAccessor::value_type>::RealPromote TempType;
    typedef NumericTraits<typename DestAccessor::value_type> DestTraits;
    
    // speichert den Ergebnis der linkseitigen Filterung.
    std::vector<TempType> vyforward(w+1);
    typename std::vector<TempType>::iterator yforward = vyforward.begin();
    std::vector<TempType> vybackward(w+1);
    typename std::vector<TempType>::iterator ybackward = vybackward.begin();

    //from left to right - causal - forward
    yforward[0] = B*as(is);

    ++is;    
    yforward[1] = B*as(is) + (b1*yforward[0]);

    ++is;
    yforward[2] = B*as(is) + (b1*yforward[1]+b2*yforward[0]);

    for(x=3; x < w; ++x)
    {
	++is;
	yforward[x] = B*as(is) + (b1*yforward[x-1]+b2*yforward[x-2]+b3*yforward[x-3]);
    }

    
    //from right to left - anticausal - backward
    
    ybackward[w-1] = B*yforward[w-1];
    
    ybackward[w-2] = B*yforward[w-2]+(b1*ybackward[w-1]);
    
    ybackward[w-3] = B*yforward[w-3]+(b1*ybackward[w-2]+b2*ybackward[w-1]);
  
    for(x=w-4; x>=0; --x)
    {
       ybackward[x] = B*yforward[x]+(b1*ybackward[x+1]+b2*ybackward[x+2]+b3*ybackward[x+3]);
    }

    // output
    for(x=0; x < w; ++x, ++id)
    {
	ad.set(ybackward[x], id);
	//ad.set(yforward[x], id);
    }
}

template <class SrcIterator, class SrcAccessor,
          class DestIterator, class DestAccessor>
void recursiveVlietFilterLine(SrcIterator is, SrcIterator isend, SrcAccessor as,
                         DestIterator id, DestAccessor ad,  ArrayVector<double> const &b1,  ArrayVector<double> const &b2,  ArrayVector<double> const &b3, ArrayVector<double> const &B)
{

    int w = isend - is;
    //SrcIterator istart = is; //FIXME: this is not used...
    
    int x;
    
    typedef typename
        NumericTraits<typename SrcAccessor::value_type>::RealPromote TempType;
    typedef NumericTraits<typename DestAccessor::value_type> DestTraits;
    
    // speichert den Ergebnis der linkseitigen Filterung.
    std::vector<TempType> vyforward(w+1);
    typename std::vector<TempType>::iterator yforward = vyforward.begin();
    std::vector<TempType> vybackward(w+1);
    typename std::vector<TempType>::iterator ybackward = vybackward.begin();

    //from left to right - causal - forward
    yforward[0] = B[0]*as(is);

    ++is;    
    yforward[1] = B[1]*as(is) + (b1[1]*yforward[0]);

    ++is;
    yforward[2] = B[2]*as(is) + (b1[2]*yforward[1]+b2[2]*yforward[0]);

    
    for(x=3; x < w; ++x)
    {
	++is;
	yforward[x] = B[x]*as(is) + (b1[x]*yforward[x-1]+b2[x]*yforward[x-2]+b3[x]*yforward[x-3]);
    }

    x= w-1;
    //from right to left - anticausal - backward
    ybackward[w-1] = B[x]*yforward[w-1];

    
    ybackward[w-2] = B[x]*yforward[w-2]+(b1[x]*ybackward[w-1]);

    
    ybackward[w-3] = B[x]*yforward[w-3]+(b1[x]*ybackward[w-2]+b2[x]*ybackward[w-1]);


    for(x=w-4; x>=0; --x)
    {
       ybackward[x] = B[x]*yforward[x]+(b1[x]*ybackward[x+1]+b2[x]*ybackward[x+2]+b3[x]*ybackward[x+3]);
    }

    // output
    for(x=0; x < w; ++x, ++id)
    {
	ad.set(ybackward[x], id);
    }



}

/********************************************************/
/*                                                      */
/*            recursiveVlietFilterX     (sebo)          */
/*                                                      */
/********************************************************/

//doxygen_overloaded_function(template <...> void recursiveVlietFilterX)
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
void recursiveVlietFilterX(SrcImageIterator supperleft, 
                       SrcImageIterator slowerright, SrcAccessor as,
                       DestImageIterator dupperleft, DestAccessor ad, 
                       double sigma)
{
    int w = slowerright.x - supperleft.x;
    int h = slowerright.y - supperleft.y;
    
    int y;
    
    for(y=0; y<h; ++y, ++supperleft.y, ++dupperleft.y)
    {
        typename SrcImageIterator::row_iterator rs = supperleft.rowIterator();
        typename DestImageIterator::row_iterator rd = dupperleft.rowIterator();

        recursiveVlietFilterLine(rs, rs+w, as, 
                             rd, ad, 
                             sigma);
    }
  
}
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
inline void recursiveVlietFilterX(
            triple<SrcImageIterator, SrcImageIterator, SrcAccessor> src,
            pair<DestImageIterator, DestAccessor> dest, 
                       double sigma)
{
    recursiveVlietFilterX(src.first, src.second, src.third,
                      dest.first, dest.second, sigma);
}
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
void recursiveVlietFilterX(SrcImageIterator supperleft, 
                       SrcImageIterator slowerright, SrcAccessor as,
                       DestImageIterator dupperleft, DestAccessor ad, 
                       double *sigma)
{
	int w = slowerright.x - supperleft.x;
    	int x;

       	double q;
	double qq;
	double qqq;
	double b0;
	ArrayVector<double> b1(w);
	ArrayVector<double> b2(w);
	ArrayVector<double> b3(w);
	ArrayVector<double> B(w);

    	for(x=0; x<w; ++x)
    	{
       		//coefficoents taken out Luigi Rosa's Implemtentation for Matlab
		q= 1.31564 * (sqrt(1 + 0.490811 * (sigma[x])* (sigma[x])) - 1);
		qq = q*q;
		qqq = qq*q;
		b0 = 1.0/(1.57825 + 2.44413*q + 1.4281*qq + 0.422205*qqq);
		b1[x] = (2.44413*q + 2.85619*qq + 1.26661*qqq)*b0;
		b2[x] = (-1.4281*qq - 1.26661*qqq)*b0;
		b3[x] = (0.422205*qqq) * (b0);
		B[x] = 1.0 - (b1[x] + b2[x] + b3[x]);
    	}
	 

    	int h = slowerright.y - supperleft.y;
        int y;
        for(y=0; y<h; ++y, ++supperleft.y, ++dupperleft.y)
    	{	
        	typename SrcImageIterator::row_iterator rs = supperleft.rowIterator();
        	typename DestImageIterator::row_iterator rd = dupperleft.rowIterator();
        	//recursiveVlietFilterLine(rs, rs+w, as, rd, ad, *sigma);
	        recursiveVlietFilterLine(rs, rs+w, as, rd, ad, b1, b2, b3, B );
    	}
}
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
inline void recursiveVlietFilterX(
            triple<SrcImageIterator, SrcImageIterator, SrcAccessor> src,
            pair<DestImageIterator, DestAccessor> dest, 
                       double *sigma)
{
    recursiveVlietFilterX(src.first, src.second, src.third,
                      dest.first, dest.second, sigma);
}

/********************************************************/
/*                                                      */
/*            recursiveVlietFilterY     (sebo)          */
/*                                                      */
/********************************************************/
doxygen_overloaded_function(template <...> void recursiveVlietFilterY)
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
void recursiveVlietFilterY(SrcImageIterator supperleft, 
                       SrcImageIterator slowerright, SrcAccessor as,
                       DestImageIterator dupperleft, DestAccessor ad, 
                       double sigma)
{
    int w = slowerright.x - supperleft.x;
    int h = slowerright.y - supperleft.y;
    
    int x;
    
    for(x=0; x<w; ++x, ++supperleft.x, ++dupperleft.x)
    {
        typename SrcImageIterator::column_iterator cs = supperleft.columnIterator();
        typename DestImageIterator::column_iterator cd = dupperleft.columnIterator();

        recursiveVlietFilterLine(cs, cs+h, as, 
                            cd, ad, 
                            sigma);
    } 
}
template <class SrcImageIterator, class SrcAccessor,
          class DestImageIterator, class DestAccessor>
inline void recursiveVlietFilterY(
            triple<SrcImageIterator, SrcImageIterator, SrcAccessor> src,
            pair<DestImageIterator, DestAccessor> dest, 
                       double sigma)
{
    recursiveVlietFilterY(src.first, src.second, src.third,
                      dest.first, dest.second, sigma);
}



} // namespace vigra

#endif // VIGRA_RECURSIVECONVOLUTION_EXTENSIONS_HXX
