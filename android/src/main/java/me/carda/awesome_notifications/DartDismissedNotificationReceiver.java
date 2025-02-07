package me.carda.awesome_notifications;

import android.content.Context;
import android.content.Intent;

import me.carda.awesome_notifications.core.AwesomeNotifications;
import me.carda.awesome_notifications.core.broadcasters.receivers.DismissedNotificationReceiver;
import me.carda.awesome_notifications.core.broadcasters.senders.BroadcastSender;
import me.carda.awesome_notifications.core.builders.NotificationBuilder;
import me.carda.awesome_notifications.core.enumerators.NotificationLifeCycle;
import me.carda.awesome_notifications.core.exceptions.AwesomeNotificationsException;
import me.carda.awesome_notifications.core.logs.Logger;
import me.carda.awesome_notifications.core.managers.StatusBarManager;
import me.carda.awesome_notifications.core.models.returnedData.ActionReceived;

public class DartDismissedNotificationReceiver extends DismissedNotificationReceiver {

    private static final String TAG = "DartDismissedNotificationReceiver";

    @Override
    public void initializeExternalPlugins(Context context) throws Exception {
        AwesomeNotificationsFlutterExtension.initialize();
    }

    @Override
    public void onReceiveBroadcastEvent(Context context, Intent intent) throws Exception {
        String action = intent.getAction();
        Logger.i(TAG, action);
        if (action != null && action.equals("DISMISSED_NOTIFICATION")) {

            NotificationLifeCycle appLifeCycle =
                    AwesomeNotifications
                            .getApplicationLifeCycle();

            ActionReceived actionReceived = null;
            try {
                NotificationBuilder builder = NotificationBuilder.getNewBuilder();

                actionReceived = builder
                        .buildNotificationActionFromIntent(
                                context,
                                intent,
                                appLifeCycle);
            } catch (AwesomeNotificationsException e) {
                e.printStackTrace();
            }

            if(actionReceived == null) {
                if(AwesomeNotifications.debug)
                    Logger.i(TAG,
                            "The action received do not contain any awesome " +
                                    "notification data and was discarded");
                return;
            }

            actionReceived.registerDismissedEvent(appLifeCycle);

            AwesomeNotifications awesomeNotifications = new AwesomeNotifications(context);
            awesomeNotifications.cancelSchedule(actionReceived.id);

            Logger.i(TAG, "removing notification" + actionReceived.id.toString());

            // In this case, the notification is always dismissed
            StatusBarManager
                    .getInstance(context)
                    .unregisterActiveNotification(context, actionReceived.id);

            BroadcastSender
                    .sendBroadcastNotificationDismissed(context, actionReceived);
        }
    }
}
