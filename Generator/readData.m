function D = readData( fileDir, fileRegexp )
%READDATA Reads the data from the files in fileDir that match fileRegexp.
%   Files should have the Gendreau structure.
%
%   Packages with parameters that are zero are filtered out.
%   Output is a nx2 cell matrix where n is equal to the number of packages.
%   The first column of the cell matrix contains the matched file names.
%   The second column of the cell matrix contains the corresponding data
%   matrix. Data matrices have a column for every packages, and each row
%   corresponds to a package property as described by the map in define_Cn.

    % Get filenames. Files are assumed to be in a directory called 'existing'
    listing = dir(strcat([fileDir '/' fileRegexp]));
    D = cell(length(listing),2);
    % Initialize data array
    D(:,1) = {listing(:).name}.';

    for i=1:size(D,1)
        % Read file
        D{i,2} = dlmread(strcat([fileDir '/' D{i,1}],' '));
        % Transpose the matrix. Each package is now in its own column.
        D{i,2} = D{i,2}.';
        assert(size(D{i,2},1) == 11);
        % Remove nonsensical packages that have zero values
        D{i,2}(:,any(D{i,2}<0)) = [];
    end
end