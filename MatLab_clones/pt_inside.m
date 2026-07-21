function factor = pt_inside[Cmat,z]
% factor = pt_inside[Cmat,z]
%   Find z w.r.t. the circle given by 2x2 matrix. Cmat must
%   have its standard normalization, so (1,1) entry is 0 (line),
%   +1, normal circle, or -1 outside of normal circle

cent=Cmat(1,2);

radius=sqrt(cent^2-Cmat(2,2));
dif=abs(cent-z);
if dif>radius
    factor = -1; % Point is outside the circle
elseif dif<radius
    factor = 1; % Point is inside the circle
else
    factor=0;
end