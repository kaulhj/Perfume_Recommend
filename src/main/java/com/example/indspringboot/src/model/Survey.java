package com.example.indspringboot.src.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Survey {

    private int gender;
    private int time;
    private int scent;
    private int season;
    private int resultId1;
    private int resultId2;
    private int resultId3;

    @Builder(builderMethodName = "SurveyBuilder")
    public Survey(int gender, int time, int scent, int season, int resultId1, int resultId2, int resultId3) {
        this.gender = gender;
        this.time = time;
        this.scent = scent;
        this.season = season;
        this.resultId1 = resultId1;
        this.resultId2 = resultId2;
        this.resultId3 = resultId3;
    }
}
