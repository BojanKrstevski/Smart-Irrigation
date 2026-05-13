from datetime import date

from agronomy import (
    best_time_from_forecast,
    compute_deficit,
    compute_soil_buffer_mm,
    liters_for_area,
)
from llm import generate_explanation
from models import (
    Computation,
    RecommendationRequest,
    RecommendationResponse,
    WeatherSnapshot,
)
from weather import fetch_weather

IRRIGATION_DEFICIT_THRESHOLD_MM = 0.5


async def build_recommendation(req: RecommendationRequest) -> RecommendationResponse:
    weather_data = await fetch_weather(req.location)

    current = weather_data["current"]
    temp_now = float(current["main"]["temp"])
    humidity = float(current["main"]["humidity"])
    wind = float(current.get("wind", {}).get("speed", 0.0))
    description = ""
    if current.get("weather"):
        description = current["weather"][0].get("description", "")

    temp_max_next = weather_data["tempMaxNext24hC"]
    rain_last = weather_data["rainLast24hMm"]
    rain_next = weather_data["rainNext24hMm"]

    today = date.today()

    if req.recentIrrigations:
        soil_buffer = compute_soil_buffer_mm(
            recent_irrigations=req.recentIrrigations,
            area_m2=req.size,
            crop=req.cropType,
            today=today,
        )
        days_since = None  # bucket model already accounts for recency
    else:
        soil_buffer = 0.0
        days_since = (today - req.lastIrrigation).days if req.lastIrrigation else None

    comp = compute_deficit(
        crop=req.cropType,
        temp_max_c=temp_max_next,
        humidity_pct=humidity,
        rain_last_24h_mm=rain_last,
        rain_next_24h_mm=rain_next,
        days_since_last_irrigation=days_since,
        soil_buffer_mm=soil_buffer,
    )

    deficit = comp["deficitMm"]
    irrigation_needed = deficit >= IRRIGATION_DEFICIT_THRESHOLD_MM
    liters = liters_for_area(deficit, req.size) if irrigation_needed else 0.0

    forecast_list = weather_data["forecast"].get("list", [])
    best_time = best_time_from_forecast(forecast_list) if irrigation_needed else "—"

    explanation, source = generate_explanation(
        crop=req.cropType,
        irrigation_needed=irrigation_needed,
        temp_max=temp_max_next,
        humidity=humidity,
        rain_next_24h=rain_next,
        deficit_mm=deficit,
        liters=liters,
        best_time=best_time,
        parcel_size_m2=req.size,
    )

    return RecommendationResponse(
        parcelId=req.parcelId,
        irrigationNeeded=irrigation_needed,
        bestTime=best_time,
        recommendedWaterAmount=liters,
        explanation=explanation,
        weather=WeatherSnapshot(
            tempC=round(temp_now, 1),
            tempMaxNext24hC=temp_max_next,
            humidityPct=humidity,
            windMs=wind,
            rainLast24hMm=rain_last,
            rainNext24hMm=rain_next,
            description=description,
        ),
        computation=Computation(**comp),
        explanationSource=source,
    )
