package com.adara.assistant;
import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.List;
public final class NotificationReaderService extends NotificationListenerService {
    private static volatile NotificationReaderService instance;
    @Override public void onListenerConnected(){super.onListenerConnected();instance=this;}
    @Override public void onListenerDisconnected(){if(instance==this)instance=null;super.onListenerDisconnected();}
    @Override public void onDestroy(){if(instance==this)instance=null;super.onDestroy();}
    public static List<String> snapshot(){NotificationReaderService s=instance;if(s==null)return null;try{List<String> out=new ArrayList<>();StatusBarNotification[] all=s.getActiveNotifications();if(all==null)return out;for(StatusBarNotification x:all){Notification n=x.getNotification();if(n==null||n.extras==null)continue;CharSequence title=n.extras.getCharSequence(Notification.EXTRA_TITLE),body=n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);if(body==null)body=n.extras.getCharSequence(Notification.EXTRA_TEXT);String row=title==null?x.getPackageName():title.toString();if(body!=null&&body.length()>0)row+=" — "+body;out.add(row.length()>240?row.substring(0,237)+"…":row);}return out;}catch(RuntimeException e){return null;}}
    public static Integer clearClearable(){NotificationReaderService s=instance;if(s==null)return null;try{int count=0;StatusBarNotification[] all=s.getActiveNotifications();if(all==null)return 0;for(StatusBarNotification x:all)if(x.isClearable()){s.cancelNotification(x.getKey());count++;}return count;}catch(RuntimeException e){return null;}}
}
