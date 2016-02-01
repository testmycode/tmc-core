package fi.helsinki.cs.tmc.core.domain.submission;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationError;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ValidationResultImpl implements ValidationResult {

    private Strategy strategy;
    private Map<File, List<ValidationError>> validationErrors;

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Map<File, List<ValidationError>> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<File, List<ValidationError>> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
