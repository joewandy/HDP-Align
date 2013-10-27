function e = checksumy(Z,x,subZ,Zposn)
K = size(Z{1},2);
e = [];
for k = 1:K
    for l = 1:length(subZ{k}.ID)
        ty = 0;
        for r = 1:length(Z)
            ty = ty + sum(x{r}(Zposn{r}==subZ{k}.ID(l) & Z{r}(:,k)));
        end
        e = [e;ty subZ{k}.sumy(l)];
    end
end