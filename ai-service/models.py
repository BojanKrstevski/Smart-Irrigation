from datetime import date
from typing import Optional

from pydantic import BaseModel, Field


class IrrigationRecord(BaseModel):
    date: date
    waterAmount: float


class RecommendationRequest(BaseModel):
    parcelId: int
    name: str
    location: str = Field(..., description="City name, e.g. 'Skopje'")
    size: float = Field(..., gt=0, description="Parcel area in m²")
    cropType: str
    lastIrrigation: Optional[date] = None
    recentIrrigations: list[IrrigationRecord] = Field(default_factory=list)


class WeatherSnapshot(BaseModel):
    tempC: float
    tempMaxNext24hC: float
    humidityPct: float
    windMs: float
    rainLast24hMm: float
    rainNext24hMm: float
    description: str


class Computation(BaseModel):
    baseMm: float
    temperatureFactor: float
    humidityFactor: float
    recencyFactor: float
    grossNeedMm: float
    effectiveRainMm: float
    deficitMm: float


class RecommendationResponse(BaseModel):
    parcelId: int
    irrigationNeeded: bool
    bestTime: str
    recommendedWaterAmount: float = Field(..., description="Liters")
    explanation: str
    weather: WeatherSnapshot
    computation: Computation
    explanationSource: str = Field(..., description="'llm' or 'template'")
