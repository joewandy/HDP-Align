#ifndef VIGRA_SEEDEDREGIONGROWING_EXTENSIONS_HXX
#define VIGRA_SEEDEDREGIONGROWING_EXTENSIONS_HXX

#include <vector>
#include <stack>
#include <queue>
#include "utilities.hxx"
#include "stdimage.hxx"
#include "stdimagefunctions.hxx"
#include "seededregiongrowing.hxx"

namespace vigra {

template <class SrcImageIterator, class SrcAccessor,
          class SeedImageIterator, class SeedAccessor,
          class DestImageIterator, class DestAccessor,
          class RegionStatisticsArray, class CostThresholdType>
void seededRegionGrowing(SrcImageIterator srcul,
                         SrcImageIterator srclr, SrcAccessor as,
                         SeedImageIterator seedsul, SeedAccessor aseeds,
                         DestImageIterator destul, DestAccessor ad,
                         RegionStatisticsArray & stats,
                         const SRGType srgType, CostThresholdType max_cost)
{
    int w = srclr.x - srcul.x;
    int h = srclr.y - srcul.y;
    int count = 0;

    SrcImageIterator isy = srcul, isx = srcul;  // iterators for the src image

    typedef typename RegionStatisticsArray::value_type RegionStatistics;
    typedef typename RegionStatistics::cost_type CostType;
    typedef detail::SeedRgPixel<CostType> Pixel;

    typename Pixel::Allocator allocator;

    typedef std::priority_queue< Pixel*, std::vector<Pixel*>, typename Pixel::Compare>  SeedRgPixelHeap;

    // copy seed image in an image with border
    IImage regions(w+2, h+2);
    IImage::Iterator ir = regions.upperLeft() + Diff2D(1,1);
    IImage::Iterator iry, irx;
    initImageBorder(destImageRange(regions), 1, SRGWatershedLabel);
    copyImage(seedsul, seedsul+Diff2D(w,h), aseeds, ir, regions.accessor());

    SeedRgPixelHeap pheap;
    int cneighbor;
    static const Diff2D dist[] = { Diff2D(-1,0), Diff2D(0,-1), Diff2D(1,0),  Diff2D(0,1) };
    Point2D pos(0,0);
    for(isy=srcul, iry=ir, pos.y=0; pos.y<h; ++pos.y, ++isy.y, ++iry.y) {
        for(isx=isy, irx=iry, pos.x=0; pos.x<w; ++pos.x, ++isx.x, ++irx.x) {
            if(*irx == 0) {
                // find candidate pixels for growing and fill heap
                for(int i=0; i<4; i++) {
                    cneighbor = irx[dist[i]];
                    if(cneighbor > 0) {
                        CostType cost = stats[cneighbor].cost(as(isx));
                        Pixel * pixel = allocator.create(pos, pos+dist[i], cost, count++, cneighbor);
                        pheap.push(pixel);
                    }
                }
            }
        }
    }

    // perform region growing
    while(!pheap.empty()) {
        // get pixel from heap
        Pixel * pixel = pheap.top();
        pheap.pop();
        // sebo
        if(max_cost > NumericTraits<CostThresholdType>::zero() && pixel->cost_ > max_cost) {
            // has been popped, hence deallocate
            allocator.dismiss(pixel);
            continue;
        }
        Point2D pos = pixel->location_;
        Point2D nearest = pixel->nearest_;
        int lab = pixel->label_;
        // done with the pixel. Deallocate.
        allocator.dismiss(pixel);

        irx = ir + pos;
        isx = srcul + pos;
        // already labelled region / watershed?
        if(*irx) continue;
        // otherwise go on
        if(srgType == KeepContours) {
            for(int i=0; i<4; i++) {
                cneighbor = irx[dist[i]];
                if((cneighbor>0) && (cneighbor != lab)) {
                    lab = SRGWatershedLabel;
                    break;
                }
            }
        }
        *irx = lab;
        if((srgType != KeepContours) || (lab > 0)) {
            // update statistics
            stats[*irx](as(isx));
            // search neighborhood
            // second pass: find new candidate pixels
            for(int i=0; i<4; i++) {
                if(irx[dist[i]] == 0) {
                    CostType cost = stats[lab].cost(as(isx, dist[i]));
                    Pixel * new_pixel = allocator.create(pos+dist[i], nearest, cost, count++, lab);
                    pheap.push(new_pixel);
                }
            }
        }
    }
    // make sure the heap is empty
    if (!pheap.empty()) {
        while(!pheap.empty()) {
            delete pheap.top();
            pheap.pop();
        }
        throw vigra::PostconditionViolation("seededregiongrowing.hxx: The heap has not been cleared! Booboo!");
    }
    // write result
    transformImage(ir, ir+Point2D(w,h), regions.accessor(), destul, ad, detail::UnlabelWatersheds());
}


/********************************************************/
/*                                                      */
/*               next block added by sebo             */
/*                                                      */
/********************************************************/

template <class SrcImageIterator, class SrcAccessor,
          class SeedImageIterator, class SeedAccessor,
          class DestImageIterator, class DestAccessor,
          class RegionStatisticsArray, class CostThresholdType>
inline void
seededRegionGrowing(SrcImageIterator srcul,
                    SrcImageIterator srclr, SrcAccessor as,
                    SeedImageIterator seedsul, SeedAccessor aseeds,
                    DestImageIterator destul, DestAccessor ad,
                    RegionStatisticsArray & stats, CostThresholdType max_cost)
{
    seededRegionGrowing(srcul, srclr, as,
                        seedsul, aseeds,
                        destul, ad,
                        stats, CompleteGrow, max_cost);
}

template <class SrcImageIterator, class SrcAccessor,
          class SeedImageIterator, class SeedAccessor,
          class DestImageIterator, class DestAccessor,
          class RegionStatisticsArray, class CostThresholdType>
inline void
seededRegionGrowing(triple<SrcImageIterator, SrcImageIterator, SrcAccessor> img1,
                    pair<SeedImageIterator, SeedAccessor> img3,
                    pair<DestImageIterator, DestAccessor> img4,
                    RegionStatisticsArray & stats,
                    SRGType srgType, CostThresholdType max_cost)
{
    seededRegionGrowing(img1.first, img1.second, img1.third,
                        img3.first, img3.second,
                        img4.first, img4.second,
                        stats, srgType, max_cost);
}

template <class SrcImageIterator, class SrcAccessor,
          class SeedImageIterator, class SeedAccessor,
          class DestImageIterator, class DestAccessor,
          class RegionStatisticsArray, class CostThresholdType>
inline void
seededRegionGrowing(triple<SrcImageIterator, SrcImageIterator, SrcAccessor> img1,
                    pair<SeedImageIterator, SeedAccessor> img3,
                    pair<DestImageIterator, DestAccessor> img4,
                    RegionStatisticsArray & stats, CostThresholdType max_cost)
{
    seededRegionGrowing(img1.first, img1.second, img1.third,
                        img3.first, img3.second,
                        img4.first, img4.second,
                        stats, CompleteGrow, max_cost);
}

//***********************************
//* sebo			   *
//**********************************


template <class Value>
class SeedRgOneOverValueFunctor
{
  public:
        /** the functor's argument type
        */
    typedef Value argument_type;

