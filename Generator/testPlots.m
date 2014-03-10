
function testPlots
    
    [mC,mD,eC,eD] = localRead('*240_24');
    t ='Short scenarios, 24 requests / hour';
    makePlots(mD,eD,1,t);
    plotTimeData(mD,size(mC,1),eD,size(eC,1),240,t,3);
    
    [mC,mD,eC,eD] = localRead('*240_33');
    t = 'Short scenarios, 33 requests / hour';
    makePlots(mD,eD,4,t);
    plotTimeData(mD,size(mC,1),eD,size(eC,1),240,t,6);
% 
    [mC,mD,eC,eD] = localRead('*450_24');
    t = 'Long scenarios, 24 requests / hour';
    makePlots(mD,eD,7,t);
    plotTimeData(mD,size(mC,1),eD,size(eC,1),240,t,9);
    
end

function [mC,mD,eC,eD] = localRead(regexp)
    mC = readData('output',regexp);
    mD = [mC{:,2}];
    eC = readData('existing',regexp);
    eD = [eC{:,2}];
end

function plotTimeData(myEvents,nMyScenarios,existingEvents,nExistingScenarios,simulationTime,myTitle,figureBaseId)
%     nMyScenarios = size(myCell,1);
%     myEvents = [myCell{:,2}];
%     nExistingScenarios = size(existingCell,1);
%     existingEvents = [existingCell{:,2}];

    define_Cn
    figureId = figureBaseId-1;
    relativePeriodLength = [1 1 .5 1 1 ].';
    nH = length(relativePeriodLength);%+1;
    
    figureId = figureId+1;
    figure(figureId); clf;
    totalSimulationTime = simulationTime*60; % Simulation length of 4 hours [seconds]
    periodLengths = relativePeriodLength/sum(relativePeriodLength)*(totalSimulationTime);
    subplotId=0;
    
    disp(myTitle);
    for period=1:length(periodLengths)
        subplotId = subplotId+1;
        periodStartTime = sum(periodLengths(1:period-1));
%         if period <= length(periodLengths)
        periodEndTime = sum(periodLengths(1:period));
%         else
%             periodEndTime = inf;
%         end
        
        myLogical = myEvents(cN.requestArrivalTime,:) >= periodStartTime;
        myLogical = myLogical & (myEvents(cN.requestArrivalTime,:) < periodEndTime);
        
        
        existingLogical = existingEvents(cN.requestArrivalTime,:) >= periodStartTime;
        existingLogical = existingLogical & (existingEvents(cN.requestArrivalTime,:) < periodEndTime);

        nMyPeriodData = sum(myLogical);
        nExistingPeriodData = sum(existingLogical);
        group=[repmat({'My data'},1,nMyPeriodData) repmat({'Existing data'},1,nExistingPeriodData)];
        
        fprintf('\tTime period in [%f,%f)\n',periodStartTime/60,periodEndTime/60);
        fprintf('\t\tAverage number of events/scenario my data set: %f\n',nMyPeriodData/nMyScenarios);
        fprintf('\t\tAverage number of events/scenario existing data set: %f\n',nExistingPeriodData/nExistingScenarios);
        
        subplot(nH,1,subplotId);hold on;
        boxplot([myEvents(cN.requestArrivalTime,myLogical),...
            existingEvents(cN.requestArrivalTime,existingLogical)],group);
        t = sprintf('Time period \\in [%f,%f)',periodStartTime/60,periodEndTime/60);
        title(t);
    end
    figure(figureId);mtit(myTitle);
end

function makePlots(myData,existingData,figureId,plotName)
define_Cn
nV = 5;
nH = 2;

