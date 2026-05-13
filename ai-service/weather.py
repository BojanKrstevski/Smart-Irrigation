import os

import httpx

OWM_BASE = "https://api.openweathermap.org/data/2.5"


class WeatherError(Exception):
    pass


def _city_candidates(city: str) -> list[str]:
    """Generate fallback queries for fuzzy locations like 'Skopje - Aerodrom'."""
    seen = []
    raw = city.strip()
    if raw and raw not in seen:
        seen.append(raw)
    for sep in (" - ", ", ", "/", "-"):
        if sep in raw:
            head = raw.split(sep, 1)[0].strip()
            if head and head not in seen:
                seen.append(head)
    return seen


async def fetch_weather(city: str) -> dict:
    """Fetch current weather + 5-day/3-hour forecast for a city.

    If the city string contains a district suffix (e.g. 'Skopje - Aerodrom') and
    OpenWeatherMap returns 404, retry with just the head ('Skopje').

    Returns a dict with:
      - current: raw OWM /weather response
      - forecast: raw OWM /forecast response
      - rainLast24hMm: mm of rain in current weather snapshot (rain.1h or rain.3h)
      - rainNext24hMm: total mm of rain across next 8 forecast slots (≈ 24h)
      - tempMaxNext24hC: max temp in next 24h forecast
    """
    api_key = os.environ.get("OPENWEATHERMAP_API_KEY")
    if not api_key:
        raise WeatherError("OPENWEATHERMAP_API_KEY not set")

    candidates = _city_candidates(city)
    last_error = None
    current_resp = None
    forecast_resp = None

    async with httpx.AsyncClient(timeout=10.0) as client:
        for candidate in candidates:
            params_common = {"q": candidate, "units": "metric", "appid": api_key}
            try:
                current_resp, forecast_resp = await _gather(
                    client.get(f"{OWM_BASE}/weather", params=params_common),
                    client.get(f"{OWM_BASE}/forecast", params=params_common),
                )
            except httpx.HTTPError as e:
                last_error = WeatherError(f"OpenWeatherMap request failed: {e}")
                continue

            if current_resp.status_code != 404:
                break
            last_error = WeatherError(f"City '{candidate}' not found in OpenWeatherMap")
            current_resp = None  # try next candidate

    if current_resp is None:
        raise last_error or WeatherError(f"City '{city}' not found in OpenWeatherMap")
    if current_resp.status_code != 200:
        raise WeatherError(
            f"OpenWeatherMap /weather returned {current_resp.status_code}: "
            f"{current_resp.text[:200]}"
        )
    if forecast_resp.status_code != 200:
        raise WeatherError(
            f"OpenWeatherMap /forecast returned {forecast_resp.status_code}: "
            f"{forecast_resp.text[:200]}"
        )

    current = current_resp.json()
    forecast = forecast_resp.json()

    rain_last = 0.0
    rain_block = current.get("rain") or {}
    rain_last = float(rain_block.get("1h") or rain_block.get("3h") or 0.0)

    next_slots = forecast.get("list", [])[:8]  # 8 × 3h = 24h
    rain_next = 0.0
    temp_max_next = float("-inf")
    for slot in next_slots:
        slot_rain = (slot.get("rain") or {}).get("3h") or 0.0
        rain_next += float(slot_rain)
        t = slot.get("main", {}).get("temp_max") or slot.get("main", {}).get("temp")
        if t is not None:
            temp_max_next = max(temp_max_next, float(t))

    if temp_max_next == float("-inf"):
        temp_max_next = float(current["main"]["temp"])

    return {
        "current": current,
        "forecast": forecast,
        "rainLast24hMm": round(rain_last, 2),
        "rainNext24hMm": round(rain_next, 2),
        "tempMaxNext24hC": round(temp_max_next, 1),
    }


async def _gather(*coros):
    import asyncio

    return await asyncio.gather(*coros)
