/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.lib.subsystems.drive

import org.ghrobotics.lib.commands.FalconCommand
import org.ghrobotics.lib.debug.LiveDashboard
import org.ghrobotics.lib.mathematics.twodim.geometry.x_u
import org.ghrobotics.lib.mathematics.twodim.geometry.y_u
import org.ghrobotics.lib.mathematics.twodim.trajectory.Trajectory
import org.ghrobotics.lib.mathematics.units.inFeet
import org.ghrobotics.lib.utils.Source

class TrajectoryTrackerCommand(
    private val drivetrain: TrajectoryTrackerDriveBase,
    private val trajectory: Source<Trajectory>
) : FalconCommand(drivetrain) {

    override fun initialize() {
        drivetrain.trajectoryTracker.reset(trajectory())
        LiveDashboard.isFollowingPath = true
    }

    override fun execute() {
        drivetrain.setOutput(drivetrain.trajectoryTracker.nextState(drivetrain.robotPosition))

        val referencePoint = drivetrain.trajectoryTracker.referencePoint
        if (referencePoint != null) {
            val referencePose = referencePoint.state.pose

            // Update Current Path Location on Live Dashboard
            LiveDashboard.pathX = referencePose.translation.x_u.inFeet()
            LiveDashboard.pathY = referencePose.translation.y_u.inFeet()
            LiveDashboard.pathHeading = referencePose.rotation.radians
        }
    }

    override fun end(interrupted: Boolean) {
        drivetrain.setNeutral()
        LiveDashboard.isFollowingPath = false
    }

    override fun isFinished() = drivetrain.trajectoryTracker.isFinished
}
