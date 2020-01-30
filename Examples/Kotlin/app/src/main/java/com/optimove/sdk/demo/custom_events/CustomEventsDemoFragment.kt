package com.optimove.sdk.demo.custom_events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.material.textfield.TextInputEditText
import com.optimove.sdk.demo.R
import com.optimove.sdk.optimove_sdk.main.Optimove
import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent

import java.util.HashMap
import androidx.fragment.app.Fragment

class CustomEventsDemoFragment : Fragment() {

    private var eventNameInput: TextInputEditText? = null
    private var eventParamInput: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_events_demo, container, false)
        view.findViewById<View>(R.id.reportEventButton).setOnClickListener { v -> reportEvent() }
        eventNameInput = view.findViewById(R.id.eventDemoNameInput)
        eventParamInput = view.findViewById(R.id.eventDemoParamInput)
        return view
    }

    fun reportEvent() {
        val eventName = eventNameInput!!.text!!.toString()
        val eventParams = HashMap<String, Any>()
        eventParams["param_key"] = this.eventParamInput!!.text!!.toString()

        // Report Simple Events
        Optimove.getInstance().reportEvent(eventName, eventParams)
        // Report Complex Events
        Optimove.getInstance().reportEvent(ComplexCustomEvent("some value"))
    }


    private class ComplexCustomEvent(private val someValue: String) : OptimoveEvent {

        override fun getName(): String {
            return "complex_event_example"
        }

        override fun getParameters(): Map<String, Any> {
            val eventParams = HashMap<String, Any>()
            eventParams["remote_param_key"] = this.someValue
            return eventParams
        }
    }
}