        /** the functor's result type (unused, only necessary for
            use of SeedRgDirectValueFunctor in \ref vigra::ArrayOfRegionStatistics
        */
    typedef Value result_type;

        /** \deprecated use argument_type
        */
    typedef Value value_type;

        /** the return type of the cost() function
        */
    typedef Value cost_type;

        /** Do nothing (since we need not update region statistics).
        */
    void operator()(argument_type const & v) 
	{
	}
        /** Return argument (since cost is identical to feature value)
        */
    cost_type const & cost(argument_type const & v) 
    {
		currentCost=1/v; // /max;
        return currentCost;
    }
  private:

	Value currentCost;
};

template <class Value>
class SeedRgFractionOfMaxFunctor
{
  public:
        /** the functor's argument type
        */
    typedef Value argument_type;

        /** the functor's result type (unused, only necessary for
            use of SeedRgDirectValueFunctor in \ref vigra::ArrayOfRegionStatistics
        */
    typedef Value result_type;

        /** \deprecated use argument_type
        */
    typedef Value value_type;

        /** the return type of the cost() function
        */
    typedef Value cost_type;

        /** Do nothing (since we need not update region statistics).
        */
    void operator()(argument_type const & v) 
	{
		if (called==false)
		{
			max=v;
			called=true;
		}
	}

        /** Return argument (since cost is identical to feature value)
        */
    cost_type const & cost(argument_type const & v) 
    {
		currentCost=1-v/max;
        return currentCost;
    }
  private:
	bool called;
	Value max;
	Value currentCost;
};

} // namespace vigra

#endif // VIGRA_SEEDEDREGIONGROWING_EXTENSIONS_HXX