figure(figureId); clf;
subplot(nV,nH,1);hold on;
    title('Delivery - X coordinate')
    [f1,x1] = ecdf(myData(cN.deliveryX,:));
    [f2,x2] = ecdf(existingData(cN.deliveryX,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,2);hold on;
    title('Delivery - Y coordinate')
    [f1,x1] = ecdf(myData(cN.deliveryY,:));
    [f2,x2] = ecdf(existingData(cN.deliveryY,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,3);hold on;
    title('Pickup - X coordinate')
    [f1,x1] = ecdf(myData(cN.pickupX,:));
    [f2,x2] = ecdf(existingData(cN.pickupX,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,4);hold on;
    title('Pickup - Y coordinate')
    [f1,x1] = ecdf(myData(cN.pickupY,:));
    [f2,x2] = ecdf(existingData(cN.pickupY,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,5);hold on;
    title('Delivery - begin of time window')
    [f1,x1] = ecdf(myData(cN.deliveryTimeWindowBegin,:));
    [f2,x2] = ecdf(existingData(cN.deliveryTimeWindowBegin,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,6);hold on;
    title('Delivery - end of time window')
    [f1,x1] = ecdf(myData(cN.deliveryTimeWindowEnd,:));
    [f2,x2] = ecdf(existingData(cN.deliveryTimeWindowEnd,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,7);hold on;
    title('Pickup - begin of time window')
    [f1,x1] = ecdf(myData(cN.pickupTimeWindowBegin,:));
    [f2,x2] = ecdf(existingData(cN.pickupTimeWindowBegin,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,8);hold on;
    title('Pickup - end of time window')
    [f1,x1] = ecdf(myData(cN.pickupTimeWindowEnd,:));
    [f2,x2] = ecdf(existingData(cN.pickupTimeWindowEnd,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
subplot(nV,nH,[9 10]);hold on;
    title('Request arrival times')
    [f1,x1] = ecdf(myData(cN.requestArrivalTime,:));
    [f2,x2] = ecdf(existingData(cN.requestArrivalTime,:));
    plot(x1,f1,'r');
    plot(x2,f2,'g');
mtit(plotName);

% c = {'colors',['r','g']};
figure(figureId+1); clf
nMydata = size(myData,2);
nExistingData = size(existingData,2);
group=[repmat({'My data'},1,nMydata) repmat({'Existing data'},1,nExistingData)];
c = {group};

subplot(nV,nH,1); hold on;
    title('Delivery - X coordinate')
    boxplot([myData(cN.deliveryX,:) existingData(cN.deliveryX,:)],c{:});
subplot(nV,nH,2); hold on;
    title('Delivery - Y coordinate')
    boxplot([myData(cN.deliveryY,:) existingData(cN.deliveryY,:)],c{:});
subplot(nV,nH,3);hold on;
    title('Pickup - X coordinate')
    boxplot([myData(cN.pickupX,:) existingData(cN.pickupX,:)],c{:});
subplot(nV,nH,4);hold on;
    title('Pickup - Y coordinate')
    boxplot([myData(cN.pickupY,:) existingData(cN.pickupY,:)],c{:});
subplot(nV,nH,5);hold on;
    title('Delivery - begin of TW')
    boxplot([myData(cN.deliveryTimeWindowBegin,:) existingData(cN.deliveryTimeWindowBegin,:)],c{:})
subplot(nV,nH,6);hold on;
    title('Delivery - (end - begin) of TW')
    my = myData(cN.deliveryTimeWindowEnd,:) - myData(cN.deliveryTimeWindowBegin,:);
    existing = existingData(cN.deliveryTimeWindowEnd,:) - existingData(cN.deliveryTimeWindowBegin,:);
    boxplot([my existing] ,c{:});
subplot(nV,nH,7);hold on;
    title('Pickup - begin of TW')
    boxplot([myData(cN.pickupTimeWindowBegin,:) existingData(cN.pickupTimeWindowBegin,:)],c{:})
subplot(nV,nH,8);hold on;
    title('Pickup - (end - begin) of TW')
    my = myData(cN.pickupTimeWindowEnd,:) - myData(cN.pickupTimeWindowBegin,:);
    existing = existingData(cN.pickupTimeWindowEnd,:) - existingData(cN.pickupTimeWindowBegin,:);
    boxplot([my existing] ,c{:});
subplot(nV,nH,[9 10]);hold on;
    title('Request arrival times')
    boxplot([myData(cN.requestArrivalTime,:) existingData(cN.requestArrivalTime,:)],c{:});
mtit(plotName);
end