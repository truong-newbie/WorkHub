package org.example.workhub.domain.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "#{@jobSearchIndexName}")
@Setting(settingPath = "elasticsearch/job-settings.json")
@Getter
@Setter
public class JobSearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "search_analyzer")
    private String title;

    @Field(type = FieldType.Keyword)
    private String slug;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    private String description;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    private String requirement;

    @Field(type = FieldType.Text, analyzer = "search_analyzer")
    private String benefit;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "search_analyzer")
    private String location;

    @Field(type = FieldType.Long)
    private Long companyId;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "search_analyzer")
    private String companyName;

    @Field(type = FieldType.Keyword)
    private String companyLogo;

    @Field(type = FieldType.Boolean)
    private Boolean companyActive;

    @Field(type = FieldType.Boolean)
    private Boolean companyVerified;

    @Field(type = FieldType.Keyword)
    private String recruiterId;

    @Field(type = FieldType.Double)
    private BigDecimal salaryMin;

    @Field(type = FieldType.Double)
    private BigDecimal salaryMax;

    @Field(type = FieldType.Boolean)
    private Boolean negotiableSalary;

    @Field(type = FieldType.Integer)
    private Integer experienceYears;

    @Field(type = FieldType.Keyword)
    private String level;

    @Field(type = FieldType.Keyword)
    private String workMode;

    @Field(type = FieldType.Keyword)
    private String employmentType;

    @Field(type = FieldType.Long)
    private List<Long> skillIds;

    @Field(type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "search_analyzer")
    private List<String> skillNames;

    @Field(type = FieldType.Boolean)
    private Boolean published;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant expiredAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdDate;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime lastModifiedDate;
}
