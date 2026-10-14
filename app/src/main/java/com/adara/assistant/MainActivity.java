package com.adara.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private static final int REQ_MIC=71, REQ_ASSISTANT=72, REQ_DIALER=73, REQ_OVERLAY=74, REQ_CALL=75;
    private String pendingCallNumber;
    private TextView statusView;
    private EditText commandInput;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private Voice offlineVoice;
    private boolean ttsReady;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        buildUi(); initOfflineTts(); handleDialIntent(getIntent());
        if (getIntent().getBooleanExtra("start_voice", false)) startOfflineDictation();
    }
    private void handleDialIntent(Intent intent) {
        if (intent == null || (!Intent.ACTION_DIAL.equals(intent.getAction()) && !Intent.ACTION_CALL_BUTTON.equals(intent.getAction()))) return;
        Uri uri = intent.getData();
        String number = uri == null ? "" : uri.getSchemeSpecificPart();
        if (number != null && !number.trim().isEmpty()) { commandInput.setText("call " + Uri.decode(number)); status("Dial request received. Press RUN COMMAND; ADARA will ask before placing it."); }
        else status("Dialer opened. Type `call <number>` and press RUN COMMAND.");
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleDialIntent(intent); if (intent.getBooleanExtra("start_voice", false)) startOfflineDictation(); }

    private void buildUi() {
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(20),dp(24),dp(20),dp(28)); page.setBackgroundColor(Color.BLACK); scroll.addView(page);
        page.addView(label("ADARA // ACTUALLY DECENT AND REASONABLE ASSISTANT",19,true));
        TextView sub=label("LOCAL-FIRST CONTROL SURFACE · NO ACCOUNT · NO TELEMETRY",11,false); sub.setPadding(0,dp(8),0,dp(18)); page.addView(sub);
        commandInput=new EditText(this); commandInput.setMinLines(2); commandInput.setTextColor(Color.WHITE); commandInput.setHintTextColor(0xFF8B5961); commandInput.setHint("Try: navigate library / play / read notifications"); commandInput.setTypeface(Typeface.MONOSPACE); commandInput.setPadding(dp(12),dp(9),dp(12),dp(9)); commandInput.setBackground(stroke()); page.addView(commandInput,new LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT));
        addButton(page,"> RUN COMMAND",this::submitTypedCommand); addButton(page,"> OFFLINE DICTATION",this::startOfflineDictation); addButton(page,"> TEST OFFLINE TTS",this::speakLocalTest);
        divider(page,"SYSTEM ROLES / USER-GRANTED ACCESS");
        addButton(page,"Set as Android assistant",this::requestAssistantRole); addButton(page,"Set as default phone app",this::requestDialerRole);
        addButton(page,"Enable Accessibility controls",()->openSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        addButton(page,"Enable notification access",()->openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)); addButton(page,"Floating terminal overlay",this::toggleOverlay);
        divider(page,"CURRENT SESSION"); statusView=label("> READY. No background microphone; no network permission.",12,false); page.addView(statusView); setContentView(scroll);
    }
    private TextView label(String text,int size,boolean bold){TextView t=new TextView(this);t.setText(text);t.setTextColor(0xFFFF304F);t.setTextSize(size);t.setTypeface(Typeface.MONOSPACE,bold?Typeface.BOLD:Typeface.NORMAL);return t;}
    private void divider(LinearLayout p,String s){TextView t=label("\n[ "+s+" ]",11,true);t.setPadding(0,dp(14),0,dp(6));p.addView(t);}
    private void addButton(LinearLayout p,String title,Runnable run){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextColor(0xFFFF526B);b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(12),0,dp(12),0);b.setBackground(stroke());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(50));lp.topMargin=dp(7);p.addView(b,lp);b.setOnClickListener(v->run.run());}
    private GradientDrawable stroke(){GradientDrawable d=new GradientDrawable();d.setColor(0xFF050505);d.setCornerRadius(dp(5));d.setStroke(dp(1),0xFF9E2034);return d;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private void status(String s){if(statusView!=null)statusView.setText("> "+s);}

    private void initOfflineTts(){tts=new TextToSpeech(this,result->{if(result!=TextToSpeech.SUCCESS){status("TTS engine unavailable.");return;}ttsReady=true;Set<Voice> voices=tts.getVoices();if(voices!=null){String lang=Locale.getDefault().getLanguage();for(Voice v:voices)if(!v.isNetworkConnectionRequired()&&v.getLocale()!=null&&v.getLocale().getLanguage().equals(lang)){offlineVoice=v;break;}if(offlineVoice==null)for(Voice v:voices)if(!v.isNetworkConnectionRequired()){offlineVoice=v;break;}}if(offlineVoice!=null)tts.setVoice(offlineVoice);status(offlineVoice==null?"No installed offline TTS voice. No network fallback.":"Offline TTS ready: "+offlineVoice.getName());});}
    private void speakLocalTest(){if(!ttsReady||offlineVoice==null||tts==null){status("No installed non-network TTS voice; refusing online fallback.");return;}tts.setVoice(offlineVoice);tts.speak("ADARA. Offline voice test.",TextToSpeech.QUEUE_FLUSH,null,"adara-local-test");status("Speaking with an installed voice marked non-network.");}

    private void startOfflineDictation(){if(Build.VERSION.SDK_INT<31){status("On-device speech recognition needs Android 12/API 31+.");return;}if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC);return;}if(!SpeechRecognizer.isOnDeviceRecognitionAvailable(this)){status("No on-device recognizer/language pack. No cloud fallback.");return;}try{if(recognizer!=null){recognizer.destroy();recognizer=null;}recognizer=SpeechRecognizer.createOnDeviceSpeechRecognizer(this);recognizer.setRecognitionListener(new RecognitionListener(){
        @Override public void onReadyForSpeech(Bundle b){status("Listening once on-device. No wake-word service.");}@Override public void onBeginningOfSpeech(){}@Override public void onRmsChanged(float v){}@Override public void onBufferReceived(byte[] b){}@Override public void onEndOfSpeech(){status("Transcribing locally…");}@Override public void onError(int e){status("Offline dictation failed ("+e+"). No network fallback.");}
        @Override public void onResults(Bundle b){ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a==null||a.isEmpty()){status("No transcript returned.");return;}commandInput.setText(a.get(0));status("On-device transcript ready. Review, then press RUN COMMAND.");}@Override public void onPartialResults(Bundle b){}@Override public void onEvent(int e,Bundle b){}
    });Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag());i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);recognizer.startListening(i);}catch(RuntimeException e){status("Could not start on-device speech: "+e.getClass().getSimpleName());}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_MIC){if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startOfflineDictation();else status("Microphone permission not granted. Typing remains available.");}else if(r==REQ_CALL){if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)placeConfirmedCall();else{pendingCallNumber=null;status("Call permission denied; no call placed.");}}}
    private void submitTypedCommand(){String s=commandInput.getText().toString().trim();if(!s.isEmpty())runCommand(s);}

    private void runCommand(String c){String l=c.trim().toLowerCase(Locale.ROOT);try{
        if(l.startsWith("call ")){startPhoneRequest(c.trim().substring(5).trim());}
        else if(l.startsWith("navigate ")||l.startsWith("directions to ")){String place=c.trim().replaceFirst("(?i)^(navigate|directions to)\\s+","").trim();startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q="+Uri.encode(place))));status("Handed destination to maps: "+place);}
        else if(l.equals("play")||l.equals("pause")||l.equals("play/pause")||l.equals("next")||l.equals("previous")){int key=l.equals("next")?KeyEvent.KEYCODE_MEDIA_NEXT:l.equals("previous")?KeyEvent.KEYCODE_MEDIA_PREVIOUS:KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;mediaKey(key);status("Sent media key: "+l+". Active player decides what happens.");}
        else if(l.startsWith("tap ")){String target=c.trim().substring(4).trim();new AlertDialog.Builder(this).setTitle("Confirm accessibility tap").setMessage("Tap the visible control whose text exactly matches:\n\n“"+target+"”?").setNegativeButton("Cancel",(d,w)->status("Tap cancelled.")).setPositiveButton("Tap",(d,w)->{boolean ok=AdaraAccessibilityService.clickText(target);status(ok?"Requested exact-text tap: "+target:"No matching accessible clickable text; nothing tapped.");}).show();}
        else if(l.equals("scroll down")||l.equals("scroll up")){boolean ok=AdaraAccessibilityService.scroll(l.equals("scroll down"));status(ok?"Requested one accessible scroll.":"No accessible scrollable control found.");}
        else if(l.equals("answer call")){status(AdaraInCallService.answerActiveCall()?"Answer action sent to current cellular call.":"No active ADARA-controlled call; grant default phone role first.");}
        else if(l.equals("reject call")||l.equals("decline call")){status(AdaraInCallService.rejectActiveCall()?"Reject action sent to current cellular call.":"No active ADARA-controlled call; grant default phone role first.");}
        else if(l.equals("read notifications aloud")){readNotificationsAloud();}else if(l.equals("read notifications")){showNotifications();}
        else if(l.equals("clear notifications")){confirmClearNotifications();}
        else status("Unknown. Try call / navigate / play / pause / next / previous / tap <visible text> / scroll down / answer call / reject call / read notifications / clear notifications.");
    }catch(Exception e){status("Action unavailable: "+e.getClass().getSimpleName());}}
    private void mediaKey(int code){AudioManager a=getSystemService(AudioManager.class);long now=SystemClock.uptimeMillis();a.dispatchMediaKeyEvent(new KeyEvent(now,now,KeyEvent.ACTION_DOWN,code,0));a.dispatchMediaKeyEvent(new KeyEvent(now,now,KeyEvent.ACTION_UP,code,0));}

    private void requestAssistantRole(){if(Build.VERSION.SDK_INT<29){status("Assistant role needs Android 10+.");return;}RoleManager r=getSystemService(RoleManager.class);if(r==null||!r.isRoleAvailable(RoleManager.ROLE_ASSISTANT)){status("Device does not expose assistant role to ADARA.");return;}if(r.isRoleHeld(RoleManager.ROLE_ASSISTANT)){status("ADARA already selected as assistant.");return;}startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT),REQ_ASSISTANT);}
    private void requestDialerRole(){if(Build.VERSION.SDK_INT<29){status("Default dialer role request needs Android 10+.");return;}RoleManager r=getSystemService(RoleManager.class);if(r==null||!r.isRoleAvailable(RoleManager.ROLE_DIALER)){status("Device does not expose the dialer role to ADARA.");return;}if(r.isRoleHeld(RoleManager.ROLE_DIALER)){status("ADARA already selected as phone app.");return;}startActivityForResult(r.createRequestRoleIntent(RoleManager.ROLE_DIALER),REQ_DIALER);}
    private void toggleOverlay(){if(AssistantOverlayService.isShowing()){stopService(new Intent(this,AssistantOverlayService.class));status("Floating overlay stopped.");}else if(Build.VERSION.SDK_INT>=23&&!Settings.canDrawOverlays(this)){startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())),REQ_OVERLAY);}else{startService(new Intent(this,AssistantOverlayService.class));status("Floating overlay requested; Android may stop it in background.");}}
    private void startPhoneRequest(String number) {
        if (number == null || number.trim().isEmpty()) { status("Enter a number after `call`."); return; }
        RoleManager roles = Build.VERSION.SDK_INT >= 29 ? getSystemService(RoleManager.class) : null;
        if (roles == null || !roles.isRoleHeld(RoleManager.ROLE_DIALER)) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)));
            status("Opened the system dialer. Review and confirm there; ADARA did not place the call.");
            return;
        }
        new AlertDialog.Builder(this).setTitle("Confirm cellular call")
            .setMessage("Place a call to " + number + " now?")
            .setNegativeButton("Cancel", (d,w) -> status("Call cancelled."))
            .setPositiveButton("Call", (d,w) -> {
                pendingCallNumber = number;
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
                else placeConfirmedCall();
            }).show();
    }
    private void placeConfirmedCall() {
        String number = pendingCallNumber; pendingCallNumber = null;
        if (number == null) return;
        try { TelecomManager telecom = getSystemService(TelecomManager.class); if (telecom == null) { status("Telecom service unavailable; no call placed."); return; } telecom.placeCall(Uri.fromParts("tel", number, null), Bundle.EMPTY); status("Call handed to Android Telecom."); }
        catch (RuntimeException e) { status("Android could not start the call: " + e.getClass().getSimpleName()); }
    }
    private void openSettings(String action){try{startActivity(new Intent(action));status("Opened Android settings. Enable ADARA there, then return.");}catch(Exception e){status("Settings page unavailable on this device.");}}
    @Override protected void onActivityResult(int r,int result,Intent data){super.onActivityResult(r,result,data);if(r==REQ_ASSISTANT)status(result==RESULT_OK?"Assistant role granted by Android.":"Assistant role not granted.");else if(r==REQ_DIALER)status(result==RESULT_OK?"Phone-app role granted by Android.":"Phone-app role not granted.");else if(r==REQ_OVERLAY&&Build.VERSION.SDK_INT>=23&&Settings.canDrawOverlays(this))toggleOverlay();}
    private void showNotifications(){java.util.List<String> a=NotificationReaderService.snapshot();if(a==null){openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);return;}if(a.isEmpty()){status("No active notifications exposed to ADARA.");return;}new AlertDialog.Builder(this).setTitle("Active notifications · on device").setItems(a.toArray(new String[0]),(d,w)->status("Shown locally; ADARA did not store notification content.")).setPositiveButton("Done",null).show();}
    private void readNotificationsAloud(){java.util.List<String> a=NotificationReaderService.snapshot();if(a==null){openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);return;}if(a.isEmpty()){status("No active notifications exposed to ADARA.");return;}if(!ttsReady||offlineVoice==null||tts==null){status("No installed offline TTS voice; notification text was not spoken.");return;}String text=String.join(". ",a);if(text.length()>4000)text=text.substring(0,4000);final String spoken=text;new AlertDialog.Builder(this).setTitle("Read notifications aloud?").setMessage("The notification text may be audible to people nearby.").setNegativeButton("Cancel",null).setPositiveButton("Read",(d,w)->{tts.setVoice(offlineVoice);tts.speak(spoken,TextToSpeech.QUEUE_FLUSH,null,"adara-notifications-local");status("Reading notification text with installed non-network TTS.");}).show();}
    private void confirmClearNotifications(){new AlertDialog.Builder(this).setTitle("Clear dismissible notifications?").setMessage("Only notifications Android marks clearable will be dismissed; ongoing/system notifications may remain.").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->{Integer n=NotificationReaderService.clearClearable();if(n==null)openSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);else status("Dismissed "+n+" clearable notification(s).");}).show();}
    @Override protected void onDestroy(){if(recognizer!=null){recognizer.cancel();recognizer.destroy();recognizer=null;}if(tts!=null){tts.stop();tts.shutdown();tts=null;}super.onDestroy();}
}
