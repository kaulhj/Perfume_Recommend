package com.example.indspringboot.src.recomend;


import com.example.indspringboot.src.model.GetRecommends;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

public class Provider {
    private final Dao dao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired

    public Provider(Dao dao) {
        this.dao = dao;
    }


    public List<GetRecommends> filter(int standardNum){
        try {
            List<GetRecommends> result = dao.filter(standardNum);
            return result;
        }catch (Exception exception){
            exception.printStackTrace();
            return new ArrayList<GetRecommends>();
        }
    }



}
