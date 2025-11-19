FROM python:3.9-slim

WORKDIR /app

COPY ./ ./

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gcc \
    libsndfile1-dev \
    libatlas3-base \
    libgfortran5 \
    && rm -rf /var/lib/apt/lists/*

RUN pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
RUN pip config set global.extra-index-url https://download.pytorch.org/whl/cpu

RUN python -m pip install python-multipart


RUN pip install --upgrade pip

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8000

CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "8000"]
