package houtbecke.rs.workingon.test;

import houtbecke.rs.workingon.WorkingOn;
import roboguice.RoboGuice;

import android.app.Activity;
import androidx.test.rule.ActivityTestRule;
import android.util.Log;

import org.junit.Before;
import org.junit.After;

public class WorkingOnActivityInstrumentationTestCase<T extends Activity> extends ActivityTestRule<T> {

	protected WorkingOnActivityInstrumentationTestCase(Class<T> activityClass) {
		super(activityClass);
	}

	@Before
	protected void setUp() throws Exception {
        super.setUp();
        WorkingOn.configureTestTasks(this.getClass());
		T activity = getActivity();
		RoboGuice.injectMembers(activity, this);
	}

	@After
	public void tearDown() throws Exception {
		getActivity().finish();
		super.tearDown();
	}
	
}
