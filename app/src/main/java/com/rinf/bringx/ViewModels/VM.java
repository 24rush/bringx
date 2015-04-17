package com.rinf.bringx.ViewModels;

public class VM {
    public static LoginViewModel LoginViewModel;
    public static MeetingsViewModel MeetingsViewModel;

    public VM() {
        LoginViewModel = new LoginViewModel();
        MeetingsViewModel = new MeetingsViewModel();
    }
}
