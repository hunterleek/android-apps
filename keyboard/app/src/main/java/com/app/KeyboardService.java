package com.app;

import android.inputmethodservice.InputMethodService;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class KeyboardService extends InputMethodService {
    @Override
    public View onCreateInputView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF000000);
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (String row : rows) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (char c : row.toCharArray()) {
                Button btn = new Button(this);
                btn.setText(String.valueOf(c));
                btn.setTextColor(0xFFFFFFFF);
                btn.setBackgroundColor(0xFF333333);
                btn.setOnClickListener(v -> getCurrentInputConnection().commitText(String.valueOf(c), 1));
                rowLayout.addView(btn);
            }
            layout.addView(rowLayout);
        }
        return layout;
    }
}
