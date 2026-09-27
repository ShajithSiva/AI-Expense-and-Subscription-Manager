package com.example.aiexpensemanagementapplication.ai;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class SMSMNBClassifier {

    // =========================================================
    // MODEL DATA
    // =========================================================

    private final Map<String, Integer> vocabulary = new HashMap<>();

    private double[] idf;
    private double[] classLogPrior;
    private double[][] featureLogProb;

    private String[] classes;

    private boolean lowercase;
    private boolean sublinearTf;

    private int ngramMin;
    private int ngramMax;

    private boolean modelLoaded = false;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SMSMNBClassifier(Context context) {

        try {

            loadModel(context);

        } catch (Exception e) {

            e.printStackTrace();

            modelLoaded = false;
        }
    }


    // =========================================================
    // LOAD JSON MODEL
    // =========================================================

    private void loadModel(Context context) throws Exception {

        InputStream inputStream =
                context.getAssets().open("model1_mnb_android.json");

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(inputStream)
                );

        StringBuilder jsonBuilder =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {

            jsonBuilder.append(line);
        }

        reader.close();

        JSONObject model =
                new JSONObject(jsonBuilder.toString());


        // -----------------------------------------------------
        // Basic settings
        // -----------------------------------------------------

        lowercase =
                model.optBoolean(
                        "lowercase",
                        true
                );

        sublinearTf =
                model.optBoolean(
                        "sublinear_tf",
                        true
                );


        // -----------------------------------------------------
        // N-GRAM RANGE
        // -----------------------------------------------------

        JSONArray ngramRange =
                model.getJSONArray("ngram_range");

        ngramMin =
                ngramRange.getInt(0);

        ngramMax =
                ngramRange.getInt(1);


        // -----------------------------------------------------
        // CLASSES
        // -----------------------------------------------------

        JSONArray classArray =
                model.getJSONArray("classes");

        classes =
                new String[classArray.length()];

        for (int i = 0; i < classArray.length(); i++) {

            classes[i] =
                    classArray.getString(i);
        }


        // -----------------------------------------------------
        // VOCABULARY
        // -----------------------------------------------------

        JSONObject vocabularyObject =
                model.getJSONObject("vocabulary");

        java.util.Iterator<String> keys =
                vocabularyObject.keys();

        while (keys.hasNext()) {

            String key =
                    keys.next();

            vocabulary.put(
                    key,
                    vocabularyObject.getInt(key)
            );
        }


        // -----------------------------------------------------
        // IDF
        // -----------------------------------------------------

        JSONArray idfArray =
                model.getJSONArray("idf");

        idf =
                new double[idfArray.length()];

        for (int i = 0; i < idfArray.length(); i++) {

            idf[i] =
                    idfArray.getDouble(i);
        }


        // -----------------------------------------------------
        // CLASS LOG PRIOR
        // -----------------------------------------------------

        JSONArray priorArray =
                model.getJSONArray("class_log_prior");

        classLogPrior =
                new double[priorArray.length()];

        for (int i = 0; i < priorArray.length(); i++) {

            classLogPrior[i] =
                    priorArray.getDouble(i);
        }


        // -----------------------------------------------------
        // FEATURE LOG PROBABILITY
        // -----------------------------------------------------

        JSONArray featureArray =
                model.getJSONArray("feature_log_prob");

        featureLogProb =
                new double[featureArray.length()][];

        for (int i = 0;
             i < featureArray.length();
             i++) {

            JSONArray row =
                    featureArray.getJSONArray(i);

            featureLogProb[i] =
                    new double[row.length()];

            for (int j = 0;
                 j < row.length();
                 j++) {

                featureLogProb[i][j] =
                        row.getDouble(j);
            }
        }


        modelLoaded = true;

        System.out.println(
                "SMS MNB model loaded successfully."
        );

        System.out.println(
                "Vocabulary size: "
                        + vocabulary.size()
        );

        System.out.println(
                "Classes: "
                        + classes[0]
                        + ", "
                        + classes[1]
        );
    }


    // =========================================================
    // PREDICTION
    // =========================================================

    public Prediction predict(String sms) {

        if (!modelLoaded) {

            return new Prediction(
                    "ERROR",
                    0.0
            );
        }

        if (sms == null ||
                sms.trim().isEmpty()) {

            return new Prediction(
                    "Non-Transaction",
                    0.0
            );
        }


        String normalized =
                normalizeText(sms);


        Map<Integer, Double> features =
                createTfidfFeatures(normalized);


        double[] scores =
                new double[classes.length];


        // -----------------------------------------------------
        // MNB LOG-PROBABILITY
        // -----------------------------------------------------

        for (int classIndex = 0;
             classIndex < classes.length;
             classIndex++) {

            double score =
                    classLogPrior[classIndex];


            for (Map.Entry<Integer, Double> entry :
                    features.entrySet()) {

                int featureIndex =
                        entry.getKey();

                double tfidfValue =
                        entry.getValue();

                if (featureIndex >= 0 &&
                        featureIndex <
                                featureLogProb[classIndex].length) {

                    score +=
                            tfidfValue
                                    * featureLogProb[classIndex]
                                    [featureIndex];
                }
            }

            scores[classIndex] =
                    score;
        }


        // -----------------------------------------------------
        // FIND BEST CLASS
        // -----------------------------------------------------

        int bestClass = 0;

        for (int i = 1;
             i < scores.length;
             i++) {

            if (scores[i] >
                    scores[bestClass]) {

                bestClass = i;
            }
        }


        // -----------------------------------------------------
        // CONFIDENCE
        // -----------------------------------------------------

        double confidence =
                calculateConfidence(scores);


        return new Prediction(
                classes[bestClass],
                confidence
        );
    }


    // =========================================================
    // TEXT NORMALIZATION
    // =========================================================

    private String normalizeText(String text) {

        if (lowercase) {

            text =
                    text.toLowerCase();
        }

        // Match strip_accents="unicode"
        text =
                Normalizer.normalize(
                        text,
                        Normalizer.Form.NFD
                );

        text =
                text.replaceAll(
                        "\\p{M}+",
                        ""
                );

        // Normalize whitespace
        text =
                text.replaceAll(
                        "\\s+",
                        " "
                );

        return text.trim();
    }


    // =========================================================
    // TF-IDF
    // =========================================================

    private Map<Integer, Double>
    createTfidfFeatures(String text) {

        Map<Integer, Double> termCounts =
                new HashMap<>();


        // -----------------------------------------------------
        // Generate unigrams
        // -----------------------------------------------------

        String[] words =
                text.split("\\s+");


        for (int i = 0;
             i < words.length;
             i++) {

            String unigram =
                    words[i];

            addTerm(
                    unigram,
                    termCounts
            );


            // -------------------------------------------------
            // Generate bigrams
            // -------------------------------------------------

            if (ngramMax >= 2 &&
                    i < words.length - 1) {

                String bigram =
                        words[i]
                                + " "
                                + words[i + 1];

                addTerm(
                        bigram,
                        termCounts
                );
            }
        }


        // -----------------------------------------------------
        // Convert TF → TF-IDF
        // -----------------------------------------------------

        Map<Integer, Double> tfidf =
                new HashMap<>();


        for (Map.Entry<Integer, Double> entry :
                termCounts.entrySet()) {

            int index =
                    entry.getKey();

            double count =
                    entry.getValue();


            double tf;


            // Match sklearn sublinear_tf=True
            if (sublinearTf) {

                tf =
                        1.0
                                + Math.log(count);

            } else {

                tf = count;
            }


            if (index >= 0 &&
                    index < idf.length) {

                double value =
                        tf * idf[index];

                tfidf.put(
                        index,
                        value
                );
            }
        }


        // -----------------------------------------------------
        // L2 NORMALIZATION
        //
        // sklearn TfidfVectorizer default:
        // norm = "l2"
        // -----------------------------------------------------

        double squaredSum = 0.0;

        for (double value :
                tfidf.values()) {

            squaredSum +=
                    value * value;
        }


        double norm =
                Math.sqrt(squaredSum);


        if (norm > 0) {

            for (Map.Entry<Integer, Double> entry :
                    tfidf.entrySet()) {

                entry.setValue(
                        entry.getValue()
                                / norm
                );
            }
        }


        return tfidf;
    }


    // =========================================================
    // ADD TERM
    // =========================================================

    private void addTerm(
            String term,
            Map<Integer, Double> termCounts) {

        Integer index =
                vocabulary.get(term);

        if (index == null) {

            return;
        }

        Double current =
                termCounts.get(index);

        if (current == null) {

            termCounts.put(
                    index,
                    1.0
            );

        } else {

            termCounts.put(
                    index,
                    current + 1.0
            );
        }
    }


    // =========================================================
    // CONFIDENCE
    // =========================================================

    private double calculateConfidence(
            double[] scores) {

        double maxScore =
                scores[0];

        for (int i = 1;
             i < scores.length;
             i++) {

            if (scores[i] > maxScore) {

                maxScore =
                        scores[i];
            }
        }


        double sum =
                0.0;

        double[] probabilities =
                new double[scores.length];


        for (int i = 0;
             i < scores.length;
             i++) {

            probabilities[i] =
                    Math.exp(
                            scores[i]
                                    - maxScore
                    );

            sum +=
                    probabilities[i];
        }


        if (sum == 0) {

            return 0.0;
        }


        return
                (probabilities[0] / sum
                        * 100.0
                        > probabilities[1] / sum
                        * 100.0)

                        ?

                        (probabilities[0] / sum
                                * 100.0)

                        :

                        (probabilities[1] / sum
                                * 100.0);
    }


    // =========================================================
    // CHECK MODEL
    // =========================================================

    public boolean isModelLoaded() {

        return modelLoaded;
    }


    // =========================================================
    // PREDICTION RESULT
    // =========================================================

    public static class Prediction {

        private final String label;

        private final double confidence;


        public Prediction(
                String label,
                double confidence) {

            this.label =
                    label;

            this.confidence =
                    confidence;
        }


        public String getLabel() {

            return label;
        }


        public double getConfidence() {

            return confidence;
        }


        public boolean isTransaction() {

            return "Transaction"
                    .equalsIgnoreCase(label);
        }


        @Override
        public String toString() {

            return
                    "Prediction{" +
                            "label='" +
                            label +
                            '\'' +
                            ", confidence=" +
                            confidence +
                            '}';
        }
    }
}