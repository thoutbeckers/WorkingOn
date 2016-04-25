package houtbecke.rs.workingon.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;

public class WorkingOnRobolectricTestRunner extends RobolectricGradleTestRunner {

    public WorkingOnRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        System.out.println("testClass:" + testClass.getName());
        System.setProperty("testClass", testClass.getName());


    }
}