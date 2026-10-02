package com.example.aiexpensemanagementapplication.data.remote.gmail;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GmailServiceManager {

    // =========================================================
    // CONTEXT
    // =========================================================

    private final Context context;


    // =========================================================
    // GMAIL SERVICE
    // =========================================================

    private Gmail gmailService;


    // =========================================================
    // BACKGROUND THREAD
    // =========================================================

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();


    private final Handler mainHandler =
            new Handler(
                    Looper.getMainLooper()
            );


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public GmailServiceManager(
            Context context
    ) {

        this.context =
                context.getApplicationContext();
    }


    // =========================================================
    // CALLBACK
    // =========================================================

    public interface GmailCallback {

        void onSuccess(
                ArrayList<GmailMessageData> messages
        );


        void onError(
                String error
        );
    }


    // =========================================================
    // GMAIL MESSAGE DATA
    // =========================================================

    public static class GmailMessageData {

        private final String id;

        private final String sender;

        private final String subject;

        private final String snippet;

        private final String fullBody;


        // =====================================================
        // FULL CONSTRUCTOR
        // =====================================================

        public GmailMessageData(
                String id,
                String sender,
                String subject,
                String snippet,
                String fullBody
        ) {

            this.id =
                    safeValue(id);

            this.sender =
                    safeValue(sender);

            this.subject =
                    safeValue(subject);

            this.snippet =
                    safeValue(snippet);

            this.fullBody =
                    safeValue(fullBody);
        }


        // =====================================================
        // OLD CONSTRUCTOR
        //
        // Kept for compatibility.
        // =====================================================

        public GmailMessageData(
                String id,
                String sender,
                String subject,
                String snippet
        ) {

            this(
                    id,
                    sender,
                    subject,
                    snippet,
                    snippet
            );
        }


        public String getId() {

            return id;
        }


        public String getSender() {

            return sender;
        }


        public String getSubject() {

            return subject;
        }


        public String getSnippet() {

            return snippet;
        }


        public String getFullBody() {

            return fullBody;
        }


        private static String safeValue(
                String value
        ) {

            if (value == null) {

                return "";
            }


            return value.trim();
        }
    }


    // =========================================================
    // INITIALIZE GMAIL SERVICE
    // =========================================================

    public void initialize(
            GoogleSignInAccount account
    ) throws Exception {

        if (account == null) {

            throw new Exception(
                    "Google account is not available."
            );
        }


        if (account.getAccount() == null) {

            throw new Exception(
                    "Google account information is unavailable."
            );
        }


        NetHttpTransport transport =
                GoogleNetHttpTransport
                        .newTrustedTransport();


        GoogleAccountCredential credential =
                GoogleAccountCredential
                        .usingOAuth2(

                                context,

                                Collections.singleton(
                                        GmailScopes.GMAIL_READONLY
                                )
                        );


        credential.setSelectedAccount(
                account.getAccount()
        );


        gmailService =
                new Gmail.Builder(

                        transport,

                        GsonFactory
                                .getDefaultInstance(),

                        credential
                )
                        .setApplicationName(
                                "AI Expense Management Application"
                        )
                        .build();


        System.out.println(
                "========================================"
        );

        System.out.println(
                "GMAIL SERVICE INITIALIZED"
        );

        System.out.println(
                "========================================"
        );
    }


    // =========================================================
    // READ SUBSCRIPTION EMAILS
    // =========================================================

    public void readSubscriptionEmails(
            GmailCallback callback
    ) {

        executorService.execute(
                () -> {

                    try {

                        if (gmailService == null) {

                            throw new Exception(
                                    "Gmail service is not initialized."
                            );
                        }


                        // =================================================
                        // LAST 90 DAYS
                        //
                        // First scan:
                        //   all matching messages are analyzed.
                        //
                        // Later scans:
                        //   SubscriptionActivity checks ProcessedGmailMessage
                        //   and skips already analyzed Gmail message IDs.
                        // =================================================

                        String query =
                                "newer_than:90d "
                                        + "(subscription OR "
                                        + "renewal OR "
                                        + "renew OR "
                                        + "recurring OR "
                                        + "payment OR "
                                        + "invoice OR "
                                        + "receipt OR "
                                        + "charged OR "
                                        + "billing OR "
                                        + "membership OR "
                                        + "plan OR "
                                        + "trial)";


                        ArrayList<GmailMessageData> result =
                                new ArrayList<>();


                        String nextPageToken =
                                null;


                        int pageNumber =
                                0;


                        // =================================================
                        // PAGINATION
                        //
                        // Gmail can return multiple pages.
                        // Continue until there is no next page.
                        // =================================================

                        do {

                            pageNumber++;


                            System.out.println(
                                    "========================================"
                            );

                            System.out.println(
                                    "READING GMAIL PAGE: "
                                            + pageNumber
                            );

                            System.out.println(
                                    "========================================"
                            );


                            Gmail.Users.Messages.List request =
                                    gmailService
                                            .users()
                                            .messages()
                                            .list(
                                                    "me"
                                            )
                                            .setQ(
                                                    query
                                            )
                                            .setMaxResults(
                                                    100L
                                            );


                            if (nextPageToken != null &&
                                    !nextPageToken.trim().isEmpty()) {

                                request.setPageToken(
                                        nextPageToken
                                );
                            }


                            ListMessagesResponse response =
                                    request.execute();


                            if (response.getMessages() != null) {

                                for (
                                        Message message :
                                        response.getMessages()
                                ) {

                                    if (message == null ||
                                            message.getId() == null ||
                                            message.getId()
                                                    .trim()
                                                    .isEmpty()) {

                                        continue;
                                    }


                                    try {

                                        // =========================================
                                        // FETCH FULL MESSAGE
                                        // =========================================

                                        Message fullMessage =
                                                gmailService
                                                        .users()
                                                        .messages()
                                                        .get(
                                                                "me",
                                                                message.getId()
                                                        )
                                                        .setFormat(
                                                                "full"
                                                        )
                                                        .execute();


                                        if (fullMessage == null) {

                                            continue;
                                        }


                                        // =========================================
                                        // MESSAGE ID
                                        // =========================================

                                        String messageId =
                                                safeString(
                                                        fullMessage.getId()
                                                );


                                        // =========================================
                                        // HEADERS
                                        // =========================================

                                        String sender =
                                                getHeader(
                                                        fullMessage,
                                                        "From"
                                                );


                                        String subject =
                                                getHeader(
                                                        fullMessage,
                                                        "Subject"
                                                );


                                        // =========================================
                                        // GMAIL SNIPPET
                                        // =========================================

                                        String snippet =
                                                safeString(
                                                        fullMessage
                                                                .getSnippet()
                                                );


                                        // =========================================
                                        // FULL EMAIL BODY
                                        // =========================================

                                        String fullBody =
                                                extractFullEmailBody(
                                                        fullMessage
                                                );


                                        // =========================================
                                        // FALLBACK
                                        // =========================================

                                        if (fullBody.isEmpty()) {

                                            fullBody =
                                                    snippet;
                                        }


                                        // =========================================
                                        // DEBUG
                                        // =========================================

                                        System.out.println(
                                                "----------------------------------------"
                                        );

                                        System.out.println(
                                                "GMAIL MESSAGE ID: "
                                                        + messageId
                                        );

                                        System.out.println(
                                                "FROM: "
                                                        + sender
                                        );

                                        System.out.println(
                                                "SUBJECT: "
                                                        + subject
                                        );

                                        System.out.println(
                                                "BODY LENGTH: "
                                                        + fullBody.length()
                                        );

                                        System.out.println(
                                                "SUBJECT TEXT: " + subject
                                        );

                                        System.out.println(
                                                "EMAIL BODY: " + fullBody
                                        );

                                        System.out.println(
                                                "----------------------------------------"
                                        );


                                        // =========================================
                                        // ADD RESULT
                                        // =========================================

                                        result.add(
                                                new GmailMessageData(

                                                        messageId,

                                                        sender,

                                                        subject,

                                                        snippet,

                                                        fullBody
                                                )
                                        );


                                    } catch (Exception emailException) {

                                        System.out.println(
                                                "FAILED TO READ GMAIL MESSAGE: "
                                                        + message.getId()
                                        );


                                        emailException
                                                .printStackTrace();
                                    }
                                }
                            }


                            // =================================================
                            // NEXT PAGE
                            // =================================================

                            nextPageToken =
                                    safeString(
                                            response.getNextPageToken()
                                    );


                        } while (
                                !nextPageToken.isEmpty()
                        );


                        System.out.println(
                                "========================================"
                        );

                        System.out.println(
                                "90-DAY GMAIL FETCH COMPLETE"
                        );

                        System.out.println(
                                "TOTAL CANDIDATE EMAILS: "
                                        + result.size()
                        );

                        System.out.println(
                                "========================================"
                        );


                        mainHandler.post(
                                () -> {

                                    if (callback != null) {

                                        callback.onSuccess(
                                                result
                                        );
                                    }
                                }
                        );


                    } catch (Exception e) {

                        e.printStackTrace();


                        final String errorMessage;


                        if (e.getMessage() != null &&
                                !e.getMessage()
                                        .trim()
                                        .isEmpty()) {

                            errorMessage =
                                    e.getMessage();


                        } else {

                            errorMessage =
                                    "Failed to read Gmail.";
                        }


                        mainHandler.post(
                                () -> {

                                    if (callback != null) {

                                        callback.onError(
                                                errorMessage
                                        );
                                    }
                                }
                        );
                    }
                }
        );
    }


    // =========================================================
    // EXTRACT FULL EMAIL BODY
    // =========================================================

    private String extractFullEmailBody(
            Message message
    ) {

        if (message == null ||
                message.getPayload() == null) {

            return "";
        }


        BodyAccumulator accumulator =
                new BodyAccumulator();


        collectMessageBody(
                message.getPayload(),
                accumulator
        );


        String plainText =
                cleanEmailText(
                        accumulator
                                .plainText
                                .toString()
                );


        String htmlText =
                cleanEmailText(
                        htmlToPlainText(
                                accumulator
                                        .htmlText
                                        .toString()
                        )
                );


        // =====================================================
        // PREFER TEXT/PLAIN
        // =====================================================

        if (!plainText.isEmpty()) {

            return plainText;
        }


        // =====================================================
        // FALLBACK TO HTML -> TEXT
        // =====================================================

        return htmlText;
    }


    // =========================================================
    // RECURSIVELY READ MIME PARTS
    // =========================================================

    private void collectMessageBody(
            MessagePart part,
            BodyAccumulator accumulator
    ) {

        if (part == null ||
                accumulator == null) {

            return;
        }


        String mimeType =
                safeString(
                        part.getMimeType()
                )
                        .toLowerCase(
                                Locale.US
                        );


        String filename =
                safeString(
                        part.getFilename()
                );


        // =====================================================
        // SKIP NORMAL ATTACHMENTS
        //
        // Examples:
        // invoice.pdf
        // receipt.jpg
        //
        // PDF parsing is a different feature.
        // =====================================================

        boolean isAttachment =
                !filename.isEmpty();


        if (!isAttachment &&
                part.getBody() != null &&
                part.getBody().getData() != null &&
                !part.getBody()
                        .getData()
                        .trim()
                        .isEmpty()) {

            String decodedText =
                    decodeBodyData(
                            part.getBody()
                                    .getData()
                    );


            if (!decodedText.isEmpty()) {

                // =================================================
                // PLAIN TEXT
                // =================================================

                if (mimeType.equals(
                        "text/plain"
                )) {

                    appendBodyText(
                            accumulator.plainText,
                            decodedText
                    );
                }


                // =================================================
                // HTML
                // =================================================

                else if (mimeType.equals(
                        "text/html"
                )) {

                    appendBodyText(
                            accumulator.htmlText,
                            decodedText
                    );
                }
            }
        }


        // =====================================================
        // RECURSE INTO CHILD PARTS
        //
        // multipart/alternative
        // multipart/mixed
        // multipart/related
        // etc.
        // =====================================================

        List<MessagePart> parts =
                part.getParts();


        if (parts != null &&
                !parts.isEmpty()) {

            for (
                    MessagePart childPart :
                    parts
            ) {

                collectMessageBody(
                        childPart,
                        accumulator
                );
            }
        }
    }


    // =========================================================
    // APPEND BODY TEXT
    // =========================================================

    private void appendBodyText(
            StringBuilder builder,
            String text
    ) {

        if (builder == null) {

            return;
        }


        String value =
                safeString(
                        text
                );


        if (value.isEmpty()) {

            return;
        }


        if (builder.length() > 0) {

            builder.append(
                    "\n\n"
            );
        }


        builder.append(
                value
        );
    }


    // =========================================================
    // DECODE GMAIL BODY
    //
    // Gmail body data uses Base64 URL-safe encoding.
    // =========================================================

    private String decodeBodyData(
            String encodedData
    ) {

        if (encodedData == null ||
                encodedData.trim().isEmpty()) {

            return "";
        }


        try {

            byte[] decodedBytes =
                    Base64.decode(

                            encodedData,

                            Base64.URL_SAFE
                                    | Base64.NO_WRAP
                                    | Base64.NO_PADDING
                    );


            return new String(
                    decodedBytes,
                    StandardCharsets.UTF_8
            );


        } catch (Exception firstError) {

            // =================================================
            // FALLBACK FOR NON URL-SAFE BASE64
            // =================================================

            try {

                byte[] decodedBytes =
                        Base64.decode(
                                encodedData,
                                Base64.DEFAULT
                        );


                return new String(
                        decodedBytes,
                        StandardCharsets.UTF_8
                );


            } catch (Exception secondError) {

                System.out.println(
                        "FAILED TO DECODE GMAIL BODY"
                );


                return "";
            }
        }
    }


    // =========================================================
    // HTML -> READABLE TEXT
    // =========================================================

    private String htmlToPlainText(
            String html
    ) {

        if (html == null ||
                html.trim().isEmpty()) {

            return "";
        }


        try {

            // Remove script/style content first.

            String cleanedHtml =
                    html.replaceAll(
                            "(?is)<script.*?>.*?</script>",
                            " "
                    );


            cleanedHtml =
                    cleanedHtml.replaceAll(
                            "(?is)<style.*?>.*?</style>",
                            " "
                    );


            Spanned spanned =
                    Html.fromHtml(
                            cleanedHtml,
                            Html.FROM_HTML_MODE_LEGACY
                    );


            if (spanned == null) {

                return "";
            }


            return spanned
                    .toString()
                    .trim();


        } catch (Exception e) {

            return "";
        }
    }


    // =========================================================
    // CLEAN EMAIL TEXT
    // =========================================================

    private String cleanEmailText(
            String value
    ) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "";
        }


        String result =
                value;


        // =====================================================
        // NORMALIZE NEWLINES
        // =====================================================

        result =
                result.replace(
                        "\r\n",
                        "\n"
                );


        result =
                result.replace(
                        "\r",
                        "\n"
                );


        // =====================================================
        // NON-BREAKING SPACE
        // =====================================================

        result =
                result.replace(
                        '\u00A0',
                        ' '
                );


        // =====================================================
        // REMOVE EXCESS TABS
        // =====================================================

        result =
                result.replaceAll(
                        "[\\t\\f]+",
                        " "
                );


        // =====================================================
        // MULTIPLE SPACES
        // =====================================================

        result =
                result.replaceAll(
                        " {2,}",
                        " "
                );


        // =====================================================
        // SPACES AROUND NEW LINES
        // =====================================================

        result =
                result.replaceAll(
                        " *\\n *",
                        "\n"
                );


        // =====================================================
        // TOO MANY EMPTY LINES
        // =====================================================

        result =
                result.replaceAll(
                        "\\n{3,}",
                        "\n\n"
                );


        return result.trim();
    }


    // =========================================================
    // GET HEADER
    // =========================================================

    private String getHeader(
            Message message,
            String headerName
    ) {

        if (message == null ||
                message.getPayload() == null ||
                message.getPayload()
                        .getHeaders() == null) {

            return "";
        }


        for (
                MessagePartHeader header :
                message.getPayload()
                        .getHeaders()
        ) {

            if (header == null) {

                continue;
            }


            if (headerName.equalsIgnoreCase(
                    header.getName()
            )) {

                return safeString(
                        header.getValue()
                );
            }
        }


        return "";
    }


    // =========================================================
    // SAFE STRING
    // =========================================================

    private static String safeString(
            String value
    ) {

        if (value == null) {

            return "";
        }


        return value.trim();
    }


    // =========================================================
    // BODY ACCUMULATOR
    // =========================================================

    private static class BodyAccumulator {

        final StringBuilder plainText =
                new StringBuilder();


        final StringBuilder htmlText =
                new StringBuilder();
    }


    // =========================================================
    // SHUTDOWN
    // =========================================================

    public void shutdown() {

        executorService.shutdown();
    }
}