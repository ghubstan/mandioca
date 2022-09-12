package mandioca.ioc.example.service;

public class PrettyTextFormatterService implements TextFormatterService {

    @Override
    public String format(String text) {
        return "Pretty text: <" + text + ">";
    }
}
