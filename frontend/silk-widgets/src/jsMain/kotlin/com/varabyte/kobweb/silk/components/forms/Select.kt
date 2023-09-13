package com.varabyte.kobweb.silk.components.forms


// MISC NOTES TO DELETE BEFORE SUBMITTING
// https://chakra-ui.com/docs/components/select
// https://nextui.org/docs/components/select


import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.dom.ElementRefScope
import com.varabyte.kobweb.compose.dom.ElementTarget
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.icons.ChevronDownIcon
import com.varabyte.kobweb.silk.components.overlay.AdvancedPopover
import com.varabyte.kobweb.silk.components.overlay.KeepPopupOpenStrategy
import com.varabyte.kobweb.silk.components.overlay.ManualOpenClosePopupStrategy
import com.varabyte.kobweb.silk.components.overlay.never
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.ComponentVariant
import com.varabyte.kobweb.silk.components.style.addVariant
import com.varabyte.kobweb.silk.components.style.common.DisabledStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import com.varabyte.kobweb.silk.components.style.vars.animation.TransitionDurationVars
import com.varabyte.kobweb.silk.components.style.vars.color.BorderColorVar
import com.varabyte.kobweb.silk.components.style.vars.color.FocusOutlineColorVar
import com.varabyte.kobweb.silk.components.style.vars.size.BorderRadiusVars
import com.varabyte.kobweb.silk.components.style.vars.size.FontSizeVars
import com.varabyte.kobweb.silk.components.style.vars.size.HeightVars
import com.varabyte.kobweb.silk.components.style.vars.size.PaddingVars
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.theme.colors.ColorScheme
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.w3c.dom.HTMLElement

object SelectDefaults {
    const val Enabled = true
    val Size = SelectSize.MD
    val Variant = OutlinedSelectVariant
}

object SelectVars {
    val BorderColor by StyleVariable(prefix = "silk", defaultFallback = BorderColorVar.value())
    val BorderRadius by StyleVariable<CSSLengthNumericValue>(prefix = "silk", defaultFallback = 0.125.cssRem)
    val ColorTransitionDuration by StyleVariable(
        prefix = "silk",
        defaultFallback = TransitionDurationVars.Normal.value()
    )
    val FontSize by StyleVariable<CSSLengthNumericValue>(prefix = "silk")
    val FocusOutlineColor by StyleVariable(prefix = "silk", defaultFallback = FocusOutlineColorVar.value())
    val FocusOutlineSpread by StyleVariable(prefix = "silk", defaultFallback = 0.1875.cssRem)
    val Height by StyleVariable<CSSLengthNumericValue>(prefix = "silk")
    val IconBackgroundColor by StyleVariable<CSSColorValue>(prefix = "silk")
    val IconBackgroundHoverColor by StyleVariable<CSSColorValue>(prefix = "silk")
    val Padding by StyleVariable<CSSLengthNumericValue>(prefix = "silk")
}

private val ColorTransition = CSSTransition.group(
    listOf("border-color", "box-shadow", "background-color"),
    duration = SelectVars.ColorTransitionDuration.value()
)

val SelectStyle by ComponentStyle(prefix = "silk") {
    base {
        Modifier
            .userSelect(UserSelect.None)
            .textAlign(TextAlign.Start)
            .height(SelectVars.Height.value())
            .fontSize(SelectVars.FontSize.value())
            .outline(0.px, LineStyle.Solid, Colors.Transparent) // Disable, we'll use box shadow instead
            .border(0.px, LineStyle.Solid, Colors.Transparent) // Overridden by variants
            .gap(0.5.cssRem)
            .cursor(Cursor.Pointer)
            .transition(ColorTransition)
    }
}

val SelectOptionsStyle by ComponentStyle(prefix = "silk") {
    base {
        Modifier
            .userSelect(UserSelect.None)
            .cursor(Cursor.Pointer)
            .textAlign(TextAlign.Start)
            .fontSize(SelectVars.FontSize.value())
            .transition(ColorTransition)
    }
}

private fun Modifier.inputPadding(): Modifier {
    val padding = SelectVars.Padding.value()
    return this.paddingInline(start = padding, end = padding)
}

val OutlinedSelectVariant by SelectStyle.addVariant {
    fun Modifier.bordered(color: CSSColorValue): Modifier {
        return this.border(1.px, LineStyle.Solid, color).boxShadow(spreadRadius = 1.px, color = color)
    }

    base {
        Modifier.inputPadding().borderRadius(SelectVars.BorderRadius.value())
            .border(1.px, LineStyle.Solid, SelectVars.BorderColor.value())
    }

//    ariaInvalid { Modifier.bordered(SelectVars.BorderInvalidColor.value()) }
//    (hover + not(disabled)) { Modifier.border { color(SelectVars.BorderHoverColor.value()) } }
//    (focusVisible + not(disabled)) { Modifier.bordered(SelectVars.BorderFocusColor.value()) }
}

