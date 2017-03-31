package com.smartsecurity.android.piudonnacouture.ui;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartsecurity.android.piudonnacouture.Config;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.AuthTokenResponse;
import com.smartsecurity.android.piudonnacouture.model.UserInfo;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;
import com.smartsecurity.android.piudonnacouture.sync.WebService;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    public static final String STATE_OPERATION_IN_PROGRESS = "state_operation_in_progress";

    public static final int MIN_PASSWORD_LENGTH = 5;

    private AuthenticatorActivity mActivity;
    private SignInTask mSignInTask;

    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;
    private Button mSignInButton;
    private TextView mForgotPasswordButton;
    private View mCurrentFocusView;

    private boolean mOperationInProgress = false;
    private int mStatusBarColor;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mSignInButton.setEnabled(validateFields());
        }
    };

    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mCurrentFocusView = v;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (AuthenticatorActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mEmailInputLayout = (TextInputLayout) rootView.findViewById(R.id.email_input_layout);
        mEmailInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mEmailInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mPasswordInputLayout = (TextInputLayout) rootView.findViewById(R.id.password_input_layout);
        mPasswordInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mPasswordInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);
        mPasswordInputLayout.getEditText().setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == R.id.ime_action_sign_in || actionId == EditorInfo.IME_NULL) {
                attemptSignIn();
                return true;
            }
            return false;
        });

        mSignInButton = (Button) rootView.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(v -> attemptSignIn());

        mForgotPasswordButton = (TextView) rootView.findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(v -> {});

        mStatusBarColor = ContextCompat.getColor(getActivity(), R.color.theme_primary_dark);

        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");

        // We must wait until this call (onViewStateRestored), where the system already restored the
        // state of the views in this fragment, to additionally restore the entire form state.
        if (savedInstanceState != null) {
            mOperationInProgress = savedInstanceState.getBoolean(STATE_OPERATION_IN_PROGRESS, false);

            // The system restores the view's state after onCreateView happens, so any TextWatcher
            // associated with an EditText is triggered when the text is restored, in this case, our
            // TextWatcher enables/disables the submit button according to the form validation
            // state, so the submit button state is restored well. We only need to handle the case
            // of state restoration when the form is fully disabled due to an operation in progress.
            if (mOperationInProgress) {
                setFormEditable(!mOperationInProgress);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mActivity != null) {
            mActivity.setStatusBarColor(mStatusBarColor);
            mActivity.setActionBarShown(true);
            mActivity.setActionBarTitle(getString(R.string.title_fragment_sign_in));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_OPERATION_IN_PROGRESS, mOperationInProgress);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateFields() {
        return isValidEmail(mEmailInputLayout.getEditText().getText()) &&
                isValidPassword(mPasswordInputLayout.getEditText().getText());
    }

    private boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(CharSequence password) {
        return !TextUtils.isEmpty(password) && password.length() >= MIN_PASSWORD_LENGTH;
    }

    @SuppressWarnings("ConstantConditions")
    private void setFormEditable(boolean editable) {
        if (mActivity != null) {
            mActivity.setDisplayHomeAsUpEnabled(editable);
            mActivity.setToolbarProgressEnabled(!editable);
        }

        // FIXME: Experimental.
        // Clear the focus of the current focused text field and hide the soft input keyboard
        // (experimental).
        View focusedView = getActivity().getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }

        // Clear the focus of the current focused text field.
        /*if (mCurrentFocusView != null) {
            mCurrentFocusView.clearFocus();
        }*/

        mEmailInputLayout.getEditText().setEnabled(editable);
        mPasswordInputLayout.getEditText().setEnabled(editable);
        mSignInButton.setEnabled(editable);
        mForgotPasswordButton.setEnabled(editable);
    }

    /*public void attemptSignIn(CharSequence email, CharSequence password) {
        if (mSignInTask != null) {
            return;
        }

        mSignInTask = new SignInTask();
        mSignInTask.execute(email.toString(), password.toString());
    }*/

    @SuppressWarnings("ConstantConditions")
    public void attemptSignIn() {
        if (mSignInTask != null || !validateFields()) {
            return;
        }

        mSignInTask = new SignInTask();
        mSignInTask.execute(
                mEmailInputLayout.getEditText().getText().toString(),
                mPasswordInputLayout.getEditText().getText().toString());
    }

    private class SignInTask extends AsyncTask<String, Void, Intent> {
        private static final String TAG = "SignInTask";

        public static final String JSON_FIELD_ERROR = "error";
        public static final String JSON_FIELD_ERROR_DESCRIPTION = "error_description";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setFormEditable(!(mOperationInProgress = true));
        }

        @Override
        protected Intent doInBackground(String... params) {
            String email = params[0];
            String password = params[1];

            Bundle extras = new Bundle();

            try {
                AuthTokenResponse tokenResponse = SyncUtils.sWebService.getAuthToken(
                        email, password, Config.OAUTH_CLIENT_ID, Config.OAUTH_CLIENT_SECRET,
                                Config.OAUTH_GRANT_TYPE_PASSWORD);

                // TODO: Get the info of the user signed in.
                // UserInfo info = SyncUtils.sWebService.getUserInfo(tokenResponse.getAccessToken(), email);

                extras.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                extras.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                extras.putString(AccountManager.KEY_AUTHTOKEN, tokenResponse.getAccessToken());
                extras.putString(AuthenticatorActivity.EXTRA_REFRESH_TOKEN, tokenResponse.getRefreshToken());

            } catch (RetrofitError error) {
                error.printStackTrace();

                if (error.getKind() == RetrofitError.Kind.NETWORK) {
                    extras.putString(AuthenticatorActivity.EXTRA_ERROR_MESSAGE, "No connection.");
                } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Response response = error.getResponse();

                    String jsonString = new String(((TypedByteArray) response.getBody()).getBytes());
                    JsonObject errorJson = new JsonParser().parse(jsonString).getAsJsonObject();
                    extras.putString(AuthenticatorActivity.EXTRA_ERROR_MESSAGE,
                            errorJson.get(JSON_FIELD_ERROR_DESCRIPTION).getAsString());
                    Log.d(TAG, "doInBackground() > ErrorJsonResponse: " + jsonString);
                } else {
                    extras.putString(AuthenticatorActivity.EXTRA_ERROR_MESSAGE,
                            error.getLocalizedMessage());
                }
            }

            Intent result = new Intent();
            result.putExtras(extras);

            return result;
        }

        @Override
        protected void onPostExecute(Intent resultIntent) {
            super.onPostExecute(resultIntent);
            Log.d(TAG, "onPostExecute");

            mSignInTask = null;
            mOperationInProgress = false;

            if (resultIntent.hasExtra(AuthenticatorActivity.EXTRA_ERROR_MESSAGE)) {
                // Set form as editable only if some error occurred.
                setFormEditable(!mOperationInProgress);

                Toast.makeText(mActivity,
                        resultIntent.getStringExtra(AuthenticatorActivity.EXTRA_ERROR_MESSAGE),
                                Toast.LENGTH_LONG).show();
                return;
            }

            mActivity.finishSignIn(resultIntent);
        }
    }
}
