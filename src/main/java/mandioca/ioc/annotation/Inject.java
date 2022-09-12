package mandioca.ioc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation on constructor to indicate to the framework to inject the proper implementation.
 * Shamelessly copied & modified from https://github.com/zeldan/your-own-dependency-injection-framework
 */
@Target({CONSTRUCTOR, FIELD})
@Retention(RUNTIME)
@Documented
public @interface Inject {

}
