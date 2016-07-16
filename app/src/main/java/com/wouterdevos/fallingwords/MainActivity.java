package com.wouterdevos.fallingwords;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<WordPair> wordPairs = new ArrayList<>();

    private TextView description;
    private Button startButton;
    private FallingWordsView fallingWordsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        populateLanguageItems();

        description = (TextView) findViewById(R.id.description);
        startButton = (Button) findViewById(R.id.start_button);
        fallingWordsView = (FallingWordsView) findViewById(R.id.falling_words_view);
        fallingWordsView.setWordPairs(wordPairs);
    }

    public void start(View view) {
        description.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        fallingWordsView.startGame();
    }

    private void populateLanguageItems() {
        Type listType = new TypeToken<ArrayList<WordPair>>(){}.getType();
        wordPairs = new GsonBuilder().create().fromJson(loadJsonFromAsset(), listType);
    }

    private String loadJsonFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("words.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
