function [ output ] = createScenario( I )
%CREATESCENARIO Creates simulation matrix using given input structure.
%   input is a structure that has the fields
%   - A: activity matrix [-]
%   - speed: speed of vehicles [km/h]
%   - periodLength: period lenghts [minutes]
%   - poissonPeriodIntensities: poisson period intensities [requests / minute]
%   - pickupDuration: pickup duration [seconds]
%   - deliveryDuration: delivery duration [seconds]
%   - maxWidth: maximum X coordinate value [km]
%   - maxHeight: maximum Y coordinate value [km]
%   - minimumSeparation: minimum time between packet announce time and the
%     end of the pickup time window [seconds]
%   
%   Optional fields:
%   - verbose: if present, script is more verbose
    
    %% Validate input
    validateInput(I);

    %% Process input
    % Verbosity
    if (isfield(I,'verbose'))
        verbose = I.verbose;
    else
        verbose = false;
    end
    % Number of (discrete) time periods [-]
    nPeriods = length(I.periodLength);
    % Total simulation time [seconds]
    totalSimulationTime = sum(I.periodLength)*60;
    % Period start times [minutes]
    periodStartTimes = cumsum(I.periodLength)-I.periodLength;
    % Speed of the vehicles [km/s]
    speed = I.speed/3600;
    % Depot locations [km]
    depotLocation =  reshape(I.depotLocation,2,1);
    % Width and height of the matrix A [km]
    [ah,aw] = size(I.A);
    % Proportion between width and height of A and maximum width and height [-]
    ph = I.maxHeight/ah; pw = I.maxWidth/aw;
    props = [pw;ph;pw;ph];
    

    %% Process the activity matrix
    P = I.A(:)*I.A(:).';
    P = reshape(P,ah,aw,ah,aw);
    PP = cumsum(P(:));
    assert(numel(P) == ah*aw*ah*aw);
    assert(P(3,2,1,1) == I.A(3,2) * I.A(1,1));
    assert(P(3,3,1,3) == I.A(3,3) * I.A(1,3));
    assert(P(1,1,3,1) == I.A(1,1) * I.A(3,1));
    
    
    %% Determine number of (possible) packets
    % Preallocate for speed (three times average length required)
    requestPeriods = zeros(round(sum(I.poissonPeriodIntensities .* I.periodLength)) * 10,1);
    nRequests = 0;
    for l = 1:nPeriods
        % 'A Poisson law of intensity $$lambda^l$$ is applied to determine
        % the occurence of the next request => use the poisson intensities
        % to determine the number of requests in a time slot.
        nRequestsThisPeriod = poissrnd(I.poissonPeriodIntensities(l)*I.periodLength(l));
        %newNRequests = nRequests + nRequestsThisPeriod;
        %requestPeriods(nRequests + 1:newNRequests) = repmat(l,nRequestsThisPeriod,1);
        requestPeriods(nRequests + 1: nRequests + nRequestsThisPeriod) = l;
        nRequests = nRequests + nRequestsThisPeriod;
    end
    requestPeriods = requestPeriods(1:nRequests);
    nValidRequests = 0;
    output = zeros(11,nRequests);
    
    %% Looping
    for k = 1:nRequests
        period = requestPeriods(k);
        % Determine packet announce / request arrival time
        % Although not explicitly mentioned (?), we assume the request
        % arrival time to be uniformely distributed within its time slot.
        requestArrivalTime = periodStartTimes(period) + rand*I.periodLength(period);
        if period ~= nPeriods
            assert(requestArrivalTime <= periodStartTimes(period+1));
        else
            assert(requestArrivalTime <= totalSimulationTime/60);
        end
        % Transform from [minutes] to [seconds]
        requestArrivalTime = requestArrivalTime*60;
        
        %% Determine packet position
        % Determine position square index
        iPos = min([find(PP > rand,1) length(PP)]);
        [ppY,ppX,dpY,dpX] = ind2sub([ah aw ah aw],iPos); % square position
        pos = [ppX;ppY;dpX;dpY] - ones(4,1) + rand(4,1); % uniform random position within square
        pos = pos .* props;
        assert(pos(1) < I.maxHeight);
        assert(pos(2) < I.maxWidth);
        assert(pos(3) < I.maxHeight);
        assert(pos(4) < I.maxWidth);
        Pp = pos(1:2); % pickup point
        Pd = pos(3:4); % delivery point
        
        
        %% Determine windows
        % I DO KNOW WHAT THE CURRENT TIME IS. I THINK THIS IS THE REQUEST
        % ARRIVAL TIME.
        cT = requestArrivalTime; % current time
        dij = norm(Pp - Pd);
        dj0 = norm(Pd - depotLocation);
        % Minimum travel time from pickup point to delivery point
        tij = dij/speed;
        % Minimum travel time from delivery point to depot
        tj0 = dj0/speed;
        % Latest feasible time to start a delivery (really)
        % lftDelivery = totalSimulationTime - mttDelivery - I.deliveryDuration;
        % Latest feasible time to start a delivery (Gendreau)
        lftDelivery = totalSimulationTime - tj0;
        % Latest feasible time to start a pickup (really)
        % lftPickup = lftDelivery - mttBetween - I.pickupDuration;
        % Latest feasible time to start a pickup (Gendreau)
        lftPickup = totalSimulationTime - tij - tj0;
        
        % WHAT TO DO IN THIS CASE IS NOT SPECIFIED
        % I skip generation of these scenario's (the existing Gendreau
        % scenario's do not contain scenario's that match this case)
        if cT > lftPickup
            if verbose
                disp('Dismissing package: infeasible packet')
            end
            continue; % call is not accepted
        end

        %% Deterimine pickup time window

        % Determine halftime for pickup
        ht = (cT + lftPickup)/2;
        % Determine random pickup beta value
        beta = 0.6 + 0.4*rand;
        if rand < beta
            ptwBegin = cT + (ht - cT)*rand;
        else
            ptwBegin = ht + (lftPickup - ht)*rand;
        end
        assert(ptwBegin <= lftPickup);
        % The end of the time window at the pick-up location is set to
        % the beginning of the time window + a fraction of the time
        % remaining until the end of the day.
        % Remaining time (actual)
        % remainingTime = lftPickup - (ptwBegin + I.pickupDuration);
        % Remaining time (Gendreau)
        %remainingTime = totalSimulationTime - cT;
        remainingTime = max(totalSimulationTime - ptwBegin,0);
        remainingTimeFraction = I.pickupDeltas(1) + diff(I.pickupDeltas)*rand;
        ptwEnd = ptwBegin + remainingTime*remainingTimeFraction;
        % I added the min here myself
        ptwEnd = min(ptwEnd,totalSimulationTime);
        assert(ptwEnd > ptwBegin);
        
        if (ptwEnd < requestArrivalTime + I.minimumSeparation)
            if verbose
                disp('Dismissing package: minimum separation not met');
            end
            continue;
        end
        
