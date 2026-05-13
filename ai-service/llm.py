import os

GROQ_MODEL = "llama-3.3-70b-versatile"
ANTHROPIC_MODEL = "claude-haiku-4-5-20251001"


def _build_user_prompt(facts: dict) -> str:
    return (
        "Ти си агрономски советник кој зборува со земјоделец на македонски јазик. "
        "Врз основа на следните податоци, напиши КРАТКО објаснување (2–3 реченици) "
        "зошто е (или не е) потребно наводнување денес. Биди јасен, без воведни фрази, "
        "без листи, без markdown.\n\n"
        f"Податоци: {facts}"
    )


def _template_explanation(
    crop: str,
    irrigation_needed: bool,
    temp_max: float,
    humidity: float,
    rain_next_24h: float,
    deficit_mm: float,
    liters: float,
    best_time: str,
) -> str:
    if not irrigation_needed:
        if rain_next_24h > 0:
            return (
                f"Не е потребно наводнување за културата '{crop}'. Се очекува "
                f"{rain_next_24h:.1f} mm дожд во следните 24 часа, што е доволно "
                f"за моменталните потреби."
            )
        return (
            f"Не е потребно наводнување за културата '{crop}'. Влажноста е "
            f"{humidity:.0f}%, а температурата умерена, па почвата нема значителен дефицит."
        )

    return (
        f"Препорачано е наводнување на културата '{crop}' со ~{liters:.0f} литри. "
        f"Температурата ќе достигне {temp_max:.0f}°C, влажноста е {humidity:.0f}%, "
        f"а очекуван дожд {rain_next_24h:.1f} mm — дефицит од {deficit_mm:.1f} mm. "
        f"Најдобро време: {best_time}."
    )


def _try_groq(facts: dict) -> str | None:
    api_key = os.environ.get("GROQ_API_KEY")
    if not api_key:
        return None
    try:
        from groq import Groq
    except ImportError:
        return None

    try:
        client = Groq(api_key=api_key)
        resp = client.chat.completions.create(
            model=GROQ_MODEL,
            max_tokens=300,
            messages=[{"role": "user", "content": _build_user_prompt(facts)}],
        )
        text = (resp.choices[0].message.content or "").strip()
        return text or None
    except Exception:
        return None


def _try_anthropic(facts: dict) -> str | None:
    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        return None
    try:
        from anthropic import Anthropic
    except ImportError:
        return None

    try:
        client = Anthropic(api_key=api_key)
        resp = client.messages.create(
            model=ANTHROPIC_MODEL,
            max_tokens=300,
            messages=[{"role": "user", "content": _build_user_prompt(facts)}],
        )
        text = "".join(
            block.text for block in resp.content if getattr(block, "type", "") == "text"
        ).strip()
        return text or None
    except Exception:
        return None


def generate_explanation(
    crop: str,
    irrigation_needed: bool,
    temp_max: float,
    humidity: float,
    rain_next_24h: float,
    deficit_mm: float,
    liters: float,
    best_time: str,
    parcel_size_m2: float,
) -> tuple[str, str]:
    """Returns (explanation_text, source). source ∈ {'groq', 'anthropic', 'template'}.

    Provider order: Groq → Anthropic → template fallback.
    """
    facts = {
        "култура": crop,
        "површина_m2": parcel_size_m2,
        "максимална_температура_C": temp_max,
        "влажност_%": humidity,
        "очекуван_дожд_24h_mm": rain_next_24h,
        "дефицит_mm": deficit_mm,
        "препорачани_литри": liters,
        "најдобро_време": best_time,
        "потребно_наводнување": irrigation_needed,
    }

    groq_text = _try_groq(facts)
    if groq_text:
        return groq_text, "groq"

    anthropic_text = _try_anthropic(facts)
    if anthropic_text:
        return anthropic_text, "anthropic"

    return _template_explanation(
        crop, irrigation_needed, temp_max, humidity,
        rain_next_24h, deficit_mm, liters, best_time,
    ), "template"
