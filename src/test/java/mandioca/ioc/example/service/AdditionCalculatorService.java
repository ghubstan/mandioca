package mandioca.ioc.example.service;

public class AdditionCalculatorService implements CalculatorService {

    @Override
    public int calculate(int firstNumber, int secondNumber) {
        return firstNumber + secondNumber;
    }

}
