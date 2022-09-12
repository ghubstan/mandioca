package mandioca.ioc.module;


import java.util.HashMap;
import java.util.Map;

/**
 * Abstract module to configure and retrieve proper dependency injection mapping.
 * Shamelessly copied & modified from https://github.com/zeldan/your-own-dependency-injection-framework
 */
public abstract class AbstractSimpleModule implements SimpleModule {

    private final Map<Class<?>, Object> singletonMap = new HashMap<>();

    protected <T> void createSingletonMapping(final Class<T> singletonClass, final Object value) {
        if (!singletonMap.containsKey(singletonClass)) {
            singletonMap.put(singletonClass, value);
        } else {
            throw new RuntimeException("attempt to cache singleton object already present in object map");
        }
    }

    @Override
    public Object getSingletonMapping(final Class<?> type) {
        final Object singleton = singletonMap.get(type);
        if (singleton == null) {
            throw new IllegalArgumentException("Couldn't find the mapped singleton object for " + type);
        }
        return singleton;
    }


    private final Map<String, Object> objectMap = new HashMap<>();

    protected void createMapping(final String fieldName, final Object value) {
        objectMap.put(fieldName, value);
    }

    @Override
    public Object getMapping(final String fieldName) {
        final Object value = objectMap.get(fieldName);
        if (value == null) {
            throw new IllegalArgumentException("Couldn't find value mapping for field " + fieldName);
        }
        return value;
    }

    private final Map<Class<?>, Class<?>> classMap = new HashMap<>();

    protected <T> void createMapping(final Class<T> baseClass, final Class<? extends T> subClass) {
        classMap.put(baseClass, subClass.asSubclass(baseClass));
    }

    @Override
    public <T> Class<? extends T> getMapping(final Class<T> type) {
        final Class<?> implementation = classMap.get(type);
        if (implementation == null) {
            throw new IllegalArgumentException("Couldn't find the mapping (subclass / implementation) for " + type);
        }
        return implementation.asSubclass(type);
    }

}