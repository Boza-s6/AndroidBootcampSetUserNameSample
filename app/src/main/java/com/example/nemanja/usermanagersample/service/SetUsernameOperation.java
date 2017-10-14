package com.example.nemanja.usermanagersample.service;

import android.support.annotation.IntDef;
import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Random;

/**
 * This class handles connection to server, preparing parameters, creating Json, etc and invoking
 * right REST method.
 * In real app there will be abstract class that handles most of ths staff above and subclasses
 * fill operation-specific parts.
 */
public class SetUsernameOperation {
    private static final String TAG = "SetUsernameOperation";
    private final String mName;

    public static class SetUsernameException extends Exception {
        public static final int TYPE_IO_ERROR = 0;
        public static final int TYPE_BAD_NAME = 1;
        public static final int TYPE_CANCELLED = 2;
        public final int mErrorType;

        /**
         * Enums are more expensive than plain int constants. This is the way to get some
         * type-safety. Lint will highlight places where parameters passed to function that has
         * argument annotated with @ErrorType is not in declared @IntDef annotation.
         *
         * Ex: public void bla(@ErrorType int errorType){}
         * myObject.bla(42); <- this will be highlighted.
         */
        @IntDef({TYPE_IO_ERROR, TYPE_BAD_NAME, TYPE_CANCELLED})
        @Retention(RetentionPolicy.SOURCE)
        public @interface ErrorType {}

        public SetUsernameException(@ErrorType int errorType){
            mErrorType = errorType;
        }

        public SetUsernameException(@ErrorType int errorType, Throwable cause) {
            super(cause);
            mErrorType = errorType;
        }

        @Override
        public String toString() {
            return "merrorType=" + mErrorType + "; "+super.toString();
        }
    }

    public SetUsernameOperation(String name) {
        mName = name;
    }

    public void doOperation() throws SetUsernameException {
        Log.d(TAG, "doOperation for name " + mName);
        // sleep for a little while, simulating network connection. In 10% of cases throw io
        // error, and 9% (i think) simulate server returning statuc code for invalid name.
        try {
            Thread.sleep(10_000L);
        } catch (InterruptedException e) {
            throw new SetUsernameException(SetUsernameException.TYPE_CANCELLED, e);
        }

        Random random = new Random();
        if (random.nextInt(100) < 10) {
            throw new SetUsernameException(SetUsernameException.TYPE_IO_ERROR,
                    new IOException("dummy"));
        }

        if (random.nextInt(100) < 10) {
            throw new SetUsernameException(SetUsernameException.TYPE_BAD_NAME);
        }
    }
}
