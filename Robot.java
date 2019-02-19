/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.SerialPort.Port;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.Solenoid;
//import edu.wpi.first.wpilibj.Compressor;
//import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.AnalogInput;
//import edu.wpi.first.wpilibj.Counter;
//import edu.wpi.first.wpilibj.Ultrasonic;

public class Robot extends TimedRobot {

  private WPI_TalonSRX leftDrive; //fl
  private WPI_TalonSRX leftSlave; //bl
  private WPI_TalonSRX rightDrive; //fr
  private WPI_TalonSRX rightSlave; //br
  private DifferentialDrive m_drive;
   
  private WPI_TalonSRX intake;

  private WPI_TalonSRX lifter;
  private WPI_TalonSRX lifterslave;
 
  private Joystick m_joystick;

  private int[] ball = new int[4];
  private int[] cargoPanel1 = new int[4];
  private int[] cargoPanel2 = new int[4];

  private int[] hatchPanel1 = new int[4];
  private int[] hatchPanel2 = new int[4];

  private int center = 159;

  private int lifterPos = 0;

 //AnalogInput analoginput = new AnalogInput(0);
  
  private final Timer m_timer = new Timer();
  SerialPort spi;

 // private Compressor compressor;

  private Solenoid s0;
  private Solenoid s1;
  private Solenoid s2;
  private Solenoid s3;
  private Solenoid s4;
  private Solenoid s5;

  private double prevSlider = 0;
  
  private boolean hatchToggle = false;
  private boolean intakeToggle = false;
  private boolean ballPusher = false;
  private boolean driverInverse = false;
 
  private double sliderPos = 0;
  private int prevSliderLv = 0;

  private String lastChanged = "button";
 
  private double homeHeight = 10;
  private double liftLimit = 300;
  private double ballHeight1 = 76;
  private double ballHeight2 = 142;
  private double ballHeight3 = 198;
  private double panelHeight1 = 53;
  private double panelHeight2 = 124;
  private double panelHeight3 = 193;
  @Override
  public void robotInit() {
    leftDrive = new WPI_TalonSRX(2); //fl
    leftSlave = new WPI_TalonSRX(3); //bl

    rightDrive = new WPI_TalonSRX(4); //fr
    rightSlave = new WPI_TalonSRX(5); //br

    leftSlave.follow(leftDrive);
    rightSlave.follow(rightDrive);

    intake = new WPI_TalonSRX(6);

    lifter = new WPI_TalonSRX(7);
     lifterslave = new WPI_TalonSRX(8);
    lifterslave.follow(lifter);

    s0 = new Solenoid(0);
    s1 = new Solenoid(1);
    s2 = new Solenoid(2);
    s3 = new Solenoid(3);
    s4 = new Solenoid(5);
    s5 = new Solenoid(6);

    m_drive = new DifferentialDrive(rightDrive, leftDrive);
    m_joystick = new Joystick(0);
    CameraServer.getInstance().startAutomaticCapture(); //bottom port
    spi = new SerialPort(115200, Port.kUSB1); //top port
    spi.enableTermination();
  }

  @Override
  public void teleopInit() {
    m_timer.reset();
    m_timer.start();
    //encoderMotor1.setDistancePerPulse(10);
    //encoderMotor1.reset();
  }

