package club.xiaolong.apps.hbhandler.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import club.xiaolong.apps.hbhandler.R;

/**
 * Created by xlli5 on 2017/2/7 0007.
 */
public class RobService extends AccessibilityService {

    private MediaPlayer hbPlayer;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /**
         * 获取事件类型
         */
        int eventType = event.getEventType();
        switch (eventType) {
            /**
             * 通知栏状态改变
             */
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (texts != null && !texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        Log.i("HBHandler", "text:" + content);
                        if (content != null && content.contains("[微信红包]")) {
                            /**
                             * 提示音
                             */
                            hbPlayer.start();
                            /**
                             * 模拟打开通知栏
                             */
                            if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                                Notification notify = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notify.contentIntent;
                                try {
                                    pendingIntent.send();
                                } catch (PendingIntent.CanceledException e) {
                                    Log.e("HBHandler", "打开通知栏消息失败" + e);
                                }
                            }
                        }
                    }
                }
                break;
            /**
             * 监听是否进入微信红包界面
             */
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                String className = event.getClassName().toString();
//                Log.i("HBHandler", "className:"+className);
//                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
//                    //开始抢红包
//                    getPacket();
//                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
//                    //开始打开红包
//                    openPacket();
//                }
//                break;
            default:
                break;
        }

    }

    /**
     * 查找到
     */
    @SuppressLint("NewApi")
    private void openPacket() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            AccessibilityNodeInfo info = nodeInfo.getChild(2);
            if (info != null) {
                info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

    }

    @SuppressLint("NewApi")
    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycle(rootNode);
    }

    /**
     * 打印一个节点的结构
     *
     * @param info
     */
    @SuppressLint("NewApi")
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                if ("领取红包".equals(info.getText().toString())) {
                    //这里有一个问题需要注意，就是需要找到一个可以点击的View
                    Log.i("HBHandler", "Click,isClick:" + info.isClickable());
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        Log.i("HBHandler", "parent isClick:" + parent.isClickable());
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }

                }
            }

        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        hbPlayer = MediaPlayer.create(this, R.raw.hbll_1);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (hbPlayer != null) {
            hbPlayer.release();
            hbPlayer = null;
        }
        return super.onUnbind(intent);
    }
}
