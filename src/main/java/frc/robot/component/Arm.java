package frc.robot.component;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import java.lang.Math;

public class Arm {
    private static final double ArmencoderPulse = 42;// do the number of turns calculate
    private static final double Armgearing = 198;
    private static final double lineencoderPulse = 8192;
    private static final double linegearing = 64;
    private static CANSparkMax ArmMotorleft;// rotate arm
    private static CANSparkMax ArmMotorright;
    private static MotorControllerGroup Arm;
    private static WPI_VictorSPX lineMotor;// take up and pay off device
    private static final int armL = 10;
    private static final int armR = 11;
    private static final int line = 2;
    private static RelativeEncoder ArmEncoder;
    private static Double kP = 0.35;
    private static Double kI = 0.0;
    private static Double kD = 0.0;
    private static PIDController ArmPID;

    public static void init() {
        ArmMotorleft = new CANSparkMax(armL, MotorType.kBrushless);
        ArmMotorright = new CANSparkMax(armR, MotorType.kBrushless);
        Arm = new MotorControllerGroup(ArmMotorleft, ArmMotorright);
        ArmMotorleft.setInverted(true);
        lineMotor = new WPI_VictorSPX(line);
        ArmPID = new PIDController(kP, kI, kD);

        lineMotor.configClearPositionOnQuadIdx(true, 10);
        ArmMotorleft.getEncoder().setPosition(0);
        ArmMotorright.getEncoder().setPosition(0);

        SmartDashboard.putNumber("arm_kP", kP);
    }

    public static void teleop() {
        kP = SmartDashboard.getNumber("arm_kP", kP);
        ArmPID.setP(kP);

        double angle = positionToDegree();// get the angular position
        double length = positionTolength(); // get length position

        // take up and pay off device
        if (Robot.xbox.getPOV() == 0) {
            lineMotor.set(0.5);
        } else if (Robot.xbox.getPOV() == 180) {
            lineMotor.set(-0.5);
        } 
        // else if (Robot.xbox.getXButton()) {
        //     if (length > 122 * (1 / Math.cos(35.2)) - 58) {
        //         lineMotor.set(-0.5);
        //     }
        // } else if (length > 122 * (1 / Math.cos(angle)) - 58) {
        //     lineMotor.set(-0.5);
        // } 
        else {
            lineMotor.set(0);
        }

        if (Robot.xbox.getXButton()) {
            ArmPID.setSetpoint(35.2);
        } else if (Robot.xbox.getAButton()) {
            ArmPID.setSetpoint(68.5);
            length = 0;
        } else {
            double armAngleModify = (Robot.xbox.getLeftTriggerAxis() - Robot.xbox.getRightTriggerAxis()) * 0.01;
            ArmPID.setSetpoint(ArmPID.getSetpoint() + armAngleModify);
        }
        SmartDashboard.putNumber("setpoint", ArmPID.getSetpoint());
        SmartDashboard.putNumber("current", positionToDegree());
        SmartDashboard.putNumber("arm enc", ArmEncoder.getPosition());

        controlloop();
    }

    public static void controlloop() {
        var ArmVolt = ArmPID.calculate(positionToDegree());

        if (Math.abs(ArmVolt) > 10) {
            ArmVolt = 10 * (ArmVolt > 0 ? 1 : -1);
        }
        Arm.setVoltage(ArmVolt);

        SmartDashboard.putNumber("ArmVolt", ArmVolt);
    }

    // do the number of turns calculate(to a particular angle)
    public static double positionToDegree() {
        double armRate = ArmEncoder.getPosition() * 360 / (Armgearing * ArmencoderPulse);
        return armRate;
    }

    // do the number of turns calculate(to a particular length)
    public static double positionTolength() {
        double length = lineMotor.getSelectedSensorPosition() / (linegearing * lineencoderPulse);
        return length;
    }

    public static double autoArm(double speed) {
        Arm.set(speed);
        return 0;
    }

    public static double autoLine(double speed) {
        lineMotor.set(speed);
        return 0;
    }
}