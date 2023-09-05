package com.varabyte.kobweb.silk.theme.animation

import com.varabyte.kobweb.compose.css.*
import org.jetbrains.compose.web.css.*

object TransitionDurationVars {
    val UltraFast by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 50.ms)
    val VeryFast by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 100.ms)
    val Fast by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 150.ms)
    val Normal by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 200.ms)
    val Slow by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 300.ms)
    val VerySlow by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 400.ms)
    val UltraSlow by StyleVariable<CSSTimeValue>(prefix = "silk", defaultFallback = 500.ms)
}
