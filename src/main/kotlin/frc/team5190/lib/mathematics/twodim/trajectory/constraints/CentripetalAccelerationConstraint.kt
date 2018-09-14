/*
 * FRC Team 5190
 * Green Hope Falcons
 */

/*
 * Some implementations and algorithms borrowed from:
 * NASA Ames Robotics "The Cheesy Poofs"
 * Team 254
 */


@file:Suppress("unused")

package frc.team5190.lib.mathematics.twodim.trajectory.constraints

import frc.team5190.lib.mathematics.twodim.geometry.Pose2dWithCurvature

class CentripetalAccelerationConstraint(private val mMaxCentripetalAccel: Double) : TimingConstraint<Pose2dWithCurvature> {

    override fun getMaxVelocity(state: Pose2dWithCurvature): Double {
        return Math.sqrt(Math.abs(mMaxCentripetalAccel / state.curvature))
    }

    override fun getMinMaxAcceleration(state: Pose2dWithCurvature, velocity: Double): TimingConstraint.MinMaxAcceleration {
        return TimingConstraint.MinMaxAcceleration.kNoLimits
    }
}