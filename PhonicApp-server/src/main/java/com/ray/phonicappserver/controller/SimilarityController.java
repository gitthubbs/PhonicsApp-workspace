package com.ray.phonicappserver.controller;

import com.ray.phonicappserver.audio.AudioConvertService;
import com.ray.phonicappserver.audio.PythonApiClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/voice")
public class SimilarityController {

    private final AudioConvertService audioConvertService;
    private final PythonApiClient pythonApiClient;

    public SimilarityController(AudioConvertService audioConvertService,
            PythonApiClient pythonApiClient) {
        this.audioConvertService = audioConvertService;
        this.pythonApiClient = pythonApiClient;
    }

    @PostMapping("/compare")
    public String compare(@RequestParam("audio1") MultipartFile audio1,
            @RequestParam(value = "targetText", required = false) String targetText) {
        try {

            File temp1 = File.createTempFile("audio1", ".webm");

            Files.copy(audio1.getInputStream(), temp1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Temp1 size: " + temp1.length());
            System.out.println("Target text: " + targetText);

            File wav1 = audioConvertService.convertToWav(temp1);

            String result = pythonApiClient.sendToPython(wav1, targetText);

            temp1.delete();
            wav1.delete();

            return result;
        } catch (Exception e) {
            e.printStackTrace();

            return "{\"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

}