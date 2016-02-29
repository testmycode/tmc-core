package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.communication.http.UriUtils;
import fi.helsinki.cs.tmc.core.communication.serialization.CourseInfoParser;
import fi.helsinki.cs.tmc.core.communication.serialization.CourseListParser;
import fi.helsinki.cs.tmc.core.communication.serialization.ReviewListParser;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.exceptions.ObsoleteClientException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.communication.http.HttpTasks;
import fi.helsinki.cs.tmc.core.spyware.LoggableEvent;
import fi.helsinki.cs.tmc.core.communication.serialization.JsonMaker;
import fi.helsinki.cs.tmc.core.communication.serialization.ByteArrayGsonSerializer;
import fi.helsinki.cs.tmc.core.communication.serialization.JsonMakerGsonSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

/**
 * A frontend for the server.
 */
public class TmcServerCommunicationTaskFactory {
    public static final int API_VERSION = 7;

    private TmcSettings settings;
    private CourseListParser courseListParser;
    private CourseInfoParser courseInfoParser;
    private ReviewListParser reviewListParser;
    private String clientVersion;

    public TmcServerCommunicationTaskFactory() {
        this(TmcSettingsHolder.get());
    }

    public TmcServerCommunicationTaskFactory(TmcSettings settings) {
        this(settings, new CourseListParser(), new CourseInfoParser(), new ReviewListParser());
    }

    public TmcServerCommunicationTaskFactory(
            TmcSettings settings,
            CourseListParser courseListParser,
            CourseInfoParser courseInfoParser,
            ReviewListParser reviewListParser) {
        this.settings = settings;
        this.courseListParser = courseListParser;
        this.courseInfoParser = courseInfoParser;
        this.reviewListParser = reviewListParser;
        this.clientVersion = getClientVersion();
    }

    private static String getClientVersion() {
        return TmcSettingsHolder.get().clientVersion();
    }

    public void setSettings(TmcSettings settings) {
        this.settings = settings;
    }

    private String getCourseListUrl() {
        return addApiCallQueryParameters(settings.getServerAddress() + "/courses.json");
    }

    private String addApiCallQueryParameters(String url) {
        url = UriUtils.withQueryParam(url, "api_version", "" + API_VERSION);
        url = UriUtils.withQueryParam(url, "client", settings.clientName());
        url = UriUtils.withQueryParam(url, "client_version", clientVersion);
        return url;
    }

    private HttpTasks createHttpTasks() {
        return new HttpTasks().setCredentials(settings.getUsername(), settings.getPassword());
    }

    public boolean hasEnoughSettings() {
        return !settings.getUsername().isEmpty()
                && !settings.getPassword().isEmpty()
                && !settings.getServerAddress().isEmpty();
    }

    public boolean needsOnlyPassword() {
        return !settings.getUsername().isEmpty()
                && settings.getPassword().isEmpty()
                && !settings.getServerAddress().isEmpty();
    }

