package org.firstinspires.ftc.hdcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.hdlib.Controls.HDGamepad;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDOpMode;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.HDRobot;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems.HDDriveHandler;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Created by akash on 8/8/2017.
 */

@TeleOp
public class HDTeleop extends HDOpMode implements HDGamepad.HDButtonMonitor{

    private HDGamepad driverGamepad;
    private HDGamepad servoBoyGamepad;
    private HDRobot robot;

    private boolean collectorOn = false;
    private boolean gripBlock = false;

    private enum liftHeight{
        GROUND, //0
        COLLECT2, //2100
        DEPOSITHIGH,
        BALANCINGSTONE,
    }

    private enum driveMode{
        FIELD_CENTRIC_DRIVE,
        HALO_DRIVE,
        TANK_DRIVE;

        public driveMode getNext() {
            return this.ordinal() < driveMode.values().length - 1
                    ? driveMode.values()[this.ordinal() + 1]
                    : driveMode.values()[0];
        }
    }

    private double speed = 0.75;
    private driveMode curDriveMode = driveMode.HALO_DRIVE;

    @Override
    public void initialize() {
        robot = new HDRobot(hardwareMap);

        driverGamepad = new HDGamepad(gamepad1, this);
        servoBoyGamepad = new HDGamepad(gamepad2, this);

        robot.robotDrive.reverseSide(HDDriveHandler.Side.Right);
        robot.robotDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        robot.robotJewel.raiseLeftServo();
        robot.robotJewel.raiseRightServo();
    }

    @Override
    public void initializeLoop() {

    }

    @Override
    public void Start() {
        driverGamepad.setGamepad(gamepad1);
        servoBoyGamepad.setGamepad(gamepad2);
    }

    @Override
    public void continuousRun(double elapsedTime) {
        if(robot.IMU1.isCalibrated()) {
            telemetry();
            driveTrain();
            glyphSystem();
        }else{
            robot.robotDrive.motorBreak();
        }


    }

    private void telemetry(){
        dashboard.addProgramSpecificTelemetry(0, "Speed: " + String.valueOf(speed));
        dashboard.addProgramSpecificTelemetry(1, "Drive Mode: %s", String.valueOf(curDriveMode));
        dashboard.addProgramSpecificTelemetry(2, "Collector On?: %s", String.valueOf(collectorOn));
        dashboard.addDiagnosticSpecificTelemetry(0, "Gyro Z Heading: %f", robot.IMU1.getZheading());
        dashboard.addDiagnosticSpecificTelemetry(1, "Lift Encoder Value: %d", (robot.robotGlyph.leftPinionMotor.getCurrentPosition()+robot.robotGlyph.rightPinionMotor.getCurrentPosition())/2);
        dashboard.addDiagnosticSpecificTelemetry(2, "Color Sensor a: %d, r: %d, g: %d, b: %d, in: %.2f", robot.robotGlyph.bottomGlyphColor.alpha(), robot.robotGlyph.bottomGlyphColor.red(),
                robot.robotGlyph.bottomGlyphColor.green(), robot.robotGlyph.bottomGlyphColor.blue(), robot.robotGlyph.bottomGlyphDistance.getDistance(DistanceUnit.INCH));
        if(robot.robotGlyph.bottomGlyphDistance.getDistance(DistanceUnit.INCH) < 2.5){
            dashboard.addDiagnosticSpecificTelemetry(3, "Block in bottom bay");
        }else{
            dashboard.addDiagnosticSpecificTelemetry(3, "No Block in bottom bay");
        }
    }

