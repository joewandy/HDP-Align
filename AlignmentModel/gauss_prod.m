% compute probability product kernel for mass
function product = gauss_prod(gauss1, gauss2)

    % the resulting gaussian integrates to 1 -- leaving only this constant term cc
    cc = ( 1/(sqrt(det(2*pi*(gauss1.cov+gauss2.cov)))) ) * exp( (-1/2)*(gauss1.mu-gauss2.mu)'*inv(gauss1.cov+gauss2.cov)*(gauss1.mu-gauss2.mu) );
    product = cc;

