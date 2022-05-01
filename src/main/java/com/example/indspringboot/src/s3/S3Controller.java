package com.example.indspringboot.src.s3;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    @Autowired
    private S3Uploader s3Uploader;

    @Autowired
    private S3UploadService s3UploadService;

    @ResponseBody
    @GetMapping("/find")
    public String findImg() {

        String imgPath = s3UploadService.getThumbnailPath("perfume/50 5Th Avenue.jpg");
        return imgPath;
        //log.info(imgPath);
        //Assertions.assertThat(imgPath).isNotNull();
    }
}



