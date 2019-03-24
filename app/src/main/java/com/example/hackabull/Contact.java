package com.example.hackabull;

import java.util.ArrayList;

public class Contact {
    private String mName;
    private boolean mOnline;
    private String mMessage;

    public Contact(String name, String message, boolean online) {
        mName = name;
        mOnline = online;
        mMessage = message;
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

    public static ArrayList<Contact> createContactsList(int numContacts) {
        ArrayList<Contact> contacts = new ArrayList<Contact>();

        for (int i = 1; i <= numContacts; i++) {
            contacts.add(new Contact("Person " + ++lastContactId,"", i <= numContacts / 2));
        }

        return contacts;
    }
}