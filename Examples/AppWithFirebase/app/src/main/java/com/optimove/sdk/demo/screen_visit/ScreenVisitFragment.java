package com.optimove.sdk.demo.screen_visit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.optimove.sdk.demo.R;
import com.optimove.sdk.optimove_sdk.main.Optimove;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScreenVisitFragment extends Fragment {

  private TextInputEditText urlInput;
  private TextInputEditText titleInput;
  private TextInputEditText categoryInput;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_screen_visit, container, false);
    urlInput = view.findViewById(R.id.customUrlInput);
    titleInput = view.findViewById(R.id.pageTitleInput);
    categoryInput = view.findViewById(R.id.categoryInput);
    view.findViewById(R.id.setScreenVisitButton).setOnClickListener(v -> setScreenVisit());
    return view;
  }

  public void setScreenVisit() {
    String url = urlInput.getText().toString();
    String title = titleInput.getText().toString();
    String category = categoryInput.getText().toString();

    Optimove.getInstance().setScreenVisit(url, title, category);
  }
}
