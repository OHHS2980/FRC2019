/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
//import all of the required packages
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

//Main pogram class
public class Robot extends TimedRobot {
  //declares drivetrain type and drivetrain motors
  private WPI_TalonSRX leftDrive; //fl
  private WPI_TalonSRX leftSlave; //bl
  private WPI_TalonSRX rightDrive; //fr
  private WPI_TalonSRX rightSlave; //br
  private DifferentialDrive m_drive;
  //declares intake motor
  private WPI_TalonSRX intake;
  //declares lifter motors
  private WPI_TalonSRX lifter;
  private WPI_TalonSRX lifterslave;
  //declares the m_joystick
  private Joystick m_joystick;
  //declares variables for the pixycam data
  private int[] ball = new int[4];
  private int[] cargoPanel1 = new int[4];
  private int[] cargoPanel2 = new int[4];
  private int[] hatchPanel1 = new int[4];
  private int[] hatchPanel2 = new int[4];
  //declares a variable for the centerof the pixycam
  private int center = 159;
  //declares a variable lifter position
  private int lifterPos = 0;

  //AnalogInput analoginput = new AnalogInput(0);
  //creates a timer variable
  private final Timer m_timer = new Timer();
  //
  SerialPort spi;

 // private Compressor compressor;
  //creates the solenoid
  private Solenoid s0;
  private Solenoid s1;
  private Solenoid s2;
  private Solenoid s3;
  private Solenoid s4;
  private Solenoid s5;
  //creates a variable to record the previous state of the slider
  private double prevSlider = 0;
  //creates toggle variables for all of the togglable robot functons
  private boolean hatchToggle = false;
  private boolean intakeToggle = false;
  private boolean ballPusher = false;
  private boolean driverInverse = false;
  //creates variables for tracking all of the sliders positions
  private double sliderHeight = 0;
  private double sliderSpeed = 0;
  private double lifterSpeed;
  //creates a variable for tracking the previous lifter lever based off the slider
  private int prevSliderLv = 0;
  //creates a variable to see if the slider or button was used last
  private String lastChanged = "button";
  //creates variables for all of the lifter positions
  private double homeHeight = 10;
  private double liftLimit = 300;
  private double ballHeight1 = 76;
  private double ballHeight2 = 142;
  private double ballHeight3 = 198;
  private double panelHeight1 = 53;
  private double panelHeight2 = 124;
  private double panelHeight3 = 193;
  private double targetHeight = 0;

  @Override
  public void robotInit() {//The program that runs on initialization
    leftDrive = new WPI_TalonSRX(2); //fl
    leftSlave = new WPI_TalonSRX(3); //bl+

    rightDrive = new WPI_TalonSRX(4); //fr
    rightSlave = new WPI_TalonSRX(5); //br
    //slaves the front and back motors together
    leftSlave.follow(leftDrive);
    rightSlave.follow(rightDrive);
    //creates the intake
    intake = new WPI_TalonSRX(6);
    //creates the lifter and slaves the motors together
    lifter = new WPI_TalonSRX(7); 
     lifterslave = new WPI_TalonSRX(8);
    lifterslave.follow(lifter); 
    //creates the solenoid
    s0 = new Solenoid(0);
    s1 = new Solenoid(1);
    s2 = new Solenoid(2);
    s3 = new Solenoid(3);
    s4 = new Solenoid(5);
    s5 = new Solenoid(6);

    m_drive = new DifferentialDrive(rightDrive, leftDrive); //Slave drive motor together
    m_joystick = new Joystick(0); // Creates joystick leaver
    CameraServer.getInstance().startAutomaticCapture(); //bottom port
    spi = new SerialPort(115200, Port.kUSB1); //top port, startes serial port at 115200 baud rate
    spi.enableTermination();
  }

