package hy.tmc.cli.testhelpers;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExampleJson {

    private static final String jsonFolder = "src/test/resources/json/";

    public static String courseExample = courseExample();
    public static String allCoursesExample = allCoursesExample();
    public static String successfulSubmission = successfulSubmission();
    public static String failedSubmission = failedSubmission();
    public static String submitResponse = submitResponse();
    public static String noDeadlineCourseExample = noDeadlineCourseExample();
    public static String failingCourse = failingCourse();
    public static String failedSubmitResponse = failedSubmitResponse();
    public static String pasteResponse = pasteResponse();
    public static String feedbackExample = feedbackExample();
    public static String noFeedbackExample = noFeedbackExample();
    public static String feedbackCourse = feedbackCourseExample();
    public static String trivialNoFeedback = trivialNoFeedback();
    public static String checkstyleFailed = checkstyleFailed();
    public static String valgrindFailed = valgrindFailed();
    public static String expiredCourseExample = expiredCourseExample();
    public static String sentFeedbackExample = sentFeedbackExample();

    private static String sentFeedbackExample() {
        return readFile("sentFeedbackExample.json");
    }
    
    private static String failingCourse() {
        return readFile("failingCourse.json");
    }
    
    private static String failedSubmitResponse() {
        return readFile("failedSubmitResponse.json");
    }

    private static String successfulSubmission() {
        return readFile("successfulSubmission.json");
    }

    private static String feedbackExample() {
        return readFile("feedback.json");
    }

    private static String noFeedbackExample() {
        return readFile("noFeedback.json");
    }

    private static String trivialNoFeedback() {
        return readFile("trivialNoFeedback.json");
    }

    private static String failedSubmission() {
        return readFile("failedSubmission.json");
    }

    private static String courseExample() {
        return readFile("course.json");
    }
    
    private static String noDeadlineCourseExample(){
        return readFile("nodeadlinecourse.json");
    }
    
    private static String expiredCourseExample(){
        return readFile("expiredCourse.json");
    }
   
    private static String allCoursesExample() {
        return readFile("courses.json");
    }

    private static String feedbackCourseExample() {
        return readFile("feedbackCourse.json");
    }

    private static String submitResponse() {
        return readFile("submitResponse.json");
    }

    private static String pasteResponse() {
        return readFile("pasteResponse.json");
    }

    private static String checkstyleFailed() {
        return readFile("checkstyleFailed.json");
    }

    private static String valgrindFailed() {
        return readFile("valgrindFailed.json");
    }

    private static String readFile(final String path) {
        try {
            return FileUtils.readFileToString(new File(jsonFolder + path));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return "";
        }
    }
}
