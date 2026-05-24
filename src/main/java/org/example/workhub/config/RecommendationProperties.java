package org.example.workhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    private Weights weights = new Weights();

    private Behavior behavior = new Behavior();

    private Collaborative collaborative = new Collaborative();

    private Result result = new Result();

    @Getter
    @Setter
    public static class Weights {
        private double content = 0.50;
        private double behavior = 0.30;
        private double collaborative = 0.20;
    }

    @Getter
    @Setter
    public static class Behavior {
        private double viewWeight = 1;
        private double clickWeight = 3;
        private double saveWeight = 5;
        private double applyWeight = 10;
        private double searchKeywordWeight = 2;
        private int historyDays = 30;
    }

    @Getter
    @Setter
    public static class Collaborative {
        private int similarUserLimit = 20;
        private double minSimilarity = 0.25;
    }

    @Getter
    @Setter
    public static class Result {
        private int maxCandidates = 200;
        private int defaultPageSize = 10;
    }
}
