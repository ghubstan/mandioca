package mandioca.ioc.example;

import mandioca.ioc.annotation.Inject;
import mandioca.ioc.example.service.CalculatorService;
import mandioca.ioc.example.service.TextFormatterService;

public class FieldInjectionExample {

    @Inject
    private CalculatorService calculatorService;

    @Inject
    private TextFormatterService textFormatterService;

    public String processNumbers(int firstNumber, int secondNumber) {
        int number = calculatorService.calculate(firstNumber, secondNumber);
        return textFormatterService.format(String.valueOf(number));
    }
}