val FilledSelectVariant by SelectStyle.addVariant {
    fun Modifier.bordered(color: CSSColorValue): Modifier {
        return this.border { color(color) }.boxShadow(spreadRadius = 1.px, color = color)
    }

//    base {
//        Modifier
//            .inputPadding()
//            .backgroundColor(SelectVars.FilledColor.value())
//            .borderRadius(SelectVars.BorderRadius.value())
//            .border(1.px, LineStyle.Solid, Colors.Transparent)
//    }
//    (hover + not(disabled)) { Modifier.backgroundColor(SelectVars.FilledHoverColor.value()) }
//    ariaInvalid { Modifier.bordered(SelectVars.BorderInvalidColor.value()) }
//    (focusVisible + not(disabled)) {
//        Modifier
//            .backgroundColor(SelectVars.FilledFocusColor.value())
//            .bordered(SelectVars.BorderFocusColor.value())
//    }
}


interface SelectSize {
    val borderRadius: CSSLengthNumericValue
    val fontSize: CSSLengthNumericValue
    val height: CSSLengthNumericValue
    val padding: CSSLengthNumericValue


    object XS : SelectSize {
        override val borderRadius = BorderRadiusVars.XS.value()
        override val fontSize = FontSizeVars.XS.value()
        override val height = HeightVars.XS.value()
        override val padding = PaddingVars.XS.value()
    }

    object SM : SelectSize {
        override val borderRadius = BorderRadiusVars.SM.value()
        override val fontSize = FontSizeVars.SM.value()
        override val height = HeightVars.SM.value()
        override val padding = PaddingVars.SM.value()
    }

    object MD : SelectSize {
        override val borderRadius = BorderRadiusVars.MD.value()
        override val fontSize = FontSizeVars.MD.value()
        override val height = HeightVars.MD.value()
        override val padding = PaddingVars.MD.value()
    }

    object LG : SelectSize {
        override val borderRadius = BorderRadiusVars.LG.value()
        override val fontSize = FontSizeVars.LG.value()
        override val height = HeightVars.LG.value()
        override val padding = PaddingVars.LG.value()
    }
}

fun SelectSize.toModifier() = Modifier
    .setVariable(SelectVars.BorderRadius, borderRadius)
    .setVariable(SelectVars.FontSize, fontSize)
    .setVariable(SelectVars.Height, height)
    .setVariable(SelectVars.Padding, padding)

class SelectOption(
    val id: String,
    val text: String,
)

@Composable
fun Select(
    options: List<String>,
    selectedOption: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = SelectDefaults.Variant,
    enabled: Boolean = SelectDefaults.Enabled,
    size: SelectSize = SelectDefaults.Size,
    colorScheme: ColorScheme? = null,
    ref: ElementRefScope<HTMLElement>? = null,
) {
    Select(
        options.map { SelectOption(it, it) },
        selectedOption,
        onSelected,
        modifier,
        variant,
        enabled,
        size,
        colorScheme,
        ref,
    )
}


