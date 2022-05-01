package com.example.indspringboot.src.recomend;


import com.example.indspringboot.src.model.GetRecommends;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController

@RequestMapping("")


public class Controller {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ReService reService;
    @Autowired
    private final Provider provider;
    @Autowired
    private final Dao dao;

    public Controller(ReService reservice, Provider provider, Dao dao) {
        this.reService = reservice;
        this.provider = provider;
        this.dao = dao;
    }


    @ResponseBody
    @PostMapping("/pushGender")
    public String pushFilter(@RequestParam(value = "gender") int genderNum){
        try{
            String result = reService.pushGender(genderNum);
            return result;
        }catch (Exception exception){
            return new String(exception.getMessage());
        }
    }

    @PatchMapping("/pushTimeScent")
    public String pushTimeScent(@RequestParam(value = "time",required = false) Integer timeNum,
                                @RequestParam(value = "scent",required = false) Integer scentNum){
        try{
            String result = null;
            if(timeNum != null){
               result = reService.pushTime(timeNum);
            }
            else {
                result = reService.pushScent(scentNum);
            }
            return result;
        }catch (Exception exception){
            return new String(exception.getMessage());
        }
    }

    @PatchMapping("/pushPrice")
    public String pushPrice(@RequestParam(value = "priceRange",required = true) Integer priceRangeNum){
        try{
            String result = reService.pushPrice(priceRangeNum);
            return result;
        }catch (Exception exception){
            exception.printStackTrace();
            return new String(exception.getMessage());
        }
    }


        //필터링, 1,2,3케이스 추가
    @GetMapping("/filtering")
    public List<GetRecommends> filter(@RequestParam(value = "standard",required = true) Integer standardNum){
        try{
            List<GetRecommends> result = provider.filter(standardNum);
            return result;
        }catch (Exception exception){
            exception.printStackTrace();
            return new ArrayList<GetRecommends>();
        }


    }








}
