package com.influenceflow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexValidator {
    private static final Pattern INSTAGRAM_PATTERN = Pattern.compile("https?://(www\\.)?instagram\\.com/reel/[A-Za-z0-9_-]+/?");
    private static final Pattern TIKTOK_PATTERN = Pattern.compile("https?://(www\\.)?tiktok\\.com/@[A-Za-z0-9._]+/video/\\d+/?");
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("https?://(www\\.)?youtube\\.com/shorts/[A-Za-z0-9_-]+/?");
    private static final Pattern REDDIT_PATTERN = Pattern.compile("https?://(www\\.)?reddit\\.com/r/[A-Za-z0-9_]+/comments/[A-Za-z0-9]+/[A-Za-z0-9_]+/?");
    private static final Pattern METRICS_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*([0-9]{1,9})");

    private RegexValidator() {
    }

    public static boolean isValidSubmissionUrl(String url) {
        if (url == null) {
            return false;
        }
        return INSTAGRAM_PATTERN.matcher(url).matches()
                || TIKTOK_PATTERN.matcher(url).matches()
                || YOUTUBE_PATTERN.matcher(url).matches()
                || REDDIT_PATTERN.matcher(url).matches();
    }

    public static Map<String, Integer> parseMetrics(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Metrics input is empty");
        }
        Matcher matcher = METRICS_PATTERN.matcher(input);
        Map<String, Integer> metrics = new HashMap<>();
        int cursor = 0;
        while (matcher.find()) {
            if (!isSeparator(input.substring(cursor, matcher.start()))) {
                throw new IllegalArgumentException("Metrics input has invalid format near: "
                        + input.substring(cursor, matcher.start()).trim());
            }
            metrics.put(matcher.group(1), Integer.parseInt(matcher.group(2)));
            cursor = matcher.end();
        }
        if (metrics.isEmpty() || !isSeparator(input.substring(cursor))) {
            throw new IllegalArgumentException("Metrics input has invalid format: " + input);
        }
        return metrics;
    }

    private static boolean isSeparator(String fragment) {
        if (fragment == null || fragment.isEmpty()) {
            return true;
        }
        for (int i = 0; i < fragment.length(); i++) {
            char ch = fragment.charAt(i);
            if (!(Character.isWhitespace(ch) || ch == ',' || ch == ';')) {
                return false;
            }
        }
        return true;
    }
}
