%generateSimulations
xStretch = 1;
yStretch = 4/5;
% A obtained by reversing 'hard' Gendreau data
    A33 = reverseA(readData('existing','req*33'),xStretch,yStretch);
% A obtained by reversing 'easy' Gendreau data
    A24 = reverseA(readData('existing','req*24'),xStretch,yStretch);
% A obtained by reversing all original Gendreau data
    AT = reverseA(readData('existing','req*'),xStretch,yStretch);
% A obtained by reversing simulations generated by me
    myA = reverseA(readData('output','*req*'),xStretch,yStretch);

figure(1)
subplot(1,2,1)
    surf(AT)
    title('Input activation matrix for generating my data');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')
subplot(1,2,2)
    surf(myA)
    title('Activation matrix found by reversing my data');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')

figure(2)
subplot(1,3,1)
    surf(A33)
    title('Activation matrix found by reversing *33 Gendreau data');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')
subplot(1,3,2)
    surf(A24)
    title('Activation matrix found by reversing *24 Gendreau data');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')
subplot(1,3,3)
    surf(AT)
    title('Activation matrix found by reversing all Gendreau data');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')