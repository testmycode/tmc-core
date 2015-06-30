package hy.tmc.cli.frontend.communication.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hy.tmc.cli.backend.TmcCore;
import hy.tmc.cli.backend.communication.UrlCommunicator;
import hy.tmc.cli.configuration.ConfigHandler;
import hy.tmc.cli.domain.submission.FeedbackQuestion;
import hy.tmc.cli.frontend.FrontendListener;
import hy.tmc.cli.frontend.RangeFeedbackHandler;
import hy.tmc.cli.frontend.TextFeedbackHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements FrontendListener, Runnable {

    private ServerSocket serverSocket;
    private boolean isRunning;
    private TmcCore tmcCore;
    private ExecutorService socketThreadPool;
    private BufferedReader in;
    private JsonArray feedbackAnswers = new JsonArray();
    private RangeFeedbackHandler rangeFeedbackHandler;
    private TextFeedbackHandler textFeedbackHandler;

    /**
     * Constructor for server. It finds a free port to be listened to.
     *
     * @throws IOException if failed to write port to configuration file
     */
    public Server() throws IOException {
        this(new TmcCore(), Executors.newCachedThreadPool(), new RangeFeedbackHandler(null)); //NULL NULL NULL
    }
    
    /**
     * Constructor for dependency injection.
     *
     * @throws IOException if failed to write port to configuration file
     */
    public Server(RangeFeedbackHandler handler) throws IOException {
        this(new TmcCore(), Executors.newCachedThreadPool(), handler);
    }

    
    public Server(TmcCore tmcCore, ExecutorService socketThreadPool) throws IOException {
        this(tmcCore, socketThreadPool, new RangeFeedbackHandler(null));
    }
    
    
    /**
     * Constructor for dependency injection.
     *
     * @param tmcCore
     * @param socketThreadPool
     */
    public Server(TmcCore tmcCore, ExecutorService socketThreadPool, RangeFeedbackHandler handler) throws IOException {
        this.tmcCore = tmcCore;
        this.socketThreadPool = socketThreadPool;
        initServerSocket();
        this.rangeFeedbackHandler = handler;
        this.textFeedbackHandler = new TextFeedbackHandler(this);

    }

    private void initServerSocket() {
        try {
            serverSocket = new ServerSocket(0);
            new ConfigHandler().writePort(serverSocket.getLocalPort());
        } catch (IOException ex) {
            System.err.println("Server creation failed");
            System.err.println(ex.getMessage());
        }
    }

    public int getCurrentPort() {
        return this.serverSocket.getLocalPort();
    }

    /**
     * Start is general function to set up server listening for the frontend.
     */
    @Override
    public void start() {
        this.run();
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Run is loop that accepts new client connection and handles it. Submits the new socket task
     * into a thread pool that executes is with a thread that is free.
     */
    @Override
    public final void run() {
        isRunning = true;
        while (true) {
            try {
                if (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    socketThreadPool.submit(new SocketRunnable(clientSocket, tmcCore));
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Closes serverSocket. Destroys the Socket pool.
     *
     * @throws IOException if failed to close socket
     */
    public void close() throws IOException {
        isRunning = false;
        serverSocket.close();
        socketThreadPool.shutdown();
    }

    @Override
    public void feedback(List<FeedbackQuestion> feedbackQuestions, String feedbackUrl) {
        if (feedbackQuestions.isEmpty()) {
            return;
        }

        List<FeedbackQuestion> rangeQuestions = new ArrayList<>();
        List<FeedbackQuestion> textQuestions = new ArrayList<>();
        for (FeedbackQuestion feedbackQuestion : feedbackQuestions) {
            if (feedbackQuestion.getKind().equals("text")) {
                textQuestions.add(feedbackQuestion);
            } else {
                rangeQuestions.add(feedbackQuestion);
            }
        }

        this.rangeFeedbackHandler.feedback(rangeQuestions, feedbackUrl);
        this.textFeedbackHandler.feedback(textQuestions, feedbackUrl);

        if (!rangeQuestions.isEmpty()) {
            rangeFeedbackHandler.askQuestion(); // ask first questions
        } else {
            textFeedbackHandler.askQuestion();
        }
    }

    /**
     * Takes the answer from a range feedback question.
     */
    public void rangeFeedbackAnswer(String answer) throws ProtocolException {
        JsonObject jsonAnswer = new JsonObject();
        jsonAnswer.addProperty("question_id", rangeFeedbackHandler.getLastId());
        String validAnswer = rangeFeedbackHandler.validateAnswer(answer);
        jsonAnswer.addProperty("answer", validAnswer);
        feedbackAnswers.add(jsonAnswer);

        if (this.rangeFeedbackHandler.allQuestionsAsked()) {
            if (textFeedbackHandler.allQuestionsAsked()) {
                sendToTmcServer();
                this.feedbackAnswers = new JsonArray();
            } else {
                textFeedbackHandler.askQuestion(); // start asking text questions
            }
        } else {
            rangeFeedbackHandler.askQuestion();
        }
    }

    /**
     * Takes the answer from a text feedback question.
     */
    public void textFeedbackAnswer(String answer) throws ProtocolException {
        JsonObject jsonAnswer = new JsonObject();
        jsonAnswer.addProperty("question_id", textFeedbackHandler.getLastId());
        jsonAnswer.addProperty("answer", answer);
        feedbackAnswers.add(jsonAnswer);

        if (this.textFeedbackHandler.allQuestionsAsked()) {
            sendToTmcServer();
            this.feedbackAnswers = new JsonArray();
        } else {
            textFeedbackHandler.askQuestion();
        }
    }

    protected void sendToTmcServer() throws ProtocolException {
        JsonObject req = getAnswersJson();
        try {
            UrlCommunicator.makePostWithJson(req, getFeedbackUrl());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private String getFeedbackUrl() {
        return rangeFeedbackHandler.getFeedbackUrl() + "?" + new ConfigHandler().apiParam;
    }

    private JsonObject getAnswersJson() {
        JsonObject req = new JsonObject();
        req.add("answers", feedbackAnswers);
        return req;
    }
}
