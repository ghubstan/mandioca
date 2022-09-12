package mandioca.ioc.example.service;

public class SimpleTextFormatterService implements TextFormatterService {

    @Override
    public String format(String text) {
        return "Simple text: " + text;
    }

}
