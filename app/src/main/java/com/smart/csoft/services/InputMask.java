package com.smart.csoft.services;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by umprasad on 12/24/2017.
 */

public class InputMask implements TextWatcher {

    private boolean isRunning = false;
    private boolean isDeleting = false;
    private String mask;

    public InputMask(String mask) {
        this.mask = mask;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        isDeleting = count > after;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable != null) {
            int editableLength = editable.length();
            if (editableLength < mask.length()) {
                if (mask.charAt(editableLength) != '#') {
                    editable.append(mask.charAt(editableLength));
                } else if (mask.charAt(editableLength - 1) != '#') {
                    editable.insert(editableLength - 1, mask, editableLength - 1, editableLength);
                }
            }
        }
    }
}