  @Override
  public void teleopPeriodic() {
    String s = spi.readString();
    pixyData(s);
    if(!s.isEmpty())
      SmartDashboard.putString("value", s);
    
    if(m_joystick.getRawButtonPressed(4)){
      driverInverse = !driverInverse;
    }

    if(driverInverse) {
      m_drive.arcadeDrive(-m_joystick.getY(), m_joystick.getZ());
    }
    else {
      m_drive.arcadeDrive(m_joystick.getY(), m_joystick.getZ());
    }
        
    sliderPos = (-m_joystick.getRawAxis(5) + 1)*2; //range from 0-4
    SmartDashboard.putNumber("SliderPos", sliderPos);
    checkLifterChanged();

    if(m_joystick.getRawButton(2)) {
      intake.set(1);
    }
    else if(m_joystick.getRawButton(7)) {
      intake.set(-1);
    }
    else {
      intake.set(0);
    }

    //changeLifterLevel();
    
    if(lifterPos>liftLimit) {
      lifter.set(0);
    }
    else if(m_joystick.getRawButton(1)) {//lifter down
      lifter.set(-0.35);
      lastChanged = "button";
    }
    else if(m_joystick.getRawButton(5)) { //lift UP
      lifter.set(0.65);
      lastChanged = "button";
    }
    else if(lastChanged.equals("button")) {
      lifter.set(0);
    }
    
    if(m_joystick.getRawButtonPressed(3)){
      hatchToggle = !hatchToggle;
      if(hatchToggle) {
        s1.set(true);
        s2.set(true);
        s3.set(true);
      }
      else {
        s1.set(false);
        s2.set(false);
        s3.set(false);
      }
    }
    
    if(m_joystick.getRawButtonPressed(8)){
      intakeToggle = !intakeToggle;
      if(intakeToggle) {
        s0.set(true);  
      }
      else {
        s0.set(false);    
      }
    }

    if(m_joystick.getRawButtonPressed(6)){
      ballPusher = !ballPusher;
      if(ballPusher) {
        s4.set(true);  
      }
      else {
        s4.set(false);    
      }
    }
    if(driverInverse)
      SmartDashboard.putString("Mode", "Cargo");
    else  
      SmartDashboard.putString("Mode", "Hatch");
  }

  public void pixyData(String s){
    SmartDashboard.putNumber("length", s.length());
    //format D:000
    if(!s.isEmpty() && s.length()==6 && s.charAt(0)=='D') {
      lifterPos = Integer.valueOf(s.substring(2,5));
      SmartDashboard.putNumber("Lifter Position (cm)", lifterPos);
    }
    else if(!s.isEmpty() && s.length()==23 && s.charAt(0)=='P') {
      SmartDashboard.putString("parsed", s);
      int sig = Character.getNumericValue(s.charAt(3));

      //format "P1S0X000Y000W000H000N0\n"
      //        0123456789012345678901
      int pixyNum = Integer.valueOf(s.substring(1,2));
      int x = Integer.valueOf(s.substring(5,8));
      int y = Integer.valueOf(s.substring(9,12));
      int width = Integer.valueOf(s.substring(13, 16));
      int height = Integer.valueOf(s.substring(17,20));
      int sigNum = Integer.valueOf(s.substring(21,22));

      if(sig==1&&pixyNum==2) {
        ball[0] = x;
        ball[1] = y;
        ball[2] = width;
        ball[3] = height;
      }
      else if(sig==2&&pixyNum==2) {
        if(sigNum==0) {
          cargoPanel1[0] = x;
          cargoPanel1[1] = y;
          cargoPanel1[2] = width;
          cargoPanel1[3] = height;
        }
        else if(sigNum==1) {
          cargoPanel2[0] = x;
          cargoPanel2[1] = y;
          cargoPanel2[2] = width;
          cargoPanel2[3] = height;
        }
        if(cargoPanel2[0]<cargoPanel1[0]) {
          int temp[] = cargoPanel1.clone();
          cargoPanel1 = cargoPanel2.clone();
          cargoPanel2 = temp.clone();
        }
      }
      if(pixyNum==1&&sig==2) {
        if(sigNum==0) {
          hatchPanel1[0] = x;
          hatchPanel1[1] = y;
          hatchPanel1[2] = width;
          hatchPanel1[3] = height;
        }
        else if(sigNum==1) {
          hatchPanel2[0] = x;
          hatchPanel2[1] = y;
          hatchPanel2[2] = width;
          hatchPanel2[3] = height;
        }
        if(cargoPanel2[0]<cargoPanel1[0]) {
          int temp[] = hatchPanel1.clone();
          hatchPanel1 = hatchPanel2.clone();
          hatchPanel2 = temp.clone();
        }
      }
    }
  }
  public void hatchPlace() {
    double start = m_timer.get();
    String s = spi.readString();
    SmartDashboard.putString("value", s);
    pixyData(s);
    while(Math.abs(159-(cargoPanel2[0]+cargoPanel1[0])/2)>3) {
      //get x value from pixy
      double turn = 0;
      if((cargoPanel2[0]+cargoPanel1[0])/2<159) {
        turn = 0.5;
      }
      else {
        turn = -0.5;
      }
      m_drive.arcadeDrive(turn, 0);
      if(m_timer.get()-start>2.5 || m_joystick.getRawButtonPressed(8)) {
        return;
      }
    }
    while(Math.abs(cargoPanel2[0]-cargoPanel1[0])>25) {
      m_drive.arcadeDrive(0.5, 0);
      if(m_timer.get()-start>4.5|| m_joystick.getRawButtonPressed(8)) {
        m_drive.arcadeDrive(0, 0);
        return;
      }
    }
    //place hatch
  }
  
