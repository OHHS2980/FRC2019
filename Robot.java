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
  private int[] panel1 = new int[4];
  private int[] panel2 = new int[4];
  private int center = 159;

  
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
  private boolean prevState5 = false;
  private boolean prevState6 = false;
  private boolean prevState7 = false;
  private boolean prevState8 = false;
  
  private boolean hatchToggle = false;
  private boolean intakeToggle = false;
  private boolean ballPusher = false;
  private boolean driverInverse = false;
 

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

    s0 = new Solenoid(1);
    s1 = new Solenoid(2);
    s2 = new Solenoid(3);
    s3 = new Solenoid(4);
    s4 = new Solenoid(5);
    s5 = new Solenoid(6);

    m_drive = new DifferentialDrive(rightDrive, leftDrive);
    m_joystick = new Joystick(0);
    CameraServer.getInstance().startAutomaticCapture(0);
    spi = new SerialPort(115200, Port.kUSB1);
    spi.enableTermination();
    CameraServer.getInstance().startAutomaticCapture(0);
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
    if(s!="")
      SmartDashboard.putString("value", s);
    //double sliderPos = (m_joystick.getRawAxis(9) + 1)*3/2; //range from 0-3

    //if(sliderPos<= 1){}
  
    if(m_joystick.getRawButton(8) && !prevState8){
      driverInverse = !driverInverse;
    }
    prevState8 = m_joystick.getRawButton(8);

    if(driverInverse) {
      m_drive.arcadeDrive(-m_joystick.getY(), m_joystick.getZ());
    }
    else {
      m_drive.arcadeDrive(m_joystick.getY(), m_joystick.getZ());
    }
        
    if(m_joystick.getRawButton(1)) {
      intake.set(0.5);
    }
    else if(m_joystick.getRawButton(2)) {
      intake.set(-.5);
    }
    else {
      intake.set(0);
    }
    //second one was getX
    /*if(m_joystick.getRawButtonPressed(1)) {
      hatchPlace();
    }*/
    if(m_joystick.getRawButton(3)) {
      lifter.set(1);
    }
    else if(m_joystick.getRawButton(4)) {
      lifter.set(-1);
    }
    else {
      lifter.set(0);
    }
    

    if(m_joystick.getRawButton(5) && !prevState5){
      hatchToggle = !hatchToggle;
      if(hatchToggle) {
        s3.set(true);
        s1.set(true);
        s2.set(true);
      }
      else {
        s3.set(false);
        s1.set(false);
        s2.set(false);
      }
    }
    prevState5 = m_joystick.getRawButton(5);

    
    if(m_joystick.getRawButton(6) && !prevState6){
      intakeToggle = !intakeToggle;
      if(intakeToggle) {
        s0.set(true);  
      }
      else {
        s0.set(false);    
      }
    }
    prevState6 = m_joystick.getRawButton(6);

    if(m_joystick.getRawButton(7) && !prevState7){
      ballPusher = !ballPusher;
      if(ballPusher) {
        s4.set(true);  
      }
      else {
        s4.set(false);    
      }
    }
    prevState7 = m_joystick.getRawButton(7);
  }

  public void autonomousInit() {
    m_drive.setSafetyEnabled(true);
  }

  public void autonomousPeriodic() {
    
  }

  public void pixyData(String s){
    SmartDashboard.putNumber("length", s.length());
    if(!s.isEmpty() && s.length()==23 && s.charAt(0)=='P') {
      SmartDashboard.putString("parsed", s);
      int sig = Character.getNumericValue(s.charAt(3));

      //format "P1S0X000Y000W000H000N0\n"
      //        0123456789012345678901
      int x = Integer.valueOf(s.substring(5,8));
      int y = Integer.valueOf(s.substring(9,12));
      int width = Integer.valueOf(s.substring(13, 16));
      int height = Integer.valueOf(s.substring(17,20));
      int sigNum = Integer.valueOf(s.substring(21,22));

      if(sig==1) {
        ball[0] = x;
        ball[1] = y;
        ball[2] = width;
        ball[3] = height;
      }
      else if(sig==2) {
        if(sigNum==0) {
          panel1[0] = x;
          panel1[1] = y;
          panel1[2] = width;
          panel1[3] = height;
        }
        else if(sigNum==1) {
          panel2[0] = x;
          panel2[1] = y;
          panel2[2] = width;
          panel2[3] = height;
        }/*
        if(panel2[0]<panel1[0]) {
          int temp[] = panel1.clone();
          panel1 = panel2.clone();
          panel2 = temp.clone();
        }*/
      }
    }
  }
  public void hatchPlace() {
    double start = m_timer.get();
    String s = spi.readString();
    SmartDashboard.putString("value", s);
    pixyData(s);
    while(Math.abs(159-(panel2[0]+panel1[0])/2)>3) {
      //get x value from pixy
      double turn = 0;
      if((panel2[0]+panel1[0])/2<159) {
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
    while(Math.abs(panel2[0]-panel1[0])>25) {
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
}