package com.smartsecurity.android.piudonnacouture.ui.checkout;

import android.content.Context;
import android.graphics.drawable.LevelListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.Pair;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.smartsecurity.android.piudonnacouture.R;
import com.smartsecurity.android.piudonnacouture.model.CreditCard;
import com.smartsecurity.android.piudonnacouture.ui.CheckoutActivity;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentCheckoutFragment extends CheckoutFragment {
    private static final String TAG = "PaymentCheckoutFragment";

    public static PaymentCheckoutFragment newInstance() {
        return new PaymentCheckoutFragment();
    }

    private static final String STATE_CURRENT_CARD_TYPE = "state_current_card_type";

    private static final int MAX_LENGTH_EXP_DATE = 5;

    private static Pattern[] sCCPatterns = {
            Pattern.compile("^4[0-9]{2,12}(?:[0-9]{3})?$"), // Visa.
            Pattern.compile("^5[1-5][0-9]{1,14}$"),         // Master Card.
            Pattern.compile("^3[47][0-9]{1,13}$")           // American Express.
    };

    private static int COLOR_ERR0R = 0xFFFF4444;
    private static ForegroundColorSpan sErrorColorSpan = new ForegroundColorSpan(COLOR_ERR0R);

    private TextInputLayout mCardHolderInputLayout;
    private TextInputLayout mCardNumberInputLayout;
    private TextInputLayout mCardExpDateInputLayout;
    private TextInputLayout mCardCvvInputLayout;
    private Button mProceedToReviewButton;

    private CheckoutActivity mActivity;
    private View mCurrentFocusView;

    // Credit card icons wrapped in a Level List Drawable.
    private LevelListDrawable mCreditCardIcon;

    private CreditCard.CardType mCurrentCardType = CreditCard.CardType.OTHER;
    private int mCurrentMonth;
    private int mCurrentYear;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (CheckoutActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checkout_payment, container, false);

        mCardHolderInputLayout = (TextInputLayout) rootView.findViewById(R.id.card_holder_input_layout);
        mCardHolderInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mCardHolderInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mCardNumberInputLayout = (TextInputLayout) rootView.findViewById(R.id.card_number_input_layout);
        mCardNumberInputLayout.getEditText().addTextChangedListener(mCreditCardTextWatcher);
        mCardNumberInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mCardNumberInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mCardExpDateInputLayout = (TextInputLayout) rootView.findViewById(R.id.exp_date_input_layout);
        mCardExpDateInputLayout.getEditText().addTextChangedListener(mExpDateTextWatcher);
        mCardExpDateInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mCardExpDateInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mCardCvvInputLayout = (TextInputLayout) rootView.findViewById(R.id.cvv_input_layout);
        mCardCvvInputLayout.getEditText().addTextChangedListener(mTextWatcher);
        mCardCvvInputLayout.getEditText().setOnFocusChangeListener(mFocusChangeListener);

        mProceedToReviewButton = (Button) rootView.findViewById(R.id.proceed_to_review_button);
        mProceedToReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToReview();
            }
        });

        Calendar calendar = Calendar.getInstance();
        mCurrentMonth = calendar.get(Calendar.MONTH) + 1;
        mCurrentYear = calendar.get(Calendar.YEAR) - 2000;

        if (savedInstanceState != null) {
            mCurrentCardType = CreditCard.CardType.fromValue(
                    savedInstanceState.getInt(STATE_CURRENT_CARD_TYPE));
        }

        ImageView creditCardImageView = (ImageView) rootView.findViewById(R.id.credit_card_image_view);
        mCreditCardIcon = (LevelListDrawable) creditCardImageView.getDrawable();
        mCreditCardIcon.setEnterFadeDuration(200);
        mCreditCardIcon.setExitFadeDuration(200);
        mCreditCardIcon.setLevel(mCurrentCardType.getValue());
        mCreditCardIcon.jumpToCurrentState();

        mCardNumberInputLayout.getEditText().setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(mCurrentCardType.getMaxLength()) });

        mCardCvvInputLayout.getEditText().setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(mCurrentCardType.getCvvMaxLenght()) });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_CARD_TYPE, mCurrentCardType.getValue());
    }

    @Override
    public boolean isCompleted() {
        return validateFields();
    }

    @SuppressWarnings("ConstantConditions")
    private boolean validateFields() {
        // This uses '&' operator, which is the non-short-circuit AND.
        return isValidCardNumber(mCardNumberInputLayout.getEditText().getEditableText())
                & isValidExpDate(mCardExpDateInputLayout.getEditText().getEditableText())
                & isValidCvv(mCardCvvInputLayout.getEditText().getText())
                & !TextUtils.isEmpty(mCardHolderInputLayout.getEditText().getText());
    }

    private boolean isValidCardNumber(Editable s) {
        if (TextUtils.isEmpty(s) || s.length() != mCurrentCardType.getMaxLength()) {
            s.removeSpan(sErrorColorSpan);
            return false;
        }

        boolean isValid = luhnCheck(s.toString());

        if (!isValid) {
            s.setSpan(sErrorColorSpan, 0, s.length(), 0);
        }

        return isValid;
    }

    private boolean luhnCheck(String formattedCardNumber) {
        // Remove al non-digit characters.
        String cardNumber = formattedCardNumber.replaceAll("\\D", "");

        // Convert the String into an array of digits.
        int[] digits = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            digits[i] = Character.getNumericValue(cardNumber.charAt(i));
        }

        int sum = 0;
        boolean alt = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            if (alt) {
                digits[i] *= 2;
                if (digits[i] > 9) {
                    digits[i] -= 9;
                }
            }

            sum += digits[i];
            alt = !alt;
        }

        return sum % 10 == 0;
    }

    private boolean isValidExpDate(Editable s) {
        if (TextUtils.isEmpty(s) || s.length() != MAX_LENGTH_EXP_DATE) {
            s.removeSpan(sErrorColorSpan);
            return false;
        }

        String text = s.toString();
        int month = -1;
        int year = -1;

        int slashIndex = text.indexOf('/');
        if (slashIndex != -1) {
            month = Integer.parseInt(text.substring(0, slashIndex));
            year = Integer.parseInt(text.substring(slashIndex + 1, text.length()));
        }

        boolean isValid = (year > mCurrentYear && month > 0 && month <= 12)
                || (year == mCurrentYear && month >= mCurrentMonth && month <= 12);

        if (!isValid) {
            s.setSpan(sErrorColorSpan, 0, s.length(), 0);
        }

        return isValid;
    }

    private boolean isValidCvv(CharSequence cvv) {
        return !TextUtils.isEmpty(cvv) && cvv.length() == mCurrentCardType.getCvvMaxLenght();
    }

    @SuppressWarnings("ConstantConditions")
    private void proceedToReview() {
        Pair<String, String> expDate = getExpDate();

        CreditCard card = new CreditCard(mCurrentCardType,
                mCardHolderInputLayout.getEditText().getText().toString(),
                getCardNumber(),
                mCardCvvInputLayout.getEditText().getText().toString(),
                expDate.first,
                expDate.second);

        clearFocus();
        mActivity.proceedToReview(card);
    }

    private String getCardNumber() {
        return mCardNumberInputLayout.getEditText().getText().toString().replaceAll("\\D","");
    }

    private Pair<String, String> getExpDate() {
        String text = mCardExpDateInputLayout.getEditText().getText().toString();
        String month;
        String year;

        int slashIndex = text.indexOf('/');
        month = text.substring(0, slashIndex);
        year = text.substring(slashIndex + 1, text.length());

        return new Pair<>(month, year);
    }

    private void clearFocus() {
        if (mCurrentFocusView != null) {
            mCurrentFocusView.clearFocus();
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mProceedToReviewButton.setEnabled(validateFields());
        }
    };

    private TextWatcher mCreditCardTextWatcher = new TextWatcher() {
        private static final String TAG = "CreditCardTextWatcher";

        private char mCardNumberSeparator = ' ';
        private boolean mManualTrigger = false;

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void afterTextChanged(Editable s) {
            // In case of a trigger of this method caused by a manual change, ignore it.
            if (mManualTrigger) {
                mManualTrigger = false;
                return;
            }

            // Remove all non-digit characters from the input text.
            String text = s.toString().replaceAll("\\D", "");
            String formatted;
            int level;

            // Look for the credit card type.
            for (level = 0; level < sCCPatterns.length; level++) {
                Pattern pattern = sCCPatterns[level];
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    break;
                }
            }

            if (mCurrentCardType.getValue() != level) {
                Log.d(TAG, "afterTextChanged: changing filters");
                mCurrentCardType = CreditCard.CardType.fromValue(level);

                s.setFilters(new InputFilter[]{
                        new InputFilter.LengthFilter(mCurrentCardType.getMaxLength()) });

                mCardCvvInputLayout.getEditText().setFilters(new InputFilter[] {
                        new InputFilter.LengthFilter(mCurrentCardType.getCvvMaxLenght()) });
            }

            // Mask the text depending on the card type.
            formatted = formatCardNumber(text, mCurrentCardType);

            mManualTrigger = true;
            mCreditCardIcon.setLevel(level);
            s.replace(0, s.length(), formatted);
        }

        private String formatCardNumber(String text, CreditCard.CardType type) {
            switch (type) {
                case AMERICAN_EXPRESS:
                    return formatAmex(text);
                default:
                    return formatVisa(text);
            }
        }

        // Format: XXXX XXXX XXXX XXXX
        private String formatVisa(String text) {
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                if (i > 0  &&  i % 4 == 0) {
                    formatted.append(mCardNumberSeparator);
                }
                formatted.append(text.charAt(i));
            }

            return formatted.toString();
        }

        // Format: XXXX XXXXXX XXXXX
        private String formatAmex(String text) {
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                if (i > 0 && (i == 4 || i == 10)) {
                    formatted.append(mCardNumberSeparator);
                }
                formatted.append(text.charAt(i));
            }

            return formatted.toString();
        }
    };

    private TextWatcher mExpDateTextWatcher = new TextWatcher() {

        public CharSequence mDateSeparator = "/";
        private boolean mManualTrigger = false;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mManualTrigger) {
                mManualTrigger = false;
                return;
            }

            // Remove all non-number characters from the input text.
            String text = s.toString().replaceAll("[^0-9]", "");
            String month = text.length() >= 2 ? text.substring(0, 2) : null;
            String year = text.length() >= 4 ? text.substring(2, 4) : null;

            // Mask the text.
            if (text.length() > 2) {
                text = new StringBuilder(text).insert(2, mDateSeparator).toString();
            }

            mManualTrigger = true;
            s.replace(0, s.length(), text);
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

    private class CreditCardKeyListener extends NumberKeyListener {

        private final char[] CHARACTERS = {
                 '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ' };

        @Override
        protected char[] getAcceptedChars() {
            return CHARACTERS;
        }

        @Override
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER;
        }
    }
}
