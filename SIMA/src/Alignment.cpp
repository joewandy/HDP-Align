/*$Id$*/

/*
 * Alignment.cpp
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

#include <ms++/Alignment.h>


namespace ms
{
    void Alignment::setRTC(DBL_MATRIX& rtc)
    {
        rtc_ = rtc;
        return;
    }



    DBL_MATRIX Alignment::getMasterTimeScale(int n_points)
    {
        //search for minimum/maximum rt value in rtc_
        double min_rt = DBL_MAX;
        double max_rt = 0;
        for(int i = 0; i < rtc_.rowCount(); i++){
            for(int j = 0; j < rtc_.columnCount(); j++){
                if(rtc_(i,j) < min_rt && rtc_(i,j) >= 0){
                    min_rt = rtc_(i,j);
                }
                if(rtc_(i,j) > max_rt){
                    max_rt = rtc_(i,j);
                }
            }
        }


        //Enlarge MTS by 5% at each end
        double length = max_rt - min_rt;
        min_rt = min_rt - 0.05*length;
        max_rt = max_rt + 0.05*length;

        //dimensionality
        int d = rtc_.columnCount();


        //initialize y with bisectional line from rt_min to rt_max with dimension d
        DBL_MATRIX y = Alignment::initTimeScale_(d,min_rt,max_rt, n_points);

        //initialize linePoints with bisectional line from rt_min to rt_max with dimension d-1
        DBL_MATRIX lp = Alignment::initTimeScale_(d-1,min_rt,max_rt, n_points);


        //assign constants for WOLFE condition 
        // heuristic parameters. Need to be adjusted manually.
        double c1 = 0.25; //0 < c1 < 0.5
        double c2 = 0.75; //0.5 < c2 < 1

        //get estimates for alpha
        DBL_MATRIX alpha = Alignment::estimateAlpha_(y, rtc_);


        //run line distortion with given parameters
        DBL_MATRIX rv = Alignment::lineDistort_(lp, y, rtc_, c1, c2, alpha);

        return rv;
    }



    double Alignment::sigmoid_(double delta, double alpha)
    {
        return 1. / (1. + exp(-alpha*delta) );
    }



    DBL_MATRIX Alignment::sigmoid_(DBL_MATRIX& delta, double alpha)
    {
        if(delta.columnCount() != 1 ){
            MSPP_LOG(logERROR) << "Alignment::sigmoid_(): Wrong dimensionality of input - delta must be a column vector!" << std::endl;
        }
        mspp_precondition(delta.columnCount() == 1 , "Alignment::sigmoid_(): Wrong dimensionality of input - delta must be a column vector!");

        unsigned int size = delta.rowCount();

        //create result vector of correct length
        DBL_MATRIX sig (size,1);

        //fill result vector
        for(unsigned int i = 0; i < size; i++){
            //calculate each entry with 1-dimensional sigmoid_
            sig(i,0) = sigmoid_(delta(i,0), alpha);
        }

        return sig;
    }



    double Alignment::sumSigmoid_(DBL_MATRIX& x0, DBL_MATRIX& rt_vec, double alpha)
    {
        if(x0.columnCount() != 1){
            MSPP_LOG(logERROR) << "Alignment::sumSigmoid_(): Wrong dimensionality of input - x0 must be a column vector!" << std::endl;
        }
        mspp_precondition(x0.columnCount() == 1 , "Alignment::sumSigmoid_(): Wrong dimensionality of input - x0 must be a column vector!");
        
        int rt_size = rt_vec.rowCount();
        
        DBL_MATRIX x0mat (rt_size,x0.rowCount(),0.) ;
        
        //fill each line in x0mat with x0
        for(int k = 0; k < rt_size; k++){
            for(int l = 0; l < x0.rowCount(); l++){
                x0mat(k,l) = x0(l,0);
            }
        }  

        DBL_MATRIX delta = rt_vec - x0mat;

        //return value. increases in loop (sum)
        double value = 0;

        //go through all vectors in rt_vec
        for(int i = 0; i < rt_size; i++){
            double prod = -1;
            double mprod = -1;
            int count = 0; /*WEIGHTS*/
            //build products over the dimensions
            for(int j = 0; j < rt_vec.columnCount(); j++){
                if(rt_vec(i,j) != -1.){
                    count++; /*WEIGHTS*/
                    double dij = delta(i,j); 
                    prod *= sigmoid_(dij,alpha);
                    mprod *= sigmoid_(-dij,alpha);
                }
            }
            value += prod + mprod;
            //value += double(count)/double(rt_vec.columnCount()) * (prod + mprod); /*WEIGHTS*/
        }
        return value;
    }



    DBL_MATRIX Alignment::estimateAlpha_(DBL_MATRIX& points, DBL_MATRIX& rt_vec)
    {
        unsigned int n_points = points.rowCount();
        unsigned int rt_size = rt_vec.rowCount();

        if(n_points < 1){
            MSPP_LOG(logERROR) << "Alignment::estimateAlpha_(): number of points must be greater than 0!" << std::endl;
        }
        mspp_precondition(n_points >= 1 , "Alignment::estimateAlpha_(): number of points must be greater than 0!");
 
        if(points.columnCount() != rt_vec.columnCount()){
            MSPP_LOG(logERROR) << "Alignment::estimateAlpha_(): Wrong dimensionality of input - points and rt_vec must have equal dimensions!" << std::endl;
        }
        mspp_precondition(points.columnCount() == rt_vec.columnCount() , "Alignment::estimateAlpha_(): Wrong dimensionality of input - points and rt_vec must have equal dimensions!");

        //vector containing new alpha values
        DBL_MATRIX returnAlpha (n_points,1,0.);

        for(unsigned int i = 0; i < n_points; i++){
            DBL_MATRIX pointMat = rt_vec;
            pointMat.init(0.);

            //fill each line in pointMat with points(i,*)
            for(unsigned int k = 0; k < rt_size; k++){
                for(int l = 0; l < rt_vec.columnCount(); l++){
                    pointMat(k,l) = points(i,l);
                }
            }

            //calculate distances from rt_vec to points(i,*)
            vigra::linalg::Matrix<double> distances = rt_vec - pointMat;
            std::vector<double> sqrdist (rt_size); //needs to be sorted --> vector
            for(unsigned int k = 0; k < rt_size; k++){
                double sum = 0;
                for(int l = 0; l < rt_vec.columnCount(); l++){
                    sum += pow(distances(k,l) , 2);
                }
                sqrdist[k] = sum;
            }

            //sort sqrdist
            std::sort(sqrdist.begin(),sqrdist.end());

            double mean = 0;
            int range = ((20<rt_size)?20:rt_size);
            for(int k = 0; k < range; k++){
                mean += sqrdist[k];
            }
            mean /= range;

            returnAlpha(i,0) =( 1./sqrt(mean) * 10 + 0.1 );
           
        }

        return returnAlpha;
    }



    DBL_MATRIX Alignment::initTimeScale_(unsigned int dimensions, double min_rt, double max_rt, unsigned int size)
    {
        DBL_MATRIX ts(size,dimensions,0.);
        
        for(unsigned int i = 0; i < size; i++){
            //create vector on the bisectional line
            for(unsigned int k = 0; k < dimensions; k++){
                ts(i,k) = min_rt + (max_rt - min_rt)/(size-1)*i;
            }
        }

        return ts;

    }



    double Alignment::valOnHPlane_(DBL_MATRIX& x0, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double alpha)
    {
        if(x0.columnCount() != 1 || y.columnCount() != 1) {
            MSPP_LOG(logERROR) << "Alignment::valOnHPlane_(): Wrong dimensionality of input - x0 and y must be column vectors!" << std::endl;
        }
        mspp_precondition(x0.columnCount() == 1 && y.columnCount() == 1 , "Alignment::valOnHPlane_(): Wrong dimensionality of input - x0 and y must be column vectors!");
        
        unsigned int dim = rt_vec.columnCount(); 

        if(x0.rowCount() != (int)dim-1 || y.rowCount() != (int)dim) {
            MSPP_LOG(logERROR) << "Alignment::valOnHPlane_(): Dimension mismatch - x0 and y dimensions must match rt_vec!" << std::endl;
        }
        mspp_precondition(x0.rowCount() == (int)dim-1 && y.rowCount() == (int)dim , "Alignment::valOnHPlane_(): Dimension mismatch - x0 and y dimensions must match rt_vec!");
           
        //compute the last component of x0 s.t. newx0 lies on the hyperplane
        double sumy = 0;
        double sumx0 = 0;
        for(int i = 0; i < y.rowCount(); i++){
            sumy += y(i,0);
        }
        for(int i = 0; i < x0.rowCount(); i++){
            sumx0 += x0(i,0);
        }

        DBL_MATRIX newx0 (x0.rowCount()+1,1);
        for(int k = 0; k < x0.rowCount(); k++){
            newx0(k,0) = x0(k,0);
        }

        newx0(x0.rowCount(),0)=(sumy-sumx0);

        return sumSigmoid_(newx0, rt_vec,alpha);
    }



    DBL_MATRIX Alignment::gradOnHPlane_(DBL_MATRIX& x0, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double alpha)
    {
        if(x0.columnCount() != 1 || y.columnCount() != 1 ){
            MSPP_LOG(logERROR) << "Alignment::gradOnHPlane_(): Wrong dimensionality of input - x0 and y must be column vectors!" << std::endl;
        }
        mspp_precondition(x0.columnCount() == 1 && y.columnCount() == 1 , "Alignment::gradOnHPlane_(): Wrong dimensionality of input - x0 and y must be column vectors!");
        
        //get number of dimensions and size
        int dim = rt_vec.columnCount(); 
        int size = rt_vec.rowCount();

        if(x0.rowCount() != dim-1 || y.rowCount() != dim) {
            MSPP_LOG(logERROR) << "Alignment::gradOnHPlane_(): Dimension mismatch - x0 and y dimensions must match rt_vec!" << std::endl;
        }
        mspp_precondition(x0.rowCount() == dim-1 && y.rowCount() == dim , "Alignment::gradOnHPlane_(): Dimension mismatch - x0 and y dimensions must match rt_vec!");
          
        //compute the last component of x0 s.t. newx0 lies on the hyperplane
        double sumy = 0;
        double sumx0 = 0;
        for(int i = 0; i < y.rowCount(); i++){
            sumy += y(i,0);
        }
        for(int i = 0; i < x0.rowCount(); i++){
            sumx0 += x0(i,0);
        }

        DBL_MATRIX newx0 (x0.rowCount()+1,1);
        for(int k = 0; k < x0.rowCount(); k++){
            newx0(k,0) = x0(k,0);
        }

        newx0(x0.rowCount(),0)=(sumy-sumx0);


        //compute distance between newx0 and all points in rt_vec        
        DBL_MATRIX x0mat (size,newx0.rowCount(),0.) ;
        //fill each line in x0mat with newx0
        for(int k = 0; k < size; k++){
            for(int l = 0; l < newx0.size(); l++){
                x0mat(k,l) = newx0(l,0);
            }
        }  

        DBL_MATRIX delta = x0mat - rt_vec;


        //Björn's implementation with missing correspondences
        DBL_MATRIX grad (dim-1,1,0.);

        for(int i = 0; i<dim-1; i++){
            DBL_MATRIX A (size,1,0.);
            DBL_MATRIX B (size,1,0.);

            std::vector<int> counter (size, 0); /*WEIGHTS*/

            for(int j = 0; j<size; j++){
                double d = delta(j,i);
                double de = delta(j,dim-1);

                double sig_d = Alignment::sigmoid_(d,alpha);
                double msig_d = Alignment::sigmoid_(-d,alpha);
                double sig_de = Alignment::sigmoid_(de,alpha);
                double msig_de = Alignment::sigmoid_(-de,alpha);

                double Ag = 1;
                double Adg = 0;
                double Ah = 1;
                double Adh = 0;
                double Bg = 1;
                double Bdg = 0;
                double Bh = 1;
                double Bdh = 0;

                if(rt_vec(j,i) != -1. ){
                    counter[j]++; /*WEIGHTS*/
                    Ag = sig_d;
                    Bg = msig_d;
                    Adg = -alpha*(1.- sig_d)*sig_d;
                    Bdg = alpha*(1.- msig_d)*msig_d;
                }
                if(rt_vec(j,dim-1) != -1. ){
                    counter[j]++; /*WEIGHTS*/
                    Ah = sig_de;
                    Bh = msig_de;
                    Adh = alpha*(1.- sig_de)*sig_de;
                    Bdh = -alpha*msig_de*(1.- msig_de);
                }
                A(j,0) = Adg*Ah + Ag*Adh;
                B(j,0) = Bdg*Bh + Bg*Bdh;
            }
            for(int j = 0; j<dim-1; j++){
                if(i!=j){
                    for(int k = 0; k<size; k++){
                        if(rt_vec(k,j) != -1. ){
                            double d = delta(k,j);
                            A(k,0) *= Alignment::sigmoid_(d,alpha);
                            B(k,0) *= Alignment::sigmoid_(-d,alpha);
                        }
                    }
                }
            }
            for(int j = 0; j<size; j++){
                grad(i,0) += A(j,0) + B(j,0);
                //grad(i,0) += double(counter[j])/double(dim) * (A(j,0) + B(j,0)); /*WEIGHTS*/
            }
        }

        
        return grad;
    }



    double Alignment::stepSizeND_(DBL_MATRIX& xk, DBL_MATRIX& y, DBL_MATRIX& d, double lambda_old, double c1, double c2, DBL_MATRIX& rt_vec, double alpha)
    {
        if(xk.columnCount() != 1 || y.columnCount() != 1 || d.columnCount() != 1){
            MSPP_LOG(logERROR) << "Alignment::stepSizeND_(): Wrong dimensionality of input - xk, y and d must be column vectors!" << std::endl;
        }
        mspp_precondition(xk.columnCount() == 1 && y.columnCount() == 1 && d.columnCount() == 1 , "Alignment::stepSizeND_(): Wrong dimensionality of input - xk, y and d must be column vectors!");
        
        //precondition size xk = size d
        if(xk.rowCount() != d.rowCount()){
            MSPP_LOG(logERROR) << "Alignment::stepSizeND_(): xk and d must have eqal size!" << std::endl;
        }
        mspp_precondition(xk.rowCount() == d.rowCount() , "Alignment::stepSizeND_(): xk and d must have eqal size!");
        
        //precondition size xk,d = size y -1
        if(xk.rowCount() != y.rowCount()-1){
            MSPP_LOG(logERROR) << "Alignment::stepSizeND_(): xk and d must have lower dimensionality than y!" << std::endl;
        }
        mspp_precondition(xk.rowCount() == y.rowCount()-1 , "Alignment::stepSizeND_(): xk and d must have lower dimensionality than y!");

        // initialize lambda with previous lambda
        double lambda = lambda_old;

        bool wolfe = false;
        int iter = 0;
        
        while(!wolfe){
            // multiplications and summations of vectors

            DBL_MATRIX new_xk = xk + lambda * d;

            DBL_MATRIX new_grad_d = gradOnHPlane_(new_xk,y,rt_vec,alpha);
            DBL_MATRIX old_grad_d = gradOnHPlane_(xk,y,rt_vec,alpha);;

            DBL_MATRIX sum_new_grad_d = new_grad_d.transpose() * d;
            DBL_MATRIX sum_old_grad_d = old_grad_d.transpose() * d;

            if(sum_new_grad_d.rowCount() != 1 || sum_new_grad_d.columnCount() != 1 || sum_old_grad_d.rowCount() != 1 || sum_old_grad_d.columnCount() != 1){
                MSPP_LOG(logERROR) << "Alignment::stepSizeND_(): Internal error - new_grad_d is not scalar!" << std::endl;
            }
            
            //check if stepsize is too big or too small
            if(valOnHPlane_(new_xk,y,rt_vec,alpha) >= valOnHPlane_(xk,y,rt_vec,alpha) + lambda*c1*sum_old_grad_d(0,0)){
                lambda -= lambda/2.;
            } else if (sum_new_grad_d(0,0) <= c2*sum_old_grad_d(0,0)){
                lambda += lambda/2.;
            } else {
                //stepsize ok --> wolfe condition fulfilled
                wolfe = true;
            }


            if(iter > 200){
                //too many iterations
                wolfe = true;
                MSPP_LOG(logDEBUG) << "Alignment::stepSizeND_(): POWELL_WOLFE reached maximum number of iterations." << std::endl;
            }

            if(lambda < 1e-4){
                wolfe = true;
            }

            iter += 1;
        }

        return lambda;
                
    }



    DBL_MATRIX Alignment::lineDistort_(DBL_MATRIX& linePoints, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double c1, double c2,  DBL_MATRIX& alpha)
    {
        if(alpha.columnCount() != 1){
            MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Wrong dimensionality of input - alpha must be a column vector!" << std::endl;
        }
        mspp_precondition(alpha.columnCount() == 1 , "Alignment::lineDistort_(): Wrong dimensionality of input - alpha must be a column vector!");
        
        if(linePoints.columnCount() != y.columnCount()-1){
            MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Dimension mismatch - the dimension of linePoints must be one less than the dimension of y!" << std::endl;
        }
        mspp_precondition(alpha.columnCount() == 1 , "Alignment::lineDistort_(): Dimension mismatch - the dimension of linePoints must be one less than the dimension of y!");
        
        if(alpha.rowCount() != linePoints.rowCount()){
            MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Dimension mismatch - the length of alpha does not match the given data!" << std::endl;
        }
        mspp_precondition(alpha.columnCount() == 1 , "Alignment::lineDistort_(): Dimension mismatch - the length of alpha does not match the given data!");

        if(linePoints.columnCount() != rt_vec.columnCount()-1){
            MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Dimension mismatch - rt_vec must be 1 dimension larger than linePoints!" << std::endl;
        }
        mspp_precondition(linePoints.columnCount() == rt_vec.columnCount()-1 , "Alignment::lineDistort_(): Dimension mismatch - rt_vec must be 1 dimension larger than linePoints!");

        if(y.columnCount() != rt_vec.columnCount()){
            MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Dimension mismatch - rt_vec and y must have equal dimensionality!" << std::endl;
        }
        mspp_precondition(y.columnCount() == rt_vec.columnCount() , "Alignment::lineDistort_(): Dimension mismatch - rt_vec and y must have equal dimensionality!");

        //get number of dimensions
        int dim = rt_vec.columnCount(); 
        int size = linePoints.rowCount();


        //go through points in line
        for(int u = 0; u < size; u++){
            MSPP_LOG(logINFO) << "Alignment: Processing line point " << u+1 << "/" << size << std::endl;
            //calculate distance vector between current point u and next point u+1
            std::vector<double> dist (dim-1);
            if(u < size-1){
                for(int i = 0; i < dim-1; i++){
                    dist[i] = linePoints(u+1,i) - linePoints(u,i);
                }
            }

            //init stepsize lambda
            double lambda = 1;

            //get current support vector
            DBL_MATRIX y_curr = extractRow(y,u).transpose();
            
            //get current line points
            DBL_MATRIX linePoints_curr = extractRow(linePoints,u).transpose();
            
            //init iteration counter
            int ITER = 0;

            while(ITER < 10000){
                ITER++;
                if(ITER==1000){
                    MSPP_LOG(logDEBUG) << "Alignment::lineDistort_(): Algorithm probably does not converge!" << std::endl;
                }
                if(ITER==9999){
                    MSPP_LOG(logERROR) << "Alignment::lineDistort_(): Too many iterations. Algorithm does not converge!" << std::endl;
                    
                }

                //calculate negative gradient on hyperplane
                DBL_MATRIX proj_vec = -Alignment::gradOnHPlane_(linePoints_curr,y_curr,rt_vec,alpha(u,0));

                //calculate vector with length 1 pointing in gradient direction
                double proj_vec_norm = 0;
                for(int i = 0; i<proj_vec.rowCount(); i++){
                    proj_vec_norm += proj_vec(i,0)*proj_vec(i,0);
                }
                proj_vec_norm = sqrt(proj_vec_norm);

                DBL_MATRIX d (proj_vec.rowCount(),1);
                for(int i = 0; i<proj_vec.rowCount(); i++){
                    d(i,0) = proj_vec(i,0)/proj_vec_norm;
                }
               
                //compute new stepsize
                lambda = Alignment::stepSizeND_(linePoints_curr,y_curr,d,lambda,c1,c2,rt_vec,alpha(u,0));
                
                //new position of current point u
                DBL_MATRIX x_new (d.rowCount(),1);
                for(int i = 0; i<d.rowCount(); i++){
                    x_new(i,0) = linePoints_curr(i,0) + lambda*d(i,0);
                }
                
                //termination condition
                double mean_change = 0;
                for(int i = 0; i<x_new.size(); i++){
                    mean_change += abs(x_new(i,0) - linePoints_curr(i,0));
                } 
                
                //update current linepoints
                linePoints_curr = x_new;

                //double cond = (0.1*lambda>1e-5 ? 0.1*lambda : 1e-5);
                double cond = 1e-3;
                
                if(mean_change < cond){
                    break;
                };                   

            }
            
            for(int i = 0; i<linePoints_curr.rowCount(); i++){
                linePoints(u,i) = linePoints_curr(i,0);
            }

            //shift next point u+1 to a better start position
            if(u < linePoints.rowCount()-1){
                for(unsigned int i = 0; i<dist.size(); i++){
                    linePoints(u+1,i) = (linePoints(u+1,i) + linePoints(u,i) + dist[i])/2.;
                }                    
            }
        }
        

        //Output of line distortion
        DBL_MATRIX newLinePoints (linePoints.rowCount(),linePoints.columnCount()+1);

        //calculate last component
        for(int i = 0; i<linePoints.rowCount(); i++){
            double sum_y = 0;
            for(int k = 0; k<y.columnCount();k++){
                sum_y += y(i,k);
            }
            
            double sum_lp = 0;
            for(int k = 0; k<linePoints.columnCount();k++){
                sum_lp += linePoints(i,k);
            }
            
            double z = sum_y - sum_lp;
            
            for(int k = 0; k<newLinePoints.columnCount()-1;k++){
                newLinePoints(i,k) = linePoints(i,k);
            }
            
            newLinePoints(i,newLinePoints.columnCount()-1) = z;

        }

        //check monotonicity of MTS
        bool sane = true;
        for(int i = 0; i < newLinePoints.columnCount(); i++){
            for(int j = 1; j < newLinePoints.rowCount(); j++){
                if(newLinePoints(j,i) < newLinePoints(j-1,i)){
                    sane = false;
                }
            }
        }
        if(!sane){
            std::cout << "WARNING: Master Time Scale is not monotonous. Results may be incorrect!" << std::endl;
        }

        return newLinePoints;

    }

    //Extracts row i from given matrix and returns a 1*n row vector
    DBL_MATRIX extractRow(DBL_MATRIX& mat, int i)
    {
        int num_rows = mat.rowCount();
        int num_col = mat.columnCount();
        if(i >= num_rows || i < 0){
            MSPP_LOG(logERROR) << "Alignment::extractRow_(): Index exceeds matrix dimensions." << std::endl;
        }
        mspp_precondition(i < num_rows && i >= 0 , "Alignment::extractRow_(): Index exceeds matrix dimensions.");

        DBL_MATRIX row_vector (1,num_col);

        for(int k = 0; k < num_col; k++){
            row_vector(0,k) = mat(i,k);
        }

        return row_vector;
    }

    //Extracts column i from given matrix and returns a n*1 column vector
    DBL_MATRIX extractColumn(DBL_MATRIX& mat, int i)
    {
        int num_rows = mat.rowCount();
        int num_col = mat.columnCount();
        if(i >= num_col || i < 0){
            MSPP_LOG(logERROR) << "Alignment::extractColumn_(): Index exceeds matrix dimensions." << std::endl;
        }
        mspp_precondition(i < num_col && i >= 0 , "Alignment::extractColumn_(): Index exceeds matrix dimensions.");

        DBL_MATRIX column_vector (num_rows,1);

        for(int k = 0; k < num_rows; k++){
            column_vector(k,0) = mat(k,i);
        }

        return column_vector;
    }


    void Alignment::writeRTC(std::string filename)
    {
        std::fstream f (filename.c_str(), std::fstream::out | std::fstream::trunc);
        f << rtc_;
        f.close();
        return;
    }

    void Alignment::writeMTS(std::string filename)
    {
        std::fstream f (filename.c_str(), std::fstream::out | std::fstream::trunc);
            f << getMasterTimeScale(100);
        f.close();
        return;
    }
    void Alignment::writeMTS(std::string filename, DBL_MATRIX& MTS)
    {
        std::fstream f (filename.c_str(), std::fstream::out | std::fstream::trunc);
            f << MTS;
        f.close();
        return;
    }

} /*namespace ms*/
