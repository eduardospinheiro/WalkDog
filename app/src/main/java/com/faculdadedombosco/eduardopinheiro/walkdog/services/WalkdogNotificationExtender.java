package com.faculdadedombosco.eduardopinheiro.walkdog.services;

import android.app.ActivityManager;
import android.content.Context;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import java.util.List;

public class WalkdogNotificationExtender extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // RETORNA TRUE SE O APP ESTA EM PRIMEIRO PLANO
        // ASSIM NAO MOSTRA A NOTIFICACAO
        return this.isRunningInForeground();
    }

    protected boolean isRunningInForeground() {
        ActivityManager manager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1);
        if (tasks.isEmpty()) {
            return false;
        }
        String topActivityName = tasks.get(0).topActivity.getPackageName();
        return topActivityName.equalsIgnoreCase(getPackageName());
    }
}