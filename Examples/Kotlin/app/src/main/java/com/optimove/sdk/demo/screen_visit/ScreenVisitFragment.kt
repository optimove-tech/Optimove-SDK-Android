package com.optimove.sdk.demo.screen_visit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.textfield.TextInputEditText
import com.optimove.sdk.demo.R
import com.optimove.sdk.optimove_sdk.main.Optimove
import androidx.fragment.app.Fragment

class ScreenVisitFragment : Fragment() {

    private var urlInput: TextInputEditText? = null
    private var titleInput: TextInputEditText? = null
    private var categoryInput: TextInputEditText? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_screen_visit, container, false)
        urlInput = view.findViewById(R.id.customUrlInput)
        titleInput = view.findViewById(R.id.pageTitleInput)
        categoryInput = view.findViewById(R.id.categoryInput)
        view.findViewById<View>(R.id.setScreenVisitButton).setOnClickListener { v -> setScreenVisit() }
        return view
    }

    fun setScreenVisit() {
        val url = urlInput!!.text!!.toString()
        val title = titleInput!!.text!!.toString()
        val category = categoryInput!!.text!!.toString()

        Optimove.getInstance().setScreenVisit(url, title, category)
    }
}
