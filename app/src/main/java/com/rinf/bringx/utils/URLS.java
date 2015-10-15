package com.rinf.bringx.utils;

public class URLS {

    public static Boolean IsDebug = true;
    public static String LoginURL = "https://dev-auftrag.bringx.com/json/login/1";
    public static String LogoutURL = "https://dev-auftrag.bringx.com/json/drivers/%s/logout?auth_token=%s&version=1.0.1";
    public static String OrdersETA = "https://dev-auftrag.bringx.com/json/drivers/%s/orders-eta?auth_token=%s&version=1.0.1";
    public static String OrdersInfo = "https://dev-auftrag.bringx.com/json/drivers/%s/orders/%s?auth_token=%s&version=1.0.1";
    public static String StatusURL = "https://dev-auftrag.bringx.com/json/drivers/%s/orders/%s/status?auth_token=%s&version=1.0.1";
    public static String PositionUpdateURL = "https://dev-auftrag.bringx.com/json/driver/%s";
/*
    public static Boolean IsDebug = false;
    public static String LoginURL = "https://auftrag.bringx.com/json/login/1";
    public static String LogoutURL = "https://auftrag.bringx.com/json/drivers/%s/logout?auth_token=%s&version=1.0.1";
    public static String OrdersETA = "https://auftrag.bringx.com/json/drivers/%s/orders-eta?auth_token=%s&version=1.0.1";
    public static String OrdersInfo = "https://auftrag.bringx.com/json/drivers/%s/orders/%s?auth_token=%s&version=1.0.1";
    public static String StatusURL = "https://auftrag.bringx.com/json/drivers/%s/orders/%s/status?auth_token=%s&version=1.0.1";
    public static String PositionUpdateURL = "https://auftrag.bringx.com/json/driver/%s";
*/
}