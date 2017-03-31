package com.smartsecurity.android.piudonnacouture.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class CreditCard implements Parcelable {
    private static final String TAG = "CreditCard";

    public enum CardType {
        VISA(0, "Visa", 19, 3),
        MASTER_CARD(1, "Master Card", 19, 3),
        AMERICAN_EXPRESS(2, "American Express", 17, 4),
        OTHER(3, "Other", 19, 3);

        private int value;
        private String name;
        private int numberLength; // max length with separators.
        private int cvvLength;

        CardType(int value, String name, int numberLength, int cvvLength) {
            this.value = value;
            this.name = name;
            this.numberLength = numberLength;
            this.cvvLength = cvvLength;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public int getMaxLength() {
            return numberLength;
        }

        public int getCvvMaxLenght() {
            return cvvLength;
        }

        public static CardType fromValue(int value) {
            return CardType.values()[value];
        }
    }

    private String cardHolder;

    private String number;

    private String cvv;

    private String expMonth;

    private String expYear;

    private CardType type;

    public CreditCard(CardType type, String cardHolder, String number, String cvv, String expMonth, String expYear) {
        this.cardHolder = cardHolder;
        this.number = number;
        this.cvv = cvv;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.type = type;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public String getNumber() {
        return number;
    }

    public String getCvv() {
        return cvv;
    }

    public String getLastFourNumbers() {
        return (number != null && number.length() >= 4) ?
                number.substring(number.length() - 4) : null;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public String getExpYear() {
        return expYear;
    }

    public CardType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CreditCard{" +
                "cardHolder='" + cardHolder + '\'' +
                ", number='" + number + '\'' +
                ", cvv='" + cvv + '\'' +
                ", expMonth='" + expMonth + '\'' +
                ", expYear='" + expYear + '\'' +
                ", type=" + type.getName() +
                '}';
    }

    protected CreditCard(Parcel in) {
        Log.w(TAG, "CreditCard(Parcel in)");
        cardHolder = in.readString();
        number = in.readString();
        cvv = in.readString();
        expMonth = in.readString();
        expYear = in.readString();
        //type = CardType.fromValue(in.readInt());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.w(TAG, "writeToParcel: ");
        dest.writeString(cardHolder);
        dest.writeString(number);
        dest.writeString(cvv);
        dest.writeString(expMonth);
        dest.writeString(expYear);
        //dest.writeInt(type.getValue());
    }

    public static final Creator<CreditCard> CREATOR = new Creator<CreditCard>() {
        @Override
        public CreditCard createFromParcel(Parcel in) {
            return new CreditCard(in);
        }

        @Override
        public CreditCard[] newArray(int size) {
            return new CreditCard[size];
        }
    };
}