package com.varabyte.kobweb.silk.theme.size

import com.varabyte.kobweb.compose.css.*
import org.jetbrains.compose.web.css.*

object BorderRadiusVars {
    val XS by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.125.cssRem)
    val SM by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.25.cssRem)
    val MD by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.375.cssRem)
    val LG by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.375.cssRem)
}

object FontSizeVars {
    val XS by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.75.cssRem)
    val SM by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.875.cssRem)
    val MD by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 1.cssRem)
    val LG by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 1.125.cssRem)
}

object HeightVars {
    val XS by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 1.25.cssRem)
    val SM by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 1.75.cssRem)
    val MD by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 2.25.cssRem)
    val LG by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 2.5.cssRem)
}

object PaddingVars {
    val XS by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.375.cssRem)
    val SM by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.5.cssRem)
    val MD by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.625.cssRem)
    val LG by StyleVariable<CSSLengthValue>(prefix = "silk", defaultFallback = 0.875.cssRem)
}
