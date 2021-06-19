/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.lib.motors.ctre

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.IMotorController
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.StatusFrame
import edu.wpi.first.wpilibj.RobotController
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import org.ghrobotics.lib.mathematics.units.SIKey
import org.ghrobotics.lib.mathematics.units.SIUnit
import org.ghrobotics.lib.mathematics.units.Second
import org.ghrobotics.lib.mathematics.units.derived.Acceleration
import org.ghrobotics.lib.mathematics.units.derived.Velocity
import org.ghrobotics.lib.mathematics.units.derived.Volt
import org.ghrobotics.lib.mathematics.units.derived.volts
import org.ghrobotics.lib.mathematics.units.inMilliseconds
import org.ghrobotics.lib.mathematics.units.nativeunit.NativeUnitModel
import org.ghrobotics.lib.mathematics.units.nativeunit.inNativeUnitsPer100ms
import org.ghrobotics.lib.mathematics.units.nativeunit.inNativeUnitsPer100msPerSecond
import org.ghrobotics.lib.mathematics.units.operations.div
import org.ghrobotics.lib.mathematics.units.seconds
import org.ghrobotics.lib.mathematics.units.unitlessValue
import org.ghrobotics.lib.motors.AbstractFalconMotor
import org.ghrobotics.lib.motors.FalconMotor

/**
 * Represents the abstract class for all CTRE motor controllers.
 *
 * @param motorController The underlying motor controller.
 * @param model The native unit model.
 */
