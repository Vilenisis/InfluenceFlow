package com.influenceflow.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegexValidatorTest {
    @Test
    void validSubmissionUrlsShouldPass() {
        assertTrue(RegexValidator.isValidSubmissionUrl("https://www.instagram.com/reel/ABC123/"));
        assertTrue(RegexValidator.isValidSubmissionUrl("https://tiktok.com/@user/video/123456789/"));
        assertTrue(RegexValidator.isValidSubmissionUrl("https://www.youtube.com/shorts/xyzABC"));
        assertTrue(RegexValidator.isValidSubmissionUrl("https://www.reddit.com/r/marketing/comments/abc123/cool_post/"));
    }

    @Test
    void invalidSubmissionUrlsShouldFail() {
        assertFalse(RegexValidator.isValidSubmissionUrl("https://example.com/post/123"));
        assertFalse(RegexValidator.isValidSubmissionUrl(""));
        assertFalse(RegexValidator.isValidSubmissionUrl(null));
    }

    @Test
    void parseMetricsShouldReturnMap() {
        Map<String, Integer> metrics = RegexValidator.parseMetrics("views=123 likes=45 comments=6");
        assertEquals(123, metrics.get("views"));
        assertEquals(45, metrics.get("likes"));
        assertEquals(6, metrics.get("comments"));
    }

    @Test
    void parseMetricsShouldFailOnInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> RegexValidator.parseMetrics("views=abc"));
        assertThrows(IllegalArgumentException.class, () -> RegexValidator.parseMetrics(""));
    }
}
