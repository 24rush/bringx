package com.rinf.bringx.ViewModels;

import com.rinf.bringx.utils.Log;

public class VM {
    public static LoginViewModel LoginViewModel;
    public static MeetingsViewModel MeetingsViewModel;

    public VM() {
        Log.d("new VM");
        LoginViewModel = new LoginViewModel();
        MeetingsViewModel = new MeetingsViewModel();
    }
}
