
from __future__ import annotations

import colorsys
from typing import Tuple

HUE = 20.0

MIN_SATURATION = 0.35
MIN_VALUE = 0.35


def _split(colour: int) -> Tuple[int, float, float, float]:
    alpha = (colour >> 24) & 0xFF
    red = ((colour >> 16) & 0xFF) / 255.0
    green = ((colour >> 8) & 0xFF) / 255.0
    blue = (colour & 0xFF) / 255.0
    hue, saturation, value = colorsys.rgb_to_hsv(red, green, blue)
    return alpha, hue, saturation, value


def _join(alpha: int, hue: float, saturation: float, value: float) -> int:
    red, green, blue = colorsys.hsv_to_rgb(hue % 1.0, _clamp(saturation), _clamp(value))
    return ((alpha & 0xFF) << 24
            | int(round(red * 255)) << 16
            | int(round(green * 255)) << 8
            | int(round(blue * 255)))


def _clamp(value: float) -> float:
    return 0.0 if value < 0.0 else (1.0 if value > 1.0 else value)


def captures(colour: int, reference: int) -> bool:
    if colour & 0xFFFFFF == reference & 0xFFFFFF:
        return True

    _alpha, hue, saturation, value = _split(colour)
    _ref_alpha, ref_hue, _ref_saturation, _ref_value = _split(reference)
    if reference & 0xFFFFFF == 0xFE2C55 and saturation >= 0.06 and value >= 0.25 and (hue * 360 >= 300 or hue * 360 <= 10):
        return True
    if saturation < MIN_SATURATION or value < MIN_VALUE:
        return False
    apart = abs(hue - ref_hue)
    apart = min(apart, 1.0 - apart) * 360.0
    return apart <= HUE


def map_colour(colour: int, reference: int, accent: int) -> int:
    if reference == accent or not captures(colour, reference):
        return colour
    if colour & 0xFFFFFF == accent & 0xFFFFFF:
        return colour

    alpha, hue, saturation, value = _split(colour)
    _ra, ref_hue, ref_saturation, ref_value = _split(reference)
    _aa, accent_hue, accent_saturation, accent_value = _split(accent)

    hue = accent_hue + (hue - ref_hue)
    saturation = accent_saturation * (saturation / ref_saturation) if ref_saturation else accent_saturation
    value = accent_value * (value / ref_value) if ref_value else accent_value
    return _join(alpha, hue, saturation, value)
