package mandioca.ioc;


import mandioca.ioc.module.SimpleModule;

/**
 * Initializes dependency injection configuration based on the configuration module; it binds interfaces to implementations.
 * Shamelessly copied & modified from https://github.com/zeldan/your-own-dependency-injection-framework
 */
public class DependencyInjectionConfig {

    public DependencyInjectionConfig() {
    }

    public static DependencyInjectionFramework getFramework(final SimpleModule module) {
        module.configure();
        return new DependencyInjectionFramework(module);
    }
}
