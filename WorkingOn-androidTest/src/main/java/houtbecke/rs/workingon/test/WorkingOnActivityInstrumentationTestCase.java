package houtbecke.rs.workingon.test;

import houtbecke.rs.workingon.WorkingOn;
import roboguice.RoboGuice;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

public class WorkingOnActivityInstrumentationTestCase<T extends Activity> extends ActivityInstrumentationTestCase2<T> {

	public WorkingOnActivityInstrumentationTestCase(Class<T> activityClass) {
		super(activityClass);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        WorkingOn.configureTestTasks(this.getClass());
		T activity = getActivity();
		RoboGuice.injectMembers(activity, this);
	}

	@Override
	public void tearDown() throws Exception {
		getActivity().finish();
		super.tearDown();
	}
	
}
