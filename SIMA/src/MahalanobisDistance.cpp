/*
 * MahalanobisDistance.cpp
 *
 * Copyright (c) 2009 Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>
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

#include "ms++/MahalanobisDistance.h"

using namespace ms;


double MahalanobisDistance::operator()(const Peak& A, const Peak& B, double drt, double dmz, double dz=0., double dint=0.)
{
    if(drt <= 0 || dmz <= 0){
        MSPP_LOG(logERROR) << "MahalanobisDistance::operator(): drt and dmz have to be positive." << std::endl;
    }
    mspp_precondition(drt > 0. && dmz > 0. , "MahalanobisDistance::operator(): drt and dmz have to be positive.");

    if(A.getAbundance() < dint || B.getAbundance() < dint){
        // low abundance peak --> high distance
        return DBL_MAX;
    }

    if(dz == 0.){
        // only calculate distance if peaks have same charge
        if(A.getCharge() != B.getCharge()){
            // charge differs and dz == 0: max. distance
            return DBL_MAX;
        }else{
            // charge equal: normal distance
            double rt = (A.getRt()-B.getRt());
            double mz = (A.getMz()-B.getMz());

            return sqrt(rt*rt/(drt*drt) + mz*mz/(dmz*dmz));
        }
    }else{
        // return normal distance
        double rt = (A.getRt()-B.getRt());
        double mz = (A.getMz()-B.getMz());
        double z = A.getCharge() - B.getCharge();

        return sqrt(rt*rt/(drt*drt) + mz*mz/(dmz*dmz) + z*z/(dz*dz));
    }

}
