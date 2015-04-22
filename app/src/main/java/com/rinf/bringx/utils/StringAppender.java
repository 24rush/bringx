package com.rinf.bringx.utils;

public class StringAppender {
    public static String Separator = ", ";

    public static String AppendIfFilled(String str, String... strToAppendList) {
        for (String strToAppend : strToAppendList) {
            if (strToAppend.isEmpty())
                continue;

            if (str.isEmpty())
                str += strToAppend;
            else
                str += (Separator + strToAppend);
        }

        return str;
    }
}
