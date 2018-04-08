package org.firstinspires.ftc.hdcode.Autonomous;

import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.hdlib.General.Alliance;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDAuto;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.HDRobot;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems.HDDriveHandler;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems.HDJewel;
import org.firstinspires.ftc.hdlib.Sensors.HDVuforiaVuMarks;
import org.firstinspires.ftc.hdlib.StateMachines.HDStateMachine;
import org.firstinspires.ftc.hdlib.StateMachines.HDWaitTypes;
import org.firstinspires.ftc.hdlib.Telemetry.HDDashboard;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;

/**
 * Created by FIRSTMentor on 1/13/2018.
 */

public class Red_Relic_Glyph_Jewel implements HDAuto {

    private enum States{
        delay,
        wait,
        scanVuMark,
        lowerJewelArm,
        readJewel,
        hitJewel,
        driveToCryptobox,
        turnToCryptobox,
        driveForward,
        done,
    }

    private HDRobot robot;
    private HDStateMachine SM;
    private HDVuforiaVuMarks vuforiaVuMarks;
    private RelicRecoveryVuMark vuMark;

    private HDDashboard dashboard;
    private double delay, waitTime;
    private boolean turnLeft;
    private Alliance alliance;

    private States nextState;

    public Red_Relic_Glyph_Jewel(double delay, Alliance alliance, HardwareMap hardwareMap, HDDashboard dashboard){

        this.dashboard = dashboard;

        robot = new HDRobot(hardwareMap);

        SM = new HDStateMachine(robot.robotDrive);

        vuforiaVuMarks = new HDVuforiaVuMarks(hardwareMap);
        vuforiaVuMarks.activateTracking();

        robot.robotDrive.reverseSide(HDDriveHandler.Side.Right);
        robot.robotDrive.resetEncoders();
        robot.robotDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.robotDrive.setAlliance(alliance);

        this.delay = delay;
        this.alliance = alliance;

        Runnable reset = new Runnable() {
            @Override
            public void run() {
                robot.robotDrive.resetPIDvalues();
            }
        };

        SM.setResetCode(reset);
    }

    @Override
    public void initializeLoop() {
        dashboard.addDiagnosticSpecificTelemetry(1, "Vumark Reading: %s", String.valueOf(vuforiaVuMarks.scan()));
    }

    @Override
    public void start() {
        SM.setState(States.delay);
    }

