package com.smartsecurity.android.piudonnacouture.ui;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.sync.AccountAuthenticator;

import java.util.concurrent.ExecutionException;

public class SignInFragment extends Fragment {
    private static final String TAG = "SignInFragment";

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    public static final String STATE_OPERATION_IN_PROGRESS = "state_operation_in_progress";

    public static final int MIN_PASSWORD_LENGTH = 5;

    private AuthenticatorActivity mActivity;
    private SignInTask mSignInTask;

    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

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
                setFormEditable(false);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        if (mActivity != null) {
            mActivity.setStatusBarColor(mStatusBarColor);
            mActivity.setActionBarShown(true);
            mActivity.setActionBarTitle(getString(R.string.title_fragment_sign_in));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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

        /*String email = mEmailInputLayout.getEditText().getText().toString();
        String password = mPasswordInputLayout.getEditText().getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(getActivity(), "Try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
    }

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged: User signed in.");
            } else {
                // User is signed out, this should never happen in this fragment since there is no
                // option to sign out, but I want to know if happens, so lets log it.
                Log.d(TAG, "onAuthStateChanged: User signed out.");
            }
        }
    };

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
                Tasks.await(mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), (@NonNull Task<AuthResult> task) -> {
                            Log.d(TAG, "signInWithEmail:onComplete: " + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                //Toast.makeText(getActivity(), "Try again", Toast.LENGTH_SHORT).show();
                            }
                        }));

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "FirebaseAuth.getCurrentUser: NOT null");
                    Tasks.await(user.getToken(false).addOnCompleteListener((@NonNull Task<GetTokenResult> task) -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getToken: " + task.getResult().getToken());
                            extras.putString(AccountManager.KEY_AUTHTOKEN, task.getResult().getToken());
                        } else {
                            Log.e(TAG, "getToken: Error getting token.");
                        }
                    }));

                    extras.putString(AccountManager.KEY_ACCOUNT_NAME, user.getEmail());
                    extras.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
                    extras.putString(AuthenticatorActivity.EXTRA_REFRESH_TOKEN, null);
                } else {
                    Log.d(TAG, "FirebaseAuth.getCurrentUser: null");
                    extras.putString(AuthenticatorActivity.EXTRA_ERROR_MESSAGE,
                            "Could not get firebase auth token.");
                }

            } catch (ExecutionException e) {
                Log.d(TAG, "ExecutionException", e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException", e);
                e.printStackTrace();
            }

            /*try {
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
            }*/

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


    /*private class SignInTask extends AsyncTask<String, Void, Intent> {
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
    }*/
}