abstract class FalconCTRE<K : SIKey>(
    val motorController: IMotorController,
    private val model: NativeUnitModel<K>,
    units: K,
    simName: String = "FalconCTRE[${motorController.deviceID}]"
) : AbstractFalconMotor<K>(simName) {

    /**
     * The previous demand.
     */
    private var lastDemand =
        Demand(ControlMode.Disabled, 0.0, DemandType.Neutral, 0.0)

    /**
     * The encoder (if any) that is connected to the motor controller.
     */
    override val encoder = FalconCTREEncoder(motorController, 0, model, units)

    /**
     * Returns the voltage across the motor windings.
     */
    override val voltageOutput: SIUnit<Volt>
        get() = if (simVoltageOutput != null) simVoltageOutput.get().volts else
            motorController.motorOutputVoltage.volts

    /**
     * Whether the output of the motor is inverted or not. Slaves do not
     * follow the inversion properties of the master motor and therefore
     * must be set inverted explicitly.
     */
    override var outputInverted: Boolean by Delegates.observable(false) { _, _, newValue ->
        motorController.inverted = newValue
    }

    /**
     * Configures brake mode for the motor controller.
     */
    override var brakeMode: Boolean by Delegates.observable(false) { _, _, newValue ->
        motorController.setNeutralMode(if (newValue) NeutralMode.Brake else NeutralMode.Coast)
    }

    /**
     * Configures voltage compensation for the motor controller.
     */
    override var voltageCompSaturation: SIUnit<Volt> by Delegates.observable(12.0.volts) { _, _, newValue ->
        motorController.configVoltageCompSaturation(newValue.value, 0)
        motorController.enableVoltageCompensation(true)
    }

    /**
     * Configures the motion profile cruise velocity for MotionMagic.
     */
    override var motionProfileCruiseVelocity: SIUnit<Velocity<K>> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        motorController.configMotionCruiseVelocity(
            model.toNativeUnitVelocity(newValue).inNativeUnitsPer100ms().roundToInt(),
            0
        )
    }

    /**
     * Configures the max acceleration for the motion profile generated by MotionMagic.
     */
    override var motionProfileAcceleration: SIUnit<Acceleration<K>> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        motorController.configMotionAcceleration(
            model.toNativeUnitAcceleration(newValue).inNativeUnitsPer100msPerSecond().roundToInt(),
            0
        )
    }

    /**
     * Configures the forward soft limit and enables it.
     */
    override var softLimitForward: SIUnit<K> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        motorController.configForwardSoftLimitThreshold(model.toNativeUnitPosition(newValue).value.toInt(), 0)
        motorController.configForwardSoftLimitEnable(true, 0)
    }

    /**
     * Configures the reverse soft limit and enables it.
     */
    override var softLimitReverse: SIUnit<K> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        motorController.configReverseSoftLimitThreshold(model.toNativeUnitPosition(newValue).value.toInt(), 0)
        motorController.configReverseSoftLimitEnable(true, 0)
    }

    /**
     * Configures the open loop ramp.
     */
    var openLoopRamp by Delegates.observable(0.seconds) { _, _, newValue ->
        motorController.configOpenloopRamp(newValue.value, 0)
    }

    /**
     * Configures the closed loop ramp.
     */
    var closedLoopRamp by Delegates.observable(0.seconds) { _, _, newValue ->
        motorController.configClosedloopRamp(newValue.value, 0)
    }

    /**
     * Configures the peak forward output.
     */
    var peakOutputForward by Delegates.observable(1.0) { _, _, newValue ->
        motorController.configPeakOutputForward(newValue, 0)
    }

    /**
     * Configures the peak reverse output.
     */
    var peakOutputReverse by Delegates.observable(-1.0) { _, _, newValue ->
        motorController.configPeakOutputReverse(newValue, 0)
    }

    /**
     * Configures the nominal forward output.
     */
    var nominalOutputForward by Delegates.observable(0.0) { _, _, newValue ->
        motorController.configNominalOutputForward(newValue, 0)
    }

    /**
     * Configures the nominal reverse output.
     */
    var nominalOutputReverse by Delegates.observable(0.0) { _, _, newValue ->
        motorController.configNominalOutputReverse(newValue, 0)
    }

    /**
     * Configures the % neutral deadband.
     */
    var neutralDeadband by Delegates.observable(0.04) { _, _, newValue ->
        motorController.configNeutralDeadband(newValue, 0)
    }

    /**
     * Sets the status frame period on a specific status frame.
     *
     * @param statusFrame The status frame.
     * @param period The period.
     */
    fun setStatusFramePeriod(statusFrame: StatusFrame, period: SIUnit<Second>) {
        motorController.setStatusFramePeriod(statusFrame, period.inMilliseconds().toInt(), 0)
    }

    /**
     * Constructor that enables voltage compensation on the motor controllers by default.
     */
    init {
        motorController.configVoltageCompSaturation(12.0, 0)
        motorController.enableVoltageCompensation(true)
    }

    /**
     * Sets a certain voltage across the motor windings.
     *
     * @param voltage The voltage to set.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setVoltage(voltage: SIUnit<Volt>, arbitraryFeedForward: SIUnit<Volt>) {
        if (simVoltageOutput != null) {
            simVoltageOutput.set(voltage.value + arbitraryFeedForward.value)
            return
        }

        sendDemand(
                Demand(
                    ControlMode.PercentOutput, (voltage / voltageCompSaturation).unitlessValue,
                    DemandType.ArbitraryFeedForward, (arbitraryFeedForward / voltageCompSaturation).unitlessValue
                )
        )
    }

    /**
     * Commands a certain duty cycle to the motor.
     *
     * @param dutyCycle The duty cycle to command.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setDutyCycle(dutyCycle: Double, arbitraryFeedForward: SIUnit<Volt>) {
        if (simVoltageOutput != null) {
            simVoltageOutput.set(dutyCycle * RobotController.getBatteryVoltage() + arbitraryFeedForward.value)
            return
        }

        sendDemand(
            Demand(
                ControlMode.PercentOutput, dutyCycle,
                DemandType.ArbitraryFeedForward, (arbitraryFeedForward / voltageCompSaturation).unitlessValue
            )
        )
    }

    /**
     * Sets the velocity setpoint of the motor controller.
     *
     * @param velocity The velocity setpoint.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setVelocity(velocity: SIUnit<Velocity<K>>, arbitraryFeedForward: SIUnit<Volt>) =
        sendDemand(
            Demand(
                ControlMode.Velocity, model.toNativeUnitVelocity(velocity).inNativeUnitsPer100ms(),
                DemandType.ArbitraryFeedForward, (arbitraryFeedForward / voltageCompSaturation).unitlessValue
            )
        )

    /**
     * Sets the position setpoint of the motor controller. This uses a motion profile
     * if motion profiling is configured.
     *
     * @param position The position setpoint.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setPosition(position: SIUnit<K>, arbitraryFeedForward: SIUnit<Volt>) =
        sendDemand(
            Demand(
                if (useMotionProfileForPosition) ControlMode.MotionMagic else ControlMode.Position,
                model.toNativeUnitPosition(position).value,
                DemandType.ArbitraryFeedForward, (arbitraryFeedForward / voltageCompSaturation).unitlessValue
            )
        )

    /**
     * Gives the motor neutral output.
     */
    override fun setNeutral() = sendDemand(
        Demand(ControlMode.Disabled, 0.0, DemandType.Neutral, 0.0)
    )

    /**
     * Sends the demand to the motor controller.
     *
     * @param demand The demand to send.
     */
    private fun sendDemand(demand: Demand) {
        if (demand != lastDemand) {
            motorController.set(demand.mode, demand.demand0, demand.demand1Type, demand.demand1)
            lastDemand = demand
        }
    }

    /**
     * Follows the output of another motor controller.
     *
     * @param motor The other motor controller.
     */
    override fun follow(motor: FalconMotor<*>): Boolean =
        if (motor is FalconCTRE<*>) {
            motorController.follow(motor.motorController)
            true
        } else {
            super.follow(motor)
        }

    /**
     * Represents a demand to send to the motor controller.
     *
     * @param mode The control mode.
     * @param demand0 The primary demand.
     * @param demand1Type The auxilary demand type.
     * @param demand1 The auxilary demand.
     */
    data class Demand(
        val mode: ControlMode,
        val demand0: Double,
        val demand1Type: DemandType,
        val demand1: Double
    )
}
