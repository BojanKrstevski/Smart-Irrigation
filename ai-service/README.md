# Smart Irrigation — AI Service (FastAPI)

Микросервис кој прима податоци за парцела, зема моментални временски услови
од OpenWeatherMap, ја пресметува потребата за наводнување според агрономски
правила и генерира објаснување (со LLM ако има клуч, инаку шаблон).

Се повикува од Spring Boot backend-от преку HTTP.

## Барања
- Python 3.10+
- OpenWeatherMap API key (бесплатен на https://openweathermap.org/api)
- (опционално) Groq или Anthropic API key за AI објаснувања

## Поставување

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env   # ако веќе нема .env
# уреди го .env со твоите клучеви
```

## Стартување

```bash
uvicorn main:app --reload --port 8000
```

Сервисот сега слуша на `http://localhost:8000`.
- `GET /health` — провери дали ради
- `POST /ai/recommendation` — препорака за парцела
- `GET /docs` — автоматска Swagger документација

## Пример повик

```bash
curl -X POST http://localhost:8000/ai/recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "parcelId": 1,
    "name": "Парцела со домати",
    "location": "Skopje",
    "size": 200.0,
    "cropType": "domati",
    "lastIrrigation": "2026-04-30",
    "recentIrrigations": []
  }'
```

## LLM провајдери (по приоритет)

Сервисот ги пробува по овој ред и паѓа на template-based објаснување ако
ниту еден клуч не е поставен:

| # | Провајдер | Цена | Како да добиеш клуч |
|---|---|---|---|
| 1 | **Groq** (Llama 3.3 70B) | бесплатен (rate-limited) | https://console.groq.com → Sign up → API Keys → Create |
| 2 | **Anthropic** (Claude Haiku 4.5) | $5 free кредит, потоа платен | https://console.anthropic.com |
| — | template fallback | бесплатен | без клуч, користи статичен текст |

Полето `explanationSource` во одговорот покажува кој провајдер бил искористен:
`groq`, `anthropic`, или `template`.

**За да го тестира професорот:** доволно е сервисот да се стартира без LLM клуч
— template fallback дава комплетно валиден одговор. Ако сака да го види LLM
објаснувањето, нека направи Groq account (2 минути, бесплатно) и нека го
стави клучот во `.env` како `GROQ_API_KEY=...`.

## Како функционира

1. Зема `current` и `forecast` од OpenWeatherMap за градот.
2. Пресметува дневна потреба за вода:
   `base_mm(crop) × tempFactor × humidityFactor × recencyFactor`
3. Одзема ефективен дожд (последни + следни 24h, 70% усвоен).
4. Дефицит × површина (m²) = литри.
5. Најладниот сув 3-часовен прозорец во следните 24h = `bestTime`.
6. Објаснување — Groq → Anthropic → template (по приоритет, видеш горе).

## Структура

- `main.py` — FastAPI app, endpoint `POST /ai/recommendation`
- `recommendation.py` — оркестрира weather + agronomy + llm
- `weather.py` — OpenWeatherMap клиент
- `agronomy.py` — crop табели, тежински фактори, формула за дефицит
- `llm.py` — multi-provider LLM (Groq → Anthropic → template)
- `models.py` — Pydantic schemas (request/response)