  @Override
  public void teleopInit() {
    m_timer.reset();
    m_timer.start();

    lastChanged = "button";
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

    if(driverInverse) { // Switchs joystick value for inverse buttons 
      m_drive.arcadeDrive(-m_joystick.getY(), m_joystick.getZ());
    }
    else {
      m_drive.arcadeDrive(m_joystick.getY(), m_joystick.getZ());
    }
        
    sliderHeight = (-m_joystick.getRawAxis(5) + 1)*2; //range from 0-4, gets value of slider 6
    SmartDashboard.putNumber("SliderHeight", sliderHeight); 

    sliderSpeed = (-m_joystick.getRawAxis(4) + 1)/2;
    SmartDashboard.putNumber("SliderSpeed", sliderSpeed);

    checkLifterChanged();
    setTargetHeight();

    if(lastChanged.equals("slider")&&lifterPos!=0) {
      if(m_joystick.getRawButton(1)&&targetHeight>lifterPos) {
        lifter.set(-0.35*sliderSpeed);
      }
      else if(m_joystick.getRawButton(5)&&targetHeight<lifterPos) {
        lifter.set(0.65*sliderSpeed);
      }
      else if((m_joystick.getRawButtonPressed(5)&&targetHeight>lifterPos)||(m_joystick.getRawButtonPressed(1)&&targetHeight<lifterPos)){
        lastChanged = "button";
      }
      else {
        lifter.set(0);
      }
    }

    if(lifterPos>liftLimit) {
      lifter.set(0);
    }
    else if(m_joystick.getRawButton(1)) {//lifter up
      lifter.set(-.6*sliderSpeed);
    }
    else if(m_joystick.getRawButton(5)) { //lift down
      lifter.set(.4*sliderSpeed);
    }
    else if(lastChanged.equals("button")) { //If button is not pressed lifter will stop 
      lifter.set(0);
    }
    else {
      lifter.set(0);
    }
    
    if(m_joystick.getRawButton(2)) {
      intake.set(1);
    }
    else if(m_joystick.getRawButton(7)) {
      intake.set(-1);
    }
    else {
      intake.set(0);
    }

    if(m_joystick.getRawButtonPressed(3)){ 
      hatchToggle = !hatchToggle;
      if(hatchToggle) { //pistions is extended, ejecting hatch panel
        
        s2.set(true);
        s3.set(true);
      }
      else {  //pistions is retracted
      
        s2.set(false);
        s3.set(false);
      }
    }
    
    if(m_joystick.getRawButtonPressed(8)){
      intakeToggle = !intakeToggle;
      if(intakeToggle) { //intake is put down
        s0.set(true);  
      }
      else { //intake is retracted
        s0.set(false);    
      }
    }

    if(m_joystick.getRawButtonPressed(6)){
      ballPusher = !ballPusher;
      if(ballPusher) { //pistion is extended, pushing ball out
        s1.set(true);  
      }
      else { //pistion is retracted
        s1.set(false);    
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
      //format "P1S0X000Y000W000H000N0\n"
      //0123456789012345678901
      int sig = Character.getNumericValue(s.charAt(3));
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
 /* public void hatchPlace() {
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
  } */

  private void checkLifterChanged() {
    if(Math.abs(sliderHeight-prevSlider)>0.05) {
      lastChanged = "slider";
    }
    prevSlider = sliderHeight;
  }  

  private void changeLifterLevel() {
    if(lastChanged.equals("slider")) {
      if(lifterPos>liftLimit) {
        lifter.set(0);
      }
      else if(driverInverse) { //placing balls
        if(sliderHeight<= 1 && lifterPos<homeHeight){
          lifter.set(0.65*sliderSpeed);
          s0.set(true);
        }
        else if(sliderHeight <= 1 && lifterPos>homeHeight){
          lifter.set(-0.35);
          s0.set(true);
        }
        else if(sliderHeight <= 2 && lifterPos<ballHeight1) {
          lifter.set(0.65*sliderSpeed);
          s0.set(true);
          intake.set(0.65);
        }
        else if(sliderHeight <= 2 && lifterPos>ballHeight1){
          lifter.set(-0.35*sliderSpeed); 
        }
        else if(sliderHeight <= 3 && lifterPos<ballHeight2){
          lifter.set(0.65*sliderSpeed);  
        }
        else if(sliderHeight <= 3 && lifterPos>ballHeight2){
          lifter.set(-0.35*sliderSpeed);         
        }
        else if(sliderHeight <= 4 && lifterPos<ballHeight3){
          lifter.set(0.65*sliderSpeed);          
        }
        else if(sliderHeight <= 4 && lifterPos>ballHeight3){
          lifter.set(-0.35*sliderSpeed);  
        }
        else {
          lifter.set(0);
        }
      }
      else { //placing hatches
        if(sliderHeight <= 1 && lifterPos<homeHeight){
          lifter.set(0.65*sliderSpeed);
          s0.set(true);
        }
        else if(sliderHeight <= 1 && lifterPos>homeHeight){
          lifter.set(-0.35*sliderSpeed);
          s0.set(true);
        }
        else if(sliderHeight <= 2 && lifterPos<panelHeight1){
          lifter.set(0.65*sliderSpeed);
          s0.set(true);
        }
        else if(sliderHeight <= 2 && lifterPos>panelHeight1){
          lifter.set(-0.35*sliderSpeed);
        }
        else if(sliderHeight <= 3 && lifterPos<panelHeight2){
          lifter.set(0.65*sliderSpeed);
        }
        else if(sliderHeight <= 3 && lifterPos>panelHeight2){
          lifter.set(-0.35*sliderSpeed);
        }
        else if(sliderHeight <= 4 && lifterPos<panelHeight3){
          lifter.set(0.65*sliderSpeed);
        }
        else if(sliderHeight <= 4 && lifterPos>panelHeight3){
          lifter.set(-0.35*sliderSpeed);
        }
        else {
          lifter.set(0);
         }
      }
    }
  }
  private void setTargetHeight() {
    if(driverInverse) { //placing balls
      if(sliderHeight <= 1){
        targetHeight = homeHeight;
      }
      else if(sliderHeight <= 2){
        targetHeight = ballHeight1;
      }
      else if(sliderHeight <= 3){
        targetHeight = ballHeight2;
      }
      else if(sliderHeight <= 4){
        targetHeight = ballHeight3;
      }
    }
    else { //placing hatches
      if(sliderHeight <= 1){
        targetHeight = homeHeight;
      }
      else if(sliderHeight <= 2){
        targetHeight = panelHeight1;
      }
      else if(sliderHeight <= 3){
        targetHeight = panelHeight2;
      }
      else if(sliderHeight <= 4){
        targetHeight = panelHeight2;
      }
    }
  }
}