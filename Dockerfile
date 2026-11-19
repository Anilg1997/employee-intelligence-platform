FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

COPY src ./src
COPY models ./models

RUN addgroup --gid 10001 appgroup \
    && adduser --uid 10001 --ingroup appgroup --disabled-password --gecos "" appuser \
    && chown -R appuser:appgroup /app

USER appuser
ENV HOME=/tmp

EXPOSE 8000

CMD ["uvicorn", "src.api:app", "--host", "0.0.0.0", "--port", "8000"]