package com.example.indspringboot.src.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import net.bytebuddy.asm.Advice;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRecommends {
    private String standard;
    private int number;
    private String name;
    private String gender;
    private String scent;
    private String baseNote;
    private String middleNote;
    private String topNote;
    private String season;
    private String mainAccords;
    private String buyUrl;
    private String sillage;
    private String imageUrl;
    private String longevityRating;




    @Builder
    public GetRecommends(String standard, int number, String name, String gender, String scent, String baseNote, String middleNote, String topNote, String season, String mainAccords, String buyUrl, String sillage, String longevityRating
    ,String imageUrl) {
        this.standard = standard;
        this.number = number;
        this.name = name;
        this.gender = gender;
        this.scent = scent;
        this.baseNote = baseNote;
        this.middleNote = middleNote;
        this.topNote = topNote;
        this.season = season;
        this.mainAccords = mainAccords;
        this.buyUrl = buyUrl;
        this.sillage = sillage;
        this.longevityRating = longevityRating;
        this.imageUrl = imageUrl;
    }
}
