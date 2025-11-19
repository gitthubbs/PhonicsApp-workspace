package com.ray.phonicappserver.audio;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
public class AudioConvertService {

    @PostConstruct
    public void init() {
        try {
            Process process = new ProcessBuilder("ffmpeg", "-version").start();
            if (process.waitFor() != 0) {
                throw new RuntimeException("FFmpeg未正确安装");
            }
        } catch (Exception e) {
            throw new RuntimeException("FFmpeg检查失败: " + e.getMessage());
        }
    }

    public File convertToWav(File inputFile) throws Exception {
        String outputPath = inputFile.getAbsolutePath().replaceAll("\\.\\w+$", ".wav");
        File outputFile = new File(outputPath);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputFile.getAbsolutePath(),
                "-ac", "1",          // 单声道
                "-ar", "16000",      // 采样率16kHz
                "-sample_fmt", "s16",// PCM
                outputPath
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();

        if (!outputFile.exists()) {
            throw new RuntimeException("WAV 转换失败，请检查 FFmpeg 是否安装");
        }
        return outputFile;
    }
}