package com.example.nemanja.usermanagersample.service;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.nemanja.usermanagersample.service.SetUsernameOperation.SetUsernameException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.nemanja.usermanagersample.service.UserService.Status.CANCELLED;
import static com.example.nemanja.usermanagersample.service.UserService.Status.ERROR;
import static com.example.nemanja.usermanagersample.service.UserService.Status.SUCCESS;

/**
 * Handles background execution of operation with cancellation support and status delivery on
 * main thread.
 */
@MainThread
public class UserService {
    private static final UserService sInstance = new UserService();
    private static final Executor sExecutor = Executors.newSingleThreadExecutor();
    private static int sIdGenerator = 1;
    /**
     * Holds tasks in progress for cancellation support.
     */
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, SetUsernameTask> mTaskMap = new HashMap<>();

    public static UserService getInstance() {
        return sInstance;
    }

    public void cancel(int id) {
        SetUsernameTask setUsernameTask = mTaskMap.remove(id);
        if (setUsernameTask != null) {
            setUsernameTask.removeCallback();
            setUsernameTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public int execute(SetUsernameOperation operation, Callback callback) {
        int opId = sIdGenerator++;
        SetUsernameTask setUsernameTask = new SetUsernameTask(callback, operation, opId);
        mTaskMap.put(opId, setUsernameTask);
        setUsernameTask.executeOnExecutor(sExecutor);
        return opId;
    }

    public enum Status {
        IN_FLIGHT, SUCCESS, ERROR, CANCELLED
    }

    public interface Callback {
        void onStatusChange(Result result, int id);
    }

    public static class Result {
        @Override
        public String toString() {
            return "Result{" +
                    "mStatus=" + mStatus +
                    ", exception=" + exception +
                    '}';
        }

        @NonNull
        public final Status mStatus;
        @Nullable // not null only if mStatus == error
        public final SetUsernameException exception;

        Result(@NonNull Status status, @Nullable SetUsernameException exception) {
            mStatus = status;
            this.exception = exception;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetUsernameTask extends AsyncTask<Void, Void, Result> {
        private final int mId;
        private Callback callback;
        private SetUsernameOperation mOperation;

        private SetUsernameTask(Callback callback, SetUsernameOperation operation, int id) {
            this.callback = callback;
            this.mOperation = operation;
            mId = id;
        }

        @Override
        protected void onPreExecute() {
            callback(new Result(UserService.Status.IN_FLIGHT, null));
        }

        @Override
        protected void onPostExecute(UserService.Result result) {
            callback(result);
            onFinish();
        }

        @Override
        protected void onCancelled(@Nullable UserService.Result status) {
            if (status == null) {
                callback(new Result(CANCELLED, null));
            } else {
                callback(status);
            }
            onFinish();
        }

        private void onFinish() {
            callback = null;
            mOperation = null;
            mTaskMap.remove(mId);
        }

        private void callback(@NonNull Result result) {
            if (callback != null) {
                callback.onStatusChange(result, mId);
            }
        }

        @Override
        protected UserService.Result doInBackground(Void... voids) {
            try {
                mOperation.doOperation();
            } catch (SetUsernameException e) {
                switch (e.mErrorType) {
                    case SetUsernameException.TYPE_CANCELLED:
                        return new Result(CANCELLED, null);
                    default:
                        return new Result(ERROR, e);
                }
            }
            return new Result(SUCCESS, null);
        }

        public void removeCallback() {
            callback = null;
        }
    }
}
