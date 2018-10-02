package houtbecke.rs.workingon;

import android.support.v4.app.Fragment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ ElementType.TYPE })
public @interface WorkingOnFragment {
    Class<? extends Fragment> value();
}
