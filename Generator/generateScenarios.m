% Number of scenarios to generate in each category
% (:,1) - short/easy, (:,2) - short/hard, (:,3) - long/easy, (:,4) - long/hard.
% (1,:) - training scenarios - (2,:) - test scenarios
NUMBER_OF_SOLUTIONS = [60 60 60 0
    0 0 0 0];
%NUMBER_OF_SOLUTIONS = [1000 1000 1000 0
%    500 500 500 0];
OUTPUT_FOLDER = 'output';

%% Goal
%
% In  Java we interpret the output file like this
% * |requestArrivalTime = (long) (Double.parseDouble(parts[0]) * 1000.0)|
% * |pickupServiceTime = Long.parseLong(parts[1]) * 1000|
% * |pickupX = Double.parseDouble(parts[2])|
% * |pickupY = Double.parseDouble(parts[3])|
% * |pickupTimeWindowBegin = (long) (Double.parseDouble(parts[4]) * 1000.0)|
% * |pickupTimeWindowEnd = (long) (Double.parseDouble(parts[5]) * 1000.0)|
% * |deliveryServiceTime = Long.parseLong(parts[6]) * 1000|
% * |deliveryX = Double.parseDouble(parts[7])|
% * |deliveryY = Double.parseDouble(parts[8])|
% * |deliveryTimeWindowBegin = (long) (Double.parseDouble(parts[9]) * 1000.0)|
% * |deliveryTimeWindowEnd = (long) (Double.parseDouble(parts[10]) * 1000.0)|

%% Set random seed
%stream = RandStream('mt19937ar','Seed',1990);
%RandStream.setGlobalStream(stream)
seed = 1990;
% If we're working with Octave
if exist('OCTAVE_VERSION','builtin')
    rand('state',seed);  % Octave
    randn('state',seed); % Octave
% If we're not working with Ocate (i.e. we're working with MATLAB)
else
    % If we are working with an old version of MATLAB
    try
         RandStream.setGlobalStream(RandStream('mt19937ar','seed',seed));
    % If we are working with an old version of MATLAB
    catch e1
        try
            % matlab 7.9+
            RandStream.setDefaultStream(RandStream('mt19937ar','seed',seed));
        catch e2
            randn('state',seed); % Matlab 5+
        end
    end
end

%% Set fixed simulation parameters
% These parameters are set as described in section 6.1 of Gendreau'
% article.
%
% Width of the area [km]
    input.maxWidth = 5;
% Height of the area [km]
    input.maxHeight = 5;
% Speed of the vehicles [km/h]
    input.speed = 30;
% Pickup service time [seconds]
    input.pickupDuration = 5*60;
% Delivery service time [seconds]
    input.deliveryDuration = 5*60;
% Minimum time between announce and latest pickup [seconds]
    input.minimumSeparation = 30*60;
% (horizontal,vertical) aka (x,y) location of the central depot [km]
    input.depotLocation = [2;2.5];
% Activity matrix - reverse-engineer from existing scenarios [-]
    hZones = 5; % number of horizontal zones
    vZones = 4; % number of vertical zones
    input.A = reverseA(readData('existing','req*'),hZones,vZones);
% Boundaries of uniform distribution to draw delta values for generating pickup time windows [-]
    input.pickupDeltas = [0.1 ; 0.8];
% Bouddaries of uniform distribution to draw delta values for generating delivery time windows [-]
    input.deliveryDeltas = [0.3 ; 1.0];
% Verbosity
    input.verbose = false;
    
%% Generate full descriptions for four sets of scenarios
% scenarioDescriptions{1} Short, easy set (4 hours, 24 requests / hour)
% scenarioDescriptions{2} Short, hard set (4 hours, 33 requests / hour)
% scenarioDescriptions{3} Long, easy set (7.5 hours, 24 requests / hour)
% scenarioDescriptions{4} Long, hard set (7.5 hours, 33 requests / hour)
 relativePeriodLength = [1 1 .5 1 1 ].';
% Period matrix for the short scenarios [minutes]
    totalSimulationTime = 240*60; % Simulation length of 4 hours [seconds]
    shortPeriod = relativePeriodLength/sum(relativePeriodLength)*(totalSimulationTime/60);
% Period matrix for the long scenarios [minutes]
    totalSimulationTime = 450*60; % Simulation length of 7.5 hours [seconds]
    longPeriod = relativePeriodLength/sum(relativePeriodLength)*(totalSimulationTime/60);
% Poisson intensity matrix for easy scenarios (24 requests / hour) [requests/minute]
    easyPoisson = [0.55 0.70 0.10 0.40 0.10].';
% Poisson intensity matrix for hard scenarios (33 requests / hour) [requests/minute]
    hardPoisson = [0.75 1.10 0.25 0.40 0.10].';

t = input;
% SET 1-2) Short
t.periodLength = shortPeriod;
    % SET 1) Short, easy (24 requests / hour)
    t.poissonPeriodIntensities = easyPoisson;
    t.suffix = '240_24';
    scenarioDescriptions{1} = t;
    % SET 2) Short, hard (33 requests / hour)
    t.poissonPeriodIntensities = hardPoisson;
    t.suffix = '240_33';
    scenarioDescriptions{2} = t;
% Set 3-4) Long
t.periodLength = longPeriod;
    % SET 3) Long, easy (24 requests / hour)
    t.poissonPeriodIntensities = easyPoisson;
    t.suffix = '450_24';
    scenarioDescriptions{3} = t;
    % SET 4) Long, hard (33 requests / hour)
    t.poissonPeriodIntensities = hardPoisson;
    t.suffix = '450_33';
    scenarioDescriptions{4} = t;

%% Generate the scenario files
for k=1:4
   tic
   for n=1:NUMBER_OF_SOLUTIONS(1,k)
       output = createSimulation(scenarioDescriptions{k});
       path = sprintf(...
           '%s/train_req_rapide_%03d_%s',...
           OUTPUT_FOLDER,n,scenarioDescriptions{k}.suffix);
       dlmwrite(path, output.','delimiter',' ','precision',20);
   end
   for n=1:NUMBER_OF_SOLUTIONS(2,k)
       output = createSimulation(scenarioDescriptions{k});
       path = sprintf(...
           '%s/test_req_rapide_%03d_%s',...
           OUTPUT_FOLDER,n,scenarioDescriptions{k}.suffix);
       dlmwrite(path, output.','delimiter',' ','precision',20);
   end
   toc
end

%% Other activity matrices to experiment with
% A = ones(aw,ah); % uniform activity matrix.
%  A = [1 1 2 3  2
%       1 6 6 6  6
%       3 6 9 13 9
%       2 6 9 9  9]; % matrix like described
% A = [0 0 0 0 0
%      0 0 0 0 0
%      0 1 0 0 0
%      0 0 0 0 0
%      0 0 0 0 1]; % matrix to test
% Normalize matrix
%A = A * (1/sum(A(:)));

fprintf('Created 6x%d solutions.\n',NUMBER_OF_SOLUTIONS)