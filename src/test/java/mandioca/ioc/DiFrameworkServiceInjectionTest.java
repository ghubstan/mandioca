package mandioca.ioc;

import mandioca.ioc.example.ConstructorInjectionExample;
import mandioca.ioc.example.FieldInjectionExample;
import mandioca.ioc.example.service.*;
import mandioca.ioc.module.AbstractSimpleModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DiFrameworkServiceInjectionTest {

    @Test
    public void shouldInjectPrettyTextFormatterAndAdditionCalculatorViaFieldInjection() throws Exception {
        DependencyInjectionFramework diFramework = DependencyInjectionConfig.getFramework(new DependencyInjectionConfigExample1());
        FieldInjectionExample example = (FieldInjectionExample) diFramework.inject(FieldInjectionExample.class);

        String processNumbers = example.processNumbers(3, 2);

        assertEquals("Pretty text: <5>", processNumbers);
    }

    @Test
    public void shouldInjectSimpleTextFormatterAndAdditionCalculatorViaFieldInjection() throws Exception {
        DependencyInjectionFramework diFramework = DependencyInjectionConfig.getFramework(new DependencyInjectionConfigExample2());
        FieldInjectionExample example = (FieldInjectionExample) diFramework.inject(FieldInjectionExample.class);

        String processNumbers = example.processNumbers(3, 2);

        assertEquals("Simple text: 5", processNumbers);
    }

    @Test
    public void shouldInjectSimpleTextFormatterAndSubtractionCalculatorViaConstructorInjection() throws Exception {
        DependencyInjectionFramework diFramework = DependencyInjectionConfig.getFramework(new DependencyInjectionConfigExample3());
        ConstructorInjectionExample example = (ConstructorInjectionExample) diFramework.inject(ConstructorInjectionExample.class);

        String processNumbers = example.processNumbers(3, 2);

        assertEquals("Simple text: 1", processNumbers);
    }

    private class DependencyInjectionConfigExample1 extends AbstractSimpleModule {

        @Override
        public void configure() {
            createMapping(CalculatorService.class, AdditionCalculatorService.class);
            createMapping(TextFormatterService.class, PrettyTextFormatterService.class);
        }
    }

    private class DependencyInjectionConfigExample2 extends AbstractSimpleModule {

        @Override
        public void configure() {
            createMapping(CalculatorService.class, AdditionCalculatorService.class);
            createMapping(TextFormatterService.class, SimpleTextFormatterService.class);
        }
    }

    private class DependencyInjectionConfigExample3 extends AbstractSimpleModule {

        @Override
        public void configure() {
            createMapping(CalculatorService.class, SubtractionCalculatorService.class);
            createMapping(TextFormatterService.class, SimpleTextFormatterService.class);
        }
    }

}
