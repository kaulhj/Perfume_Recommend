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
    public String pushPrice(int priceRangeNum) {
        String lastId = "select surveyId from survey order by surveyId\n" +
                "desc limit 1";
        int insertId = this.jdbcTemplate.queryForObject(lastId, int.class);
        String pushPriceQuery = "update survey set priceRangeId = ? where surveyId = ?";
        Object[] pushPriceParams = new Object[]{priceRangeNum, insertId};
        this.jdbcTemplate.update(pushPriceQuery, pushPriceParams);

        String dataQuery = "select genderId,spendTimeId,scentId,priceRangeId\n" +
                "from survey\n" +
                "where surveyId = ?";
        Survey dataList = this.jdbcTemplate.queryForObject(dataQuery,
                (rs, rowNum) -> Survey.SurveyBuilder()
                        .gender(rs.getInt("genderId"))
                        .time(rs.getInt("spendTimeId"))
                        .scent(rs.getInt("scentId"))
                        .price(rs.getInt("priceRangeId"))
                        .build()
                , insertId);

        String filterInsertQuery = "update survey set resultId1 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and priceRangeId = ?\n" +
                "order by item_rating desc limit 1 offset 0),\n" +
                "                  resultId2 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and priceRangeId = ?\n" +
                "order by item_rating desc limit 1 offset 1),\n" +
                "                  resultId3 = (select perfumeId\n" +
                "from perfumeDataCopy\n" +
                "where (genderId = ? or genderId = 3) and spendTimeId = ? and\n" +
                "      scentId = ? and priceRangeId = ?\n" +
                "order by item_rating desc limit 1 offset 2)\n" +
                "where surveyId = ?\n";
        Object[] filters = new Object[]{dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getPrice(),
                dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getPrice(),
                dataList.getGender(), dataList.getTime(), dataList.getScent(), dataList.getPrice(),
                insertId};
        this.jdbcTemplate.update(filterInsertQuery, filters);


        int firstPerfumeId = this.jdbcTemplate.queryForObject("select resultId1\n" +
                "from survey\n" +
                "where surveyId = ?", int.class, insertId);

        String insertResultQuery = "insert Into algorithmResult( surveyId, rankByRating, perfumeId,standard)\n" +
                " values (?, ? ,?,?)";
        Object[] insertResultParams = new Object[]{insertId, 1, firstPerfumeId, "base_note"};


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


        List<Integer> Ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        if (standardNum == 1) {
            for (int i = 0; i < 3; i++) {

                List<Integer> counts = new ArrayList<>(Arrays.asList(0, 1, 2));

                Ids.add(this.jdbcTemplate.queryForObject("SELECT a.perfumeId\n" +
                        "FROM algorithmResult a \n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on a.perfumeId = pDC.perfumeId\n" +
                        "WHERE surveyId = ? and standard = 'base_note' \n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", int.class, insertId, counts.get(i))
                );
                names.add(this.jdbcTemplate.queryForObject("select name\n" +
                        "from algorithmResult\n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on algorithmResult.perfumeId = pDC.perfumeId\n" +
                        "where surveyId = ? and standard = 'base_note'\n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", String.class, insertId, counts.get(i)));
                imagePaths.add(new String("perfume/" + Ids.get(i) + " " + names.get(i) + ".jpg"));
                imageUrls.add(s3UploadService.getThumbnailPath(imagePaths.get(i)));

            }


            String getInfoQuery = "select\n" +
                    "\n" +
                    "                       pc.perfumeId,name,brand,new_price,ml,concentration,g.genderStatus,\n" +
                    "                       s.scentName,base_note,middle_note,item_rating,seller\n" +
                    "                from perfumeDataCopy pc\n" +
                    "                    left join gender g on pc.genderId = g.genderId\n" +
                    "                left join scents s on pc.scentId = s.scentIdx\n" +
                    "                left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                #left join algorithmResult a on pc.perfumeId = a.perfumeId\n" +
                    "                inner join(select perfumeId, aR.standard\n" +
                    "                from survey\n" +
                    "                inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                where aR.surveyId = ? and standard = 'base_note') li on pc.perfumeId = li.perfumeId\n" +
                    "                order by item_rating";


            //String imgSource = new String("perfume/")
            //imgName.get(0).get("perfumeId");
            String imgPath = s3UploadService.getThumbnailPath(".jpg");

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("base_note")
                            .number(rowNum + 1)
                            .brand(rs.getString("brand"))
                            .name(rs.getString("name"))
                            .price(rs.getDouble("new_price"))
                            .ml(rs.getInt("ml"))
                            .concentration(rs.getString("concentration"))
                            .gender(rs.getString("genderStatus"))
                            .scent(rs.getString("scentName"))
                            .base_note(rs.getString("base_note"))
                            .middle_note(rs.getString("middle_note"))
                            .rating(rs.getDouble("item_rating"))
                            .seller(rs.getString("seller"))
                            .imgUrl(imageUrls.get(rowNum))
                            .build(),
                    insertId);
        } else if (standardNum == 2) {
            for (int i = 0; i < 2; i++) {

                List<Integer> counts = new ArrayList<>(Arrays.asList(0, 1, 2));

                Ids.add(this.jdbcTemplate.queryForObject("SELECT a.perfumeId\n" +
                        "FROM algorithmResult a \n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on a.perfumeId = pDC.perfumeId\n" +
                        "WHERE surveyId = ? and standard = 'middle_note' \n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", int.class, insertId, counts.get(i))
                );
                names.add(this.jdbcTemplate.queryForObject("select name\n" +
                        "from algorithmResult\n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on algorithmResult.perfumeId = pDC.perfumeId\n" +
                        "where surveyId = ? and standard = 'middle_note'\n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", String.class, insertId, counts.get(i)));
                imagePaths.add(new String("perfume/" + Ids.get(i) + " " + names.get(i) + ".jpg"));
                imageUrls.add(s3UploadService.getThumbnailPath(imagePaths.get(i)));

            }


            String getInfoQuery = "select\n" +
                    "\n" +
                    "                       pc.perfumeId,name,brand,new_price,ml,concentration,g.genderStatus,\n" +
                    "                       s.scentName,base_note,middle_note,item_rating,seller\n" +
                    "                from perfumeDataCopy pc\n" +
                    "                    left join gender g on pc.genderId = g.genderId\n" +
                    "                left join scents s on pc.scentId = s.scentIdx\n" +
                    "                left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                #left join algorithmResult a on pc.perfumeId = a.perfumeId\n" +
                    "                inner join(select perfumeId, aR.standard\n" +
                    "                from survey\n" +
                    "                inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                where aR.surveyId = ? and standard = 'middle_note') li on pc.perfumeId = li.perfumeId\n" +
                    "                order by item_rating";


            //String imgSource = new String("perfume/")
            //imgName.get(0).get("perfumeId");
            String imgPath = s3UploadService.getThumbnailPath(".jpg");

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("middle_note")
                            .number(rowNum + 1)
                            .name(rs.getString("name"))
                            .imgUrl(imageUrls.get(rowNum))
                            .build(),
                    insertId);
        }else if (standardNum == 3) {
            for (int i = 0; i < 2; i++) {

                List<Integer> counts = new ArrayList<>(Arrays.asList(0, 1, 2));

                Ids.add(this.jdbcTemplate.queryForObject("SELECT a.perfumeId\n" +
                        "FROM algorithmResult a \n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on a.perfumeId = pDC.perfumeId\n" +
                        "WHERE surveyId = ? and standard = 'middle_note' \n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", int.class, insertId, counts.get(i))
                );
                names.add(this.jdbcTemplate.queryForObject("select name\n" +
                        "from algorithmResult\n" +
                        "inner join\n" +
                        "    perfumeDataCopy pDC on algorithmResult.perfumeId = pDC.perfumeId\n" +
                        "where surveyId = ? and standard = 'middle_note'\n" +
                        "ORDER BY pDC.item_rating limit 1 offset ?", String.class, insertId, counts.get(i)));
                imagePaths.add(new String("perfume/" + Ids.get(i) + " " + names.get(i) + ".jpg"));
                imageUrls.add(s3UploadService.getThumbnailPath(imagePaths.get(i)));

            }


            String getInfoQuery = "select\n" +
                    "\n" +
                    "                       pc.perfumeId,name,brand,new_price,ml,concentration,g.genderStatus,\n" +
                    "                       s.scentName,base_note,middle_note,item_rating,seller\n" +
                    "                from perfumeDataCopy pc\n" +
                    "                    left join gender g on pc.genderId = g.genderId\n" +
                    "                left join scents s on pc.scentId = s.scentIdx\n" +
                    "                left join spendTime sT on pc.spendTimeId = sT.spendTimeId\n" +
                    "                #left join algorithmResult a on pc.perfumeId = a.perfumeId\n" +
                    "                inner join(select perfumeId, aR.standard\n" +
                    "                from survey\n" +
                    "                inner join algorithmResult aR on survey.surveyId = aR.surveyId\n" +
                    "                where aR.surveyId = ? and standard = 'middle_note') li on pc.perfumeId = li.perfumeId\n" +
                    "                order by item_rating";


            //String imgSource = new String("perfume/")
            //imgName.get(0).get("perfumeId");
            String imgPath = s3UploadService.getThumbnailPath(".jpg");

            return this.jdbcTemplate.query(getInfoQuery,
                    (rs, rowNum) -> GetRecommends.builder()
                            .standard("middle_note")
                            .number(rowNum + 1)
                            .brand(rs.getString("brand"))
                            .name(rs.getString("name"))
                            .price(rs.getDouble("new_price"))
                            .ml(rs.getInt("ml"))
                            .concentration(rs.getString("concentration"))
                            .gender(rs.getString("genderStatus"))
                            .scent(rs.getString("scentName"))
                            .base_note(rs.getString("base_note"))
                            .middle_note(rs.getString("middle_note"))
                            .rating(rs.getDouble("item_rating"))
                            .seller(rs.getString("seller"))
                            .imgUrl(imageUrls.get(rowNum))
                            .build(),
                    insertId);
        }
        return new ArrayList<>();
    }
}
