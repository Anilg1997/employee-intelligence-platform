FROM python:3.12-slim

RUN apt-get update \
    && apt-get upgrade -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY src ./src
COPY models ./models
RUN addgroup --gid 10001 appgroup \
    && adduser --uid 10001 --ingroup appgroup --disabled-password --gecos "" appuser \
    && chown -R appuser:appgroup /app

EXPOSE 8000
USER appuser
ENV HOME=/tmp

CMD ["uvicorn", "src.api:app", "--host", "0.0.0.0", "--port", "8000"]
