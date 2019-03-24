package com.example.hackabull;

import java.util.ArrayList;

public class PublicMessage {
    private String mName;
    private String mMessage;
    private boolean mOnline;

    public PublicMessage(String name, String message, boolean online) {
        mName = name;
        mMessage = message;
        mOnline = online;
    }

    public String getName() {
        return mName;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean isOnline() {
        return mOnline;
    }

    private static int lastContactId = 0;

    public static ArrayList<PublicMessage> createContactsList(int numContacts) {
        ArrayList<PublicMessage> contacts = new ArrayList<PublicMessage>();

        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new PublicMessage("Person " + ++lastContactId,String.valueOf(i), i <= numContacts / 2));
        }

        return contacts;
    }
}