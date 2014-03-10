function A = reverseA(D, nHorizontalZones, nVerticalZones)
%REVERSEA Returns the best estimation of the activation matrix A used for
%         generating D of size nVerticalZones x nHorizontalZones.
%
%   D is a cell array containing simulation data as retured by readData.
%
%   It is important that there are pickups or deliveries that fall into
%   the most 'extreme' columns/rows of A. This can be a problem only if
%   A is large and if the number of scenarios in D is very small.
    if (nargin < 3)
        disp('Not enough input arguments.');
        return;
    end
    define_Cn
    % Define BD as the matrix containing the data of the simulations.
    %   -BD has a row for each property of a transportation request
    %   -BD has a column for each transportation request in any simulation
    %       (MATLAB concatenates results by adding columns)
    BD = [D{:,2}];
    % Reduce BD so that it only contains the relevant data - the locations
    % of pickups and deliveries.
    BD = BD([cN.pickupX cN.pickupY cN.deliveryX cN.deliveryY],:);
    % Largest possible X and Y coordinates
    lX = max(max(floor(BD([1 3],:))));
    lY = max(max(floor(BD([2 4],:))));
    % Rescaling of coordinates to fall within the correct zone upon 
    % calling floor.
    % i.e,  horizontal coordinates [0,1) fall within horizontal zone 0
    %       horizontal coordinates [1,2) fall within horizontal zone 1, ...
    % We need to do this since the size of the area does not need to match
    % the number of zones. For example, we can have an area of 100x100 but
    % only 10x10 zones. Gendreau06 also uses an area of 5x5 but uses a
    % different number of horizontal and vertical zones.
    xStretch = (nHorizontalZones-1)/lX;
    yStretch = (nVerticalZones-1)/lY;
    % Replace rescaled positioning data by zone data.
    BD([1 3],:) = floor(BD([1 3],:)*xStretch);
    BD([2 4],:) = floor(BD([2 4],:)*yStretch);

    % Preallocate A
    A = zeros(nVerticalZones,nHorizontalZones);
    % Count the number of pickups and deliveries on each coordinate
    for m=0:nVerticalZones-1
    for n=0:nHorizontalZones-1
        A(m+1,n+1) = sum(BD(1,:) == n & BD(2,:) == m) ...
            + sum(BD(3,:) == n & BD(4,:) == m); 
    end
    end
    assert(sum(A(:)) == size(BD,2)*2, 'Incorrect package count');
    assert(nVerticalZones == size(A,1));
    assert(nHorizontalZones == size(A,2));
    % Turn elements of A into (estimated) probabilities, i.e. normalise A.
    A = A / sum(A(:));
end