    public Callable<List<Course>> getDownloadingCourseListTask() {
        final Callable<String> download = createHttpTasks().getForText(getCourseListUrl());
        return new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                try {
                    String text = download.call();
                    return courseListParser.parseFromJson(text);
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }
            
            //TODO: Cancellable?
        };
    }

    public Callable<Course> getFullCourseInfoTask(Course courseStub) {
        //TODO: Str -> URL
        String url = addApiCallQueryParameters(courseStub.getDetailsUrl().toString());
        final Callable<String> download = createHttpTasks().getForText(url);
        return new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                try {
                    String text = download.call();
                    return courseInfoParser.parseFromJson(text);
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }
            
            //TODO: Cancellable?
        };
    }

    public Callable<Void> getUnlockingTask(Course course) {
        Map<String, String> params = Collections.emptyMap();
        final Callable<String> download = createHttpTasks()
                .postForText(getUnlockUrl(course), params);
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    download.call();
                    return null;
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        };
    }

    private String getUnlockUrl(Course course) {
        //TODO: Str -> URL
        return addApiCallQueryParameters(course.getUnlockUrl().toString());
    }

    public Callable<byte[]> getDownloadingExerciseZipTask(Exercise exercise) {
        //TODO: Str -> URL
        String zipUrl = exercise.getDownloadUrl().toString();
        return createHttpTasks().getForBinary(zipUrl);
    }

    public Callable<byte[]> getDownloadingExerciseSolutionZipTask(Exercise exercise) {
        //TODO: Str -> URL
        String zipUrl = exercise.getSolutionDownloadUrl().toString();
        return createHttpTasks().getForBinary(zipUrl);
    }

    public Callable<SubmissionResponse> getSubmittingExerciseTask(
            final Exercise exercise,
            final byte[] sourceZip,
            Map<String, String> extraParams) {
        //TODO: Str -> URL
        final String submitUrl = addApiCallQueryParameters(exercise.getReturnUrl().toString());

        Map<String, String> params = new LinkedHashMap<>();
        params.put("client_time", "" + (System.currentTimeMillis() / 1000L));
        params.put("client_nanotime", "" + System.nanoTime());
        params.putAll(extraParams);

        final Callable<String> upload =
                createHttpTasks().uploadFileForTextDownload(
                        submitUrl,
                        params,
                        "submission[file]",
                        sourceZip);

        return new Callable<SubmissionResponse>() {
            @Override
            public SubmissionResponse call() throws Exception {
                String response;
                try {
                    response = upload.call();
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }

                JsonObject respJson = new JsonParser().parse(response).getAsJsonObject();
                if (respJson.get("error") != null) {
                    throw new RuntimeException("Server responded with error: "
                            + respJson.get("error"));
                } else if (respJson.get("submission_url") != null) {
                    try {
                        URI submissionUrl = new URI(respJson.get("submission_url")
                                .getAsString());
                        URI pasteUrl = new URI(respJson.get("paste_url").getAsString());
                        return new SubmissionResponse(submissionUrl, pasteUrl);
                    } catch (Exception e) {
                        throw new RuntimeException("Server responded with malformed "
                                + "submission url");
                    }
                } else {
                    throw new RuntimeException("Server returned unknown response");
                }
            }

            //TODO: Cancellable?
        };
    }

    public static class SubmissionResponse {

        public final URI submissionUrl;
        public final URI pasteUrl;

        public SubmissionResponse(URI submissionUrl, URI pasteUrl) {
            this.submissionUrl = submissionUrl;
            this.pasteUrl = pasteUrl;
        }
    }

    public Callable<String> getSubmissionFetchTask(String submissionUrl) {
        return createHttpTasks().getForText(submissionUrl);
    }

    public Callable<List<Review>> getDownloadingReviewListTask(Course course) {
        //TODO: Str -> URL
        String url = addApiCallQueryParameters(course.getReviewsUrl().toString());
        final Callable<String> download = createHttpTasks().getForText(url);
        return new Callable<List<Review>>() {
            @Override
            public List<Review> call() throws Exception {
                try {
                    String text = download.call();
                    return reviewListParser.parseFromJson(text);
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        };
    }

    public Callable<Void> getMarkingReviewAsReadTask(Review review, boolean read) {
        String url = addApiCallQueryParameters(review.getUpdateUrl() + ".json");
        Map<String, String> params = new HashMap<>();
        params.put("_method", "put");
        if (read) {
            params.put("mark_as_read", "1");
        } else {
            params.put("mark_as_unread", "1");
        }

        final Callable<String> task = createHttpTasks().postForText(url, params);
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                task.call();
                return null;
            }

            //TODO: Cancellable?
        };
    }

    public Callable<String> getFeedbackAnsweringJob(
            String answerUrl,
            List<FeedbackAnswer> answers) {
        final String submitUrl = addApiCallQueryParameters(answerUrl);

        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < answers.size(); ++i) {
            String keyPrefix = "answers[" + i + "]";
            FeedbackAnswer answer = answers.get(i);
            params.put(keyPrefix + "[question_id]", "" + answer.getQuestion().getId());
            params.put(keyPrefix + "[answer]", answer.getAnswer());
        }

        final Callable<String> upload = createHttpTasks().postForText(submitUrl, params);

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    return upload.call();
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        };
    }

    public Callable<Object> getSendEventLogJob(
            String spywareServerUrl,
            List<LoggableEvent> events) {

        Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("X-Tmc-Version", "1");
        extraHeaders.put("X-Tmc-Username", settings.getUsername());
        extraHeaders.put("X-Tmc-Password", settings.getPassword());

        byte[] data;
        try {
            data = eventListToPostBody(events);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String url = addApiCallQueryParameters(spywareServerUrl);
        final Callable<String> upload = createHttpTasks().rawPostForText(url, data, extraHeaders);

        return new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                upload.call();
                return null;
            }

            //TODO: Cancellable?
        };
    }

    private byte[] eventListToPostBody(List<LoggableEvent> events) throws IOException {
        ByteArrayOutputStream bufferBos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(bufferBos);
        OutputStreamWriter bufferWriter = new OutputStreamWriter(gzos, Charset.forName("UTF-8"));


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(byte[].class, new ByteArrayGsonSerializer())
                .registerTypeAdapter(JsonMaker.class, new JsonMakerGsonSerializer())
                .create();

        gson.toJson(events, new TypeToken<List<LoggableEvent>>(){}.getType(), bufferWriter);
        bufferWriter.close();
        gzos.close();

        return bufferBos.toByteArray();
    }

    private <T> T checkForObsoleteClient(FailedHttpResponseException ex)
            throws ObsoleteClientException, FailedHttpResponseException {
        if (ex.getStatusCode() == 404) {
            boolean obsolete;
            try {
                obsolete = new JsonParser()
                        .parse(ex.getEntityAsString())
                        .getAsJsonObject()
                        .get("obsolete_client")
                        .getAsBoolean();
            } catch (Exception ex2) {
                obsolete = false;
            }
            if (obsolete) {
                throw new ObsoleteClientException();
            }
        }

        throw ex;
    }
}
