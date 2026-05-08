from datetime import date, timedelta
from typing import Optional

MAX_SOIL_BUFFER_MM = 30.0

CROP_DAILY_WATER_MM = {
    "tomato": 5.0, "domati": 5.0, "domat": 5.0,
    "pepper": 4.5, "piperka": 4.5, "piperki": 4.5,
    "cucumber": 5.5, "krastavica": 5.5, "krastavici": 5.5,
    "corn": 6.0, "pcenka": 6.0, "kukuruz": 6.0,
    "wheat": 4.0, "pcenica": 4.0, "psenica": 4.0,
    "potato": 5.0, "kompir": 5.0, "kompiri": 5.0,
    "onion": 4.0, "kromid": 4.0,
    "lettuce": 3.5, "salata": 3.5, "marula": 3.5,
    "carrot": 4.0, "morkov": 4.0, "morkva": 4.0,
    "cabbage": 4.5, "zelka": 4.5,
    "watermelon": 5.5, "lubenica": 5.5,
    "melon": 5.0, "dinja": 5.0,
    "grape": 3.5, "lozje": 3.5, "grozje": 3.5,
    "apple": 4.5, "jabolko": 4.5, "jabolki": 4.5,
    "pear": 4.0, "krusha": 4.0, "krushi": 4.0,
    "plum": 4.0, "sliva": 4.0, "slivi": 4.0,
    "strawberry": 4.0, "jagoda": 4.0, "jagodi": 4.0,
    "default": 4.5,
}


def base_water_need_mm(crop: str) -> float:
    return CROP_DAILY_WATER_MM.get(crop.lower().strip(), CROP_DAILY_WATER_MM["default"])


def temperature_factor(temp_max_c: float) -> float:
    if temp_max_c >= 35:
        return 1.40
    if temp_max_c >= 30:
        return 1.25
    if temp_max_c >= 25:
        return 1.10
    if temp_max_c >= 20:
        return 1.00
    if temp_max_c >= 15:
        return 0.85
    return 0.70


def humidity_factor(humidity_pct: float) -> float:
    if humidity_pct <= 30:
        return 1.20
    if humidity_pct <= 50:
        return 1.10
    if humidity_pct <= 70:
        return 1.00
    return 0.85


def recency_factor(days_since_last: Optional[int]) -> float:
    if days_since_last is None:
        return 1.0
    if days_since_last <= 0:
        return 0.0
    if days_since_last == 1:
        return 0.4
    if days_since_last == 2:
        return 0.7
    if days_since_last == 3:
        return 0.9
    return 1.0


def compute_soil_buffer_mm(
    recent_irrigations: list,
    area_m2: float,
    crop: str,
    today: Optional[date] = None,
) -> float:
    """Estimate residual soil moisture (mm above baseline) from past irrigations.

    Simple bucket model: each irrigation adds (waterAmount / area_m2) mm to the
    buffer; each day the buffer decays by `base_water_need_mm(crop)` (≈ daily
    evapotranspiration). The buffer is clamped to [0, MAX_SOIL_BUFFER_MM] —
    excess water drains away.

    `recent_irrigations` is expected to be a list of objects with `.date` and
    `.waterAmount` attributes (Pydantic IrrigationRecord) or matching dict keys.
    """
    if not recent_irrigations or area_m2 <= 0:
        return 0.0
    if today is None:
        today = date.today()

    daily_loss = base_water_need_mm(crop)

    by_date: dict[date, float] = {}
    for irr in recent_irrigations:
        irr_date = getattr(irr, "date", None) or (irr.get("date") if isinstance(irr, dict) else None)
        irr_amount = getattr(irr, "waterAmount", None) or (irr.get("waterAmount") if isinstance(irr, dict) else None)
        if irr_date is None or irr_amount is None:
            continue
        if irr_date > today:
            continue  # ignore future-dated records
        by_date[irr_date] = by_date.get(irr_date, 0.0) + float(irr_amount)

    if not by_date:
        return 0.0

    cursor = min(by_date.keys())
    buffer = 0.0
    while cursor <= today:
        if cursor in by_date:
            buffer += by_date[cursor] / area_m2
        if cursor < today:
            buffer = max(0.0, buffer - daily_loss)
        cursor += timedelta(days=1)

    return round(min(buffer, MAX_SOIL_BUFFER_MM), 2)


def compute_deficit(
    crop: str,
    temp_max_c: float,
    humidity_pct: float,
    rain_last_24h_mm: float,
    rain_next_24h_mm: float,
    days_since_last_irrigation: Optional[int],
    soil_buffer_mm: float = 0.0,
) -> dict:
    base = base_water_need_mm(crop)
    t_f = temperature_factor(temp_max_c)
    h_f = humidity_factor(humidity_pct)
    r_f = recency_factor(days_since_last_irrigation)

    gross = base * t_f * h_f * r_f
    effective_rain = (rain_last_24h_mm + rain_next_24h_mm) * 0.7
    deficit = max(0.0, gross - effective_rain - soil_buffer_mm)

    return {
        "baseMm": round(base, 2),
        "temperatureFactor": round(t_f, 2),
        "humidityFactor": round(h_f, 2),
        "recencyFactor": round(r_f, 2),
        "soilBufferMm": round(soil_buffer_mm, 2),
        "grossNeedMm": round(gross, 2),
        "effectiveRainMm": round(effective_rain, 2),
        "deficitMm": round(deficit, 2),
    }


def liters_for_area(deficit_mm: float, area_m2: float) -> float:
    return round(deficit_mm * area_m2, 1)


def best_time_from_forecast(forecast_list: list) -> str:
    """Pick the coolest 3-hour window in the next ~24h with no rain.

    `forecast_list` is OWM /forecast `.list[]`. Each entry has dt_txt 'YYYY-MM-DD HH:MM:SS',
    main.temp, optionally rain['3h'].
    """
    best = None
    best_temp = float("inf")
    for entry in forecast_list[:8]:
        rain = (entry.get("rain") or {}).get("3h") or 0
        if rain > 0:
            continue
        temp = entry.get("main", {}).get("temp")
        if temp is None:
            continue
        if temp < best_temp:
            best_temp = temp
            best = entry

    if best is None:
        return "06:00-08:00"

    dt_txt = best.get("dt_txt", "")
    parts = dt_txt.split(" ")
    if len(parts) < 2:
        return "06:00-08:00"
    hh = parts[1][:2]
    try:
        h = int(hh)
    except ValueError:
        return "06:00-08:00"
    end = (h + 3) % 24
    return f"{h:02d}:00-{end:02d}:00"
