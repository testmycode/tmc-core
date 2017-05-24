package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.TmcServerCommunicationTaskFactory;
import fi.helsinki.cs.tmc.core.communication.serialization.SubmissionResultParser;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.exceptions.NotLoggedInException;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.Callable;


public class SubmitAdaptiveExerciseToSkillifier extends AbstractSubmissionCommand<SubmissionResult> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSubmissionCommand.class);
    private static final int DEFAULT_POLL_INTERVAL = 1000 * 2;

    private Exercise exercise;

    public SubmitAdaptiveExerciseToSkillifier(ProgressObserver observer, Exercise exercise) {
        super(observer);
        this.exercise = exercise;
    }

    @VisibleForTesting
    SubmitAdaptiveExerciseToSkillifier(
            ProgressObserver observer,
            Exercise exercise,
            TmcServerCommunicationTaskFactory tmcServerCommunicationTaskFactory) {
        super(observer, tmcServerCommunicationTaskFactory);
        this.exercise = exercise;
    }


    @Override
    public SubmissionResult call() throws Exception {
        logger.info("Submitting exercise {}", exercise.getName());
        informObserver(0, "Submitting exercise to server");

        //Compress project and upload to server, (not yet)
        //Get SubmissionResponse from server, contains submissionURL and pasteURL
        
        TmcServerCommunicationTaskFactory.SubmissionResponse submissionResponse =
                submitToSkillifier(exercise, new HashMap<String, String>());

        while (true) {
            checkInterrupt();
            try {
                Thread.sleep(DEFAULT_POLL_INTERVAL);
            } catch (InterruptedException ex) {
                logger.debug("Interrupted while sleeping", ex);
            }
            try {
                //get json from submissionurl
                logger.debug("Checking if server is done processing submission");
                Callable<String> submissionResultFetcher =
                        tmcServerCommunicationTaskFactory.getSubmissionFetchTask(
                                submissionResponse.submissionUrl);

                String submissionStatus = submissionResultFetcher.call();
                JsonElement submission = new JsonParser().parse(submissionStatus);
                if (isProcessing(submission)) {
                    logger.debug("Server not done, sleeping for {}", DEFAULT_POLL_INTERVAL);
                    informObserver(0.3, "Waiting for response from server");
                    // TODO: Replace with variable interval polling
                    Thread.sleep(DEFAULT_POLL_INTERVAL);
                } else {
                    logger.debug("Server done, parsing results");
                    informObserver(0.6, "Reading adaptive submission result");

                    SubmissionResultParser resultParser = new SubmissionResultParser();
                    SubmissionResult result = resultParser.parseFromJson(submissionStatus);

                    logger.debug("Done parsing server response");
                    informObserver(1, "Successfully read adaptive submission results");

                    //placeholder
                    SubmissionResult res = new SubmissionResult();
                    res.setCourse(exercise.getCourseName());
                    res.setExerciseName(exercise.getName());
                    return res;
                }
            } catch (Exception ex) {
                informObserver(1, "Error while waiting for response from server");
                logger.warn("Error while updating adaptive submission status from server, continuing", ex);
            }
        }

        //Download JSON from submissionURL
        // parse JSON into submissionResult
    }

    private boolean isProcessing(JsonElement submissionStatus) {
        return submissionStatus.getAsJsonObject().get("status").getAsString().equals("processing");
    }

    private TmcServerCommunicationTaskFactory.SubmissionResponse submitToSkillifier(
            Exercise exercise, HashMap<String, String> extraParams) throws TmcCoreException {

        Gson gson = new Gson();
        String json = "";
        try {
            json = gson.toJson(exercise);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        byte[] byteToSubmit = json.getBytes();

        informObserver(0, "Zipping project.");

        //Only a json containing information about the exercise is sent to skillifier at this point.

/*
        Path tmcRoot = TmcSettingsHolder.get().getTmcProjectDirectory();
        Path projectPath = exercise.getExerciseDirectory(tmcRoot);


        //logger.info("Submitting adaptive project to path {}", projectPath);

        try {
            byteToSubmit = TmcLangsHolder.get().compressProject(projectPath);
        } catch (IOException | NoLanguagePluginFoundException ex) {
            informObserver(1, "Failed to compress adaptive project");
            logger.warn("Failed to compress adaptive project", ex);
            throw new TmcCoreException("Failed to compress adaptive project", ex);
        }
*/

        extraParams.put("error_msg_locale", TmcSettingsHolder.get().getLocale().toString());

        checkInterrupt();
        informObserver(0.5, "Submitting adaptive project");
        logger.info("Submitting adaptive project to skillifier");

        //skillifier returns json which is parsed into SubmissionResponse

        try {
            TmcServerCommunicationTaskFactory.SubmissionResponse response
                    = tmcServerCommunicationTaskFactory
                    .getSubmittingExerciseToSkillifierTask(exercise, byteToSubmit, extraParams)
                    .call();

            informObserver(1, "Submission successfully completed");
            logger.info("Submission successfully completed");

            return response;
        } catch (Exception ex) {
            if (ex instanceof NotLoggedInException) {
                throw (NotLoggedInException)ex;
            }
            informObserver(1, "Failed to submit exercise");
            logger.warn("Failed to submit exercise", ex);
            throw new TmcCoreException("Failed to submit exercise", ex);
        }
    }
}
