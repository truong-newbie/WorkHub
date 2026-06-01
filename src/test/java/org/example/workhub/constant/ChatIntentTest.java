package org.example.workhub.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatIntentTest {

    @Test
    void unknownExternalIntentBecomesOutOfScope() {
        assertThat(ChatIntent.fromExternalValue("SHOW_ALL_USERS")).isEqualTo(ChatIntent.OUT_OF_SCOPE);
    }

    @Test
    void knownExternalIntentIsNormalized() {
        assertThat(ChatIntent.fromExternalValue(" search_jobs ")).isEqualTo(ChatIntent.SEARCH_JOBS);
    }
}

