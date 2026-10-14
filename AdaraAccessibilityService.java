package com.adara.assistant;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Locale;
public final class AdaraAccessibilityService extends AccessibilityService {
    private static volatile AdaraAccessibilityService instance;
    @Override protected void onServiceConnected(){super.onServiceConnected();instance=this;}
    @Override public void onAccessibilityEvent(AccessibilityEvent e){ /* no background actions or event logging */ }
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){if(instance==this)instance=null;super.onDestroy();}
    public static boolean clickText(String text){AdaraAccessibilityService s=instance;if(s==null||text==null||text.trim().isEmpty())return false;return click(s.getRootInActiveWindow(),text.trim().toLowerCase(Locale.ROOT),0);}
    private static boolean click(AccessibilityNodeInfo n,String target,int depth){if(n==null||depth>40||!n.isVisibleToUser())return false;CharSequence t=n.getText(),d=n.getContentDescription();boolean match=(t!=null&&t.toString().trim().toLowerCase(Locale.ROOT).equals(target))||(d!=null&&d.toString().trim().toLowerCase(Locale.ROOT).equals(target));if(match){AccessibilityNodeInfo c=n;for(int i=0;c!=null&&i<7;i++){if(c.isEnabled()&&c.isClickable())return c.performAction(AccessibilityNodeInfo.ACTION_CLICK);c=c.getParent();}}for(int i=0;i<n.getChildCount();i++)if(click(n.getChild(i),target,depth+1))return true;return false;}
    public static boolean scroll(boolean forward){AdaraAccessibilityService s=instance;return s!=null&&scrollNode(s.getRootInActiveWindow(),forward,0);}
    private static boolean scrollNode(AccessibilityNodeInfo n,boolean forward,int depth){if(n==null||depth>40)return false;if(n.isVisibleToUser()&&n.isScrollable()&&n.performAction(forward?AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD))return true;for(int i=0;i<n.getChildCount();i++)if(scrollNode(n.getChild(i),forward,depth+1))return true;return false;}
}
