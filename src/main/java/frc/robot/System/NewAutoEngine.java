package frc.robot.System;

import java.io.IOException;
import java.nio.file.Path;

import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.component.Arm;
import frc.robot.component.DriveBase;

public class NewAutoEngine {

    static int currentStep = 0;
    static int trajectoryAmount = 12;
    public static final int[] blueLeft = { 0, 1 };
    public static final int[] blueMiddle = { 2, 3 };
    public static final int[] blueRight = { 4, 5 };
    public static final int[] redLeft = { 6, 7 };
    public static final int[] redMiddle = { 8, 9 };
    public static final int[] redRight = { 10, 11 };
    public static final String[] trajJSON = {
            "/home/lvuser/deploy/output/output/BL-1.wpilib.json", "/home/lvuser/deploy/output/output/BL-2.wpilib.json",
            "/home/lvuser/deploy/output/output/BM-1.wpilib.json", "/home/lvuser/deploy/output/output/BM-2.wpilib.json",
            "/home/lvuser/deploy/output/output/BR-1.wpilib.json", "/home/lvuser/deploy/output/output/BR-2.wpilib.json",
            "/home/lvuser/deploy/output/output/RL-1.wpilib.json", "/home/lvuser/deploy/output/output/RL-2.wpilib.json",
            "/home/lvuser/deploy/output/output/RM-1.wpilib.json", "/home/lvuser/deploy/output/output/RM-2.wpilib.json",
            "/home/lvuser/deploy/output/output/RR-1.wpilib.json", "/home/lvuser/deploy/output/output/RR-2.wpilib.json"
    };

    static Trajectory[] trajectory = new Trajectory[trajectoryAmount];
    public static Timer timer = new Timer();
    public static SendableChooser<String> chooser;
    public static String autoSeclected;
    private static final String DoNothing = "DoNothing";
    private static final String BlueLeft = "BlueLeft";
    private static final String BlueMiddle = "BlueMiddle";
    private static final String BlueRight = "BlueRight";
    private static final String RedLeft = "RedLeft";
    private static final String RedMiddle = "RedMiddle";
    private static final String RedRight = "RedRight";

