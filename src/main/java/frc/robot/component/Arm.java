package frc.robot.component;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;

public class Arm {
    // arm motor
    private static CANSparkMax armMotorleft;
    private static CANSparkMax armMotorright;
    private static MotorControllerGroup armMotor;
    private static final int armLeftCANId = 15;
    private static final int armRightCANId = 16;
    // line motor
    protected static WPI_TalonSRX lineMotor;
    private static final int line = 17;

    // encoder
    // private static Encoder armEncoder;

    private static RelativeEncoder armEncoder;
    private static final int armEnc1Channel = 8; // for normal encoder
    private static final int armEnc2Channel = 9; // for normal encoder

    // arm pid
    private static double kAP = 0.3;
    private static double kAI = 0.0;
    private static double kAD = 0.0;
    private static PIDController armPID;
    // line pid
    private static double kLP = 0.35;
    private static double kLI = 0.0;
    private static double kLD = 0.0;
    protected static PIDController linePID;

    // arm value
    private static final double armEncoderPulse = 2048;
    private static final double armEncoderGearing = 198;
    private static final double armVoltLimit = 4;
    private static final double armAngleMin = -30;
    private static final double armAngleMax = 210;
    // line value
    private static final double lineVoltLimit = 3;
    private static final double lineLenghtMax = 100.0;
    private static final double lineLenghtMin = 0;

    public static void init() {
        // arm motor
        armMotorleft = new CANSparkMax(armLeftCANId, MotorType.kBrushless);
        armMotorright = new CANSparkMax(armRightCANId, MotorType.kBrushless);
        armMotor = new MotorControllerGroup(armMotorleft, armMotorright);
        armMotorleft.setInverted(true);

        // line motor
        // lineMotor = new WPI_TalonSRX(line);
        // lineMotor.setInverted(true);

        // arm encoder
        // armEncoder = new Encoder(armEnc1Channel, armEnc2Channel); //for normal
        // encoder
        // encoder
        armEncoder = armMotorleft.getEncoder(); // for sparkmax encoder

        // arm pid
        armPID = new PIDController(kAP, kAI, kAD);

        // line pid
        // linePID = new PIDController(kLP, kLI, kLD);

        // setLineSetpoint(0);

        // put dashboard
        SmartDashboard.putNumber("arm_kP", kAP);
        // SmartDashboard.putNumber("line_kP", kLP);
    }

    public static void teleop() {
        armLoop();
        // lineLoop();
    }

    public static void armLoop() {
        // get the angular position
        double armCurrentAngle = getArmDegree();

        // encoder reset
        if (Robot.xbox.getBackButton()) {
            // armEncoder.reset(); // for normal encoder
            armEncoder.setPosition(0); // for sparkmax encoder
            setArmSetpoint(0);
        }

        // adjust kAP
        kAP = SmartDashboard.getNumber("arm_kP", kAP);
        armPID.setP(kAP);

        // rotate arm
        boolean armInManual = (Robot.xbox.getPOV() == 90);
        double armAngleModify = 0;
        if (Robot.xbox.getXButton()) {
            setArmSetpoint(35.2);
        } else if (Robot.xbox.getYButton()) {
            setArmSetpoint(68.5);
        } else {
            armAngleModify = (Robot.xbox.getLeftTriggerAxis() -
                    Robot.xbox.getRightTriggerAxis()) * -0.3;
            setArmSetpoint(armPID.getSetpoint() + armAngleModify);
        }
        if (armInManual) {
            // control through xbox, for test
            double rotate = (Robot.xbox.getLeftTriggerAxis() -
                    Robot.xbox.getRightTriggerAxis()) * -0.15;
            armMotor.set(rotate);
            setArmSetpoint(armCurrentAngle);
        } else {
            armControlLoop();
        }

        // put dashboard
        SmartDashboard.putNumber("arm_setpoint", armPID.getSetpoint());
        SmartDashboard.putNumber("aem_current_angle", armCurrentAngle);
        SmartDashboard.putNumber("arm_angle_modify", armAngleModify);
    }

