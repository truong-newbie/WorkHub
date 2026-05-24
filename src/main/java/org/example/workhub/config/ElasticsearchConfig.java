package org.example.workhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public String jobSearchIndexName(@Value("${search.job.index-name:workhub_jobs}") String indexName) {
        return indexName;
    }
}
