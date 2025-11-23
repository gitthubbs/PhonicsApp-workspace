from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import soundfile as sf
import librosa
import torch
from transformers import Wav2Vec2ForCTC, Wav2Vec2Processor
from jiwer import wer
import re

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

# 加载 Wav2Vec2 模型 (使用轻量级英文模型)
print("正在加载 Wav2Vec2 模型...")
processor = Wav2Vec2Processor.from_pretrained("facebook/wav2vec2-base-960h")
model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")
model.eval()
print("模型加载完成!")

def is_silent(wav, threshold=0.01):
    """
    检查音频是否为静音
    """
    if len(wav) == 0:
        return True
    rms = np.sqrt(np.mean(wav**2))
    return rms < threshold

def transcribe_audio(wav, sr=16000):
    """
    使用 Wav2Vec2 转录音频
    """
    # 确保音频是 float32 类型
    if wav.dtype != np.float32:
        wav = wav.astype(np.float32)
    
    # 处理音频
    inputs = processor(wav, sampling_rate=sr, return_tensors="pt", padding=True)
    
    # 推理
    with torch.no_grad():
        logits = model(inputs.input_values).logits
    
    # 解码
    predicted_ids = torch.argmax(logits, dim=-1)
    transcription = processor.batch_decode(predicted_ids)[0]
    
    return transcription.lower().strip()

def normalize_text(text):
    """
    标准化文本:移除标点符号,转小写
    """
    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

def calculate_phoneme_similarity(text1, text2):
    """
    计算音素级别的相似度,并返回详细的错误信息
    返回: (相似度, 错误列表)
    """
    text1 = normalize_text(text1)
    text2 = normalize_text(text2)
    
    # 完全匹配
    if text1 == text2:
        return 1.0, []
    
    if len(text1) == 0 or len(text2) == 0:
        return 0.0, [{"type": "missing", "expected": text2, "actual": text1}]
    
    # 计算编辑距离并记录操作
    m, n = len(text1), len(text2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    for i in range(m + 1):
        dp[i][0] = i
    for j in range(n + 1):
        dp[0][j] = j
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i-1] == text2[j-1]:
                dp[i][j] = dp[i-1][j-1]
            else:
                dp[i][j] = 1 + min(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
    
    edit_distance = dp[m][n]
    
    # 回溯找出具体的错误
    errors = []
    i, j = m, n
    while i > 0 or j > 0:
        if i > 0 and j > 0 and text1[i-1] == text2[j-1]:
            i -= 1
            j -= 1
        elif i > 0 and j > 0 and dp[i][j] == dp[i-1][j-1] + 1:
            # 替换
            errors.append({
                "type": "substitution",
                "position": j-1,
                "expected": text2[j-1],
                "actual": text1[i-1]
            })
            i -= 1
            j -= 1
        elif j > 0 and dp[i][j] == dp[i][j-1] + 1:
            # 插入(标准中有,用户漏读)
            errors.append({
                "type": "missing",
                "position": j-1,
                "expected": text2[j-1]
            })
            j -= 1
        else:
            # 删除(用户多读)
            errors.append({
                "type": "extra",
                "position": i-1,
                "actual": text1[i-1]
            })
            i -= 1
    
    errors.reverse()
    
    # 使用更宽松的评分算法
    # 基础分数: 1 - (编辑距离 / 最大长度)
    max_len = max(len(text1), len(text2))
    base_similarity = 1.0 - (edit_distance / max_len)
    
    # 调整分数:每个错误扣除的分数递减
    # 第1个错误: -15%, 第2个错误: -10%, 第3个及以后: -5%
    penalty = 0
    for idx, error in enumerate(errors):
        if idx == 0:
            penalty += 0.15
        elif idx == 1:
            penalty += 0.10
        else:
            penalty += 0.05
    
    adjusted_similarity = max(0.0, base_similarity - penalty)
    
    # 确保至少有基础分(如果只有1-2个错误)
    if len(errors) <= 2:
        adjusted_similarity = max(adjusted_similarity, 0.5)
    
    return adjusted_similarity, errors

@app.post("/similarity")
async def calc_similarity(file1: UploadFile = File(...), target_text: str = ""):
    """
    计算音频发音与目标文本的相似度 (基于 Wav2Vec2 语音识别)
    file1: 用户录音文件
    target_text: 目标单词文本
    """
    # 读取音频
    wav1, sr1 = sf.read(file1.file)

    # 如果是多声道,转为单声道
    if wav1.ndim > 1:
        wav1 = np.mean(wav1, axis=1)

    # 简单的静音检测
    if is_silent(wav1):
        return {
            "similarity": 0.0,
            "recognized": "",
            "expected": target_text,
            "errors": [{"type": "silent", "message": "检测到静音"}]
        }

    # 重采样到 16kHz
    target_sr = 16000
    if sr1 != target_sr:
        wav1 = librosa.resample(wav1, orig_sr=sr1, target_sr=target_sr)

    # 转录用户录音
    recognized_text = transcribe_audio(wav1, sr=target_sr)
    
    print(f"转录结果: '{recognized_text}' vs 目标: '{target_text}'")
    
    # 计算相似度和错误信息
    similarity, errors = calculate_phoneme_similarity(recognized_text, target_text)
    
    print(f"相似度: {similarity:.4f}, 错误数: {len(errors)}")
    if errors:
        print(f"错误详情: {errors}")

    return {
        "similarity": float(similarity),
        "recognized": recognized_text,
        "expected": target_text,
        "errors": errors
    }

