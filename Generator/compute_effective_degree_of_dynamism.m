function [edyn,eedyn] = compute_effective_degree_of_dynamism(type,intense)
    if ~ischar(type)
        types = num2str(type);
    else
        types = type;
        type = str2double(types);
    end
    if ~ischar(intense)
        intense = num2str(intense);
    end
    D=readData('output',['*',types,'_',intense]);
    define_Cn;
    rats = zeros(0,0);
    ptwes = zeros(0,0);
    for m=1:size(D,1)
        t1 = D{m,2};
        t2 = t1(cN.requestArrivalTime,:);
        rats = [rats t2];
        t2 = t1(cN.pickupTimeWindowEnd,:);
        ptwes = [ptwes t2];
    end
    T = type*60;
    edyn = 1/(length(rats)*T)*sum(rats);
    eedyn = 1/(length(rats)*T)*sum(T-(ptwes - rats));
end