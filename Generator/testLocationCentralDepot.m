function valid = testLocationCentralDepot(depotLocation)
    valid = false;
    if test(depotLocation,450*60,'*450*')
       if test(depotLocation,240*60,'*240*')
           valid = true;
       end
    end
end

function valid = test(depotLocation,simulationLength,regexp)
    SPEED = 30/3600; %speed [km/s]
    define_Cn;
    data = readData('existing',regexp);
    for m=1:size(data,2)
        scenario = data{m,2};
        for n=1:size(scenario,2)
            request = scenario(:,n);
            dP = [request(cN.pickupX) request(cN.pickupY)]; %pickup point [km]
            dD = [request(cN.deliveryX) request(cN.deliveryY)]; %delivery point [km]
            dij = norm(dD - dP); % distance from pickup to delivery location [km]
            dj0 = norm(depotLocation - dD); % distance from delivery location to central depot [km]
            tij = dij/SPEED;
            tj0 = dj0/SPEED;
            
            % Check pickup time
            latestPickupTime = simulationLength - tij - tj0;
            tP = request(cN.pickupTimeWindowEnd);
            if (tP > latestPickupTime)
                valid = false; return
            end
            % Check delivery time
            tD = request(cN.deliveryTimeWindowEnd);
            latestDeliveryTime = simulationLength - tj0;
            if (tD > latestDeliveryTime)
                valid = false; return
            end
        end
    end
    valid = true; return
end