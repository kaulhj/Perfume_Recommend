package com.example.indspringboot.src.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import net.bytebuddy.asm.Advice;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRecommends {
    private int number;
    private String name;
    private String brand;
    private double price;
    private int ml;
    private String concentration;
    private String gender;
    private String scent;
    private String base_note;
    private String middle_note;
    private double rating;
    private String seller;
    private String ImgUrl;
    private String standard;


    @Builder
    public GetRecommends(String standard ,int number, String name, String brand, double price, int ml, String concentration, String gender, String scent, String base_note, String middle_note, double rating, String seller, String imgUrl) {
        this.number = number;
        this.standard = standard;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.ml = ml;
        this.concentration = concentration;
        this.gender = gender;
        this.scent = scent;
        this.base_note = base_note;
        this.middle_note = middle_note;
        this.rating = rating;
        this.seller = seller;
        this.ImgUrl = imgUrl;
    }
}
