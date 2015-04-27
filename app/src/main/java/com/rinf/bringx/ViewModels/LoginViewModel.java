package com.rinf.bringx.ViewModels;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.INotifier;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.utils.Error;
import com.rinf.bringx.utils.IStatusHandler;
import com.rinf.bringx.utils.Log;
import com.rinf.bringx.utils.ServiceProxy;

import org.json.JSONObject;

public class LoginViewModel {
    public Observable<Boolean> IsDriverLoggedIn = new Observable<Boolean>(false);
    public Observable<String> UserName = new Observable<String>("");
    public Observable<String> Password = new Observable<String>("");

    public Observable<Boolean> IsLoggedIn = new Observable<Boolean>(false);
    public Observable<Boolean> IsLoggingIn = new Observable<Boolean>(false);
    public Observable<Boolean> IsError = new Observable<Boolean>(false);
    public String Error;

    private final String KEY_LOGIN_USER_NAME = "username";
    private final String KEY_LOGIN_PASSWORD = "password";

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

        if (!UserName.get().equals("") && !Password.get().equals("")) {
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
            public void OnError(Error err, String... params) {
                IsLoggingIn.set(false);
                Error = err.Message;
                IsError.set(true);

                Password.set("");
                IsLoggedIn.set(false);

                updateCacheCredentials();
            }

            @Override
            public void OnSuccess(JSONObject response, String... params) {
                IsLoggingIn.set(false);
                Error = "";
                IsError.set(false);
                IsLoggedIn.set(true);

                updateCacheCredentials();
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.Login(UserName.get(), Password.get());
    }

    public void Logout() {
        Log.d("Loging out user: " + UserName.get());

        UserName.set("");
        Password.set("");
        IsLoggedIn.set(false);

        Error = "";
        IsError.set(false);

        updateCacheCredentials();
    }

    private void updateCacheCredentials() {
        App.StorageManager().Credentials().setString(KEY_LOGIN_USER_NAME, UserName.get());
        App.StorageManager().Credentials().setString(KEY_LOGIN_PASSWORD, Password.get());
    }
}
