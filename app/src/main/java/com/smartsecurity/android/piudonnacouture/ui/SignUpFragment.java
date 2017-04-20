package com.smartsecurity.android.piudonnacouture.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.RequestError;
import com.smartsecurity.android.piudonnacouture.util.SyncUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class SignUpFragment extends Fragment {

    private static final String TAG = "SignUpFragment";

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    public static final String STATE_OPERATION_IN_PROGRESS = "state_operation_in_progress";

    //public static final int MIN_PASSWORD_LENGTH = 5;

    private AuthenticatorActivity mActivity;
    private TextInputLayout mFirstNameInputLayout;
    private TextInputLayout mLastNameInputLayout;
    //private TextInputLayout mCompanyInputLayout;
    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;
    private CheckBox mTosCheckBox;
    private Button mCreateAccountButton;
    private View mCurrentFocusView;

    private SignUpTask mSignUpTask;
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
            mCreateAccountButton.setEnabled(validateFields());
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
        super.onCreate(savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        mFirstNameInputLayout = (TextInputLayout) rootView.findViewById(R.id.first_name_input_layout);
        mFirstNameInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mFirstNameInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mLastNameInputLayout = (TextInputLayout) rootView.findViewById(R.id.last_name_input_layout);
        mLastNameInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mLastNameInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        //mCompanyInputLayout = (TextInputLayout) rootView.findViewById(R.id.company_input_layout);
        //mCompanyInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mEmailInputLayout = (TextInputLayout) rootView.findViewById(R.id.email_input_layout);
        mEmailInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mEmailInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mPasswordInputLayout = (TextInputLayout) rootView.findViewById(R.id.password_input_layout);
        mPasswordInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mPasswordInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mTosCheckBox = (CheckBox) rootView.findViewById(R.id.tos_checkbox);
        mTosCheckBox.setOnCheckedChangeListener(
                (button, isChecked) -> mCreateAccountButton.setEnabled(validateFields()));

        mCreateAccountButton = (Button) rootView.findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener((View v) -> {
            mSignUpTask = new SignUpTask();
            mSignUpTask.execute(
                    mFirstNameInputLayout.getEditText().getText().toString(),
                    mLastNameInputLayout.getEditText().getText().toString(),
                    //mCompanyInputLayout.getEditText().getText().toString(),
                    mEmailInputLayout.getEditText().getText().toString(),
                    mPasswordInputLayout.getEditText().getText().toString());
        });

        mStatusBarColor = ContextCompat.getColor(getActivity(), R.color.theme_primary_dark);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.i(TAG, "onViewStateRestored");

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onStart() {
        super.onStart();
        if (mActivity != null) {
            mActivity.setStatusBarColor(mStatusBarColor);
            mActivity.setActionBarShown(true);
            mActivity.setActionBarTitle(getString(R.string.title_fragment_sign_up));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_OPERATION_IN_PROGRESS, mOperationInProgress);
    }

    @SuppressWarnings("ConstantConditions")
    private void setFormEditable(boolean editable) {
        if (mActivity != null) {
            mActivity.setDisplayHomeAsUpEnabled(editable);
            mActivity.setToolbarProgressEnabled(!editable);
        }

        // Clear the focus of the current focused text field.
        if (mCurrentFocusView != null) {
            mCurrentFocusView.clearFocus();
        }

        mFirstNameInputLayout.getEditText().setEnabled(editable);
        mLastNameInputLayout.getEditText().setEnabled(editable);
        //mCompanyInputLayout.getEditText().setEnabled(editable);
        mEmailInputLayout.getEditText().setEnabled(editable);
        mPasswordInputLayout.getEditText().setEnabled(editable);
        mTosCheckBox.setEnabled(editable);

        mCreateAccountButton.setEnabled(editable);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateFields() {
        return !TextUtils.isEmpty(mFirstNameInputLayout.getEditText().getText())
                && !TextUtils.isEmpty(mLastNameInputLayout.getEditText().getText())
                && isValidEmail(mEmailInputLayout.getEditText().getText())
                && isValidPassword(mPasswordInputLayout.getEditText().getText())
                && mTosCheckBox.isChecked();
    }

    private boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(CharSequence password) {
        return !TextUtils.isEmpty(password)/* && password.length() >= MIN_PASSWORD_LENGTH*/;
    }

    @SuppressWarnings("ConstantConditions")
    private void cleanUpForm() {
        mFirstNameInputLayout.getEditText().setText(null);
        mLastNameInputLayout.getEditText().setText(null);
        //mCompanyInputLayout.getEditText().setText(null);
        mEmailInputLayout.getEditText().setText(null);
        mPasswordInputLayout.getEditText().setText(null);
        mTosCheckBox.setChecked(false);

        setFormEditable(true);
        mCreateAccountButton.setEnabled(validateFields());
    }

    private void showSignUpSuccessDialog() {
        DialogFragment dialog = new SignUpSucceedDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), "signUpSucceed");
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {

        private static final String TAG = "SignUpTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setFormEditable(!(mOperationInProgress = true));
        }

        @Override
        protected String doInBackground(String... params) {
            String firstName = params[0];
            String lastName = params[1];
            String company = params[2];
            String email = params[3];
            String password = params[4];

            String message = null;

            try {
                Response response = SyncUtils.sWebService.createAccount(
                         firstName, lastName, email, password, company);
                Log.i(TAG, "doInBackground: response code " + response.getStatus());
            } catch (RetrofitError error) {
                error.printStackTrace();

                // TODO: Change this hardcoded string to string resources.
                if (error.getKind() == RetrofitError.Kind.NETWORK) {
                    message = "No Connection.";
                } else if (error.getKind() == RetrofitError.Kind.HTTP) {
                    Response response = error.getResponse();
                    String json = new String(((TypedByteArray) response.getBody()).getBytes());
                    RequestError reqError = new Gson().fromJson(json, RequestError.class);
                    message = reqError.getDescription();
                } else {
                    message = "Unknown error";
                }
            }

            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            // A null message means that the sign up process completed successfully.
            if (message == null) {
                cleanUpForm();
                showSignUpSuccessDialog();
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                setFormEditable(false);
            }

            mOperationInProgress = false;
            mSignUpTask = null;
        }
    }

    public static class SignUpSucceedDialogFragment extends DialogFragment {
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // TODO: Change this dialog hardcoded strings to string resources.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_PiuDonna_Dialog_Alert);
            builder.setTitle("Almost done!");
            builder.setMessage("You need to confirm your email");
            builder.setPositiveButton("Got it", null);
            return builder.create();
        }
    }
}
