package com.example.appm_trilheiros.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Fragment.hideKeyboard() {

    view?.let { v ->

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }
}
