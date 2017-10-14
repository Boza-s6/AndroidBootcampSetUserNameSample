package com.example.nemanja.usermanagersample.activity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.nemanja.usermanagersample.service.SetUsernameOperation;
import com.example.nemanja.usermanagersample.service.UserService;

public class SubmitUiLoader extends Loader<SubmitUiModel> implements UserService.Callback {
    private static final String TAG = "SubmitUiLoader";
    private SubmitUiModel mModel;
    private int mOperationId;

    public SubmitUiLoader(Context context, @Nullable String name) {
        super(context);
        if (name != null) {
            mOperationId = UserService.getInstance().execute(new SetUsernameOperation(name), this);
        }
    }

    /**
     * Called every time activity is started
     */
    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mModel == null) {
            mModel = SubmitUiModel.idle();
        }
        deliverResult(mModel);
    }

    @Override
    public void deliverResult(SubmitUiModel data) {
        mModel = data;
        super.deliverResult(data);
    }

    /**
     * This is called when loader is destroyed due to activity being destroyed (user clicked
     * back) but not in case of rotation (configuration change)
     */
    @Override
    protected void onReset() {
        super.onReset();
        UserService.getInstance().cancel(mOperationId);
    }

    @Override
    public void onStatusChange(UserService.Result result, int id) {
        Log.d(TAG, "onStatusChange: " + result.toString());
        deliverResult(mapToUiModel(result));
    }

    private SubmitUiModel mapToUiModel(UserService.Result result) {
        switch (result.mStatus) {
            case IN_FLIGHT:
                return SubmitUiModel.inProgress();
            case SUCCESS:
                return SubmitUiModel.success();
            case ERROR:
                return SubmitUiModel.error(result.exception.getMessage());
            case CANCELLED:
                return SubmitUiModel.idle();
            default:
                throw new IllegalStateException();
        }

    }
}
