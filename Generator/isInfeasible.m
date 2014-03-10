
[iP240My, iD240My] = isInfeasibleF('existing',240,1)
[iP450My, iD450My] = isInfeasibleF('existing',450,1)
[iP240Theirs, iD240Theirs] = isInfeasibleF('existing',240,2)
[iP450Theirs, iD450Theirs] = isInfeasibleF('existing',450,2)

[iP240My, iD240My] = isInfeasibleF('output',240,1)
[iP240Theirs, iD240Theirs] = isInfeasibleF('output',240,2)
%> totalSimulationTime
% cN.requestArrivalTime = 1;
% cN.pickupServiceTime = 2;
% cN.pickupX = 3;
% cN.pickupY = 4;
% cN.pickupTimeWindowBegin = 5;
% cN.pickupTimeWindowEnd = 6;
% cN.deliveryServiceTime = 7;
% cN.deliveryX = 8;
% cN.deliveryY = 9;
% cN.deliveryTimeWindowBegin = 10;
% cN.deliveryTimeWindowEnd = 11;