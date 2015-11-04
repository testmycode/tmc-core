package fi.helsinki.cs.tmc.core.communication;

import org.mockito.ArgumentMatcher;

import java.net.URI;

/**
 * Custom argument matcher for testing URI substring.
 */
public class UriContains extends ArgumentMatcher<URI> {
    private final String search;

    public UriContains(String search) {
        this.search = search;
    }

    @Override
    public boolean matches(Object argument) {
        if (argument == null) {
            return search == null;
        }
        return argument.toString().contains(search);
    }
}
