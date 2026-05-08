from dotenv import load_dotenv

load_dotenv()

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from models import RecommendationRequest, RecommendationResponse
from recommendation import build_recommendation
from weather import WeatherError

app = FastAPI(
    title="Smart Irrigation AI Service",
    description="Generates irrigation recommendations from parcel data + weather.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/ai/recommendation", response_model=RecommendationResponse)
async def recommend(req: RecommendationRequest):
    try:
        return await build_recommendation(req)
    except WeatherError as e:
        raise HTTPException(status_code=502, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Internal error: {e}")