%         if (ptwBegin > lftPickup)
%             if verbose
%                 disp('Dismissing package: infeasible packet (pickup TW)')
%             end
%             continue; % call is not accepted
%             assert(false);
% %             pwtEnd = lftPickup;
%         end

        %% Determine delivery time window

        % Earliest time we can start delivery (really)
        % earliestPossible = ptwBegin + I.pickupDuration + mttBetween;
        % Earliest time we can start delivery (by Gendreau)
        earliestPossible = ptwBegin + tij;
        assert(lftDelivery >= earliestPossible);
        % Determine halftime for delivery
        ht = (earliestPossible + lftDelivery) / 2;
        % Determine random delivery beta value
        beta = 0.6 + 0.4*rand;
        if rand < beta
            dtwBegin = earliestPossible + (ht - earliestPossible)*rand;
        else
            dtwBegin = ht + (lftDelivery - ht)*rand;
        end
        assert(lftDelivery >= dtwBegin);
        % The end of the time window at the delivery location is set to
        % the beginning of the time window + a fraction of the time
        % remaining until the end of the day.
        % Remaining time (actual)
        % remainingTime = lftDelivery - (dtwBegin + I.deliveryDuration);
        % Remaining time (Gendreau)
        %remainingTime = totalSimulationTime - cT;
        remainingTime = max(totalSimulationTime - dtwBegin,0);
        remainingTimeFraction = I.deliveryDeltas(1) + diff(I.deliveryDeltas)*rand;
        dtwEnd = dtwBegin + remainingTime*remainingTimeFraction;
        % I added the min here myself
        dtwEnd = min(dtwEnd,totalSimulationTime);
        assert(dtwEnd > dtwBegin);
        % WHAT TO DO IN THIS CASE IS NOT SPECIFIED
        % I skip generation of these scenario's (the existing Gendreau
        % scenario's do not contain scenario's that match this case)
%         if (dtwBegin > lftDelivery)
%             if verbose
%                 disp('Dismissing package: infeasible packet (delivery TW)')
%             end
% %             dwtEnd = lftDelivery;
%             continue; % call is not accepted
%             assert(false);
%         end

        %% Write delivery information to output matrix
        nValidRequests = nValidRequests + 1;
        output(:,nValidRequests) = [requestArrivalTime
            I.pickupDuration
            Pp(1) ; Pp(2)
            ptwBegin ; ptwEnd
            I.deliveryDuration
            Pd(1) ; Pd(2)
            dtwBegin ; dtwEnd];
    end
    % Reduce size of output matrix
    output = output(:,1:nValidRequests);
end

function validateInput(I)
    requiredFields = {...
        'A'
        'speed'
        'pickupDuration'
        'deliveryDuration'
        'periodLength'
        'minimumSeparation'
        'poissonPeriodIntensities'
        'pickupDeltas'
        'deliveryDeltas'
        'depotLocation'};
    for k=1:length(requiredFields)
        assert(...
            isfield(I,requiredFields{k}), ...
            sprintf('Missing required input: %s',requiredFields{k}))
    end
    assert(length(I.periodLength) == length(I.poissonPeriodIntensities), ...
        'periodLength and poissonPeriodIntensities have to have the same length')
    assert(length(I.pickupDeltas) == 2, ...
        'pickupDeltas has to have length 2')
    assert(length(I.deliveryDeltas) == 2, ...
        'deliveryDeltas has to have length 2')
    assert(length(I.depotLocation) == 2,...
        'depotLocation has to have length 2')
    assert(I.speed > 0,...
        'speed needs to be strictly positive')
    assert(all(I.poissonPeriodIntensities >= 0), ...
        'Poisson intensities need to be positive');
    assert(I.pickupDuration >= 0, 'Pickup duration needs to be positive.');
    assert(I.deliveryDuration >= 0, 'Delivery duration needs to be positive.');
    e = 10^-5; s = sum(I.A(:));
    assert(s < 1 + e && s > 1 - e, 'A is not a valid activity matrix.');
end