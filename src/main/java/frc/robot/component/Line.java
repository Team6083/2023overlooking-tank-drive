package frc.robot.component;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.math.controller.PIDController;

public class Line {
    // line motor
    private static final int lineId = 17;
    private static WPI_TalonSRX lineMotor;

    // PID controller for line motor
    private static double kLP = 0.35;
    private static double kLI = 0.0;
    private static double kLD = 0.0;
    protected static PIDController linePID;

    private static double lineLengthOffset;

    // constants for line motor
    private static final double modifiedLineVoltLimit = 3;
    private static final double maxLineLengthLimit = 140;
    private static final double minLineLengthLimit = 40;

    public Line(double lineInitLength) {
        lineMotor = new WPI_TalonSRX(lineId);
        lineMotor.setInverted(true);
        lineMotor.setSensorPhase(true);
        resetEncoder();

        linePID = new PIDController(kLP, kLI, kLD);
        linePID.setSetpoint(lineInitLength);

        lineLengthOffset = lineInitLength;
    }

    public void manualControlLoop(double manualControlSpeed) {
        lineMotor.set(manualControlSpeed);
        linePID.setSetpoint(getLineLength());
    }

    public void PIDControlLoop() {
        double lineVolt = linePID.calculate(getLineLength());
        if (Math.abs(lineVolt) > modifiedLineVoltLimit) {
            lineVolt = modifiedLineVoltLimit * (lineVolt > 0 ? 1 : -1);
        }
        lineMotor.setVoltage(lineVolt);
    }

    public double getPIDSetpoint() {
        return linePID.getSetpoint();
    }

    public void setPIDSetpoint(double setpoint) {
        final double currentSetpoint = linePID.getSetpoint();
        if (isPhyLimitExceed(currentSetpoint) != 0) {
            return;
        }
        if (isPhyLimitExceed(currentSetpoint) == -1) {
            setpoint = minLineLengthLimit;
        } else if (isPhyLimitExceed(currentSetpoint) == 1) {
            setpoint = maxLineLengthLimit;
        }
        linePID.setSetpoint(setpoint);
    }

    public void resetEncoder() {
        lineLengthOffset = 0;
        lineMotor.setSelectedSensorPosition(0);
    }

    public void stopMotor() {
        lineMotor.stopMotor();
    }

    public double getLineLength() {
        double x = lineMotor.getSelectedSensorPosition();
        double cal1 = 0.00473 * x;
        double cal2 = 0.0000000348 * x * x;
        double length = cal1 - cal2;
        return length + lineLengthOffset;
    }

    private int isPhyLimitExceed(double angle) {
        return angle < minLineLengthLimit ? -1 : (angle > maxLineLengthLimit ? 1 : 0);
    }
}
