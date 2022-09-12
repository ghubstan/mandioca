package mandioca.ioc.example.service;

public class SubtractionCalculatorService implements CalculatorService {

    @Override
    public int calculate(int firstNumber, int secondNumber) {
        return firstNumber - secondNumber;
    }
}
