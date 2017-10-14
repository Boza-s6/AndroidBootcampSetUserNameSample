package com.example.nemanja.usermanagersample.activity;

import android.support.annotation.Nullable;

/**
 * Created by nemanja on 14.10.17..
 */

public class SubmitUiModel {
    boolean inProgress;
    boolean error;
    @Nullable
    String errorMessage;

    public SubmitUiModel(boolean inProgress, boolean error, @Nullable String errorMessage) {
        this.inProgress = inProgress;
        this.error = error;
        this.errorMessage = errorMessage;
    }

    public static SubmitUiModel success(){
        return new SubmitUiModel(false, false, null);
    }

    public static SubmitUiModel error(String message) {
        return new SubmitUiModel(false, true, message);
    }

    public static SubmitUiModel inProgress() {
        return new SubmitUiModel(true, false, null);
    }

    public static SubmitUiModel idle() {
        return success();
    }

    @Override
    public String toString() {
        return "SubmitUiModel{" +
                "inProgress=" + inProgress +
                ", error=" + error +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
