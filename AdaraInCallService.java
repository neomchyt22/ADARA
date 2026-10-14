package com.adara.assistant;
import android.content.Intent;
import android.telecom.Call;
import android.telecom.InCallService;
import android.telecom.VideoProfile;
public final class AdaraInCallService extends InCallService {
    private static volatile Call active;
    @Override public void onCallAdded(Call c){super.onCallAdded(c);active=c;if(c.getState()==Call.STATE_RINGING)try{startActivity(new Intent(this,IncomingCallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));}catch(RuntimeException ignored){}}
    @Override public void onCallRemoved(Call c){if(active==c)active=null;super.onCallRemoved(c);}
    public static boolean answerActiveCall(){Call c=active;if(c==null||c.getState()!=Call.STATE_RINGING)return false;try{c.answer(VideoProfile.STATE_AUDIO_ONLY);return true;}catch(RuntimeException e){return false;}}
    public static boolean rejectActiveCall(){Call c=active;if(c==null||c.getState()!=Call.STATE_RINGING)return false;try{c.reject(false,null);return true;}catch(RuntimeException e){return false;}}
    public static String callerLabel(){Call c=active;if(c==null||c.getDetails()==null)return "Incoming cellular call";CharSequence n=c.getDetails().getCallerDisplayName();return n==null||n.length()==0?"Incoming cellular call":n.toString();}
}
