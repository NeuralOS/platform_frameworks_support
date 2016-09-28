/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.v4.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.fragment.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.test.FragmentTestActivity;
import android.support.v4.app.test.FragmentTestActivity.TestFragment;
import android.support.v4.view.ViewCompat;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class FragmentTransitionTest {
    @Rule
    public ActivityTestRule<FragmentTestActivity> mActivityRule =
            new ActivityTestRule<FragmentTestActivity>(FragmentTestActivity.class);

    private TestFragment mStartFragment;
    private TestFragment mMidFragment;
    private TestFragment mEndFragment;
    private FragmentTestActivity mActivity;
    private Instrumentation mInstrumentation;

    @Before
    public void setup() {
        mActivity = mActivityRule.getActivity();
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
    }

    @Test
    public void testFragmentTransition() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        launchStartFragment();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View sharedElement = mActivity.findViewById(R.id.hello);
                assertEquals("source", ViewCompat.getTransitionName(sharedElement));

                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mEndFragment)
                        .addSharedElement(sharedElement, "destination")
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mEndFragment, TestFragment.ENTER);
        assertTrue(mEndFragment.wasEndCalled(TestFragment.ENTER));
        assertTrue(mStartFragment.wasEndCalled(TestFragment.EXIT));
        assertTrue(mEndFragment.wasEndCalled(TestFragment.SHARED_ELEMENT_ENTER));
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View textView = mActivity.findViewById(R.id.hello);
                assertEquals("destination", ViewCompat.getTransitionName(textView));
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mStartFragment, TestFragment.REENTER);
        assertTrue(mStartFragment.wasEndCalled(TestFragment.REENTER));
        assertTrue(mEndFragment.wasEndCalled(TestFragment.RETURN));
    }

    @Test
    public void testFirstOutLastInTransition() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        launchStartFragment();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMidFragment = TestFragment.create(R.layout.fragment_middle);
                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mMidFragment)
                        .replace(R.id.content, mEndFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mEndFragment, TestFragment.ENTER);
        assertTrue(mEndFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.REENTER));

        assertTrue(mStartFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.REENTER));

        assertFalse(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        mStartFragment.clearNotifications();
        mEndFragment.clearNotifications();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mEndFragment, TestFragment.RETURN);
        assertTrue(mEndFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        assertTrue(mStartFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.RETURN));
    }

    @Test
    public void testPopTwo() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        launchStartFragment();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMidFragment = TestFragment.create(R.layout.fragment_middle);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mMidFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mMidFragment, TestFragment.ENTER);
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mEndFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForEnd(mEndFragment, TestFragment.ENTER);
        assertTrue(mEndFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.REENTER));

        assertTrue(mStartFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.REENTER));

        assertTrue(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertTrue(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        mStartFragment.clearNotifications();
        mMidFragment.clearNotifications();
        mEndFragment.clearNotifications();

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = mActivity.getSupportFragmentManager();
                int id = fm.getBackStackEntryAt(0).getId();
                fm.popBackStack(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.executePendingTransactions();
            }
        });
        waitForEnd(mEndFragment, TestFragment.RETURN);
        assertTrue(mEndFragment.wasEndCalled(TestFragment.RETURN));

        assertFalse(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        assertTrue(mStartFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.RETURN));
    }

    @Test
    public void testNullTransition() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        mInstrumentation.waitForIdleSync();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartFragment = TestFragment.create(R.layout.fragment_start);
                mStartFragment.clearTransitions();
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mStartFragment)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForStart(mStartFragment, TestFragment.ENTER);
        // No transitions
        assertFalse(mStartFragment.wasStartCalled(TestFragment.ENTER));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMidFragment = TestFragment.create(R.layout.fragment_middle);
                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mEndFragment.clearTransitions();
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mMidFragment)
                        .replace(R.id.content, mEndFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForStart(mEndFragment, TestFragment.ENTER);
        assertFalse(mEndFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mEndFragment.wasEndCalled(TestFragment.REENTER));

        assertFalse(mStartFragment.wasEndCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mStartFragment.wasEndCalled(TestFragment.REENTER));

        assertFalse(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForStart(mEndFragment, TestFragment.RETURN);
        assertFalse(mEndFragment.wasEndCalled(TestFragment.RETURN));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mMidFragment.wasStartCalled(TestFragment.RETURN));

        assertFalse(mStartFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.RETURN));
    }

    @Test
    public void testRemoveAdded() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        launchStartFragment();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mEndFragment)
                        .replace(R.id.content, mStartFragment)
                        .replace(R.id.content, mEndFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        assertTrue(waitForEnd(mEndFragment, TestFragment.ENTER));
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        assertTrue(waitForEnd(mStartFragment, TestFragment.REENTER));
    }

    @Test
    public void testAddRemoved() throws Throwable {
        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            return;
        }
        launchStartFragment();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mEndFragment = TestFragment.create(R.layout.fragment_end);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mEndFragment)
                        .replace(R.id.content, mStartFragment)
                        .addToBackStack(null)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForStart(mStartFragment, TestFragment.ENTER);
        assertFalse(mStartFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.EXIT));
        assertFalse(mEndFragment.wasStartCalled(TestFragment.ENTER));
        assertFalse(mEndFragment.wasStartCalled(TestFragment.EXIT));
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getSupportFragmentManager().popBackStack();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        waitForStart(mStartFragment, TestFragment.REENTER);
        assertFalse(mStartFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mStartFragment.wasStartCalled(TestFragment.RETURN));
        assertFalse(mEndFragment.wasStartCalled(TestFragment.REENTER));
        assertFalse(mEndFragment.wasStartCalled(TestFragment.RETURN));
    }

    private void launchStartFragment() throws Throwable {
        mInstrumentation.waitForIdleSync();
        mActivityRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStartFragment = TestFragment.create(R.layout.fragment_start);
                mActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, mStartFragment)
                        .commit();
                mActivity.getSupportFragmentManager().executePendingTransactions();
            }
        });
        assertTrue(waitForEnd(mStartFragment, TestFragment.ENTER));
        mStartFragment.clearNotifications();
    }

    private boolean waitForStart(TestFragment fragment, int key) throws InterruptedException {
        boolean started = fragment.waitForStart(key);
        mInstrumentation.waitForIdleSync();
        return started;
    }

    private boolean waitForEnd(TestFragment fragment, int key) throws InterruptedException {
        if (!waitForStart(fragment, key)) {
            return false;
        }
        final boolean ended = fragment.waitForEnd(key);
        mInstrumentation.waitForIdleSync();
        return ended;
    }
}
