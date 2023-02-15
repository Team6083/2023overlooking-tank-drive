package frc.robot.component;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import frc.robot.Robot;

public class Arm {
    private static final double ArmencoderPulse = 4096;// do the number of turns calculate
    private static final double Armgearing = 198;
    private static final double lineencoderPulse = 8192;
    private static final double linegearing = 64;
    private static CANSparkMax ArmMotorleft;// rotate arm
    private static CANSparkMax ArmMotorright;
    private static MotorControllerGroup Arm;
    private static WPI_VictorSPX lineMotor;// take up and pay off device
    private static final int karm1 = 0;
    private static final int karm2 = 0;
    private static final int line = 2;
    private static Double rotateForward;
    private static Double rotatereverse;
    private static RelativeEncoder ArmEncoder;
    private static Encoder lineEncoder;
    private static Double kP = 0.0;
    private static Double kI = 0.0;
    private static Double kD = 0.0;
    private static PIDController ArmPID;

    public static void init() {
        ArmMotorleft = new CANSparkMax(karm1, MotorType.kBrushless);
        ArmMotorright = new CANSparkMax(karm2, MotorType.kBrushless);
        Arm = new MotorControllerGroup(ArmMotorleft, ArmMotorright);
        lineMotor = new WPI_VictorSPX(line);
        ArmEncoder = ArmMotorleft.getEncoder();
        lineEncoder = new Encoder(0, 1);
        ArmPID = new PIDController(kP, kI, kD);
    }

    public static void teleop() {
        // rotate arm
        rotateForward = Robot.xbox.getLeftTriggerAxis() - Robot.xbox.getRightTriggerAxis();
        rotatereverse = Robot.xbox.getRightTriggerAxis() - Robot.xbox.getLeftTriggerAxis();
        ArmMotorleft.set(rotateForward);
        ArmMotorright.set(rotatereverse);

        // take up and pay off device
        if (Robot.xbox.getPOV() == 0) {
            lineMotor.set(0.5);
        } else if (Robot.xbox.getPOV() == 180) {
            lineMotor.set(-0.5);
        } else {
            lineMotor.set(0);
        }
        
        double Armdegree = ArmEncoder.getPosition();
        double goal = ArmEncoder.(goal);

        ArmPID.setSetpoint(goal);;

        double ArmVolt = ArmPID.calculate();

        Arm.setVoltage(ArmVolt);

        double angle = positionToDegreeMeter(ArmEncoder.getPosition());// get the angular position
        double length = positionTolengthMeter(lineEncoder.get()); // get length position

        if (Robot.xbox.getAButton()) {
            if (angle > 35) {
                ArmMotorleft.set(-0.5);
                ArmMotorright.set(0.5);
            } else if (angle < 35) {
                ArmMotorleft.set(0.5);
                ArmMotorright.set(-0.5);
            } else {
                ArmMotorleft.set(0);
                ArmMotorright.set(0);
            }

            if (length > 186) {
                lineMotor.set(-0.5);
            } else if (length < 186) {
                lineMotor.set(0.5);
            } else {
                lineMotor.set(0);
            }
        }
    }

    // do the number of turns calculate(to a particular angle)
    public static double positionToDegreeMeter(double turns) {
        double sensorRate = turns / ArmencoderPulse;
        double armRate = sensorRate / Armgearing;
        double DegreeMeter = armRate * 360;
        return DegreeMeter;
    }

    // do the number of turns calculate(to a particular length)
    public static double positionTolengthMeter(double position) {
        double sensorRate = position / lineencoderPulse;
        double armRate = sensorRate / linegearing;
        double lengthMeter = armRate;
        return lengthMeter;
    }

    public static double autoArm(double speed) {
        ArmMotorleft.set(speed);
        return 0;
    }

    public static double autoAccessDegree() {
        double Degree = positionToDegreeMeter(ArmEncoder.getPosition());
        return Degree;
    }

    public static double autoLine(double speed) {
        lineMotor.set(speed);
        return 0;
    }
}