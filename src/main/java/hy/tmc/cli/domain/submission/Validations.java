package hy.tmc.cli.domain.submission;

import java.util.List;
import java.util.Map;

public class Validations {

    private String strategy;
    private Map<String, List<ValidationError>> validationErrors;

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public Map<String, List<ValidationError>> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<ValidationError>> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
