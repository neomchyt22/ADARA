package com.adara.assistant;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public final class IncomingCallActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);if(android.os.Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(32,32,32,32);root.setBackgroundColor(Color.BLACK);TextView t=new TextView(this);t.setText("ADARA // INCOMING CALL\n\n"+AdaraInCallService.callerLabel());t.setTextColor(0xFFFF304F);t.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);t.setTextSize(22);root.addView(t);Button a=new Button(this);a.setText("ANSWER");a.setOnClickListener(v->{AdaraInCallService.answerActiveCall();finish();});root.addView(a);Button d=new Button(this);d.setText("DECLINE");d.setOnClickListener(v->{AdaraInCallService.rejectActiveCall();finish();});root.addView(d);setContentView(root);}
}
