function testAverages
%TESTAVERAGES Computes the average number of parcels/hour.
%   This is a trivial function: take a look at its source.

    fprintf('[Easy-Short]\n');
    fprintf('\tMy average number of parcels/hour: %f\n',meanNParcels('output','*240_24')/4);
    fprintf('\tGendreau average number of parcels/hour: %f\n',meanNParcels('existing','*240_24')/4);
    fprintf('[Hard-Short]\n');
    fprintf('\tMy average number of parcels/hour: %f\n',meanNParcels('output','*240_33')/4);
    fprintf('\tGendreau average number of parcels/hour: %f\n',meanNParcels('existing','*240_33')/4);
    fprintf('[Easy-Long]\n');
    fprintf('\tMy average number of parcels/hour: %f\n',meanNParcels('output','*450_24')/7.5);
    fprintf('\tGendreau average number of parcels/hour: %f\n',meanNParcels('existing','*450_24')/7.5);
end

function n = meanNParcels(folder,regexp)
    myCell = readData(folder,regexp);
    n = cellfun(@(x) size(x,2),myCell(:,2));
    n = mean(n);
end
