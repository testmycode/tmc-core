package hy.tmc.cli.frontend.formatters;

import hy.tmc.cli.domain.submission.ValidationError;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class VimSubmissionResultFormatterTest {

    private VimSubmissionResultFormatter formatter = new VimSubmissionResultFormatter();

    @Test
    public void parseValidationErrors() {
        List<ValidationError> list = new ArrayList<ValidationError>();
        ValidationError error = createValidationError();
        list.add(error);
        Map.Entry<String, List<ValidationError>> entry = new AbstractMap.SimpleEntry<String, List<ValidationError>>("file", list);
        String explanation = formatter.parseValidationErrors(entry);
        assertTrue(explanation.contains("On line: 10 Column: 5"));
    }
    
    public ValidationError createValidationError(){
        ValidationError error = new ValidationError();
        error.setLine(10);
        error.setColumn(5);
        error.setMessage("asd");
        error.setSourceName("koodit");
        return error;
    }
}