@Composable
fun Select(
    options: List<SelectOption>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    variant: ComponentVariant? = null,
    enabled: Boolean = true,
    size: SelectSize = SelectDefaults.Size,
    colorScheme: ColorScheme? = null,
    ref: ElementRefScope<HTMLElement>? = null,
) {
    val optionsMap = remember(options) { options.associate { it.id to it.text } }
    val optionsMenuOpenCloseStrategy = remember { ManualOpenClosePopupStrategy() }
    val optionsMenuKeepOpenStrategy = remember { KeepPopupOpenStrategy.never() }

    Row(
        SelectStyle
            .toModifier(variant)
            .then(size.toModifier())
            .thenIf(!enabled, DisabledStyle.toModifier())
            .then(modifier)
            .thenIf(enabled) {
                Modifier.onClick { evt ->
                    evt.stopPropagation()
                    optionsMenuOpenCloseStrategy.isOpen = true
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedId != null) {
            optionsMap[selectedId]?.let { SpanText(it, Modifier.flexGrow(1)) }
        }
        // Use "em" to be relative to parent font size
        ChevronDownIcon(Modifier.fontSize(0.7.em))
    }

    AdvancedPopover(
        ElementTarget.PreviousSibling,
        openCloseStrategy = optionsMenuOpenCloseStrategy,
        keepOpenStrategy = optionsMenuKeepOpenStrategy,
    ) {
        Column(SelectOptionsStyle.toModifier()) {
            options.forEach { option ->
                Div(
                    Modifier
                        .onClick {
                            onSelected(option.id)
                            optionsMenuOpenCloseStrategy.isOpen = false
                        }
                        .padding(0.5.cssRem)
                        .thenIf(option.id == selectedId) {
                            Modifier.backgroundColor(Colors.LightGray)
                        }
                        .toAttrs()
                ) {
                    Text(option.text)
                }
            }
        }
    }
}

//    checked: CheckedState,
//    onCheckedChange: (CheckedState) -> Unit,
//    modifier: Modifier = Modifier,
//    variant: ComponentVariant? = null,
//    enabled: Boolean = CheckboxDefaults.Enabled,
//    icon: @Composable CheckboxIconScope.() -> Unit = CheckboxDefaults.IconProvider,
//    size: CheckboxSize = CheckboxDefaults.Size,
//    spacing: CSSLengthValue? = null,
//    colorScheme: ColorScheme? = null,
//    borderColor: CSSColorValue? = null,
//    uncheckedColor: CSSColorValue? = null,
//    iconColor: CSSColorValue? = null,
//    focusOutlineColor: CSSColorValue? = null,
//    ref: ElementRefScope<HTMLElement>? = null,
//    content: (@Composable () -> Unit)? = null,
//) {
//    // Don't animate if a checkbox is being added to the DOM while already checked
//    var shouldAnimate by remember { mutableStateOf(!checked.toBoolean()) }
//
//    val colorMode = ColorMode.current
//
//    // Use a label so it intercepts clicks and passes them to the inner Input
//    Label(
//        attrs = CheckboxStyle
//            .toModifier(variant).thenIf(!enabled, DisabledStyle.toModifier()).then(size.toModifier())
//            .setVariable(SelectVars.Spacing, spacing)
//            .thenIf(colorScheme != null) {
//                @Suppress("NAME_SHADOWING") val colorScheme = colorScheme!!
//                val isDark = colorMode.isDark
//                val isBrightColor = (if (isDark) colorScheme._200 else colorScheme._500).isBright
//                Modifier
//                    .setVariable(SelectVars.IconBackgroundColor, if (isDark) colorScheme._200 else colorScheme._500)
//                    .setVariable(
//                        SelectVars.IconBackgroundHoverColor, if (isDark) colorScheme._300 else colorScheme._600
//                    ).setVariable(
//                        SelectVars.IconColor,
//                        (if (isBrightColor) ColorMode.LIGHT else ColorMode.DARK).toSilkPalette().color
//                    )
//            }
//            .setVariable(SelectVars.BorderColor, borderColor)
//            .setVariable(SelectVars.UncheckedBackgroundColor, uncheckedColor)
//            .setVariable(SelectVars.IconColor, iconColor)
//            .setVariable(SelectVars.FocusOutlineColor, focusOutlineColor).then(modifier).toAttrs()
//    ) {
//        registerRefScope(ref)
//        // We base Checkbox on a checkbox input for a11y + built-in input/keyboard support, but hide the checkbox itself
//        // and render the box + icon separately. We do however allow it to be focused, which combined with the outer
//        // label means that both clicks and keyboard events will toggle the checkbox.
//        Input(
//            type = InputType.Checkbox,
//            value = checked.toBoolean(),
//            onValueChanged = {
//                onCheckedChange(
//                    when (checked) {
//                        CheckedState.Checked -> CheckedState.Unchecked
//                        CheckedState.Unchecked -> CheckedState.Checked
//                        CheckedState.Indeterminate -> CheckedState.Checked
//                    }
//                )
//                shouldAnimate = true
//            },
//            variant = CheckboxInputVariant,
//            enabled = enabled,
//        )
//
//        Box(
//            CheckboxIconContainerStyle.toModifier(
//                if (checked.toBoolean()) CheckedCheckboxIconContainerVariant else UncheckedCheckboxIconContainerVariant
//            ),
//            contentAlignment = Alignment.Center
//        ) {
//            if (checked.toBoolean()) {
//                Box(
//                    CheckboxIconStyle.toModifier().thenIf(shouldAnimate) {
//                        Modifier.animation(
//                            CheckboxEnabledAnim.toAnimation(colorMode, SelectVars.TransitionDuration.value())
//                        )
//                    }, contentAlignment = Alignment.Center
//                ) {
//                    CheckboxIconScope(
//                        indeterminate = checked == CheckedState.Indeterminate, colorMode
//                    ).apply { icon() }
//                }
//            }
//        }
//
//        if (content != null) content()
//    }
