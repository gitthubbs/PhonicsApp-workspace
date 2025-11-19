from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from resemblyzer import VoiceEncoder
import numpy as np
import soundfile as sf
from fastdtw import fastdtw
from scipy.spatial.distance import cosine

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

encoder = VoiceEncoder()

def frame_audio(wav, frame_size=16000, hop_size=8000):
    """
    将音频切成重叠帧
    frame_size: 每帧长度（样本数）
    hop_size: 帧步长（样本数）
    返回 shape = (num_frames, frame_size)
    """
    num_samples = len(wav)
    frames = []
    for start in range(0, num_samples - frame_size + 1, hop_size):
        frames.append(wav[start:start + frame_size])
    return np.array(frames)

def embed_frames(wav, frame_size=16000, hop_size=8000):
    """
    对每帧调用 embed_utterance
    返回 shape = (num_frames, embedding_dim)
    """
    frames = frame_audio(wav, frame_size, hop_size)
    embeddings = [encoder.embed_utterance(f.astype(np.float32)) for f in frames]
    return np.array(embeddings)

@app.post("/similarity")
async def calc_similarity(file1: UploadFile = File(...), file2: UploadFile = File(...)):
    """
    计算两段音频的相似度
    """
    wav1, _ = sf.read(file1.file)
    wav2, _ = sf.read(file2.file)

    frames1 = embed_frames(wav1)
    frames2 = embed_frames(wav2)

    # 用 DTW 计算两段序列的距离
    distance, _ = fastdtw(frames1, frames2, dist=cosine)

    # 将距离转换为相似度
    similarity = np.exp(-distance)

    return {"similarity": float(similarity)}
