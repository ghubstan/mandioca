package mandioca.ioc.module;

/**
 * SimpleModule interface to configure dependency injection mapping.
 * Shamelessly copied & modified from https://github.com/zeldan/your-own-dependency-injection-framework
 */
public interface SimpleModule {

    void configure();

    Object getSingletonMapping(Class<?> type);

    Object getMapping(final String fieldName);

    <T> Class<? extends T> getMapping(Class<T> type);
}