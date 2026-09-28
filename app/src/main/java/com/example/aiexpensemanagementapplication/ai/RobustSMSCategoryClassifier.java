package com.example.aiexpensemanagementapplication.ai;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RobustSMSCategoryClassifier {

    private static final String TAG = "SMSCategoryModel";
    private static final String MODEL_FILE =
            "robust_expense_category_android_model.json";

    private final Context context;

    private final List<String> classes = new ArrayList<>();

    private final Map<String, Integer> wordVocabulary = new HashMap<>();
    private final Map<String, Integer> charVocabulary = new HashMap<>();

    private double[] wordIdf;
    private double[] charIdf;

    private double[][] coefficients;
    private double[] intercepts;

    private int wordFeatureCount;
    private int charFeatureCount;

    private int wordNgramMin;
    private int wordNgramMax;

    private int charNgramMin;
    private int charNgramMax;

    private boolean wordSublinearTf;
    private boolean charSublinearTf;

    private String wordNorm;
    private String charNorm;

    private boolean modelLoaded = false;

    public RobustSMSCategoryClassifier(Context context) {
        this.context = context.getApplicationContext();
        loadModel();
    }

    // =========================================================
    // LOAD MODEL JSON
    // =========================================================

    private void loadModel() {

        try {

            String jsonText = readAssetFile(MODEL_FILE);
            JSONObject root = new JSONObject(jsonText);

            // -------------------------
            // Load class names
            // -------------------------

            JSONArray classArray = root.getJSONArray("classes");

            for (int i = 0; i < classArray.length(); i++) {
                classes.add(classArray.getString(i));
            }

            // -------------------------
            // Load Word TF-IDF
            // -------------------------

            JSONObject wordObject = root.getJSONObject("word_tfidf");

            loadVocabulary(
                    wordObject.getJSONObject("vocabulary"),
                    wordVocabulary
            );

            wordIdf = jsonArrayToDoubleArray(
                    wordObject.getJSONArray("idf")
            );

            wordFeatureCount =
                    wordObject.getInt("feature_count");

            JSONArray wordNgram =
                    wordObject.getJSONArray("ngram_range");

            wordNgramMin = wordNgram.getInt(0);
            wordNgramMax = wordNgram.getInt(1);

            wordSublinearTf =
                    wordObject.optBoolean("sublinear_tf", true);

            wordNorm =
                    wordObject.optString("norm", "l2");

            // -------------------------
            // Load Character TF-IDF
            // -------------------------

            JSONObject charObject = root.getJSONObject("char_tfidf");

            loadVocabulary(
                    charObject.getJSONObject("vocabulary"),
                    charVocabulary
            );

            charIdf = jsonArrayToDoubleArray(
                    charObject.getJSONArray("idf")
            );

            charFeatureCount =
                    charObject.getInt("feature_count");

            JSONArray charNgram =
                    charObject.getJSONArray("ngram_range");

            charNgramMin = charNgram.getInt(0);
            charNgramMax = charNgram.getInt(1);

            charSublinearTf =
                    charObject.optBoolean("sublinear_tf", true);

            charNorm =
                    charObject.optString("norm", "l2");

            // -------------------------
            // Load Linear SVM
            // -------------------------

            JSONObject svmObject = root.getJSONObject("svm");

            coefficients =
                    jsonArrayTo2DDoubleArray(
                            svmObject.getJSONArray("coefficients")
                    );

            intercepts =
                    jsonArrayToDoubleArray(
                            svmObject.getJSONArray("intercepts")
                    );

            modelLoaded = true;

            Log.d(
                    TAG,
                    "Model loaded successfully. Classes=" + classes.size()
                            + ", WordFeatures=" + wordFeatureCount
                            + ", CharFeatures=" + charFeatureCount
            );

        } catch (Exception e) {

            modelLoaded = false;

            Log.e(
                    TAG,
                    "Failed to load SMS category model",
                    e
            );
        }
    }

    // =========================================================
    // PUBLIC PREDICTION METHOD
    // =========================================================

    public String predict(String smsText) {

        if (!modelLoaded) {
            Log.e(TAG, "Prediction failed: model is not loaded");
            return "Others";
        }

        if (smsText == null || smsText.trim().isEmpty()) {
            return "Others";
        }

        try {

            String cleanText = preprocess(smsText);

            double[] wordFeatures =
                    createWordTfidf(cleanText);

            double[] charFeatures =
                    createCharacterTfidf(cleanText);

            double[] combinedFeatures =
                    combineFeatures(
                            wordFeatures,
                            charFeatures
                    );

            int predictedIndex =
                    predictClassIndex(combinedFeatures);

            String predictedCategory =
                    classes.get(predictedIndex);

            Log.d(
                    TAG,
                    "SMS: " + smsText
                            + " | Clean: " + cleanText
                            + " | Category: " + predictedCategory
            );

            return predictedCategory;

        } catch (Exception e) {

            Log.e(TAG, "Prediction error", e);

            return "Others";
        }
    }

    // =========================================================
    // PREPROCESSING
    // =========================================================

    private String preprocess(String text) {

        String result = text.toLowerCase();

        // Account / card masked references
        result = result.replaceAll(
                "(?i)(?:a/c|acct|account|card)\\s*(?:no\\.?\\s*)?(?:xx+|\\*+)?\\d{2,}",
                " account_token "
        );

        // Dates: 12/08/2026, 12-08-2026, etc.
        result = result.replaceAll(
                "\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b",
                " date_token "
        );

        // Times: 10:45, 10:45 am
        result = result.replaceAll(
                "\\b\\d{1,2}:\\d{2}(?:\\s?[ap]m)?\\b",
                " time_token "
        );

        // Reference / transaction numbers
        result = result.replaceAll(
                "(?i)(?:ref|reference|txn|transaction)\\s*(?:no\\.?|id)?\\s*[:#-]?\\s*[a-z0-9-]+",
                " reference_token "
        );

        // Currency amounts
        result = result.replaceAll(
                "(?i)(?:lkr|rs\\.?|රු\\.?)\\s*\\d[\\d,]*(?:\\.\\d{1,2})?",
                " amount_token "
        );

        // Currency amount where currency appears after number
        result = result.replaceAll(
                "(?i)\\b\\d[\\d,]*(?:\\.\\d{1,2})?\\s*(?:lkr|rs\\.?)\\b",
                " amount_token "
        );

        // Normalize spaces
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

    // =========================================================
    // WORD TF-IDF
    // =========================================================

    private double[] createWordTfidf(String text) {

        double[] vector =
                new double[wordFeatureCount];

        Map<Integer, Integer> termFrequency =
                new HashMap<>();

        String[] words =
                text.split("\\s+");

        for (int n = wordNgramMin; n <= wordNgramMax; n++) {

            for (int i = 0; i <= words.length - n; i++) {

                StringBuilder ngram =
                        new StringBuilder();

                for (int j = 0; j < n; j++) {

                    if (j > 0) {
                        ngram.append(" ");
                    }

                    ngram.append(words[i + j]);
                }

                Integer index =
                        wordVocabulary.get(
                                ngram.toString()
                        );

                if (index != null) {

                    termFrequency.put(
                            index,
                            termFrequency.getOrDefault(index, 0) + 1
                    );
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry
                : termFrequency.entrySet()) {

            int index = entry.getKey();
            int count = entry.getValue();

            double tf;

            if (wordSublinearTf) {
                tf = 1.0 + Math.log(count);
            } else {
                tf = count;
            }

            vector[index] =
                    tf * wordIdf[index];
        }

        if ("l2".equalsIgnoreCase(wordNorm)) {
            l2Normalize(vector);
        }

        return vector;
    }

    // =========================================================
    // CHARACTER TF-IDF
    // sklearn analyzer="char_wb" compatible approach
    // =========================================================

    private double[] createCharacterTfidf(String text) {

        double[] vector =
                new double[charFeatureCount];

        Map<Integer, Integer> termFrequency =
                new HashMap<>();

        String normalized =
                text.replaceAll("\\s+", " ").trim();

        if (normalized.isEmpty()) {
            return vector;
        }

        String[] words =
                normalized.split(" ");

        for (String word : words) {

            String paddedWord =
                    " " + word + " ";

            for (int n = charNgramMin; n <= charNgramMax; n++) {

                if (paddedWord.length() < n) {
                    continue;
                }

                for (int i = 0;
                     i <= paddedWord.length() - n;
                     i++) {

                    String ngram =
                            paddedWord.substring(i, i + n);

                    Integer index =
                            charVocabulary.get(ngram);

                    if (index != null) {

                        termFrequency.put(
                                index,
                                termFrequency.getOrDefault(index, 0) + 1
                        );
                    }
                }
            }
        }

        for (Map.Entry<Integer, Integer> entry
                : termFrequency.entrySet()) {

            int index = entry.getKey();
            int count = entry.getValue();

            double tf;

            if (charSublinearTf) {
                tf = 1.0 + Math.log(count);
            } else {
                tf = count;
            }

            vector[index] =
                    tf * charIdf[index];
        }

        if ("l2".equalsIgnoreCase(charNorm)) {
            l2Normalize(vector);
        }

        return vector;
    }

    // =========================================================
    // COMBINE WORD + CHARACTER FEATURES
    // =========================================================

    private double[] combineFeatures(
            double[] wordFeatures,
            double[] charFeatures
    ) {

        double[] combined =
                new double[
                        wordFeatures.length
                                + charFeatures.length
                        ];

        System.arraycopy(
                wordFeatures,
                0,
                combined,
                0,
                wordFeatures.length
        );

        System.arraycopy(
                charFeatures,
                0,
                combined,
                wordFeatures.length,
                charFeatures.length
        );

        return combined;
    }

    // =========================================================
    // LINEAR SVM PREDICTION
    // score = W.x + b
    // =========================================================

    private int predictClassIndex(double[] features) {

        int bestClass = 0;
        double bestScore =
                Double.NEGATIVE_INFINITY;

        for (int classIndex = 0;
             classIndex < coefficients.length;
             classIndex++) {

            double score =
                    intercepts[classIndex];

            double[] classWeights =
                    coefficients[classIndex];

            int featureLength =
                    Math.min(
                            features.length,
                            classWeights.length
                    );

            for (int i = 0;
                 i < featureLength;
                 i++) {

                if (features[i] != 0.0) {

                    score +=
                            classWeights[i]
                                    * features[i];
                }
            }

            if (score > bestScore) {

                bestScore = score;
                bestClass = classIndex;
            }
        }

        return bestClass;
    }

    // =========================================================
    // L2 NORMALIZATION
    // =========================================================

    private void l2Normalize(double[] vector) {

        double sumSquares = 0.0;

        for (double value : vector) {
            sumSquares += value * value;
        }

        if (sumSquares == 0.0) {
            return;
        }

        double norm =
                Math.sqrt(sumSquares);

        for (int i = 0; i < vector.length; i++) {
            vector[i] =
                    vector[i] / norm;
        }
    }

    // =========================================================
    // JSON HELPERS
    // =========================================================

    private void loadVocabulary(
            JSONObject jsonVocabulary,
            Map<String, Integer> destination
    ) throws Exception {

        Iterator<String> keys =
                jsonVocabulary.keys();

        while (keys.hasNext()) {

            String key = keys.next();

            destination.put(
                    key,
                    jsonVocabulary.getInt(key)
            );
        }
    }

    private double[] jsonArrayToDoubleArray(
            JSONArray array
    ) throws Exception {

        double[] result =
                new double[array.length()];

        for (int i = 0; i < array.length(); i++) {
            result[i] =
                    array.getDouble(i);
        }

        return result;
    }

    private double[][] jsonArrayTo2DDoubleArray(
            JSONArray array
    ) throws Exception {

        double[][] result =
                new double[array.length()][];

        for (int i = 0; i < array.length(); i++) {

            JSONArray row =
                    array.getJSONArray(i);

            result[i] =
                    jsonArrayToDoubleArray(row);
        }

        return result;
    }

    // =========================================================
    // READ FILE FROM ASSETS
    // =========================================================

    private String readAssetFile(
            String fileName
    ) throws Exception {

        InputStream inputStream =
                context.getAssets().open(fileName);

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(inputStream)
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        inputStream.close();

        return builder.toString();
    }

    // =========================================================
    // MODEL STATUS
    // =========================================================

    public boolean isModelLoaded() {
        return modelLoaded;
    }

    public List<String> getClasses() {
        return new ArrayList<>(classes);
    }
}