package com.example.neighbourly

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.example.neighbourly.R

inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.FragmentScenarioEmptyFragmentActivityTheme,
    crossinline action: T.() -> Unit = {}
): FragmentScenario<T> {
    return launchFragmentInContainer(
        fragmentArgs = fragmentArgs,
        themeResId = themeResId
    ) {
        T::class.java.newInstance().apply {
            action(this)
        }
    }
}
