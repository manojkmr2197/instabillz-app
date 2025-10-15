package com.app.billing.instabillz.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MoveNextWatcher implements TextWatcher {
    private final EditText current;
    private final EditText next;

    public MoveNextWatcher(EditText current, EditText next) {
        this.current = current;
        this.next = next;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 1 && next != null) {
            next.requestFocus();
        }

    }
}

