package org.ghostsinthelab.app.simpletungshing

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * The template instrumented test, kept so `connectedAndroidTest` has something to run.
 * It only confirms the test APK targets this package; widget and Compose behaviour is
 * verified by hand on a device (see CLAUDE.md), not by instrumented tests.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("org.ghostsinthelab.app.simpletungshing", appContext.packageName)
    }
}