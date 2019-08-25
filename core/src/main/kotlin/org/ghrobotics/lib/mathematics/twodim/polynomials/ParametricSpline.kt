/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */

package org.ghrobotics.lib.mathematics.twodim.polynomials

import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import org.ghrobotics.lib.mathematics.twodim.geometry.Rotation2d
import org.ghrobotics.lib.mathematics.twodim.geometry.Translation2d

abstract class ParametricSpline {
    abstract fun getPoint(t: Double): Translation2d
    abstract fun getHeading(t: Double): Rotation2d
    abstract fun getCurvature(t: Double): Double
    abstract fun getDCurvature(t: Double): Double
    abstract fun getVelocity(t: Double): Double

    private fun getPose2d(t: Double) = Pose2d(getPoint(t), getHeading(t))

    fun getPose2dWithCurvature(t: Double) =
        Pose2dWithCurvature(getPose2d(t), getCurvature(t), getDCurvature(t) / getVelocity(t))
}