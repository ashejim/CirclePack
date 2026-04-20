function mob=standard3point(a,b,c)
% mob=standard3point(a,b,c)
%    map {a,b,c} to {0,1,infty}

mob = [b-c,-a*(b-c);b-a,-c*(b-a)];
end