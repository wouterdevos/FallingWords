package com.wouterdevos.fallingwords;

import com.google.gson.annotations.SerializedName;

public class WordPair {

    @SerializedName("text_eng")
    public String textEnglish;
    @SerializedName("text_spa")
    public String textSpanish;

    public WordPair(WordPair wordPair) {
        this.textEnglish = wordPair.textEnglish;
        this.textSpanish = wordPair.textSpanish;
    }
}
