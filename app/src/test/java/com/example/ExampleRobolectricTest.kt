package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GlassMaterialPreset
import com.example.data.GlassPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Liquid Glass", appName)
    }

    @Test
    fun `verify glass material preset defaults`() {
        val preset = GlassMaterialPreset.fromId("cyber_iridescent")
        assertEquals(GlassMaterialPreset.CYBER_IRIDESCENT, preset)
        assertNotNull(preset.displayName)
        assertTrue(preset.primaryColorHex != 0L)
    }

    @Test
    fun `verify glass preferences model defaults`() {
        val prefs = GlassPreferences()
        assertTrue(prefs.masterEnabled)
        assertEquals(GlassMaterialPreset.CYBER_IRIDESCENT, prefs.preset)
        assertTrue(prefs.transparency in 0.1f..0.9f)
        assertTrue(prefs.cornerRadius > 0f)
    }
}
