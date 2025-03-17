// TranslationUtil.java
package ba.sum.fsre.hackaton.utils;

import android.os.AsyncTask;
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

    public interface TranslationCallback {
        void onTranslationCompleted(String translatedText);
    }

    public static void translateText(String text, String targetLanguage, TranslationCallback callback) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Text to translate is null or empty");
            callback.onTranslationCompleted(null);
            return;
        }

        if (targetLanguage == null || targetLanguage.isEmpty()) {
            Log.e(TAG, "Target language is null or empty");
            callback.onTranslationCompleted(null);
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String encodedText = URLEncoder.encode(text, "UTF-8");
                    String urlStr = String.format("https://translation.googleapis.com/language/translate/v2?key=%s&q=%s&target=%s", API_KEY, encodedText, targetLanguage);
                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        return parseTranslatedText(response.toString());
                    } else {
                        Log.e(TAG, "Translation failed with response code: " + responseCode);
                        return null;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception during translation", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String translatedText) {
                if (callback != null) {
                    callback.onTranslationCompleted(translatedText);
                }
            }
        }.execute();
    }

    private static String parseTranslatedText(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray translations = jsonObject.getJSONObject("data").getJSONArray("translations");
            return translations.getJSONObject(0).getString("translatedText");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing translation response", e);
            return null;
        }
    }
}