package ch.polylan.polymobile;

import android.content.SearchRecentSuggestionsProvider;

public class UserSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "ch.polylan.polymobile.suggestions";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public UserSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}