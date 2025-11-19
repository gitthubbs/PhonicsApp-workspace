package com.ray.phonicappserver.controller;

import com.ray.phonicappserver.audio.AudioConvertService;
import com.ray.phonicappserver.audio.PythonApiClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
                          @RequestParam("audio2") MultipartFile audio2) throws Exception {

        File temp1 = File.createTempFile("audio1", ".tmp");
        File temp2 = File.createTempFile("audio2", ".tmp");

        audio1.transferTo(temp1);
        audio2.transferTo(temp2);

        File wav1 = audioConvertService.convertToWav(temp1);
        File wav2 = audioConvertService.convertToWav(temp2);

        String result = pythonApiClient.sendToPython(wav1, wav2);

        temp1.delete();
        temp2.delete();
        wav1.delete();
        wav2.delete();

        return result;
    }
}