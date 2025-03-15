// TranslationUtil.java
package ba.sum.fsre.hackaton.utils;

import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TranslationUtil {
    private static final String API_KEY = "AIzaSyCvUMmXKwr-4yhnI5LaqXCqOfFa8dWi9vo"; // Replace with your actual API key
    private static final String TAG = "TranslationUtil";

    public static void translateText(String text, String targetLanguage, TranslationCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // URL with API key
                    String apiUrl = "https://translation.googleapis.com/language/translate/v2?key=" + API_KEY
                            + "&q=" + URLEncoder.encode(text, "UTF-8")
                            + "&target=" + targetLanguage;

                    // Open connection
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Read response
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray translations = jsonResponse.getJSONObject("data").getJSONArray("translations");
                    return translations.getJSONObject(0).getString("translatedText");

                } catch (Exception e) {
                    Log.e(TAG, "Translation failed", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String translatedText) {
                if (callback != null) {
                    // Decode HTML entities
                    String decodedText = Html.fromHtml(translatedText).toString();
                    callback.onTranslationCompleted(decodedText);
                }
            }
        }.execute();
    }

    public interface TranslationCallback {
        void onTranslationCompleted(String translatedText);
    }
}