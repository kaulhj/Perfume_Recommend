package com.example.indspringboot.src.recomend;



import com.example.indspringboot.src.model.GetRecommends;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service

public class ReService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Dao dao;
    private final Provider provider;

    @Autowired

    public ReService(Dao dao, Provider provider) {
        this.dao = dao;
        this.provider = provider;
    }

    public String pushGender(int genderNum) throws Exception{
        try{
            String result2 = dao.pushGender(genderNum);
            return result2;
        }catch (Exception exception){
            exception.printStackTrace();
            return new String("데이터베이스에 성별이 입력되지 않음");
        }
    }

    public String pushTime(int timeNum)throws Exception{
        try{
            String result2 = dao.pushTime(timeNum);
            return result2;
        }catch (Exception exception){
            return new String("향수 선호 시간대 입력에 실패하였습니다.");
        }
    }

    public String pushScent(int scentNum)throws Exception{
        try{
            String result3 = dao.pushScent(scentNum);
            return result3;
        }catch (Exception exception){
            exception.printStackTrace();
            return new String("선호 향기 입력에 실패하였습니다.");
        }
    }

    public String pushPrice(int priceRangeNum)throws Exception{
        try{
            //해당 값까지 4개로 걸러서 데이터가 있는지 확인
            //없으면 성별 / 밤낮/ 향기 중에서 하나 제외


            String result = dao.pushPrice(priceRangeNum);
            return result;
        }catch (Exception exception){
            exception.printStackTrace();
            return new String("선호 가격 입력에 실패했습니다.");
        }
    }
}

