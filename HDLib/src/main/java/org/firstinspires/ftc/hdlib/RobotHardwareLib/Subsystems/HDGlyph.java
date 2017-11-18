package org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.hdlib.RobotHardwareLib.Servo.HDVexMotor;

/**
 * Created by FIRSTMentor on 11/11/2017.
 */

public class HDGlyph {

    public DcMotor scotchYokeMotor, leftPinionMotor, rightPinionMotor;
    private HDVexMotor bottomLeftIntake, bottomRightIntake, topLeftIntake, topRightIntake;
    public Servo blockKicker, leftBlockGrabber, rightBlockGrabber;

    private double liftMaxEnc = 10000;
    private double leftBlockGrabberGrip = 0.80;
    private double leftBlockGrabberNoGrip = 0.92;
    private double rightBlockGrabberGrip = 0.36;
    private double rightBlockGrabberNoGrip = 0.27;
    private double blockKickerOut = 0.54;
    private double blockKickerIn = 0.47;

    public HDGlyph(HardwareMap hardwareMap){

        scotchYokeMotor = hardwareMap.dcMotor.get("scotchYokeMotor");
        leftPinionMotor = hardwareMap.dcMotor.get("leftPinionMotor");
        rightPinionMotor = hardwareMap.dcMotor.get("rightPinionMotor");

        bottomLeftIntake = new HDVexMotor(hardwareMap, "bottomLeftIntake", Servo.Direction.FORWARD);
        bottomRightIntake = new HDVexMotor(hardwareMap, "bottomRightIntake", Servo.Direction.FORWARD);
        topLeftIntake = new HDVexMotor(hardwareMap, "topLeftIntake", Servo.Direction.FORWARD);
        topRightIntake = new HDVexMotor(hardwareMap, "topRightIntake", Servo.Direction.FORWARD);

        blockKicker = hardwareMap.servo.get("blockKicker");
        leftBlockGrabber = hardwareMap.servo.get("leftBlockGrabber");
        rightBlockGrabber = hardwareMap.servo.get("rightBlockGrabber");

        rightPinionMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        leftPinionMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightPinionMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftPinionMotor.setPower(0);
        rightPinionMotor.setPower(0);
        scotchYokeMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        scotchYokeMotor.setPower(.75);
        scotchYokeMotor.setTargetPosition(0);
        leftBlockGrabber.setPosition(0.89);
        rightBlockGrabber.setPosition(0.27);
        blockKicker.setPosition(0.06);
    }


    public void setLiftPower(double power){
        leftPinionMotor.setPower(power);
        rightPinionMotor.setPower(power);
    }

    public void setIntakePower(double power){
        bottomLeftIntake.setPower(-power);
        bottomRightIntake.setPower(power);
        topLeftIntake.setPower(-power);
        topRightIntake.setPower(power);
    }

    public void gripBlock(){
        leftBlockGrabber.setPosition(leftBlockGrabberGrip);
        rightBlockGrabber.setPosition(rightBlockGrabberGrip);
    }

    public void unGripBlock(){
        leftBlockGrabber.setPosition(leftBlockGrabberNoGrip);
        rightBlockGrabber.setPosition(rightBlockGrabberNoGrip);
    }

    public void blockKickerOut(){
        blockKicker.setPosition(blockKickerOut);
    }

    public void blockKickerIn(){
        blockKicker.setPosition(blockKickerIn);
    }

    public void scotchYokeBottom(){
        scotchYokeMotor.setTargetPosition(0);
    }

    public void scotchYokeTop(){
        scotchYokeMotor.setTargetPosition(1600);
    }

}
