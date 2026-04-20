function [z,r]=matrix2Circle(cirMatrix)
%[z,r]=matrix2Circle(cirmat) Convert 2x2 matrix to eucl circle
%   A circle is represented as [a b;c d] where:
%     a=0,+-1; b=-conj(z); c=-z;d=|z|^2-r^2
%   if a==0, this is a straight line; in Java, 'cirMatrix2eucl' does
%   this with more sophistication to handle lines, outside of circle
%   For reverse, see cirMatrix.m

z=-cirMatrix(2,1);
r=real(sqrt(abs(z)^2-cirMatrix(2,2)));

end

