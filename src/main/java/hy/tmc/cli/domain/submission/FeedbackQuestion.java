package hy.tmc.cli.domain.submission;

public class FeedbackQuestion {
    private int id;
    private String question;
    private String kind;

    public int getId() {
        return id;
    }

    public String getKind() {
        return kind;
    }

    public String getQuestion() {
        return question;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
