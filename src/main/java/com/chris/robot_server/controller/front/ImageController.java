package com.chris.robot_server.controller.front;

import com.chris.robot_server.service.ImageGeneratorService;
import com.chris.robot_server.vo.LotteryRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/front/image")
public class ImageController {

    @Autowired
    private ImageGeneratorService imageGeneratorService;

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateLotteryImage() throws IOException {
        // 示例数据（从图片中提取的部分行，你可以从数据库加载）
        List<LotteryRow> rows = Arrays.asList(
            new LotteryRow("2026037", "39,36,28,49,12,24,40"),
            new LotteryRow("2026036", "39,36,28,49,12,24,40"),
            new LotteryRow("2026036", "39,36,28,49,12,24,04"),
            new LotteryRow("2026018", "39,36,28,49,12,24,14"),
            new LotteryRow("2026036", "39,36,28,49,12,24,05"),
            new LotteryRow("2026036", "39,36,28,49,12,24,09"),
            new LotteryRow("2026036", "39,36,28,49,12,24,07")
        );

        byte[] imageBytes = imageGeneratorService.generateLotteryImage(rows);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
