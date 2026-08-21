package com.example.aiexpensemanagementapplication.ai;

import android.content.Context;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubscriptionDetectionAI {

    private static final String MODEL_FILE =
            "subscription_email_model.tflite";

    private static final String VOCAB_FILE =
            "subscription_vocab.json";

    private static final int SEQUENCE_LENGTH = 150;

    private final Interpreter interpreter;

    private final Map<String, Integer> vocabulary =
            new HashMap<>();


    // =========================================================
    // Constructor
    // =========================================================

    public SubscriptionDetectionAI(Context context)
            throws Exception {

        loadVocabulary(context);

        MappedByteBuffer model =
                loadModel(context);

        interpreter =
                new Interpreter(model);
    }


    // =========================================================
    // Load TFLite model
    // =========================================================

    private MappedByteBuffer loadModel(
            Context context
    ) throws IOException {

        android.content.res.AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd(MODEL_FILE);

        FileInputStream inputStream =
                new FileInputStream(
                        fileDescriptor.getFileDescriptor()
                );

        FileChannel fileChannel =
                inputStream.getChannel();

        long startOffset =
                fileDescriptor.getStartOffset();

        long declaredLength =
                fileDescriptor.getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }


    // =========================================================
    // Load vocabulary
    // =========================================================

    private void loadVocabulary(
            Context context
    ) throws Exception {

        InputStream inputStream =
                context.getAssets()
                        .open(VOCAB_FILE);

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                "UTF-8"
                        )
                );

        StringBuilder builder =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();

        JSONObject json =
                new JSONObject(
                        builder.toString()
                );

        Iterator<String> keys =
                json.keys();

        while (keys.hasNext()) {

            String word =
                    keys.next();

            int id =
                    json.getInt(word);

            vocabulary.put(
                    word,
                    id
            );
        }
    }


    // =========================================================
    // Clean text
    // =========================================================

    private String cleanText(
            String text
    ) {

        if (text == null) {
            return "";
        }

        text =
                text.toLowerCase(
                        Locale.US
                );

        text =
                text.replaceAll(
                        "[^a-z0-9\\s]",
                        " "
                );

        text =
                text.replaceAll(
                        "\\s+",
                        " "
                );

        return text.trim();
    }


    // =========================================================
    // Text → sequence
    // =========================================================

    private int[] textToSequence(
            String text
    ) {

        String cleaned =
                cleanText(text);

        String[] words;

        if (cleaned.isEmpty()) {
            words = new String[0];
        } else {
            words = cleaned.split("\\s+");
        }

        int[] sequence =
                new int[SEQUENCE_LENGTH];

        int index = 0;

        for (String word : words) {

            if (index >= SEQUENCE_LENGTH) {
                break;
            }

            Integer tokenId =
                    vocabulary.get(word);

            if (tokenId == null) {

                tokenId =
                        vocabulary.get("<UNK>");

                if (tokenId == null) {
                    tokenId = 1;
                }
            }

            sequence[index] =
                    tokenId;

            index++;
        }

        // Remaining values are 0 = <PAD>

        return sequence;
    }


    // =========================================================
    // Prediction result
    // =========================================================

    public static class PredictionResult {

        private final String label;

        private final float confidence;


        public PredictionResult(
                String label,
                float confidence
        ) {

            this.label =
                    label;

            this.confidence =
                    confidence;
        }


        public String getLabel() {
            return label;
        }


        public float getConfidence() {
            return confidence;
        }


        public boolean isSubscription() {

            return "Subscription".equals(
                    label
            );
        }
    }


    // =========================================================
    // Subscription details
    // =========================================================

    public static class SubscriptionDetails {

        private String serviceName;

        private double amount;

        private String billingCycle;

        private String nextBillingDate;


        public SubscriptionDetails(
                String serviceName,
                double amount,
                String billingCycle,
                String nextBillingDate
        ) {

            this.serviceName =
                    serviceName;

            this.amount =
                    amount;

            this.billingCycle =
                    billingCycle;

            this.nextBillingDate =
                    nextBillingDate;
        }


        public String getServiceName() {
            return serviceName;
        }


        public double getAmount() {
            return amount;
        }


        public String getBillingCycle() {
            return billingCycle;
        }


        public String getNextBillingDate() {
            return nextBillingDate;
        }
    }


    // =========================================================
    // Predict email
    // =========================================================

    public PredictionResult predict(
            String subject,
            String snippet
    ) {

        String combinedText =
                (subject == null ? "" : subject)
                        + " "
                        + (snippet == null ? "" : snippet);


        int[] sequence =
                textToSequence(
                        combinedText
                );


        int[][] input =
                new int[1][SEQUENCE_LENGTH];


        System.arraycopy(
                sequence,
                0,
                input[0],
                0,
                SEQUENCE_LENGTH
        );


        float[][] output =
                new float[1][1];


        interpreter.run(
                input,
                output
        );


        float probability =
                output[0][0];


        // =====================================================
        // NaN / Infinity protection
        // =====================================================

        if (Float.isNaN(probability)
                || Float.isInfinite(probability)) {

            return new PredictionResult(
                    "Not Subscription",
                    0.0f
            );
        }


        // Keep probability inside 0-1
        probability =
                Math.max(
                        0.0f,
                        Math.min(
                                1.0f,
                                probability
                        )
                );


        String label;

        float confidence;


        if (probability >= 0.5f) {

            label =
                    "Subscription";

            confidence =
                    probability;

        } else {

            label =
                    "Not Subscription";

            confidence =
                    1.0f - probability;
        }


        return new PredictionResult(
                label,
                confidence
        );
    }


    // =========================================================
    // Extract subscription details
    // =========================================================

    public SubscriptionDetails extractSubscriptionDetails(
            String sender,
            String subject,
            String snippet
    ) {

        String safeSender =
                sender == null ? "" : sender;

        String safeSubject =
                subject == null ? "" : subject;

        String safeSnippet =
                snippet == null ? "" : snippet;


        String text =
                safeSender
                        + " "
                        + safeSubject
                        + " "
                        + safeSnippet;


        String lowerText =
                text.toLowerCase(
                        Locale.US
                );


        // =====================================================
        // SERVICE NAME
        // =====================================================

        String serviceName =
                extractServiceName(
                        safeSender,
                        lowerText
                );


        // =====================================================
        // AMOUNT
        // =====================================================

        double amount =
                extractAmount(
                        text
                );


        // =====================================================
        // BILLING CYCLE
        // =====================================================

        String billingCycle =
                extractBillingCycle(
                        lowerText
                );


        // =====================================================
        // NEXT BILLING DATE
        // =====================================================

        String nextBillingDate =
                extractBillingDate(
                        lowerText
                );


        return new SubscriptionDetails(
                serviceName,
                amount,
                billingCycle,
                nextBillingDate
        );
    }


    // =========================================================
    // Extract service name
    // =========================================================

    private String extractServiceName(
            String sender,
            String lowerText
    ) {

        String[] knownServices = {

                "netflix",
                "spotify",
                "youtube",
                "youtube premium",
                "apple music",
                "icloud",
                "amazon prime",
                "amazon",
                "disney plus",
                "disney+",
                "chatgpt",
                "openai",
                "canva",
                "adobe",
                "dropbox",
                "google one",
                "google",
                "microsoft 365",
                "microsoft",
                "linkedin",
                "grammarly",
                "github",
                "zoom",
                "notion",
                "coursera",
                "duolingo",
                "booking.com",
                "daraz",
                "pickme",
                "uber"
        };


        for (String service :
                knownServices) {

            if (lowerText.contains(service)) {

                return capitalizeServiceName(
                        service
                );
            }
        }


        // =====================================================
        // Try sender name
        // =====================================================

        if (sender.contains("<")) {

            String senderName =
                    sender.substring(
                            0,
                            sender.indexOf("<")
                    ).trim();

            if (!senderName.isEmpty()) {

                return senderName;
            }
        }


        // =====================================================
        // Try email domain
        // =====================================================

        Pattern emailPattern =
                Pattern.compile(
                        "@([a-zA-Z0-9.-]+)"
                );

        Matcher matcher =
                emailPattern.matcher(sender);

        if (matcher.find()) {

            String domain =
                    matcher.group(1);

            if (domain != null) {

                String[] parts =
                        domain.split("\\.");

                if (parts.length > 0) {

                    return capitalizeServiceName(
                            parts[0]
                    );
                }
            }
        }


        return "Unknown";
    }


    // =========================================================
    // Capitalize service
    // =========================================================

    private String capitalizeServiceName(
            String service
    ) {

        if (service == null ||
                service.isEmpty()) {

            return "Unknown";
        }

        if (service.equalsIgnoreCase(
                "youtube"
        )) {
            return "YouTube";
        }

        if (service.equalsIgnoreCase(
                "youtube premium"
        )) {
            return "YouTube Premium";
        }

        if (service.equalsIgnoreCase(
                "spotify"
        )) {
            return "Spotify";
        }

        if (service.equalsIgnoreCase(
                "netflix"
        )) {
            return "Netflix";
        }

        if (service.equalsIgnoreCase(
                "amazon prime"
        )) {
            return "Amazon Prime";
        }

        if (service.equalsIgnoreCase(
                "apple music"
        )) {
            return "Apple Music";
        }

        if (service.equalsIgnoreCase(
                "google one"
        )) {
            return "Google One";
        }

        if (service.equalsIgnoreCase(
                "microsoft 365"
        )) {
            return "Microsoft 365";
        }

        if (service.equalsIgnoreCase(
                "chatgpt"
        )) {
            return "ChatGPT";
        }

        if (service.equalsIgnoreCase(
                "disney plus"
        ) ||
                service.equalsIgnoreCase(
                        "disney+"
                )) {
            return "Disney+";
        }

        if (service.equalsIgnoreCase(
                "uber"
        )) {
            return "Uber";
        }

        return service.substring(0, 1)
                .toUpperCase(Locale.US)
                + service.substring(1);
    }


    // =========================================================
    // Extract amount
    // =========================================================

    private double extractAmount(
            String text
    ) {

        if (text == null ||
                text.isEmpty()) {

            return 0.0;
        }


        // Rs 1,990
        Pattern rsPattern =
                Pattern.compile(
                        "(?i)(?:rs\\.?|lkr)\\s*([0-9,]+(?:\\.\\d{1,2})?)"
                );

        Matcher rsMatcher =
                rsPattern.matcher(text);

        if (rsMatcher.find()) {

            return parseAmount(
                    rsMatcher.group(1)
            );
        }


        // $19.99
        Pattern dollarPattern =
                Pattern.compile(
                        "\\$\\s*([0-9,]+(?:\\.\\d{1,2})?)"
                );

        Matcher dollarMatcher =
                dollarPattern.matcher(text);

        if (dollarMatcher.find()) {

            return parseAmount(
                    dollarMatcher.group(1)
            );
        }


        // 19.99 USD
        Pattern usdPattern =
                Pattern.compile(
                        "(?i)([0-9,]+(?:\\.\\d{1,2})?)\\s*(?:usd|dollars)"
                );

        Matcher usdMatcher =
                usdPattern.matcher(text);

        if (usdMatcher.find()) {

            return parseAmount(
                    usdMatcher.group(1)
            );
        }


        // Amount: 1990
        Pattern amountPattern =
                Pattern.compile(
                        "(?i)(?:amount|charged|charge|payment|price)"
                                + "\\s*[:=-]?\\s*"
                                + "(?:rs\\.?|lkr|\\$)?\\s*"
                                + "([0-9,]+(?:\\.\\d{1,2})?)"
                );

        Matcher amountMatcher =
                amountPattern.matcher(text);

        if (amountMatcher.find()) {

            return parseAmount(
                    amountMatcher.group(1)
            );
        }


        return 0.0;
    }


    // =========================================================
    // Parse amount
    // =========================================================

    private double parseAmount(
            String value
    ) {

        if (value == null) {
            return 0.0;
        }

        try {

            return Double.parseDouble(
                    value.replace(",", "")
            );

        } catch (Exception e) {

            return 0.0;
        }
    }


    // =========================================================
    // Extract billing cycle
    // =========================================================

    private String extractBillingCycle(
            String text
    ) {

        if (text.contains("monthly")
                || text.contains("every month")
                || text.contains("per month")) {

            return "Monthly";
        }


        if (text.contains("annual")
                || text.contains("annually")
                || text.contains("yearly")
                || text.contains("every year")
                || text.contains("per year")) {

            return "Annual";
        }


        if (text.contains("weekly")
                || text.contains("every week")
                || text.contains("per week")) {

            return "Weekly";
        }


        if (text.contains("quarterly")
                || text.contains("every 3 months")
                || text.contains("every three months")) {

            return "Quarterly";
        }


        return "Unknown";
    }


    // =========================================================
    // Extract billing date
    // =========================================================

    private String extractBillingDate(
            String text
    ) {

        // yyyy-MM-dd
        Pattern isoPattern =
                Pattern.compile(
                        "\\b(20\\d{2}-\\d{2}-\\d{2})\\b"
                );

        Matcher isoMatcher =
                isoPattern.matcher(text);

        if (isoMatcher.find()) {

            return isoMatcher.group(1);
        }


        // dd/mm/yyyy
        Pattern slashPattern =
                Pattern.compile(
                        "\\b(\\d{1,2}/\\d{1,2}/20\\d{2})\\b"
                );

        Matcher slashMatcher =
                slashPattern.matcher(text);

        if (slashMatcher.find()) {

            return slashMatcher.group(1);
        }


        // Month day, year
        Pattern monthPattern =
                Pattern.compile(
                        "(?i)\\b("
                                + "january|february|march|april|may|june|"
                                + "july|august|september|october|november|december"
                                + ")\\s+"
                                + "(\\d{1,2})(?:st|nd|rd|th)?"
                                + "(?:,)?\\s*"
                                + "(20\\d{2})\\b"
                );

        Matcher monthMatcher =
                monthPattern.matcher(text);

        if (monthMatcher.find()) {

            return monthMatcher.group(1)
                    + " "
                    + monthMatcher.group(2)
                    + ", "
                    + monthMatcher.group(3);
        }


        return "Unknown";
    }


    // =========================================================
    // Close interpreter
    // =========================================================

    public void close() {

        if (interpreter != null) {

            interpreter.close();
        }
    }
}