/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.lib.mathematics.twodim.geometry

import org.ghrobotics.lib.mathematics.kEpsilon
import org.ghrobotics.lib.mathematics.units.derived.degree
import org.ghrobotics.lib.mathematics.units.derived.toRotation2d
import org.ghrobotics.lib.mathematics.units.meter
import org.junit.Assert
import org.junit.Test

class TransformTests {
    @Test
    fun testTransforms() {
        // Position of the static object
        val staticObjectPose = Pose2d(10.0.meter, 10.0.meter, 0.0.degree)

        // Position of the camera on the robot.
        // Camera is on the back of the robot (1 foot behind the center)
        // Camera is facing backward
        val robotToCamera = Pose2d((-1.0).meter, 0.0.meter, 180.0.degree)

        // The camera detects the static object 9 meter in front and 2 meter to the right of it.
        val cameraToStaticObject = Pose2d(9.0.meter, 2.0.meter, 0.0.degree)

        // Transform the static object into the robot's coordinates
        val robotToStaticObject = robotToCamera + cameraToStaticObject

        // Get the global robot pose
        val globalRobotPose = staticObjectPose - robotToStaticObject

        println(
            "X: ${globalRobotPose.translation.x.meter}, Y: ${globalRobotPose.translation.y.meter}, " +
                "Angle: ${globalRobotPose.rotation.degree}"
        )

        Assert.assertEquals(0.0, globalRobotPose.translation.x.value, kEpsilon)
        Assert.assertEquals(8.0, globalRobotPose.translation.y.value, kEpsilon)
        Assert.assertEquals((-180.0).degree.toRotation2d(), globalRobotPose.rotation)
    }
}