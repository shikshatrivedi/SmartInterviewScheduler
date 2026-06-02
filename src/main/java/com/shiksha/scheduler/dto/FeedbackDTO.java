package com.shiksha.scheduler.dto;

import com.shiksha.scheduler.model.FeedbackResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class FeedbackDTO {

    @NotNull(message = "Interview ID is required")
    private Long interviewId;

    private String comments;

    @NotNull(message = "Technical score is required")
    @Min(value = 1, message = "Score must be between 1 and 10")
    @Max(value = 10, message = "Score must be between 1 and 10")
    private Integer technicalScore;

    @NotNull(message = "Communication score is required")
    @Min(value = 1, message = "Score must be between 1 and 10")
    @Max(value = 10, message = "Score must be between 1 and 10")
    private Integer communicationScore;

    @NotNull(message = "Result is required")
    private FeedbackResult result;

    public FeedbackDTO() {}

    public Long          getInterviewId()               { return interviewId; }
    public void          setInterviewId(Long v)         { this.interviewId = v; }
    public String        getComments()                  { return comments; }
    public void          setComments(String v)          { this.comments = v; }
    public Integer       getTechnicalScore()            { return technicalScore; }
    public void          setTechnicalScore(Integer v)   { this.technicalScore = v; }
    public Integer       getCommunicationScore()        { return communicationScore; }
    public void          setCommunicationScore(Integer v){ this.communicationScore = v; }
    public FeedbackResult getResult()                   { return result; }
    public void          setResult(FeedbackResult v)    { this.result = v; }
}
