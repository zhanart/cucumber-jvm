package cucumber.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to give additional hints to the {@link Cucumber} runner
 * about what to run. The various values are similar to what's accepted by {@link cucumber.cli.Main}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Options {
    /**
     * @return the path to the .feature
     */
    String value();

    /**
     * @return what lines in the feature should be executed
     */
    long[] lines() default {};

    /**
     * @return what tags in the feature should be executed
     */
    String[] tags() default {};

    /**
     * @return where to look for glue code (stepdefs and hooks)
     */
    String[] glue() default {};
}
