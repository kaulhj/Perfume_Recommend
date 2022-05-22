package com.example.indspringboot.src.recomend;

import com.example.indspringboot.src.model.GetRecommends;
import com.example.indspringboot.src.model.Survey;
import com.example.indspringboot.src.s3.S3UploadService;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.stylesheets.LinkStyle;
import javax.sql.DataSource;
import java.beans.Transient;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.*;

@Repository


public class Dao {
    private JdbcTemplate jdbcTemplate;

    @Autowired //readme 참고
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    private S3UploadService s3UploadService;


    public String pushGender(int genderNum) {

        String pushFilterQuery = "insert Into survey(genderId) " +
                "value (?)";

        this.jdbcTemplate.update(pushFilterQuery, genderNum);
        return new String("성별이 정상적으로 입력되었습니다.");
    }

    ;

    public String pushTime(int timeNum) {

        String lastId = "select surveyId from survey order by surveyId\n" +
                "desc limit 1;";
        int insertId = this.jdbcTemplate.queryForObject(lastId, int.class);
        String pushTimeQuery = "update survey set spendTimeId = ? where surveyId = ?";
        Object[] pushTimeParams = new Object[]{timeNum, insertId};
        this.jdbcTemplate.update(pushTimeQuery, pushTimeParams);
        return new String("향수사용시간대번호가 정상적으로 입력되었습니다.");
    }


    public String pushScent(int scentNum) {
        String lastId = "select surveyId from survey order by surveyId\n" +
                "desc limit 1";
        int insertId = this.jdbcTemplate.queryForObject(lastId, int.class);
        String pushScentQuery = "update survey set scentId = ? where surveyId = ?";
        Object[] pushScenteParams = new Object[]{scentNum, insertId};
        this.jdbcTemplate.update(pushScentQuery, pushScenteParams);
        return new String("선호향기가 정상적으로 입력되었습니다.");

    }


    @Transactional(rollbackFor = Exception.class)
    public String pushSeason(int seasonId) {
        String lastId = "select surveyId from survey order by surveyId\n" +
                "desc limit 1";
        int insertId = this.jdbcTemplate.queryForObject(lastId, int.class);
        String pushPriceQuery = "update survey set seasonId = ? where surveyId = ?";
        Object[] pushPriceParams = new Object[]{seasonId, insertId};
        this.jdbcTemplate.update(pushPriceQuery, pushPriceParams);

        String dataQuery = "select genderId,spendTimeId,scentId,seasonId\n" +
                "from survey\n" +
                "where surveyId = ?";
        Survey dataList = this.jdbcTemplate.queryForObject(dataQuery,
                (rs, rowNum) -> Survey.SurveyBuilder()
                        .gender(rs.getInt("genderId"))
                        .time(rs.getInt("spendTimeId"))
                        .scent(rs.getInt("scentId"))
                        .season(rs.getInt("seasonId"))
                        .build()
                , insertId);

        String filterInsertQuery = "update survey set resultId1 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and seasonId = ?\n" +
                "order by rating desc limit 1 offset 0),\n" +
                "                  resultId2 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and seasonId = ?\n" +
                "order by rating desc limit 1 offset 1),\n" +
                "                  resultId3 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and seasonId = ?\n" +
                "order by rating desc limit 1 offset 2)\n" +
                "where surveyId = ?\n";
        Object[] filters = new Object[]{dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getSeason(),
                dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getSeason(),
                dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getSeason(),
                insertId};
        this.jdbcTemplate.update(filterInsertQuery, filters);


        int firstPerfumeId = this.jdbcTemplate.queryForObject("select resultId1\n" +
                "from survey\n" +
                "where surveyId = ?", int.class, insertId);

        String insertResultQuery = "insert Into algorithmResult( surveyId, rankByRating, perfumeId,standard)\n" +
                " values (?, ? ,?,?)";
        Object[] insertResultParams = new Object[]{insertId, 1, firstPerfumeId, "mainAccords"};


        this.jdbcTemplate.update(insertResultQuery, insertResultParams);


        return new String("가격범위 입력, 필터링 결과값 등록되었습니다.");
    }

