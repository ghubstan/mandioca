package mandioca.ioc;

import mandioca.ioc.annotation.Inject;
import mandioca.ioc.annotation.Singleton;
import mandioca.ioc.module.SimpleModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;


/**
 * Own Dependency Injection framework, it uses reflection (Constructor, Field) to find the dependency and inject it.
 * Shamelessly copied & modified from https://github.com/zeldan/your-own-dependency-injection-framework
 */
public class DependencyInjectionFramework {

    private final SimpleModule module;


    private final Function<Class<?>, Boolean> isSingleton = (type) -> {
        for (final Constructor<?> constructor : type.getConstructors()) {
            if (constructor.isAnnotationPresent(Singleton.class)) {
                return true;
            }
        }
        return false;
    };

    public DependencyInjectionFramework(SimpleModule module) {
        this.module = module;
    }


    public Object inject(final Class<?> classToInject) {
        try {
            if (classToInject == null) {
                return null;
            }
            return injectFieldsIntoClass(classToInject);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Object injectFieldsIntoClass(final Class<?> classToInject)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (final Constructor<?> constructor : classToInject.getConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                return injectFieldsViaConstructor(classToInject, constructor);
            } else {
                return injectFields(classToInject);
            }
        }
        return null;
    }

    private Object injectFields(Class<?> classToInject)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object o = classToInject.getDeclaredConstructor().newInstance();
        for (Field field : classToInject.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                final Object dependency;
                switch (field.getType().getName()) {
                    case "int":
                    case "java.lang.Integer":
                    case "java.lang.BigInteger":
                    case "java.lang.AtomicInteger":
                    case "double":
                    case "java.lang.Double":
                    case "java.lang.BigDecimal":
                    case "java.lang.Byte":
                    case "float":
                    case "java.lang.Float":
                    case "long":
                    case "java.lang.Long":
                    case "java.lang.AtomicLong":
                    case "java.lang.String":
                        dependency = module.getMapping(field.getName());
                        field.set(o, dependency);
                        break;
                    default:
                        Class<?> fieldType = field.getType();
                        if (isSingleton.apply(fieldType)) {
                            dependency = module.getSingletonMapping(fieldType);
                            field.set(o, dependency);
                        } else {
                            dependency = module.getMapping(fieldType);
                            field.set(o, ((Class<?>) dependency).getConstructor().newInstance());
                        }
                        break;
                }
            }
        }
        return o;
    }

    private Object injectFieldsViaConstructor(Class<?> classToInject, Constructor<?> constructor)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Object[] objArr = new Object[parameterTypes.length];
        int i = 0;
        for (final Class<?> c : parameterTypes) {
            final Class<?> dependency = module.getMapping(c);
            if (c.isAssignableFrom(dependency)) {
                objArr[i++] = dependency.getConstructor().newInstance();
            }
        }
        return classToInject.getConstructor(parameterTypes).newInstance(objArr);
    }
}
