package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.VolumeUnit
import kotlin.math.roundToInt

private const val ML_PER_OZ = 29.5735

fun mlToDisplay(ml: Int, unit: VolumeUnit): Int = when (unit) {
    VolumeUnit.ML -> ml
    VolumeUnit.OZ -> (ml / ML_PER_OZ).roundToInt()
}

fun displayToMl(value: Int, unit: VolumeUnit): Int = when (unit) {
    VolumeUnit.ML -> value
    VolumeUnit.OZ -> (value * ML_PER_OZ).roundToInt()
}

fun volumeUnitLabel(unit: VolumeUnit): String = when (unit) {
    VolumeUnit.ML -> "ml"
    VolumeUnit.OZ -> "oz"
}
