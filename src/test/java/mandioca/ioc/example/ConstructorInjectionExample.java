package mandioca.ioc.example;

import mandioca.ioc.annotation.Inject;
import mandioca.ioc.example.service.CalculatorService;
import mandioca.ioc.example.service.TextFormatterService;

public class ConstructorInjectionExample {

    private final CalculatorService calculatorService;
    private final TextFormatterService textFormatterService;

    @Inject
    public ConstructorInjectionExample(CalculatorService calculatorService, TextFormatterService textFormatterService) {
        this.calculatorService = calculatorService;
        this.textFormatterService = textFormatterService;
    }

    public String processNumbers(int firstNumber, int secondNumber) {
        int number = calculatorService.calculate(firstNumber, secondNumber);
        return textFormatterService.format(String.valueOf(number));
    }
}
