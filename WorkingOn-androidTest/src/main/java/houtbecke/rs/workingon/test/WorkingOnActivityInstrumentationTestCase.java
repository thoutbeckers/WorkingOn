package houtbecke.rs.workingon.test;

import androidx.test.rule.ActivityTestRule;
import houtbecke.rs.workingon.WorkingOn;
import roboguice.RoboGuice;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;

public class WorkingOnActivityInstrumentationTestCase<T extends Activity> extends ActivityTestRule<T> {

	protected WorkingOnActivityInstrumentationTestCase(Class<T> activityClass) {
		super(activityClass);
	}

	@Before
	protected void setUp() throws Exception {
		WorkingOn.configureTestTasks(this.getClass());
		T activity = getActivity();
		RoboGuice.injectMembers(activity, this);
	}

	@After
	public void tearDown() throws Exception {
		getActivity().finish();
	}

}