    public static void lineLoop() {
        // get length position
        final double lineCurrentLength = getEncoderToLength();

        // encoder reset
        if (Robot.xbox.getAButton()) {
            lineMotor.setSelectedSensorPosition(0);
            setLineSetpoint(0);
        }
        // adjust kLP
        kLP = SmartDashboard.getNumber("line_kP", kLP);
        linePID.setP(kLP);

        // stretch line
        boolean lineInManual = (Robot.xbox.getPOV() == 90);
        double lineLengthModify = 0.0;
        if (Robot.xbox.getYButtonPressed()) {
            setLineSetpoint(33.02);
            lineLengthModify = 0.0;
            // } else if (length > 122 * (1 / Math.cos(35.2)) - 58) { //the length of the
            // outside bumper
            // lineLengthModify = 0.5;
            // } else if (length > 122 * Math.abs(1 / Math.cos(Arm.positionToDegree())) -
            // 58) {
            // lineLengthModify = -0.5;
            // lineControlLoop();
        } else if (Robot.xbox.getPOV() == 0) {
            lineLengthModify = 0.3;
            setLineSetpoint(linePID.getSetpoint() + lineLengthModify);
        } else if (Robot.xbox.getPOV() == 180) {
            lineLengthModify = -0.4;
            setLineSetpoint(linePID.getSetpoint() + lineLengthModify);
            // lineControlLoop();
        }
        if (lineInManual) {
            if (Robot.xbox.getPOV() == 0) {
                lineMotor.set(0.3);
            } else if (Robot.xbox.getPOV() == 180) {
                lineMotor.set(-0.3);
            } else {
                lineMotor.set(0);
            }
            setLineSetpoint(lineCurrentLength);
        }else{
            lineControlLoop();
        }
        // if (linePID.getSetpoint() < lineLengthLimit) {
        // linePID.setSetpoint(lineLengthLimit);
        // }

        // put dashboard
        SmartDashboard.putNumber("line_setpoint", linePID.getSetpoint());
        SmartDashboard.putNumber("line_current_length", lineCurrentLength);
        SmartDashboard.putNumber("line_length_modify", lineLengthModify);

    }

    public static void armControlLoop() {
        var armVolt = armPID.calculate(getArmDegree());

        double modifiedArmVolt = armVolt;
        if (Math.abs(modifiedArmVolt) > armVoltLimit) {
            modifiedArmVolt = armVoltLimit * (armVolt > 0 ? 1 : -1);
        }
        armMotor.setVoltage(modifiedArmVolt);

        SmartDashboard.putNumber("arm_orig_volt", armVolt);
        SmartDashboard.putNumber("arm_volt", modifiedArmVolt);
    }

    public static void lineControlLoop() {

        var lineVolt = linePID.calculate(getEncoderToLength());
        if (Math.abs(lineVolt) > 10) {
            lineVolt = lineVoltLimit * (lineVolt > 0 ? 1 : -1);
        }
        lineMotor.setVoltage(lineVolt);

        SmartDashboard.putNumber("line_volt", lineVolt);
    }

    // do the number of turns calculate(to a particular angle)
    public static double getArmDegree() {
        // double armRate = armEncoder.get() * 360 / armEncoderPulse; // for normal
        // encoder
        // SmartDashboard.putNumber("arm_encoder", armEncoder.get()); // for normal
        // encoder
        double armRate = armEncoder.getPosition() * 360 / armEncoderGearing; // for
        // sparkmax encoder
        SmartDashboard.putNumber("arm_encoder", armEncoder.getPosition()); // for
        // sparkmax encoder

        SmartDashboard.putNumber("arm_angle", armRate);
        return armRate;
    }

    // do the number of turns calculate(to a particular length)
    public static double getEncoderToLength() {
        if (lineMotor.getSelectedSensorPosition() < 0) {
            lineMotor.setSelectedSensorPosition(0.0);
        }
        double x = lineMotor.getSelectedSensorPosition();
        double length = 0.00473 * x - 0.0000000348 * x * x;
        SmartDashboard.putNumber("line_encoder", lineMotor.getSelectedSensorPosition());
        SmartDashboard.putNumber("line_length", length);
        return length;
    }

    public static void setArmSetpoint(double setpoint) {
        // Check if arm exceed it's physical limit
        if (setpoint < armAngleMin) {
            setpoint = armAngleMin;
        } else if (setpoint > armAngleMax) {
            setpoint = armAngleMax;
        }
        armPID.setSetpoint(setpoint);
    }

    public static void setLineSetpoint(double setpoint) {
        // Check if line exceed it's physical limit
        if (setpoint < lineLenghtMin) {
            setpoint = lineLenghtMin;
        } else if (setpoint > lineLenghtMax) {
            setpoint = lineLenghtMax;
        }
        // check if line exceed it's game limit
        // if (setpoint > 122 * Math.abs(1 / Math.cos(getArmDegree())) - 58) {
        // setpoint = 122 * Math.abs(1 / Math.cos(getArmDegree())) - 58;
        // }
        linePID.setSetpoint(setpoint);
    }

}