    private void driveTrain(){
        if(gamepad1.a){
            robot.robotDrive.gyroTurn(90.0, 0.018, 0.000004, 0.0006, 0.0, 2.0, 1.0, -1.0, robot.IMU1.getZheading());
        }else if(gamepad1.b){
            robot.robotDrive.gyroTurn(-90.0, 0.018, 0.000004, 0.0006, 0.0, 2.0, 1.0, -1.0, robot.IMU1.getZheading());
        }else if(gamepad1.y){
            robot.robotDrive.gyroTurn(0, 0.018, 0.000004, 0.0006, 0.0, 2.0, 1.0, -1.0, robot.IMU1.getZheading());
        }else {
            switch (curDriveMode) {
                case FIELD_CENTRIC_DRIVE:
                    robot.robotDrive.mecanumDrive_Cartesian(gamepad1.left_stick_x * speed, gamepad1.left_stick_y * speed, gamepad1.right_stick_x * speed, robot.IMU1.getZheading());
                    break;
                case HALO_DRIVE:
                    robot.robotDrive.haloDrive(gamepad1.left_stick_x * speed, gamepad1.left_stick_y * speed, gamepad1.right_stick_x * speed);
                    break;
                case TANK_DRIVE:
                    robot.robotDrive.tankDrive(gamepad1.left_stick_y * speed, gamepad1.right_stick_y * speed);
                    break;
            }
        }
    }

    private void glyphSystem(){
    if(gamepad2.right_bumper){
        robot.robotGlyph.setLiftPower(-gamepad2.left_stick_y);
    }else if(gamepad2.start){
        robot.robotGlyph.leftPinionMotor.setPower(-gamepad2.left_stick_y);
        robot.robotGlyph.rightPinionMotor.setPower(-gamepad2.right_stick_y);
    }else{
        robot.robotGlyph.setLiftPower(0.0);
    }
    if(gamepad2.y){
        gripBlock = false;
        robot.robotGlyph.setIntakePower(-.7);
        robot.robotGlyph.blockKickerOut();
        collectorOn = false;
    }else if(collectorOn){
        robot.robotGlyph.setIntakePower(.7);
        robot.robotGlyph.blockKickerIn();
    }else{
        robot.robotGlyph.setIntakePower(0.0);
        robot.robotGlyph.blockKickerIn();
    }
    }


    @Override
    public void buttonChange(HDGamepad instance, HDGamepad.gamepadButtonChange button, boolean pressed) {
        if(instance == driverGamepad){
            switch (button) {
                case A:
                    break;
                case B:
                    break;
                case X:
                    break;
                case Y:
                    break;
                case DPAD_LEFT:
                    if(pressed){
                        curDriveMode = curDriveMode.getNext();
                    }
                    break;
                case DPAD_RIGHT:
                    break;
                case DPAD_UP:
                    if(pressed) {
                        speed += 0.25;
                        speed = Range.clip(speed, 0.25, 1.0);
                    }
                    break;
                case DPAD_DOWN:
                    if(pressed) {
                        speed -= 0.25;
                        speed = Range.clip(speed, 0.25, 1.0);
                    }
                    break;
                case LEFT_BUMPER:
                    break;
                case RIGHT_BUMPER:
                    break;
                case RIGHT_TRIGGER:
                    break;
                case LEFT_TRIGGER:
                    break;
                case START:
                    if(pressed){
                        robot.IMU1.zeroZheading();
                    }
                    break;
            }
        }else if(instance == servoBoyGamepad){
            switch (button) {
                case A:
                    break;
                case B:
                    break;
                case X:
                    if(pressed) {
                        collectorOn = !collectorOn;
                    }
                    break;
                case Y:
                    break;
                case DPAD_LEFT:
                    break;
                case DPAD_RIGHT:
                    break;
                case DPAD_UP:
                    if(pressed){
                        robot.robotGlyph.setScotchYokePower(.75);
                    }else{
                        robot.robotGlyph.setScotchYokePower(0.0);
                    }
                    break;
                case DPAD_DOWN:
                    if(pressed){
                        robot.robotGlyph.setScotchYokePower(-.75);
                    }else{
                        robot.robotGlyph.setScotchYokePower(0.0);
                    }
                case LEFT_BUMPER:
                    if(pressed){
                        robot.robotGlyph.gripBlock();
                    }else{
                        robot.robotGlyph.unGripBlock();
                    }
                    break;
                case RIGHT_BUMPER:
                    break;
                case RIGHT_TRIGGER:
                    break;
                case LEFT_TRIGGER:
                    break;
                case START:

                    break;
            }
        }
    }
}
