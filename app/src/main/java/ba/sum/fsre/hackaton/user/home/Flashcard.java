package ba.sum.fsre.hackaton.user.home;

import android.os.Parcel;
import android.os.Parcelable;

public class Flashcard implements Parcelable {
    private int imageResId;
    private String nativeWord;
    private String translatedWord;

    public Flashcard(int imageResId, String nativeWord, String translatedWord) {
        this.imageResId = imageResId;
        this.nativeWord = nativeWord;
        this.translatedWord = translatedWord;
    }

    protected Flashcard(Parcel in) {
        imageResId = in.readInt();
        nativeWord = in.readString();
        translatedWord = in.readString();
    }

    public static final Creator<Flashcard> CREATOR = new Creator<Flashcard>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }

        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageResId);
        dest.writeString(nativeWord);
        dest.writeString(translatedWord);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getNativeWord() {
        return nativeWord;
    }

    public String getTranslatedWord() {
        return translatedWord;
    }
}