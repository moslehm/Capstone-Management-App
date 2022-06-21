package ca.macewan.capstone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessaging";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Since the notification is received directly from
        // FCM, the title and the body can be fetched
        // directly as below.
        super.onMessageReceived(remoteMessage);
        showNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody());
        System.out.print("Notification received");
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendTokenToServer(token);
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        // Assign channel ID
        String channel_id = "notification_channel";
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Pass the intent to PendingIntent to start the
        // next Activity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Create a Builder object using NotificationCompat
        // class. This will allow control over all the flags
        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(getApplicationContext(),
                channel_id)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(com.google.firebase.database.collection.R.drawable.common_google_signin_btn_icon_light_normal)
                .setAutoCancel(true)
                .setSound(ringtone)
                .setVibrate(new long[]{1000, 1000, 1000,
                        1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Check if the Android Version is greater than Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channel_id, "Capstone Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0, builder.build());
    }

    private void sendTokenToServer(String token) {
        //handle token sending
    }
}
