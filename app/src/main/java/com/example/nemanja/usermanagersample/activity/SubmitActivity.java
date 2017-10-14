package com.example.nemanja.usermanagersample.activity;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.nemanja.usermanagersample.R;

/**
 * Activity for changing name of user on server.
 * User can enter the name and click Submit.
 * EditText and Submit button will be disabled until operation completes. In case user clicks
 * back button we'll try to cancel operation.
 * <p>
 * State of UI of this activity is described with UISubmitModel class.
 * We'll use Loader to hold this state and to handle callbacks from UserService.
 * This is not usual use of Loaders, but in this case it helps us to handle configuration change
 * (rotation), and callbacks that arrived when app was in stopped state/was in middle of
 * recreation.
 * <p>
 * (There are better alternatives to Loaders for this case, check Android ViewModels and LiveData)
 * <p>
 * Note: Consider what would happen if we wanted to leave operation running even when user left
 * this activity?
 * Following case:
 * - User opens this activity (from some other)
 * - Enters name and clicks back
 * - Activity is destroyed, we don't cancel operation, so it's still running in background
 * - User returns to this activity, and (under the assumption that operation is still running)
 * expects to see progress bar and disabled button and submit button.
 * <p>
 * In that case Loader would not help us, because it would be destroyed with activity when
 * clicking back button.
 * We would need to save state of running operation somewhere. UserService is probably right
 * place, but it complicates it a bit, so I decided to leave it out for this sample.
 * <p>
 * What I want to say is that state in most cases should be outside of activity, otherwise you
 * have a lot of problems with inconsistent ui.
 * Activity/Fragments are Ui of app and should just reflect state of the app not hold it.
 */
public class SubmitActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<SubmitUiModel> {
    public static final int LOADER_ID = 1337;
    public static final String ARG_NAME = "name";
    private static final String TAG = "SubmitActivity";
    private EditText mNewName;
    private Button mSubmit;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNewName = findViewById(R.id.new_name_edit_text);
        mSubmit = findViewById(R.id.submit_button);
        mProgressBar = findViewById(R.id.progress_bar);

        setupCallbacks();
        // init loader. It will be created if not exists and new callback will be set in
        // LoaderManager
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void setupCallbacks() {
        mSubmit.setOnClickListener(this::changeName);
    }

    private void changeName(View view) {
        String name = mNewName.getText().toString();
        Log.d(TAG, "changeName: name = " + name);
        mNewName.setText("");
        Bundle bundle = new Bundle();
        bundle.putString(ARG_NAME, name);
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
    }

    @Override
    public Loader<SubmitUiModel> onCreateLoader(int id, Bundle args) {
        return new SubmitUiLoader(getApplicationContext(), args == null ? null : args.getString(ARG_NAME));
    }

    @Override
    public void onLoadFinished(Loader<SubmitUiModel> loader, SubmitUiModel data) {
        Log.d(TAG, "onLoadFinished: " + data);
        if (data.inProgress) {
            enableUi(false);
            showProgressBar(true);
        } else {
            enableUi(true);
            showProgressBar(false);
        }

        if (data.errorMessage != null) {
            showToast(data.errorMessage);
            data.errorMessage = null; // this is ugly
        }
    }

    private void showToast(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void enableUi(boolean enable) {
        mSubmit.setEnabled(enable);
        mNewName.setEnabled(enable);
    }

    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<SubmitUiModel> loader) {
        // no need to do anything here in this particular state
    }
}
