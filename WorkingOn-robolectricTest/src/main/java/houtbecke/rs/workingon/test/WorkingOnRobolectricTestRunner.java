package houtbecke.rs.workingon.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

public class WorkingOnRobolectricTestRunner extends RobolectricTestRunner {

    public WorkingOnRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        System.out.println("testClass:" + testClass.getName());
        System.setProperty("testClass", testClass.getName());


    }
}