    public static void init() {
        chooser = new SendableChooser<String>();
        putChooser();
        for (int i = 0; i < trajectoryAmount; i++) {
            try {
                Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajJSON[i]);
                trajectory[i] = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
            } catch (IOException ex) {
                DriverStation.reportError("unable to open trajectory" + trajJSON[i] + "\n" + ex.getMessage(),ex.getStackTrace());
            }

            var pose = trajectory[i].getInitialPose();

            DriveBase.setODOPose(pose);
        }
    }

    public static void start() {
        currentStep = 0;

        autoSeclected = chooser.getSelected();
        DriveBase.resetEncoderOn();
        DriveBase.resetGyro();
        DriveBase.resetPID();

        double autoDelayTime = SmartDashboard.getNumber("autoDelay", 0);

        Timer.delay(autoDelayTime);
        timer.reset();
        timer.start();
    }

    public static void loop() {
        DriveBase.updateODO();
        DriveBase.putDashboard();
        SmartDashboard.putNumber("AutoTimer", timer.get());
        SmartDashboard.putNumber("CurrentStep", currentStep);
        switch (autoSeclected) {
            case DoNothing:
                DriveBase.directControl(0, 0);
                break;
            case BlueLeft:
                BlueLeft();
                break;
            case BlueMiddle:
                BlueMiddle();
                break;
            case BlueRight:
                BlueRight();
                break;
            case RedLeft:
                RedLeft();
                break;
            case RedMiddle:
                RedMiddle();
                break;
            case RedRight:
                RedRight();
                break;
            default:
        }
    }

    protected static void putChooser() {
        chooser.setDefaultOption("DoNothing", DoNothing);
        chooser.setDefaultOption("BlueLeft", BlueLeft);
        chooser.setDefaultOption("BlueMiddle", BlueMiddle);
        chooser.setDefaultOption("BlueRight", BlueRight);
        chooser.setDefaultOption("RedLeft", RedLeft);
        chooser.setDefaultOption("RedMiddle", RedMiddle);
        chooser.setDefaultOption("RedRight", RedRight);
        SmartDashboard.putData(chooser);
    }

    public static void BlueLeft() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[blueLeft[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[blueLeft[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[blueLeft[0]], timer.get());
                if (timer.get()>trajectory[blueLeft[0]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    Arm.autoArm(0);
                    Arm.autoLine(0);
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftMotor1.setInverted(false);
                    DriveBase.rightMotor1.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[blueLeft[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[blueLeft[1]].getInitialPose());
                    currentStep++;
            case 4:
                DriveBase.runTraj(trajectory[blueLeft[1]], timer.get());
                if (timer.get()>trajectory[blueLeft[1]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            default:
                DriveBase.leftmotor.setInverted(true);
                DriveBase.rightmotor.setInverted(false);
        }
    }

    public static void BlueMiddle() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[blueMiddle[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[blueMiddle[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[blueMiddle[0]], timer.get());
                if (timer.get()>trajectory[blueMiddle[0]].getTotalTimeSeconds() ) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftMotor1.setInverted(false);
                    DriveBase.rightMotor1.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[blueMiddle[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[blueMiddle[1]].getInitialPose());
                    currentStep++;
            case 4:
                DriveBase.runTraj(trajectory[blueMiddle[1]], timer.get());
                if(timer.get()>trajectory[blueMiddle[1]].getTotalTimeSeconds()){
                    currentStep++;
                }
                break;
            default:
            DriveBase.leftmotor.setInverted(true);
            DriveBase.rightmotor.setInverted(false);
        }
    }

    public static void BlueRight() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[blueRight[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[blueRight[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[blueRight[0]], timer.get());
                if (timer.get()>trajectory[blueRight[0]].getTotalTimeSeconds() ) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftmotor.setInverted(false);
                    DriveBase.rightmotor.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[blueRight[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[blueRight[1]].getInitialPose());
                    currentStep++;
            case 4:
                DriveBase.runTraj(trajectory[blueRight[1]], timer.get());
                if (timer.get()>trajectory[blueRight[1]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            default:
            DriveBase.leftmotor.setInverted(true);
            DriveBase.rightmotor.setInverted(false);
        }
    }

    public static void RedLeft() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[redLeft[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[redLeft[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[redLeft[0]], timer.get());
                if (timer.get()>trajectory[redLeft[0]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftmotor.setInverted(false);
                    DriveBase.rightmotor.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[redLeft[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[redLeft[1]].getInitialPose());
            case 4:
                DriveBase.runTraj(trajectory[redLeft[1]], timer.get());
                if (timer.get()>trajectory[redLeft[1]].getTotalTimeSeconds()) {
                   currentStep++;
                }
                break;
            default:
                DriveBase.leftmotor.setInverted(true);
                DriveBase.rightmotor.setInverted(false);
        }
    }

    public static void RedMiddle() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[redMiddle[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[redMiddle[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[redMiddle[0]], timer.get());
                if ( timer.get()>trajectory[redMiddle[0]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftmotor.setInverted(false);
                    DriveBase.rightmotor.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[redMiddle[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[redMiddle[1]].getInitialPose());
            case 4:
                DriveBase.runTraj(trajectory[redMiddle[1]], timer.get());
                if(timer.get()>trajectory[redMiddle[1]].getTotalTimeSeconds()){
                    currentStep++;
                }
                break;
            default:
                DriveBase.leftmotor.setInverted(true);
                DriveBase.rightmotor.setInverted(false);
        }
    }

    public static void RedRight() {
        switch (currentStep) {
            case 0:
                DriveBase.resetEncoderOff();
                timer.reset();
                timer.start();
                DriveBase.odometry.resetPosition(trajectory[redRight[0]].getInitialPose().getRotation(),
                        DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                        DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                        trajectory[redRight[0]].getInitialPose());
                currentStep++;
                break;
            case 1:
                DriveBase.runTraj(trajectory[redRight[0]], timer.get());
                if (timer.get()>trajectory[redRight[0]].getTotalTimeSeconds()) {
                    currentStep++;
                }
                break;
            case 2:
                //put cone
                break;
            case 3:
                //turn around the arm
                    timer.reset();
                    timer.start();
                    DriveBase.resetEncoderOn();
                    DriveBase.resetEncoderOff();
                    DriveBase.leftmotor.setInverted(false);
                    DriveBase.rightmotor.setInverted(true);
                    DriveBase.odometry.resetPosition(trajectory[redRight[1]].getInitialPose().getRotation(),
                            DriveBase.positionToDistanceMeter(DriveBase.leftMotor1.getSelectedSensorPosition()),
                            DriveBase.positionToDistanceMeter(DriveBase.rightMotor1.getSelectedSensorPosition()),
                            trajectory[redRight[1]].getInitialPose());
                break;
            case 4:
                DriveBase.runTraj(trajectory[redRight[1]], timer.get());
                if (timer.get()>trajectory[redRight[1]].getTotalTimeSeconds() ) {
                    currentStep++;
                }
                break;
            default:
                DriveBase.leftmotor.setInverted(true);
                DriveBase.rightmotor.setInverted(false);//need to test
        }
    }
    // autonomous written with timer
    public static void BlueLeftTimer(){
        double leftV = 0.5;
        double rightV = 0.5;
        timer.reset();
        timer.start();
        if(timer.get()<=1.5){
            DriveBase.directControl(leftV, rightV);
        }else if (timer.get()>1.5 && timer.get()<=6){
            // arm and intake
        }else if(timer.get()>6 && timer.get()<=10.5){
            //arm
        }else if(timer.get()>10.5 && timer.get()<15){
            DriveBase.directControl(-leftV, -rightV);
        }
    }
}

//class of path setting
class pathSet{
    int path1;
    int path2;
    String pathName;
    pathSet(int path1 ,int path2 ,String pathName){
        this.path1 = path1;
        this.path2 = path2;
        this.pathName = pathName; //use in the switch
    }

}