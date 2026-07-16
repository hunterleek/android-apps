package com.app;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Locale;

public class KeyboardService extends InputMethodService {

    private boolean shiftOn = false;
    private boolean symbolsOn = false;
    private InputConnection ic;

    private static final String[][] LETTERS = {
            {"Q","W","E","R","T","Y","U","I","O","P"},
            {"A","S","D","F","G","H","J","K","L"},
            {"SHIFT","Z","X","C","V","B","N","M","BACKSPACE"}
    };

    private static final String[][] SYMBOLS = {
            {"1","2","3","4","5","6","7","8","9","0"},
            {"!","@","#","$","%","&","*","(",")","?"},
            {"ABC","-","+","=","_","/",":",";","BACKSPACE"}
    };

    @Override
    public View onCreateInputView() {
        return buildKeyboard();
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        ic = getCurrentInputConnection();
    }

    private View buildKeyboard() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF000000);
        root.setPadding(8, 16, 8, 16);

        String[][] keys = symbolsOn ? SYMBOLS : LETTERS;
        for (String[] row : keys) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
            for (String key : row) {
                Button btn = makeKey(key);
                rowLayout.addView(btn);
            }
            root.addView(rowLayout);
        }

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));
        bottom.addView(makeKey("?123"));
        bottom.addView(makeKey(","));
        Button space = makeKey("SPACE");
        space.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3.0f));
        bottom.addView(space);
        bottom.addView(makeKey("."));
        bottom.addView(makeKey("ENTER"));
        root.addView(bottom);

        return root;
    }

    private Button makeKey(String label) {
        Button btn = new Button(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        lp.setMargins(4, 4, 4, 4);
        btn.setLayoutParams(lp);
        btn.setText(label);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTextSize(18);
        btn.setBackgroundResource(R.drawable.key_bg);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> onKeyPress(label));
        return btn;
    }

    private void onKeyPress(String label) {
        ic = getCurrentInputConnection();
        if (ic == null) return;

        switch (label) {
            case "BACKSPACE":
                ic.deleteSurroundingText(1, 0);
                break;
            case "SPACE":
                ic.commitText(" ", 1);
                break;
            case "ENTER":
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                break;
            case "SHIFT":
                shiftOn = !shiftOn;
                setInputView(buildKeyboard());
                break;
            case "?123":
            case "ABC":
                symbolsOn = !symbolsOn;
                shiftOn = false;
                setInputView(buildKeyboard());
                break;
            default:
                String text = shiftOn ? label.toUpperCase(Locale.ROOT) : label.toLowerCase(Locale.ROOT);
                if (symbolsOn) text = label;
                ic.commitText(text, 1);
                if (shiftOn) {
                    shiftOn = false;
                    setInputView(buildKeyboard());
                }
                break;
        }
    }
}
