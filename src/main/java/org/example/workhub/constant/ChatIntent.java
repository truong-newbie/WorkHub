package org.example.workhub.constant;

public enum ChatIntent {
    SEARCH_JOBS,
    RECOMMEND_JOBS,
    JOB_DETAIL,
    SAVED_JOBS,
    APPLICATION_STATUS,
    MY_RESUMES,
    COMPANY_INFO,
    PLATFORM_HELP,
    OUT_OF_SCOPE;

    public static ChatIntent fromExternalValue(String value) {
        if (value == null || value.isBlank()) {
            return OUT_OF_SCOPE;
        }
        try {
            return ChatIntent.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OUT_OF_SCOPE;
        }
    }
}