    @Override
    public void runLoop(double elapsedTime){
        if(ready()){
            States states = (States) SM.getState();
            switch(states){
                case delay:
                    SM.setNextState(States.scanVuMark, HDWaitTypes.Timer, delay);
                    break;
                case wait:
                    SM.setNextState(nextState, HDWaitTypes.Timer, waitTime);
                    robot.robotDrive.resetEncoders();
                    break;
                case scanVuMark:
                    SM.setNextState(States.lowerJewelArm, HDWaitTypes.Timer, 6.0);
                    vuMark = vuforiaVuMarks.scan();
                    dashboard.addDiagnosticSpecificTelemetry(1, "Vumark Reading: %s", String.valueOf(vuMark));
                    switch (vuMark) {
                        case UNKNOWN:
                            break;
                        case LEFT:
                            SM.resetValues();
                            SM.setState(States.lowerJewelArm);
                            break;
                        case CENTER:
                            SM.resetValues();
                            SM.setState(States.lowerJewelArm);
                            break;
                        case RIGHT:
                            SM.resetValues();
                            SM.setState(States.lowerJewelArm);
                            break;
                        default:
                            break;
                    }
                    break;
                case lowerJewelArm:
                    vuforiaVuMarks.deactivateTracking();
                    robot.robotJewel.lowerArm();
                    if(robot.robotJewel.getLeftColor() != HDJewel.jewelColor.INCONCLUSIVE){
                        SM.setNextState(States.readJewel, HDWaitTypes.Timer, 0.05);
                    }
                    break;
                case readJewel:
                    if(alliance == Alliance.RED_ALLIANCE){
                        if(robot.robotJewel.getLeftColor() == HDJewel.jewelColor.RED){
                            turnLeft = true;
                            SM.setNextState(States.hitJewel, HDWaitTypes.Timer, 0.05);
                        }else if(robot.robotJewel.getLeftColor() == HDJewel.jewelColor.BLUE){
                            turnLeft = false;
                            SM.setNextState(States.hitJewel, HDWaitTypes.Timer, 0.05);
                        }
                    }else{
                        if(robot.robotJewel.getLeftColor() == HDJewel.jewelColor.RED){
                            turnLeft = true;
                            SM.setNextState(States.hitJewel, HDWaitTypes.Timer, 0.05);
                        }else if(robot.robotJewel.getLeftColor() == HDJewel.jewelColor.BLUE){
                            turnLeft = false;
                            SM.setNextState(States.hitJewel, HDWaitTypes.Timer, 0.05);
                        }
                    }
                    break;
                case hitJewel:
                    if(turnLeft){
                        SM.setNextState(States.driveToCryptobox, HDWaitTypes.Timer, 0.35);
                        robot.robotJewel.hitFront();
                    }else{
                        SM.setNextState(States.driveToCryptobox, HDWaitTypes.Timer, 0.35);
                        robot.robotJewel.hitBack();
                    }
                    break;
                case driveToCryptobox:
                    robot.robotJewel.resetJewel();
                    int targetEncoder, error;
                    double percentCompleted;
                    switch (vuMark) {
                        case UNKNOWN:
                            targetEncoder = -1495;
                            error = targetEncoder - robot.robotDrive.getEncoderAverage();
                            percentCompleted = Math.abs(((double) error)/((double) Math.abs(targetEncoder)));
                            if(percentCompleted > .75){percentCompleted = 0.65;}
                            robot.robotDrive.VLF(-((percentCompleted > 0.1) ? percentCompleted : 0.1), 0, 0.01, 2,robot.IMU1.getZheading());
                            break;
                        case LEFT:
                            targetEncoder = -1855;
                            error = targetEncoder - robot.robotDrive.getEncoderAverage();
                            percentCompleted = Math.abs(((double) error)/((double) Math.abs(targetEncoder)));
                            if(percentCompleted > .75){percentCompleted = 0.65;}
                            robot.robotDrive.VLF(-((percentCompleted > 0.1) ? percentCompleted : 0.1), 0, 0.01, 2,robot.IMU1.getZheading());
                            break;
                        case CENTER:
                            targetEncoder = -1495;
                            error = Math.abs(targetEncoder - robot.robotDrive.getEncoderAverage());
                            percentCompleted = Math.abs(((double) error)/((double) targetEncoder));
                            if(percentCompleted > .75){percentCompleted = 0.65;}
                            robot.robotDrive.VLF(-((percentCompleted > 0.2) ? percentCompleted : 0.2), 0, 0.01, 2,robot.IMU1.getZheading());
                            break;
                        case RIGHT:
                            targetEncoder = -1135;
                            error = targetEncoder - robot.robotDrive.getEncoderAverage();
                            percentCompleted = Math.abs(((double) error)/((double) Math.abs(targetEncoder)));
                            if(percentCompleted > .75){percentCompleted = 0.65;}
                            robot.robotDrive.VLF(-((percentCompleted > 0.1) ? percentCompleted : 0.1), 0, 0.01, 2,robot.IMU1.getZheading());
                            break;
                        default:
                            targetEncoder = -1495;
                            error = Math.abs(targetEncoder - robot.robotDrive.getEncoderAverage());
                            percentCompleted = Math.abs(((double) error)/((double) targetEncoder));
                            if(percentCompleted > .75){percentCompleted = 0.75;}
                            robot.robotDrive.VLF(-((percentCompleted > 0.2) ? percentCompleted : 0.2), 0, 0.01, 2,robot.IMU1.getZheading());
                            break;
                    }
                    if(error > -10){
                        robot.robotDrive.motorBreak();
                        waitBeforeNextState(0.3, States.turnToCryptobox);
                    }
                    break;
                case turnToCryptobox:
                    if(robot.robotDrive.isOnTarget()){
                        waitBeforeNextState(0.3, States.driveForward);
                    }
                    robot.robotDrive.gyroTurn(90, 0.0085, 0.000004, 0.0006, 0.00, 2.0, 1.0, -1.0, robot.IMU1.getZheading());
                    break;
                case driveForward:
                    SM.setNextState(States.done, HDWaitTypes.EncoderChangeBoth, 300.0);
                    robot.robotDrive.VLF(-.15, 90, 0.01, 2, robot.IMU1.getZheading());
                    break;
                case done:
                    robot.robotGlyph.setIntakePower(0.0);
                    robot.robotDrive.motorBreak();
                    break;
            }
        }
    }

    public void waitBeforeNextState(double waitTime, States nextState){
        this.nextState = nextState;
        this.waitTime = waitTime;
        SM.resetValues();
        SM.setState(States.wait);
    }

    private boolean ready(){
        boolean ready = true;
        if(!robot.IMU1.isCalibrated()){
            ready = false;
        }
        return ready;
    }

}
