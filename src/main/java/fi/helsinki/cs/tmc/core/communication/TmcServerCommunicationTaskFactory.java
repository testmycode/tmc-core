package fi.helsinki.cs.tmc.core.communication;

import fi.helsinki.cs.tmc.core.communication.http.HttpTasks;
import fi.helsinki.cs.tmc.core.communication.http.UriUtils;
import fi.helsinki.cs.tmc.core.communication.oauth2.Oauth;
import fi.helsinki.cs.tmc.core.communication.serialization.AdaptiveExerciseParser;
import fi.helsinki.cs.tmc.core.communication.serialization.ByteArrayGsonSerializer;
import fi.helsinki.cs.tmc.core.communication.serialization.CourseInfoParser;
import fi.helsinki.cs.tmc.core.communication.serialization.CourseListParser;
import fi.helsinki.cs.tmc.core.communication.serialization.ExerciseListParser;
import fi.helsinki.cs.tmc.core.communication.serialization.ReviewListParser;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.OauthCredentials;
import fi.helsinki.cs.tmc.core.domain.Organization;
import fi.helsinki.cs.tmc.core.domain.Review;
import fi.helsinki.cs.tmc.core.domain.Theme;
import fi.helsinki.cs.tmc.core.domain.submission.FeedbackAnswer;
import fi.helsinki.cs.tmc.core.exceptions.FailedHttpResponseException;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.ObsoleteClientException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import fi.helsinki.cs.tmc.core.utilities.JsonMaker;
import fi.helsinki.cs.tmc.core.utilities.JsonMakerGsonSerializer;
import fi.helsinki.cs.tmc.spyware.LoggableEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import org.apache.commons.io.IOUtils;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * A frontend for the server.
 */
public class TmcServerCommunicationTaskFactory {

    private static final Logger LOG = Logger.getLogger(
                TmcServerCommunicationTaskFactory.class.getName());
    public static final int API_VERSION = 8;

    private TmcSettings settings;
    private Oauth oauth;
    private AdaptiveExerciseParser adaptiveExerciseParser;
    private CourseListParser courseListParser;
    private CourseInfoParser courseInfoParser;
    private ExerciseListParser exerciseListParser;
    private ReviewListParser reviewListParser;
    private String clientVersion;

    public TmcServerCommunicationTaskFactory() {
        this(TmcSettingsHolder.get(), Oauth.getInstance());
    }

    public TmcServerCommunicationTaskFactory(TmcSettings settings, Oauth oauth) {
        this(settings, oauth, new AdaptiveExerciseParser(), new CourseListParser(),
                new CourseInfoParser(), new ReviewListParser());
    }

    public TmcServerCommunicationTaskFactory(
                TmcSettings settings,
                Oauth oauth,
                AdaptiveExerciseParser adaptiveExerciseParser,
                CourseListParser courseListParser,
                CourseInfoParser courseInfoParser,
                ReviewListParser reviewListParser) {
        this.settings = settings;
        this.oauth = oauth;
        this.adaptiveExerciseParser = adaptiveExerciseParser;
        this.courseListParser = courseListParser;
        this.courseInfoParser = courseInfoParser;
        this.reviewListParser = reviewListParser;
        this.clientVersion = getClientVersion();
        this.exerciseListParser = new ExerciseListParser();
    }

    private static String getClientVersion() {
        return TmcSettingsHolder.get().clientVersion();
    }

    public void setSettings(TmcSettings settings) {
        this.settings = settings;
    }

