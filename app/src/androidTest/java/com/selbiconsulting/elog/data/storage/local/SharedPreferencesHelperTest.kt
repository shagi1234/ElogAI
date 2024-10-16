package com.selbiconsulting.elog.data.storage.local

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class SharedPreferencesHelperTest {

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        val context = mock(Context::class.java)

        sharedPreferencesHelper = SharedPreferencesHelper(context)

        `when`(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)

        mockStatic(AppCompatDelegate::class.java)
    }


    @Test
    fun testGetAppThemeWhenUserSelectedThemeExists() {
        // Given
        val userSelectedTheme = "DARK"
        `when`(sharedPreferences.getString(SharedPreferencesHelper.KEY_APP_THEME, null)).thenReturn(
            userSelectedTheme
        )

        // When
        val result = sharedPreferencesHelper.getAppTheme()

        // Then
        assertEquals(userSelectedTheme, result)
    }

    @Test
    fun testGetAppThemeWhenUserSelectedThemeDoesNotExist() {
        // Given
        val systemDefaultTheme = "LIGHT"
        `when`(sharedPreferences.getString(SharedPreferencesHelper.KEY_APP_THEME, null)).thenReturn(
            null
        )
        `when`(AppCompatDelegate.getDefaultNightMode()).thenReturn(AppCompatDelegate.MODE_NIGHT_NO)

        // When
        val result = sharedPreferencesHelper.getAppTheme()

        // Then
        assertEquals(systemDefaultTheme, result)
    }

    // Similar tests for other methods can be written similarly
}