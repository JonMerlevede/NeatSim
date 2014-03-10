Ax = reverseA(readData('existing','req*'),4,5);
Ay = reverseA(readData('existing','req*'),5,4);

subplot(1,2,1)
    surf(Ax)
    title('Activation matrix using 4 horizontal and 5 vertical zones');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')
subplot(1,2,2)
    surf(Ay)
    title('Activation matrix using 5 horizontal and 4 vertical zones');
    xlabel('y-coordinate'), ylabel('x-coordinate'), zlabel('frequency')