    @Transactional(rollbackFor = Exception.class)
    public List<GetRecommends> filter(int standardNum) {

        String lastId = "select surveyId from survey order by surveyId\n" +
                "desc limit 1";
        int insertId = this.jdbcTemplate.queryForObject(lastId, int.class);


        //algo table에서 최초추천, 유사도 추천한 perfumeId들 가져와 이미지 호출하기
        String imgQuery = "select a.perfumeId,name\n" +
                "from algorithmResult a\n" +
                "where surveyId = ?";




        if(standardNum == 1){
            String getInfoQuery = "select\n" +
                    "\n" +
                    "                                           name,g.genderStatus,\n" +
                    "                                           s.scentName,baseNote,middleNote,topNote,season,mainAccords,\n" +
                    "       buyUrl,sillage,imageUrl,longevityRating\n" +
                    "                                from perfumeDataCopy pc\n" +
                    "                                        left join gender g on pc.genderId = g.genderId\n" +
                    "                                    left join scents s on pc.scentId = s.scentIdx\n" +
                    "                                    left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                                    left join season se on pc.seasonId = se.seasonId\n" +
                    "                                    inner join(select perfumeId, aR.standard\n" +
                    "                                    from survey\n" +
                    "                                    inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                                    where aR.surveyId = ? and standard = 'mainAccords') li on pc.perfumeId = li.perfumeId\n" +
                    "                                    order by rating;";


            //String imgSource = new String("perfume/")
            //imgName.get(0).get("perfumeId");
            String imgPath = s3UploadService.getThumbnailPath(".jpg");

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("mainAccords")
                            .number(rowNum + 1)
                            .name(rs.getString("name"))
                            .gender(rs.getString("genderStatus"))
                            .scent(rs.getString("scentName"))
                            .baseNote(rs.getString("baseNote"))
                            .middleNote(rs.getString("middleNote"))
                            .topNote(rs.getString("topNote"))
                            .season(rs.getString("season"))
                            .mainAccords(rs.getString("mainAccords"))
                            .buyUrl(rs.getString("buyUrl"))
                            .sillage(rs.getString("sillage"))
                            .longevityRating(rs.getString("longevityRating"))
                            .imageUrl(rs.getString("imageUrl"))
                            .build(),
                    insertId);
        } else if (standardNum == 2) {
            String getInfoQuery = "select\n" +
                    "\n" +
                    "                                           name,g.genderStatus,\n" +
                    "                                           s.scentName,baseNote,middleNote,topNote,season,mainAccords,\n" +
                    "       buyUrl,sillage,imageUrl,longevityRating\n" +
                    "                                from perfumeDataCopy pc\n" +
                    "                                        left join gender g on pc.genderId = g.genderId\n" +
                    "                                    left join scents s on pc.scentId = s.scentIdx\n" +
                    "                                    left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                                    left join season se on pc.seasonId = se.seasonId\n" +
                    "                                    inner join(select perfumeId, aR.standard\n" +
                    "                                    from survey\n" +
                    "                                    inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                                    where aR.surveyId = ? and standard = 'baseNote') li on pc.perfumeId = li.perfumeId\n" +
                    "                                    order by rating;";

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("baseNote")
                            .number(rowNum + 1)
                            .name(rs.getString("name"))
                            .imageUrl(rs.getString("imageUrl"))
                            .build(),
                    insertId);
        }else if (standardNum == 3) {


            String getInfoQuery = "select\n" +
                    "\n" +
                    "                                           name,g.genderStatus,\n" +
                    "                                           s.scentName,baseNote,middleNote,topNote,season,mainAccords,\n" +
                    "       buyUrl,sillage,imageUrl,longevityRating\n" +
                    "                                from perfumeDataCopy pc\n" +
                    "                                        left join gender g on pc.genderId = g.genderId\n" +
                    "                                    left join scents s on pc.scentId = s.scentIdx\n" +
                    "                                    left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                                    left join season se on pc.seasonId = se.seasonId\n" +
                    "                                    inner join(select perfumeId, aR.standard\n" +
                    "                                    from survey\n" +
                    "                                    inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                                    where aR.surveyId = ? and standard = 'baseNote') li on pc.perfumeId = li.perfumeId\n" +
                    "                                    order by rating";


            //String imgSource = new String("perfume/")
            //imgName.get(0).get("perfumeId");
            String imgPath = s3UploadService.getThumbnailPath(".jpg");

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("baseNote")
                            .number(rowNum + 1)
                            .name(rs.getString("name"))
                            .gender(rs.getString("genderStatus"))
                            .scent(rs.getString("scentName"))
                            .baseNote(rs.getString("baseNote"))
                            .middleNote(rs.getString("middleNote"))
                            .topNote(rs.getString("topNote"))
                            .season(rs.getString("season"))
                            .mainAccords(rs.getString("mainAccords"))
                            .buyUrl(rs.getString("buyUrl"))
                            .sillage(rs.getString("sillage"))
                            .longevityRating(rs.getString("longevityRating"))
                            .imageUrl(rs.getString("imageUrl"))
                            .build(),
                    insertId);
        }
        return new ArrayList<>();
    }
    @Transactional(rollbackFor = Exception.class)
    public String rate(int rating){

       return new String();
    }
}
