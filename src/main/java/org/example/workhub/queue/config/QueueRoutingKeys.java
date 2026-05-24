package org.example.workhub.queue.config;

public final class QueueRoutingKeys {

    public static final String EMAIL_SEND = "email.send";
    public static final String ATS_SCREENING_REQUEST = "ats.screening.request";
    public static final String RESUME_PARSING_REQUEST = "resume.parsing.request";
    public static final String NOTIFICATION_SEND = "notification.send";

    public static final String EMAIL_FAILED = "email.failed";
    public static final String ATS_SCREENING_FAILED = "ats.screening.failed";
    public static final String RESUME_PARSING_FAILED = "resume.parsing.failed";
    public static final String NOTIFICATION_FAILED = "notification.failed";

    private QueueRoutingKeys() {
    }
}