  public void collectBall() {
    double start = m_timer.get();
    String s = spi.readString();
    SmartDashboard.putString("value", s);
    pixyData(s);
    while(Math.abs(159-(ball[0])/2)>3) {
      //get x value from pixy
      double turn = 0;
      if((ball[0])<159) {
        turn = 0.5;
      }
      else {
        turn = -0.5;
      }
      m_drive.arcadeDrive(turn, 0);
      if(m_timer.get()-start>2.5 || m_joystick.getRawButtonPressed(8)) {
        return;
      }
    }
    //Fix This!!!
    while(ball[1]>100) {
      m_drive.arcadeDrive(0.5, 0);
      //setIntake(-1.0)
      if(m_timer.get()-start>4.5 || m_joystick.getRawButtonPressed(8)) {
        m_drive.arcadeDrive(0, 0);
        //setIntake(0)
        return;
      }
    }
  }
  private void checkLifterChanged() {
    if(Math.abs(sliderPos-prevSlider)>0.05) {
      lastChanged = "slider";
    }
    prevSlider = sliderPos;
  }
  private void changeLifterLevel() {
    if(lastChanged.equals("slider")) {
      if(lifterPos>liftLimit) {
        lifter.set(0);
      }
      else if(driverInverse) { //placing balls
        if(sliderPos<= 1 && lifterPos<homeHeight){
          lifter.set(1);
        }
        else if(sliderPos <= 1 && lifterPos>homeHeight){
          lifter.set(-1);
          s0.set(true);
        }
        else if(sliderPos <= 2 && lifterPos<ballHeight1) {
          lifter.set(1);
          s0.set(true);
          intake.set(1);
        }
        else if(sliderPos <= 2 && lifterPos>ballHeight1){
          lifter.set(-1);
        }
        else if(sliderPos <= 3 && lifterPos<ballHeight2){
          lifter.set(1);
        }
        else if(sliderPos <= 3 && lifterPos>ballHeight2){
          lifter.set(-1);
        }
        else if(sliderPos <= 4 && lifterPos<ballHeight3){
          lifter.set(1);
        }
        else if(sliderPos <= 4 && lifterPos>ballHeight3){
          lifter.set(-1);
        }
        else {
          lifter.set(0);
        }
      }
      else { //placing hatches
        if(sliderPos <= 1 && lifterPos<homeHeight){
          lifter.set(1);
        }
        else if(sliderPos <= 1 && lifterPos>homeHeight){
          lifter.set(-1);
        }
        else if(sliderPos <= 2 && lifterPos<panelHeight1){
          lifter.set(1);
        }
        else if(sliderPos <= 2 && lifterPos>panelHeight1){
          lifter.set(-1);
        }
        else if(sliderPos <= 3 && lifterPos<panelHeight2){
          lifter.set(1);
        }
        else if(sliderPos <= 3 && lifterPos>panelHeight2){
          lifter.set(-1);
        }
        else if(sliderPos <= 4 && lifterPos<panelHeight3){
          lifter.set(1);
        }
        else if(sliderPos <= 4 && lifterPos>panelHeight3){
          lifter.set(-1);
        }
        else {
          lifter.set(0);
        }
      }
    }
  }
}