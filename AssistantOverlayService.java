package com.adara.assistant;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
public final class AssistantOverlayService extends Service {
    private static volatile boolean showing;
    private WindowManager manager; private TextView bubble;
    public static boolean isShowing(){return showing;}
    @Override public int onStartCommand(Intent i,int flags,int id){if(android.os.Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}if(showing)return START_STICKY;manager=getSystemService(WindowManager.class);bubble=new TextView(this);bubble.setText("ADARA");bubble.setTextColor(0xFFFF526B);bubble.setTextSize(12);bubble.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);bubble.setGravity(Gravity.CENTER);bubble.setPadding(18,14,18,14);GradientDrawable bg=new GradientDrawable();bg.setColor(Color.BLACK);bg.setCornerRadius(30);bg.setStroke(2,0xFFFF304F);bubble.setBackground(bg);bubble.setOnClickListener(v->startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP)));WindowManager.LayoutParams p=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.END;p.x=18;p.y=120;try{manager.addView(bubble,p);showing=true;}catch(RuntimeException e){stopSelf();}return START_STICKY;}
    @Override public void onDestroy(){if(manager!=null&&bubble!=null)try{manager.removeView(bubble);}catch(RuntimeException ignored){}showing=false;super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
