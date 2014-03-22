package com.watcher.car;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
    AlarmReceiver alarm = new AlarmReceiver();
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void saveServerUrl(View view) {
        Editable editText = ((EditText) findViewById(R.id.editText)).getText();
        if (editText != null) {
            String text2 = editText.toString();
            alarm.enableTask(this);
        }
    }

    public void cancelAlarm(View view) {
        alarm.disableTask(this);
    }
}
