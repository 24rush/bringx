package com.rinf.bringx.ViewModels;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.utils.Error;
import com.rinf.bringx.utils.IStatusHandler;
import com.rinf.bringx.utils.Log;
import com.rinf.bringx.utils.ServiceProxy;
import com.rinf.bringx.utils.StringAppender;

import org.json.JSONObject;

public class LoginViewModel {
    public Observable<Boolean> IsDriverLoggedIn = new Observable<Boolean>(false);
    public Observable<String> UserName = new Observable<String>("");
    public Observable<String> Password = new Observable<String>("");

    public Observable<String> AuthToken = new Observable<String>("");
    public Observable<String> DriverId = new Observable<String>("");

    public Observable<Boolean> IsLoggedIn = new Observable<Boolean>(false);
    public Observable<Boolean> IsLoggingIn = new Observable<Boolean>(false);
    public Observable<Boolean> IsError = new Observable<Boolean>(false);
    public String Error;

    private final String KEY_LOGIN_USER_NAME = "username";
    private final String KEY_LOGIN_PASSWORD = "password";
    private final String KEY_AUTH_TOKEN = "auth_token";
    private final String KEY_DRIVER_ID = "uid";

    public LoginViewModel() {
        UserName.addObserver(new INotifier<String>() {
            @Override
            public void OnValueChanged(String value) {
                if (!value.isEmpty())
                    App.StorageManager().SetCurrentUser(value);
            }
        });

        UserName.set(App.StorageManager().Credentials().getString(KEY_LOGIN_USER_NAME));
        Password.set(App.StorageManager().Credentials().getString(KEY_LOGIN_PASSWORD));

        DriverId.set(App.StorageManager().Credentials().getString(KEY_DRIVER_ID));
        AuthToken.set(App.StorageManager().Credentials().getString(KEY_AUTH_TOKEN));

        if (!UserName.get().isEmpty() && !Password.get().isEmpty() && !DriverId.get().isEmpty() && !AuthToken.get().isEmpty()) {
            IsLoggedIn.set(true);
        }
    }

    public void DoLogin() {
        Error = "";
        IsError.set(false);
        IsLoggingIn.set(true);
        IsLoggedIn.set(false);

        IStatusHandler statusHandler = new IStatusHandler<JSONObject, String>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err, String... params) {
                IsLoggingIn.set(false);
                Error = err.Message;
                IsError.set(true);

                Password.set("");
                DriverId.set("");
                AuthToken.set("");

                updateCacheCredentials();

                IsLoggedIn.set(false);
            }

            @Override
            public void OnSuccess(JSONObject data, String... params) {
                IsLoggingIn.set(false);
                Error = "";
                IsError.set(false);


                DriverId.set(data.optString(KEY_DRIVER_ID));
                AuthToken.set(data.optString(KEY_AUTH_TOKEN));

                updateCacheCredentials();

                IsLoggedIn.set(true);
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.Login(UserName.get(), Password.get());
    }

    public void Logout(final Runnable onLogout) {
        Log.d("Logging out user: " + UserName.get());

        IStatusHandler<JSONObject, String> _logoutHandler = new IStatusHandler<JSONObject, String>() {
            @Override
            public void OnError(com.rinf.bringx.utils.Error err, String... ctx) {
                Error = err.Message;
                IsError.set(true);
            }

            @Override
            public void OnSuccess(JSONObject response, String... ctx) {
                UserName.set("");
                Password.set("");
                DriverId.set("");
                AuthToken.set("");

                IsLoggedIn.set(false);

                Error = "";
                IsError.set(false);

                updateCacheCredentials();

                if (onLogout != null) {
                    onLogout.run();
                }
            }
        };

        if (IsLoggedIn.get() == true) {
            ServiceProxy proxy = new ServiceProxy(_logoutHandler);
            proxy.Logout(DriverId.get(), AuthToken.get());
        }
        else {
            _logoutHandler.OnSuccess(null, null);
        }
    }

    private void updateCacheCredentials() {
        App.StorageManager().Credentials().setString(KEY_LOGIN_USER_NAME, UserName.get());
        App.StorageManager().Credentials().setString(KEY_LOGIN_PASSWORD, Password.get());

        App.StorageManager().Credentials().setString(KEY_AUTH_TOKEN, AuthToken.get());
        App.StorageManager().Credentials().setString(KEY_DRIVER_ID, DriverId.get());
    }
}
