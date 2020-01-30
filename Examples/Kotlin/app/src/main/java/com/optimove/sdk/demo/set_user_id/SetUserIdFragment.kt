package com.optimove.sdk.demo.set_user_id

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

import com.optimove.sdk.demo.R
import com.optimove.sdk.optimove_sdk.main.Optimove
import androidx.fragment.app.Fragment

class SetUserIdFragment : Fragment() {

    private var userNameInputEditText: EditText? = null
    private var emailInputEditText: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_set_user_id, container, false)

        userNameInputEditText = view.findViewById(R.id.userNameInputEditText)
        emailInputEditText = view.findViewById(R.id.emailInputEditText)

        view.findViewById<View>(R.id.setUserIdButton).setOnClickListener { btn ->
            val userId = userNameInputEditText!!.text.toString()
            Optimove.getInstance().setUserId(userId)
        }

        view.findViewById<View>(R.id.setEmailButton).setOnClickListener { btn ->
            val email = emailInputEditText!!.text.toString()
            Optimove.getInstance().setUserEmail(email)
        }

        view.findViewById<View>(R.id.registerUserButton).setOnClickListener { btn ->
            val userId = userNameInputEditText!!.text.toString()
            val email = emailInputEditText!!.text.toString()
            Optimove.getInstance().registerUser(userId, email)
        }

        return view
    }
}
