package com.optimove.sdk.demo.set_user_id;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.optimove.sdk.demo.R;
import com.optimove.sdk.optimove_sdk.main.Optimove;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SetUserIdFragment extends Fragment {

  private EditText userNameInputEditText;
  private TextView emailInputEditText;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_set_user_id, container, false);

    userNameInputEditText = view.findViewById(R.id.userNameInputEditText);
    emailInputEditText = view.findViewById(R.id.emailInputEditText);

    view.findViewById(R.id.setUserIdButton).setOnClickListener(btn -> {
      String userId = userNameInputEditText.getText().toString();
      Optimove.getInstance().setUserId(userId);
    });

    view.findViewById(R.id.setEmailButton).setOnClickListener(btn -> {
      String email = emailInputEditText.getText().toString();
      Optimove.getInstance().setUserEmail(email);
    });

    view.findViewById(R.id.registerUserButton).setOnClickListener(btn -> {
      String userId = userNameInputEditText.getText().toString();
      String email = emailInputEditText.getText().toString();
      Optimove.getInstance().registerUser(userId, email);
    });

    return view;
  }
}
