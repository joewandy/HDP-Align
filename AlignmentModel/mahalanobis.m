function dist = mahalanobis(peak1, peak2, dmz, drt)
    if (abs(peak1.mass - peak2.mass) > dmz)
        dist = 0;
    else
	    rt = peak1.rt - peak2.rt;
	    mz = peak1.mass - peak2.mass;
        dist = sqrt((rt*rt)/(drt*drt) + (mz*mz)/(dmz*dmz));
    end
end
