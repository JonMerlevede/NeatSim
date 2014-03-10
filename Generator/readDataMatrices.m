function [ myData, existingData ] = readDataMatrices( regexp )
%READDATAMATRIX Summary of this function goes here
%   Detailed explanation goes here
myCell = readData('output',regexp);
myData = [myCell{:,2}];
existingCell = readData('existing',regexp);
existingData = [existingCell{:,2}];

end

