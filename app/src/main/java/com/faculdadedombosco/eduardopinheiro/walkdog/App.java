package com.faculdadedombosco.eduardopinheiro.walkdog;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class App extends Application {
    private DatabaseReference mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new AppNotificationOpenedHandler(this))
                .init();
    }

    public class AppNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {

        private Application application;

        public AppNotificationOpenedHandler(Application application) {
            this.application = application;
        }

        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;
            String idPasseio = null;
            String idUsuario = null;

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();

            if (data != null) {
                idPasseio = data.optString("idPasseio", null);
                idUsuario = data.optString("idUsuario", null);
            }

            if (user.getUid() != idUsuario) {
                OSNotificationAction.ActionType actionType = result.action.type;
                if (actionType == OSNotificationAction.ActionType.ActionTaken)
                    Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

                startApp(idPasseio);
            }
        }

        private void startApp(String idPasseio) {
            Intent intent = new Intent(application, NovoPasseioActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

            intent.putExtra("idPasseio", idPasseio);
            application.startActivity(intent);
        }
    }
}