package com.example.aiexpensemanagementapplication.data.remote.gmail;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ListMessagesResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GmailServiceManager {

    private final Context context;

    private Gmail gmailService;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public GmailServiceManager(Context context) {
        this.context = context;
    }

    public interface GmailCallback {

        void onSuccess(
                ArrayList<GmailMessageData> messages
        );

        void onError(String error);
    }

    public static class GmailMessageData {

        private String id;
        private String sender;
        private String subject;
        private String snippet;

        public GmailMessageData(
                String id,
                String sender,
                String subject,
                String snippet
        ) {
            this.id = id;
            this.sender = sender;
            this.subject = subject;
            this.snippet = snippet;
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
    }

    public void initialize(
            GoogleSignInAccount account
    ) throws Exception {

        if (account == null) {
            throw new Exception(
                    "Google account is not available."
            );
        }

        NetHttpTransport transport =
                GoogleNetHttpTransport.newTrustedTransport();

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
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
                        GsonFactory.getDefaultInstance(),
                        credential
                )
                        .setApplicationName(
                                "AI Expense Management Application"
                        )
                        .build();
    }

    public void readSubscriptionEmails(
            GmailCallback callback
    ) {

        executorService.execute(() -> {

            try {

                if (gmailService == null) {

                    throw new Exception(
                            "Gmail service is not initialized."
                    );
                }

                List<String> queries =
                        new ArrayList<>();

                queries.add(
                        "newer_than:2y " +
                                "(subscription OR " +
                                "renewal OR " +
                                "payment OR " +
                                "invoice OR " +
                                "receipt)"
                );

                String query =
                        queries.get(0);

                ListMessagesResponse response =
                        gmailService
                                .users()
                                .messages()
                                .list("me")
                                .setQ(query)
                                .setMaxResults(50L)
                                .execute();

                ArrayList<GmailMessageData> result =
                        new ArrayList<>();

                if (response.getMessages() != null) {

                    for (Message message :
                            response.getMessages()) {

                        Message fullMessage =
                                gmailService
                                        .users()
                                        .messages()
                                        .get(
                                                "me",
                                                message.getId()
                                        )
                                        .setFormat("full")
                                        .execute();

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

                        String snippet =
                                fullMessage.getSnippet();

                        result.add(
                                new GmailMessageData(
                                        fullMessage.getId(),
                                        sender,
                                        subject,
                                        snippet
                                )
                        );
                    }
                }

                mainHandler.post(() ->
                        callback.onSuccess(result)
                );

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(
                                e.getMessage() != null
                                        ? e.getMessage()
                                        : "Failed to read Gmail."
                        )
                );
            }
        });
    }

    private String getHeader(
            Message message,
            String headerName
    ) {

        if (message == null ||
                message.getPayload() == null ||
                message.getPayload().getHeaders() == null) {

            return "";
        }

        for (
                MessagePartHeader header :
                message.getPayload().getHeaders()
        ) {

            if (
                    headerName.equalsIgnoreCase(
                            header.getName()
                    )
            ) {

                return header.getValue();
            }
        }

        return "";
    }

    public void shutdown() {
        executorService.shutdown();
    }
}