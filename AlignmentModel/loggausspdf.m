function l = loggausspdf(x,mu,prec)

l = -0.5*log(2*pi) + 0.5*log(prec);
l = l - 0.5*prec*(x-mu)^2;