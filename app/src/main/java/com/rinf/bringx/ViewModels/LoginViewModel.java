package com.rinf.bringx.ViewModels;

import com.rinf.bringx.App;
import com.rinf.bringx.EasyBindings.Observable;
import com.rinf.bringx.Views.LoginActivity;
import com.rinf.bringx.utils.Error;
import com.rinf.bringx.utils.IStatusHandler;
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
        UserName.set(App.StorageManager().getString(KEY_LOGIN_USER_NAME));
        Password.set(App.StorageManager().getString(KEY_LOGIN_PASSWORD));

        if (!UserName.get().equals("") && !Password.equals("")) {
            IsLoggedIn.set(true);
        }
    }

    public void DoLogin() {
        Error = "";
        IsError.set(false);
        IsLoggingIn.set(true);
        IsLoggedIn.set(false);

        IStatusHandler statusHandler = new IStatusHandler() {
            @Override
            public void OnError(Error err) {
                IsLoggingIn.set(false);
                Error = err.Message;
                IsError.set(true);

                Password.set("");
                IsLoggedIn.set(false);

                updateCacheCredentials();
            }

            @Override
            public void OnSuccess(JSONObject response) {
                IsLoggingIn.set(false);
                Error = "";
                IsError.set(false);
                IsLoggedIn.set(true);

                updateCacheCredentials();
            }
        };

        ServiceProxy proxy = new ServiceProxy(statusHandler);
        proxy.Login(UserName.get(), Password.get(), "");
    }

    public void Logout() {
        UserName.set("");
        Password.set("");
        IsLoggedIn.set(false);

        Error = "";
        IsError.set(false);

        updateCacheCredentials();
    }

    private void updateCacheCredentials() {
        App.StorageManager().setString(KEY_LOGIN_USER_NAME, UserName.get());
        App.StorageManager().setString(KEY_LOGIN_PASSWORD, Password.get());
    }
}
