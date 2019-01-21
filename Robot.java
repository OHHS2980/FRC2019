/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.SerialPort.Port;
import java.util.Arrays;


/**
 * This sample program shows how to control a motor using a joystick. In the
 * operator control part of the program, the joystick is read and the value is
 * written to the motor.
 *
 * Joystick analog values range from -1 to 1 and speed controller inputs also
 * range from -1 to 1 making it easy to work together.
 */
public class Robot extends TimedRobot {
  private static final int kMotorPort0 = 0;
  private static final int kMotorPort1 = 1;
  private static final int kMotorPort2 = 2;
  private static final int kMotorPort3 = 3;
  private static final int kJoystickPort = 0;

  private SpeedController m_motor0; //fl
  private SpeedController m_motor1; //bl
  private SpeedController m_motor2; //fr
  private SpeedController m_motor3; //br
  private DifferentialDrive m_drive;
  private Joystick m_joystick;

  private int[] ball = new int[4];
  private int[] panel = new int[4];
  private int center = 159;

  private final Timer m_timer = new Timer();
  SerialPort spi;
  

  @Override
  public void robotInit() {
    m_motor0 = new Spark(kMotorPort0); //fl
    m_motor1 = new Spark(kMotorPort1); //bl
    SpeedControllerGroup m_left = new SpeedControllerGroup(m_motor0, m_motor1);

    m_motor2 = new Spark(kMotorPort2); //fr
    m_motor3 = new Spark(kMotorPort3); //br
    SpeedControllerGroup m_right = new SpeedControllerGroup(m_motor2, m_motor3);
    m_drive = new DifferentialDrive(m_right, m_left);
    m_joystick = new Joystick(kJoystickPort);
    CameraServer.getInstance().startAutomaticCapture(1);

  }

  @Override
  public void teleopInit() {
    spi = new SerialPort(115200, Port.kUSB);
    spi.enableTermination();
    m_timer.reset();
    m_timer.start();
  }

  @Override
  public void teleopPeriodic() {
    String s = spi.readString();
    pixyData(s);
    if(s!="")
      SmartDashboard.putString("value", s);
    m_drive.arcadeDrive(m_joystick.getY(), m_joystick.getX());
  }
  public void autonomousInit() {
    spi = new SerialPort(115200, Port.kUSB);
    spi.enableTermination();
    m_drive.setSafetyEnabled(true);
  }
  public void autonomousPeriodic() {
    String s = spi.readString();
    SmartDashboard.putString("value", s);
    pixyData(s);
    //m_drive.arcadeDrive(0,0);
    SmartDashboard.putNumber("x", ball[0]-159);
    SmartDashboard.putNumber("x corrected", ball[0]-center);
    double turn;
    double drive = 0;
    if(ball[0]-center<-7) {
      turn = -0.5;
      if(ball[0]-center>-20)
        turn = -0.45;
    }
    else if(ball[0]-center>7) {
      turn = 0.5;
      if(ball[0]-159<20)
        turn = 0.45;
    }
    else {
      turn = 0;
      drive = 0.5;
    }
    m_drive.arcadeDrive(drive,turn);
  }
  public void pixyData(String s){
    SmartDashboard.putNumber("length", s.length());
    if(!s.isEmpty() && s.length()==21 && s.charAt(0)=='P') {
      SmartDashboard.putString("parsed", s);
      int sig = Character.getNumericValue(s.charAt(3));
      //try {
        //format "P1S0X000Y000W000H000\n"
        //        01234567890123456789
        int x = Integer.valueOf(s.substring(5,8));
        int y = Integer.valueOf(s.substring(9,12));
        int width = Integer.valueOf(s.substring(13, 16));
        int height = Integer.valueOf(s.substring(17,20));
        /*int x = Integer.valueOf(s.substring(s.indexOf("X")+1,s.indexOf("Y")));
        int y = Integer.valueOf(s.substring(s.indexOf("Y")+1,s.indexOf("W")));
        int width = Integer.valueOf(s.substring(s.indexOf("W")+1,s.indexOf("H")));
        int height = Integer.valueOf(s.substring(s.indexOf("H")+1,s.indexOf("\n")));*/

        if(sig==1) {
          ball[0] = x;
          ball[1] = y;
          ball[2] = width;
          ball[3] = height;
        }
        else if(sig==2) {
          panel[0] = x;
          panel[1] = y;
          panel[2] = width;
          panel[3] = height;
        }
      //} catch(Exception e){}
    }
  }
}