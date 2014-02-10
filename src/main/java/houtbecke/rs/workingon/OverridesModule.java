package houtbecke.rs.workingon;

import com.google.inject.Module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * This annotation indicates that this Module should be used to override another Module
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OverridesModule {
    /**
     * @return The Class of the Module to override
     */

    Class<? extends Module> value(); // TODO return default for implementing default behaviour: default Module.class;
}
