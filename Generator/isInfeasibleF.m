function isInfeasibleF( type, folder )
%ISINFEASIBLEF Checks if one of the pickups or deliveries in the simulation
% files in folder with times simulationTime are infeasible.
%
%   folder          Specifies the folder where the simulation files are located.
%
%   
%   - If type == 1, pickups or dropoffs are considered infeasible if it is
%       not possible to pick them up and drop them off and/or drop them off
%       and get back to the depot before the simulation is over, i.e. they
%       are considered infeasible if it is impossible for the simulation to
%       end within the simulation time.
%
%       * In case of a pickup, this includes the time required for a
%       pickup, the time required for driving from the pickup to the
%       delivery location, the time required for delivery and the time to
%       drive from the delivery location to the depot.
%       * In case of a delivery, this includes the time required for doing
%       the delivery and driving back to the depot.
%   - If type == 2, pickups or dropoffs are considered infeasible if
%       the beginning of the pickup or dropoff window plus the time that
%       has to be spent driving before the truck is back at the depot is
%       greater than the simulation time. This definition of 'feasibility'
%       does not take into account pickup- and dropoff times. As a result,
%       it might still be impossible to end within the simulation time for
%       feasible scenarios.
%
%       * In the case of a pickup, this includes the time required for
%       driving from the pickup to the delivery location and the time
%       required for driving from the delivery location to the depot.
%       * In the case of a delivery, this includes the time required for
%       driving from the delivery location to the depot.
%
%   The default value of type is 1.
%
%   Simulations considered infeasible for type = 2 are always considered
%   infeasible for type = 1, but the reverse is not true.

%% Input verification
if (nargin < 2)
    disp('Not enough input arguments.'); return
end
if (isempty(intersect(type,[1 2])))
    disp('Type needs to be equal to 1 or 2'); return
end

define_Cn
% Location of the central depot
DEPOT = [2;2.5];
% Speed
SPEED = 30/3600; % [km/s]
feasible = true;

simulationTimes = [240 450];
for sT=simulationTimes
    simulationTime = sT*60;
    %% Read data and set known variables
    D = readData(folder,strcat(['*req*' int2str(sT) '*']));
    % Squeeze together all pickup and delivery requests into a single matrix.
    D = [D{:,2}]; % [1 request / column]


    for i=1:size(D,2)
        req = D(:,i); % a single pickup and delivery request
        Pp = req([cN.pickupX cN.pickupY]); % pickup point
        Pd = req([cN.deliveryX cN.deliveryY]); % delivery point
        dij = norm(Pp-Pd); % distance from pickup to delivery point
        dj0 = norm(Pd-DEPOT); % distance from delivery point to depot
        tij = dij/SPEED; % time to drive from pickup to delivery point
        tj0 = dj0/SPEED; % time to drive from delivery point to depot
        pickupDuration = req(cN.pickupServiceTime);
        deliveryDuration = req(cN.deliveryServiceTime);
        start_pTW = req(cN.pickupTimeWindowBegin); % start of pickup TW
        start_dTW = req(cN.deliveryTimeWindowBegin); % start of delivery TW
        
        lftPickup = simulationTime - tij - tj0;
        lftDelivery = simulationTime - tj0;
        if (type == 2)
            lftPickup = lftPickup - pickupDuration - deliveryDuration;
            lftDelivery = lftDelivery - deliveryDuration;
        end
        if (start_pTW > lftPickup)
            fprintf('Infeasible pickup time\n\tstart pickup TW = %f, lft = %f\n',start_pTW/60,lftPickup/60);
            feasible = false;
        end
        if (start_dTW > lftDelivery)
            fprintf('Infeasible delivery time\n\tstart delivery TW = %f, lft = %f\n',start_dTW/60,lftDelivery/60);
            feasible = false;
        end
    end
end
if feasible
    disp('Feasible!');
end
% % Gendreau pickup and delivery time is 5 minutes (we could read this from
% % the data as well). We really do not need these variables if type == 2.
% pickupDuration = 5*60; % [s]
% deliveryDuration = 5*60; % [s]
% % Total Gendreau simulation time is simulationTime minutes
% simulationTime = simulationTime*60; % [s]
% % Speed of Gendreau wagons is 30 km/h
% totalSpeed = 30/3600; % [km / s]
% 
% 
% %% Determine driving times
% % Distance from delivery to depot location / speed
% tDriveAfterDelivery = sqrt( ...
%       (D(cN.deliveryX,:) - 2.5).^2 ...
%     + (D(cN.deliveryY,:) - 2.5).^2) * (1/totalSpeed);
% % Distance from pickup to delivery location / speed
% tDriveAfterPickup = sqrt( ...
%       (D(cN.pickupX,:) - D(cN.deliveryX,:)).^2 ...
%     + (D(cN.pickupY,:) - D(cN.deliveryY,:)).^2) * (1/totalSpeed);
% % Distance from pickup to delivery to depot location / speed
% tDriveAfterPickup = tDriveAfterPickup + tDriveAfterDelivery;
% % Alternative (identical) calculation
% % P1 = [D(cN.pickupX,:) ; D(cN.pickupY,:)];
% % P2 = [D(cN.deliveryX,:) ; D(cN.deliveryY,:)];
% % P3 = repmat(2.5,2,size(D,2));
% % tDriveAfterDelivery = sqrt(sum((P2-P3) .* (P2-P3))) * (1/totalSpeed);
% % tDriveAfterPickup = sqrt(sum((P1-P2) .* (P1-P2))) * (1/totalSpeed);
% % tDriveAfterPickup = tDriveAfterPickup + tDriveAfterDelivery;
% 
% %% Determine feasibility
% if type == 1
%     pT = D(cN.pickupTimeWindowBegin,:) ...
%         + pickupDuration ...
%         + deliveryDuration ...
%         + tDriveAfterPickup;
%     dT = D(cN.deliveryTimeWindowBegin,:) ...
%         + deliveryDuration ...
%         + tDriveAfterDelivery;
% elseif type == 2
%     pT = D(cN.pickupTimeWindowBegin,:) ...
%         + tDriveAfterPickup;
%     dT = D(cN.deliveryTimeWindowBegin,:) ...
%         + tDriveAfterDelivery;
% end
% infeasiblePickup = any(pT> simulationTime);
% infeasibleDelivery = any(dT > simulationTime);
end
% end

