function [z,r]= Mob_of_Cir(mob,cent,rad)
% [z,r]= Mob_of_Cir(mob,cent,rad): apply mobius to circle
%   
incirc=cirMatrix(cent,rad);
outcirc=appMobius2Cir(mob,incirc);
[z,r]=matrix2Circle(outcirc);
end