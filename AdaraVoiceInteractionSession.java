package com.adara.assistant;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
public final class AdaraVoiceInteractionSession extends VoiceInteractionSession {
    private final Context appContext;
    public AdaraVoiceInteractionSession(Context context) { super(context); appContext=context.getApplicationContext(); }
    @Override public void onShow(Bundle args,int showFlags) { super.onShow(args,showFlags); Intent i=new Intent(appContext,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("start_voice",true); appContext.startActivity(i); hide(); }
}
