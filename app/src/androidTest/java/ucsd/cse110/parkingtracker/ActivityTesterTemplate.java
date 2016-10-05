package ucsd.cse110.parkingtracker;

import android.test.ActivityInstrumentationTestCase2;

public class ActivityTesterTemplate extends ActivityInstrumentationTestCase2<LoginActivity> {

    public ActivityTesterTemplate() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp()  throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        // Create objects, pass an initial intent, etc.
    }

    public void testPreconditions() {
        // Test initial state of activity
    }

    public void test_MethodName_MethodParameters() {
        // Test a method with the given method parameters
    }

}
