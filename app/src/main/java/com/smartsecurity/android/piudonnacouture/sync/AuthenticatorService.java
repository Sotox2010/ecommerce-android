package com.smartsecurity.android.piudonnacouture.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class AuthenticatorService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}