    /**
     * Returns a Callable that calls the given Callable.
     * <p>
     * If the call fails once, the oauth token is refreshed and the call is done again.</p>
     * @param <T> return type of the callable
     * @param callable Callable to be wrapped
     * @return The given Callable wrapped in another Callable
     */
    private <T> Callable<T> wrapWithNotLoggedInException(final Callable<T> callable) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    return callable.call();
                } catch (FailedHttpResponseException e) {
                    LOG.log(Level.WARNING,
                                "Communication with the server failed!");
                    throw new NotLoggedInException();
                }
            }
        };
    }

    private URI getCourseListUrl()
        throws OAuthSystemException, OAuthProblemException, NotLoggedInException {
        String serverAddress = settings.getServerAddress();
        String url;
        // "/core/org/" => "/core/org"
        String urlLastPart = "api/v" + API_VERSION + "/core/org" + settings.getOrganization() + "/courses.json";
        if (serverAddress.endsWith("/")) {
            url = serverAddress + urlLastPart;
        } else {
            url = serverAddress + "/" + urlLastPart;
        }
        return addApiCallQueryParameters(URI.create(url));
    }

    private URI addApiCallQueryParameters(URI url) throws NotLoggedInException {
        url = UriUtils.withQueryParam(url, "client", settings.clientName());
        url = UriUtils.withQueryParam(url, "client_version", clientVersion);
        url = UriUtils.withQueryParam(url, "access_token", oauth.getToken());
        return url;
    }

    public URI getSkillifierUrl(String addition) {
        if (!addition.isEmpty()) {
            return URI.create("http://tmc-adapt.testmycode.io/" + addition);
        }
        return URI.create("http://tmc-adapt.testmycode.io/");
    }

    public Callable<Exercise> getAdaptiveExercise(final Theme theme, final Course course)
        throws OAuthSystemException, OAuthProblemException, NotLoggedInException {
        return wrapWithNotLoggedInException(new Callable<Exercise>() {
                @Override
                public Exercise call() throws Exception {
                    try {
                        Callable<String> download = new HttpTasks()
                                .getForText(getSkillifierUrl(course.getName() + "/" + theme.getName() + "/next.json?token=" + oauth.getToken()));
                        String json = download.call();
                        return adaptiveExerciseParser.parseFromJson(json);
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Downloading and parsing adaptive exercise URL failed.");
                        return null;
                    }
                }
            });
    }

    public Callable<List<Course>> getDownloadingCourseListTask() {
        return wrapWithNotLoggedInException(new Callable<List<Course>>() {
            @Override
            public List<Course> call() throws Exception {
                try {
                    Callable<String> download = new HttpTasks().getForText(getCourseListUrl());
                    String text = download.call();
                    return courseListParser.parseFromJson(text);
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
                //TODO: Cancellable?
            }
        });
    }

    public Callable<Course> getFullCourseInfoTask(final Course courseStub) {
        return wrapWithNotLoggedInException(new Callable<Course>() {
            @Override
            public Course call() throws Exception {
                try {
                    URI serverUrl = addApiCallQueryParameters(courseStub.getDetailsUrl());
                    Course returnedFromServer = getCourseInfo(serverUrl);

                    try {
                        URI skillifierUrl = getSkillifierUrl("course/" + courseStub.getName() + "/exercises");
                        List<Exercise> returnedFromSkillifier = getExerciseList(skillifierUrl);
                        returnedFromServer.getExercises().addAll(returnedFromSkillifier);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Downloading adaptive exercise info from skillifier failed.");
                    }
                    returnedFromServer.generateThemes();
                    return returnedFromServer;
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        });
    }
    
    private List<Exercise> getExerciseList(URI uri) throws Exception {
        final Callable<String> downloadFromServer = new HttpTasks().getForText(uri);
        String jsonFromServer = downloadFromServer.call();
        return exerciseListParser.parseFromJson(jsonFromServer);
    }

    private Course getCourseInfo(URI uri) throws Exception {
        final Callable<String> downloadFromServer = new HttpTasks().getForText(uri);
        String jsonFromServer = downloadFromServer.call();
        return courseInfoParser.parseFromJson(jsonFromServer);
    }

    public Callable<Void> getUnlockingTask(final Course course) {
        final Map<String, String> params = Collections.emptyMap();
        return wrapWithNotLoggedInException(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    final Callable<String> download = new HttpTasks()
                            .postForText(getUnlockUrl(course), params);
                    download.call();
                    return null;
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        });
    }

    private URI getUnlockUrl(Course course) throws NotLoggedInException {
        return addApiCallQueryParameters(course.getUnlockUrl());
    }

    public Callable<byte[]> getDownloadingAdaptiveExerciseZipTask(Exercise exercise) throws NotLoggedInException {
        exercise.setZipUrl(URI.create(exercise.getZipUrl() + "?token=" + oauth.getToken()));
        exercise.setDownloadUrl(exercise.getZipUrl());
        return getDownloadingExerciseZipTask(exercise);
    }

    public Callable<byte[]> getDownloadingExerciseZipTask(Exercise exercise) {
        URI zipUrl = exercise.getDownloadUrl();
        return new HttpTasks().getForBinary(zipUrl);
    }

    public Callable<byte[]> getDownloadingExerciseSolutionZipTask(Exercise exercise) {
        URI zipUrl = exercise.getSolutionDownloadUrl();
        return new HttpTasks().getForBinary(zipUrl);
    }

    public Callable<SubmissionResponse> getSubmittingExerciseToSkillifierTask(
            final Exercise exercise, final byte[] byteToSend, Map<String, String> extraParams) {

        final Map<String, String> params = new LinkedHashMap<>();
        params.put("client_time", "" + (System.currentTimeMillis() / 1000L));
        params.put("client_nanotime", "" + System.nanoTime());
        params.putAll(extraParams);

        return wrapWithNotLoggedInException(new Callable<SubmissionResponse>() {
            @Override
            public SubmissionResponse call() throws Exception {
                String response;
                try {
                    final URI submitUrl = getSkillifierUrl("/submit?usernsame=" + oauth.getToken());
                    final Callable<String> upload = new HttpTasks()
                            .uploadRawDataForTextDownload(submitUrl, params,
                                 byteToSend);
                    response = upload.call();
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }

                JsonObject respJson = new JsonParser().parse(response).getAsJsonObject();
                return getSubmissionResponse(respJson);
            }

            //TODO: Cancellable?
            });
    }


    public Callable<SubmissionResponse> getSubmittingExerciseTask(
            final Exercise exercise, final byte[] sourceZip, Map<String, String> extraParams) {

        final Map<String, String> params = new LinkedHashMap<>();
        params.put("client_time", "" + (System.currentTimeMillis() / 1000L));
        params.put("client_nanotime", "" + System.nanoTime());
        params.putAll(extraParams);

        return wrapWithNotLoggedInException(new Callable<SubmissionResponse>() {
            @Override
            public SubmissionResponse call() throws Exception {
                String response;
                try {
                    final URI submitUrl = addApiCallQueryParameters(exercise.getReturnUrl());
                    final Callable<String> upload = new HttpTasks()
                            .uploadFileForTextDownload(submitUrl, params,
                                    "submission[file]", sourceZip);
                    response = upload.call();
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }

                JsonObject respJson = new JsonParser().parse(response).getAsJsonObject();
                return getSubmissionResponse(respJson);
            }

            //TODO: Cancellable?
        });
    }

    private SubmissionResponse getSubmissionResponse(JsonObject respJson) {
        if (respJson.get("error") != null) {
            throw new RuntimeException(
                    "Server responded with error: " + respJson.get("error"));
        } else if (respJson.get("submission_url") != null) {
            try {
                URI submissionUrl = new URI(respJson.get("submission_url").getAsString());
                URI pasteUrl = new URI(respJson.get("paste_url").getAsString());
                return new SubmissionResponse(submissionUrl, pasteUrl);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Server responded with malformed " + "submission url");
            }
        } else {
            throw new RuntimeException("Server returned unknown response");
        }
    }

    public static class SubmissionResponse {

        public final URI submissionUrl;
        public final URI pasteUrl;

        public SubmissionResponse(URI submissionUrl, URI pasteUrl) {
            this.submissionUrl = submissionUrl;
            this.pasteUrl = pasteUrl;
        }
    }

    public Callable<String> getSubmissionFetchTask(URI submissionUrl) throws NotLoggedInException {
        return new HttpTasks().getForText(addApiCallQueryParameters(submissionUrl));
    }

    public Callable<List<Review>> getDownloadingReviewListTask(final Course course) {
        return wrapWithNotLoggedInException(new Callable<List<Review>>() {
            @Override
            public List<Review> call() throws Exception {
                try {
                    URI url = addApiCallQueryParameters(course.getReviewsUrl());
                    final Callable<String> download = new HttpTasks().getForText(url);
                    String text = download.call();
                    return reviewListParser.parseFromJson(text);
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        });
    }

    public Callable<Void> getMarkingReviewAsReadTask(final Review review, boolean read) {
        final Map<String, String> params = new HashMap<>();
        params.put("_method", "put");
        if (read) {
            params.put("mark_as_read", "1");
        } else {
            params.put("mark_as_unread", "1");
        }

        return wrapWithNotLoggedInException(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                URI url = addApiCallQueryParameters(URI.create(review.getUpdateUrl() + ".json"));
                final Callable<String> task = new HttpTasks().postForText(url, params);
                task.call();
                return null;
            }

            //TODO: Cancellable?
        });
    }

    public Callable<String> getFeedbackAnsweringJob(final URI answerUrl,
            List<FeedbackAnswer> answers) {
        final Map<String, String> params = new HashMap<>();
        for (int i = 0; i < answers.size(); ++i) {
            String keyPrefix = "answers[" + i + "]";
            FeedbackAnswer answer = answers.get(i);
            params.put(keyPrefix + "[question_id]", "" + answer.getQuestion().getId());
            params.put(keyPrefix + "[answer]", answer.getAnswer());
        }

        return wrapWithNotLoggedInException(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    final URI submitUrl = addApiCallQueryParameters(answerUrl);
                    final Callable<String> upload = new HttpTasks()
                            .postForText(submitUrl, params);
                    return upload.call();
                } catch (FailedHttpResponseException ex) {
                    return checkForObsoleteClient(ex);
                }
            }

            //TODO: Cancellable?
        });
    }

    public Callable<Object> getSendEventLogJob(final URI spywareServerUrl,
            List<LoggableEvent> events) throws NotLoggedInException {
        final Map<String, String> extraHeaders = new LinkedHashMap<>();
        extraHeaders.put("X-Tmc-Version", "1");
        extraHeaders.put("X-Tmc-Username", settings.getUsername());
        extraHeaders.put("X-Tmc-SESSION-ID", oauth.getToken());

        final byte[] data;
        try {
            data = eventListToPostBody(events);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return wrapWithNotLoggedInException(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                URI url = addApiCallQueryParameters(spywareServerUrl);
                final Callable<String> upload = new HttpTasks()
                        .rawPostForText(url, data, extraHeaders);
                upload.call();
                return null;
            }

            //TODO: Cancellable?
        });
    }

    public Callable<Void> getOauthCredentialsTask() throws IOException {
        URI credentialsUrl;
        if (settings.getServerAddress().endsWith("/")) {
            credentialsUrl = URI.create(
                settings.getServerAddress() + "api/v" + API_VERSION
                    + "/application/" + settings.clientName() + "/credentials");
        } else {
            credentialsUrl = URI.create(
                settings.getServerAddress() + "/api/v" + API_VERSION
                    + "/application/" + settings.clientName() + "/credentials");
        }
        OauthCredentials credentials =
                new Gson().fromJson(
                        IOUtils.toString(credentialsUrl.toURL()), OauthCredentials.class);
        settings.setOauthCredentials(credentials);
        return null;
    }

    public List<Organization> getOrganizationListTask() throws IOException {
        String url;
        String serverAddress = settings.getServerAddress();
        String urlLastPart = "api/v" + API_VERSION + "/org";
        if (serverAddress.endsWith("/")) {
            url = settings.getServerAddress() + urlLastPart;
        } else {
            url = serverAddress + "/" + urlLastPart;
        }
        URI organizationUrl = URI.create(url);
        List<Organization> organizations = new Gson().fromJson(IOUtils.toString(organizationUrl.toURL()), new TypeToken<List<Organization>>(){}.getType());
        return organizations;
    }

    private byte[] eventListToPostBody(List<LoggableEvent> events) throws IOException {
        ByteArrayOutputStream bufferBos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(bufferBos);
        OutputStreamWriter bufferWriter = new OutputStreamWriter(gzos, Charset.forName("UTF-8"));

        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(byte[].class, new ByteArrayGsonSerializer())
                        .registerTypeAdapter(JsonMaker.class, new JsonMakerGsonSerializer())
                        .create();

        gson.toJson(events, new TypeToken<List<LoggableEvent>>() {}.getType(), bufferWriter);
        bufferWriter.close();
        gzos.close();

        return bufferBos.toByteArray();
    }

    private <T> T checkForObsoleteClient(FailedHttpResponseException ex)
            throws ObsoleteClientException, FailedHttpResponseException {
        if (ex.getStatusCode() == 404) {
            boolean obsolete;
            try {
                obsolete =
                        new JsonParser()
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
