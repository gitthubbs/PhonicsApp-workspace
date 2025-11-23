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
        String outputPath = inputFile.getAbsolutePath().replaceAll("\\.\\w+$", "_converted.wav");
        File outputFile = new File(outputPath);

        // 强制转换为 16000Hz 单声道，以匹配 Python 脚本要求
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputFile.getAbsolutePath(),
                "-ar", "16000",
                "-ac", "1",
                outputPath);

        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 读取输出流，防止缓冲区满导致死锁，并记录日志
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[FFmpeg] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 转换失败，退出码: " + exitCode);
        }

        if (!outputFile.exists()) {
            throw new RuntimeException("WAV 转换失败，输出文件未生成: " + outputPath);
        }
        return outputFile;
    }
}