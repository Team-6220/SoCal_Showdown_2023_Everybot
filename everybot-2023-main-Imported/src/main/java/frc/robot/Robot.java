// TODO: 
/* 
//  * 
stops arm when released
 * Retracting the arm have issue -- voltage too low
 *  PID LOOP!!!
 * 
 * 
 * binding new keys
 * 
 * LEARN:
 * Suffle board API 
 *  EVERYTHING AUTONOMOUS
 * 
*/
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.TalonSRXConfiguration;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxRelativeEncoder.Type;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import java.util.function.Supplier;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import edu.wpi.first.math.controller.PIDController;

public class Robot extends TimedRobot {
    /*
     * Autonomous selection options.
     */
    private static final String kNothingAuto = "do nothing";
    private static final String kConeAuto = "cone";
    private static final String kCubeAuto = "cube";
    private String m_autoSelected;
    private final SendableChooser<String> m_chooser = new SendableChooser<>();

    // arm PID variables declaratoin
    // TODO: Don't forget to
    /*
     * s
     * e
     * t
     * 
     * s
     * t
     * a
     * t
     * i
     * c
     * value for P,I,D after done!!
     * (Change them to static final at the same time)
     */
    double kP = 1,kI =  0,kD = 1, kFF = 0, kIZone = 0, f = 0, errorSum = 0, lastError = 0, lastTimestamp = 0;
    SparkMaxPIDController armPidController;
    PIDController pidController;
    RelativeEncoder armEncoder;
    // for adujusting PID
    private NetworkTableEntry pidPEntry;
    private NetworkTableEntry pidIEntry;
    private NetworkTableEntry pidDEntry;
    private NetworkTableEntry pidFFEntry; // Forward Feed
    private NetworkTableEntry pidIZoneEntry; // IZone

    
    private final XboxController m_controller = new XboxController(0);
    private final Joystick m_js = new Joystick(1);
    // FIXME: figure out arm position variables
    // Arm position variables
    private final double highCone = -19.21422004;
    // private final double zeron = -19.21422004;//zero
    // private final double flatn = ;//flat
    // private final double zerop = ;
    // private final double vert = ;
    //private final double forty5 = 0;//notc
    // TODO: get the raw encoder value done.
    // private final double pickupn = ;
    // private final double pickupp = ;//pickup
    // private final double midn = ;//mid cone
    // private final double midp = ;
    private final double highn = -19.21422004;//high cone
    // private final double highp = ;
    // private final double stationp = ;
    // private final double stationn = ;
    // private final double hovern = ;
    // private final double hoverp = ;
    // private final double siden = ;
    // private final double sidep = ;
    // private final double autocubehigh = ;
    // private final double telecubehigh = ;

    // private final GenericEntry armAngle, armOutput; // for shuffle board
    // TODO: change all these limits and stuff! (These were straight from sean's
    // code, and Sam haven't tested them yet. DELETE THAT AFTER WHOEVER TESTED IT
    // AND FOUND THE BEST ANGLE)
    // in degrees? not sure
    public static final double k_SOFT_LIMIT = 50;

    // Drivetrain variables declaration
    /*
     * Drive motor controller instances.
     * 
     * Change the id's to match your robot.
     * Change kBrushed to kBrushless if you are using NEO's.
     * Use the appropriate other class if you are using different controllers.
     */
    // CANSparkMax driveLeftSpark = new CANSparkMax(1, MotorType.kBrushed);
    // CANSparkMax driveRightSpark = new CANSparkMax(2, MotorType.kBrushed);
    // TalonSRX motorLeftprimary = new TalonSRX(9);
    // TalonSRX motorLeftfollwer = new TalonSRX(0);
    // // VictorSPX driveLeftVictor = new VictorSPX(3);
    // TalonSRX motorRightprimary = new TalonSRX(8);

    // VictorSPX motorRightfollower = new VictorSPX(2);

    /*
     * Mechanism motor controller instances.
     * 
     * Like the drive motors, set the CAN id's to match your robot or use different
     * motor controller classses (TalonFX, TalonSRX, Spark, VictorSP) to match your
     * robot.
     * 
     * The arm is a NEO on Everybud.
     * The intake is a NEO 550 on Everybud.
     */
    CANSparkMax arm = new CANSparkMax(1, MotorType.kBrushless);
    // CANSparkMax intake = new CANSparkMax(4, MotorType.kBrushless);

    /**
     * The starter code uses the most generic joystick class.
     * 
     * The reveal video was filmed using a logitech gamepad set to
     * directinput mode (switch set to D on the bottom). You may want
     * to use the XBoxController class with the gamepad set to XInput
     * mode (switch set to X on the bottom) or a different controller
     * that you feel is more comfortable.
     */
    Joystick j = new Joystick(0);
    Joystick armController = new Joystick(1);
    /*
     * Magic numbers. Use these to adjust settings.
     */

    /**
     * How many amps the arm motor can use.
     */
    static final int ARM_CURRENT_LIMIT_A = 20;

    /**
     * Percent output to run the arm up/down at
     */
    static final double ARM_OUTPUT_POWER = 0.7;
    
    //FIXME: bind the slider to chnage sensitivity 
    static final double ARM_JOYSTICK_SENSITIVITY = 1.3;

    /**
     * How many amps the intake can use while picking up
     */
    static final int INTAKE_CURRENT_LIMIT_A = 25;

    /**
     * How many amps the intake can use while holding
     */
    static final int INTAKE_HOLD_CURRENT_LIMIT_A = 0;

    /**
     * Percent output for intaking
     */
    static final double INTAKE_OUTPUT_POWER = 15;

    /**
     * Percent output for holding
     */
    static final double INTAKE_HOLD_POWER = 0;

    /**
     * TODO:Time to extend or retract arm in auto
     */
    static final double ARM_EXTEND_TIME_S = 2.0;

    /**
     * TODO:Time to throw game piece in auto
     */
    static final double AUTO_THROW_TIME_S = 0.375;

    /**
     * TODO: Time to drive back in auto
     */
    static final double AUTO_DRIVE_TIME = 6.0;

    /**
     * TODO:Speed to drive backwards in auto
     */
    static final double AUTO_DRIVE_SPEED = -0.25;

    /**
     * This method is run once when the robot is first started up.
     */

     ShuffleboardTab tab = Shuffleboard.getTab("PID");
    @Override
    public void robotInit() {

         // Arm (motor, encoder, and PID loop) setup Start
        /*
         * Set the arm and intake to brake mode to help hold position.
         * If either one is reversed, change that here too. Arm out is defined
         * as positive, arm in is negative.
         */
        arm.setInverted(true);
        arm.setIdleMode(IdleMode.kBrake);
        arm.setSmartCurrentLimit(ARM_CURRENT_LIMIT_A);

        // this.armPidController = arm.getPIDController();

        // this.tab.add("P", 0.0).withWidget(BuiltInWidgets.kNumberSlider);
        // this.tab.add("I", 0.0).withWidget(BuiltInWidgets.kNumberSlider);
        // this.tab.add("D", 0.0).withWidget(BuiltInWidgets.kNumberSlider);
        // this.tab.add("FF", 0.0).withWidget(BuiltInWidgets.kNumberSlider); // Forward Feed
        // this.tab.add("IZone", 0.0).withWidget(BuiltInWidgets.kNumberSlider); // IZone
        // Bing ai changes my life? :)
        this.armPidController.setP(kP);
        this.armPidController.setI(kI);
        this.armPidController.setD(kD);
        this.armPidController.setFF(0);
        this.armPidController.setIZone(0);

        // this.positions = positions;
        //this.telePidController = new PIDController(0.00, 0.00, 0.00);
        // this.telePidController = new PIDController(.08, 0, 0);
        pidController = new PIDController(0.05, 0.00, 0.00);
        //this.armPidController = new PIDController(0.00, 0.00, 0.00);
        //this.wristPidController = new PIDController(0.00, 0.00, 0.00);
        // this.wristPidController = new PIDController(0.004, 0.00, 0.00);

        this.armEncoder = this.arm.getEncoder(Type.kHallSensor, 42);
        // ALWAYS BURN FLASH this saves changes like idle mode or set inverted to the
        // spark max
        arm.burnFlash();
        // For shuffleboard
        // Uncomment these after learned shuffleboard API
        // this.armAngle = Shuffleboard.getTab("ATW").add("Arm Angle", 0).getEntry();
        // this.armOutput = Shuffleboard.getTab("ATW").add("Arm Motor Output",
        // 0).getEntry();
        // Arm setup end

        m_chooser.setDefaultOption("do nothing", kNothingAuto);
        m_chooser.addOption("cone and mobility", kConeAuto);
        m_chooser.addOption("cube and mobility", kCubeAuto);
        SmartDashboard.putData("Auto choices", m_chooser);

        // Motor setup code Start
        // Drive train setup code start
        TalonSRXConfiguration config = new TalonSRXConfiguration();
        config.peakCurrentLimit = 40; // the peak current, in amps
        config.peakCurrentDuration = 1500; // the time at the peak current before the limit triggers, in ms
        config.continuousCurrentLimit = 30; // the current to maintain if the peak limit is triggered
        // motorLeftprimary.configAllSettings(config); // apply the config settings; this selects the quadrature encoder
        // motorLeftfollwer.configAllSettings(config); // apply the config settings; this selects the quadrature encoder
        // motorRightprimary.configAllSettings(config);// apply the config settings; this selects the quadrature encoder

        // motorRightprimary.configFactoryDefault();
        // motorRightfollower.configFactoryDefault();

        // motorLeftfollwer.follow(motorLeftprimary);
        // motorRightfollower.follow(motorRightprimary);

        // motorRightprimary.setInverted(InvertType.InvertMotorOutput);
        // motorRightfollower.setInverted(InvertType.InvertMotorOutput);
        // motorLeftprimary.setInverted(InvertType.None);
        // motorLeftfollwer.setInverted(InvertType.FollowMaster);
        // // Drivetrain setup code end

        // intake.setInverted(false);
        // intake.setIdleMode(IdleMode.kBrake);
        // Motor setup end
    }

    /*
     * Tank drive
     */
    public void setDriveMotors(double left, double right) {
        SmartDashboard.putNumber("left track power (%)", left);
        SmartDashboard.putNumber("right track power (%)", right);

        // NOTE: change the dead zone?
        // TODO: uncomment
        // if (Math.abs(right) > 0.20) {
        //     motorRightprimary.set(ControlMode.PercentOutput, right);
        //     motorRightfollower.set(ControlMode.PercentOutput, right);
        // } else {
        //     motorRightprimary.set(ControlMode.PercentOutput, 0);
        //     motorRightfollower.set(ControlMode.PercentOutput, 0);
        // }
        // if (Math.abs(left) > 0.20) {
        //     motorLeftprimary.set(ControlMode.PercentOutput, left);
        //     motorLeftfollwer.set(ControlMode.PercentOutput, left);
        // } else {
        //     motorLeftprimary.set(ControlMode.PercentOutput, 0);
        //     motorLeftfollwer.set(ControlMode.PercentOutput, 0);
        // }
    }

    /**
     * Set the arm output power. Positive is out, negative is in.
     * 
     * @param input
     */
    // theARMPID
    public void armPIDCalculation(double positions, Supplier<Double> armAdjust){

        // double error = positions - armEncoder.getPosition();
        //     double output = 0.0375*error; // 0.015 is my kp!!!!
        //     arm.set(output);
        double dt = Timer.getFPGATimestamp() - lastTimestamp;


        double armSet  = positions + (armAdjust.get()*-10); 
        double armOutput = pidController.calculate(getArmPositionDegrees(),armSet);
        armOutput = (armOutput > .3)?.3:(armOutput< -.3)?-.3:armOutput;
        setArmMotor(armOutput);
    }
    public void setArmMotor(double input) {
        // Sean's code start

       

        // stop it from going too far

        // reduce input because adding the holding value could make it over 1
        input *= .9;
        // get holding output flips itself so we just add this
        // input += getArmHoldingOutput();
        // FIXME: ask someone: what does ^ mean?
        if (Math.abs(getArmPositionDegrees()) >= k_SOFT_LIMIT && !((getArmPositionDegrees() < 0) ^ (input < 0))) {
            input = 0;
        }
        // we only set the leader cuz the follower will just do the same
        arm.set(input);
        // updating the shuffle board output
        // armOutput.setDouble(input);

        // Sean's code end
        // arm.set(percent);
        // SmartDashboard.putNumber("arm power (%)", percent); //TODO: change the
        // percent to whatever!
        // SmartDashboard.putNumber("arm output", input);
        // SmartDashboard.putNumber("arm motor current (amps)", arm.getOutputCurrent());
        // SmartDashboard.putNumber("arm motor temperature (C)", arm.getMotorTemperature());
    }

    /**
     * Set the intake output power.
     * 
     * @param percent desired speed
     * @param amps    current limit
     */
    public void setIntakeMotor(double percent, int amps) {
        // intake.set(percent);
        // intake.setSmartCurrentLimit(amps);
        // SmartDashboard.putNumber("intake power (%)", percent);
        // SmartDashboard.putNumber("intake motor current (amps)", intake.getOutputCurrent());
        // SmartDashboard.putNumber("intake motor temperature (C)", intake.getMotorTemperature());
    }

    /**
     * This method is called every 20 ms, no matter the mode. It runs after
     * the autonomous and teleop specific period methods.
     */
    @Override
    public void robotPeriodic() {
        SmartDashboard.putNumber("Time (seconds)", Timer.getFPGATimestamp());
    }

    double autonomousStartTime;
    double autonomousIntakePower;

    // TODO: Autonomous code starts here!
    @Override
    public void autonomousInit() {
        // motorLeftprimary.setIdleMode(IdleMode.kBrake); commented out cause
        // setIdleMode causes an error
        // motorLeftfollwer.setNeutralMode(NeutralMode.Brake);
        // motorRightprimary.setIdleMode(IdleMode.kBrake);
        // motorRightfollower.setNeutralMode(NeutralMode.Brake);

        m_autoSelected = m_chooser.getSelected();
        System.out.println("Auto selected: " + m_autoSelected);

        if (m_autoSelected == kConeAuto) {
            autonomousIntakePower = INTAKE_OUTPUT_POWER;
        } else if (m_autoSelected == kCubeAuto) {
            autonomousIntakePower = -INTAKE_OUTPUT_POWER;
        }

        autonomousStartTime = Timer.getFPGATimestamp();
    }

    @Override
    public void autonomousPeriodic() {
        if (m_autoSelected == kNothingAuto) {
            setArmMotor(0.0);
            setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(0.0, 0.0);
            return;
        }

        double timeElapsed = Timer.getFPGATimestamp() - autonomousStartTime;

        if (timeElapsed < ARM_EXTEND_TIME_S) {
            setArmMotor(ARM_OUTPUT_POWER);
            setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(0.0, 0.0);
        } else if (timeElapsed < ARM_EXTEND_TIME_S + AUTO_THROW_TIME_S) {
            setArmMotor(0.0);
            setIntakeMotor(autonomousIntakePower, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(0.0, 0.0);
        } else if (timeElapsed < ARM_EXTEND_TIME_S + AUTO_THROW_TIME_S + ARM_EXTEND_TIME_S) {
            setArmMotor(-ARM_OUTPUT_POWER);
            setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(0.0, 0.0);
        } else if (timeElapsed < ARM_EXTEND_TIME_S + AUTO_THROW_TIME_S + ARM_EXTEND_TIME_S + AUTO_DRIVE_TIME) {
            setArmMotor(0.0);
            setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(AUTO_DRIVE_SPEED, 0.0);
        } else {
            setArmMotor(0.0);
            setIntakeMotor(0.0, INTAKE_CURRENT_LIMIT_A);
            setDriveMotors(0.0, 0.0);
        }
    }

    /**
     * Used to remember the last game piece picked up to apply some holding power.
     */
    static final int CONE = 1;
    static final int CUBE = 2;
    static final int NOTHING = 3;
    int lastGamePiece;

    @Override
    public void teleopInit() {
        // TODO: should I have a idlemode and nutralmode???? coast or brake? I DON"T
        // WANT THE ROBOT TO MOVE???
        // motorLeftprimary.setIdleMode(IdleMode.kCoast);
        // motorLeftprimary.setNeutralMode(NeutralMode.Coast);
        // motorLeftfollwer.setNeutralMode(NeutralMode.Coast);
        // // motorRightprimary.setIdleMode(IdleMode.kCoast);
        // motorRightprimary.setNeutralMode(NeutralMode.Coast);
        // motorRightfollower.setNeutralMode(NeutralMode.Coast);
        // lastGamePiece = NOTHING;
        errorSum = 0;
        lastError = 0;
        lastTimestamp = Timer.getFPGATimestamp();
    }

    @Override
    // TODO: CHANGE THE KEY BINDINGS!!!!!!!
    public void teleopPeriodic() {
        // Code from Bing AI start
        // TODO: bing AI code
        // double p = Shuffleboard.getTab("PID").getLayout("PID",BuiltInWidgets.kNumberSlider.toString()).getEntry().getDouble(0.0);
        // double i = Shuffleboard.getTab("PID").getLayout("PID").getWidget("I").getEntry().getDouble(0.0);
        // double d = Shuffleboard.getTab("PID").getLayout("PID").getWidget("D").getEntry().getDouble(0.0);
        // double ff = Shuffleboard.getTab("PID").getLayout("PID").getWidget("FF").getEntry().getDouble(0.0); // Forward Feed
        // double izone = Shuffleboard.getTab("PID").getLayout("PID").getWidget("IZone").getEntry().getDouble(0.0); // IZone

        // kP = Shuffleboard.getTab("PID").add("P", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry().getDouble(0.0);
        // kI = Shuffleboard.getTab("PID").add("I", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry().getDouble(0.0);
        // kD = Shuffleboard.getTab("PID").add("D", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry().getDouble(0.0);
        // kFF = Shuffleboard.getTab("PID").add("FF", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry().getDouble(0.0); // Forward Feed
        // kIZone = Shuffleboard.getTab("PID").add("IZone", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry().getDouble(0.0); // IZone
        
        // armPidController.setP(kP);
        // armPidController.setI(kI);
        // armPidController.setD(kD);
        // armPidController.setFF(kFF); // Forward Feed
        // armPidController.setIZone(kIZone); // IZone

        // Code from Bing AI end

        // double armPower;
        // all the way down 0.6 percent
        // TODO: sensitivity?
        // FIXME: PID START HERE
        // if (armController.getRawButtonPressed(2)){
        //     armPIDCalculation(highn, (Double)armEncoder.getPosition());
        // }
        
        double error = highn - armEncoder.getPosition();
        kP = 0.0375;
        kI = 0.03;
        kD = 0.008;
        // -17.57139015197754;
        // goal -19.21422004
        
            double output = 0.0375*error; // 0.015 is my kp!!!!
            double dt = Timer.getFPGATimestamp() - lastTimestamp;
            double errorRate = (error - lastError) / dt;
            double outputSpeed = kP * error + kI * errorSum + kD * errorRate;
            arm.set(output);

            lastTimestamp = Timer.getFPGATimestamp();
            lastError = error;
            
        // if(Math.abs(armController.getRawAxis(1))>0.1){
        //     SmartDashboard.putNumber("Arm output value", (double) armController.getRawAxis(1)/ARM_JOYSTICK_SENSITIVITY);
        //     SmartDashboard.putNumber("arm voltage in v", (Double)arm.getBusVoltage());
        //     SmartDashboard.putNumber("Arm output current in ams", arm.getOutputCurrent());
        //     arm.set((Double)armController.getRawAxis(1)/ARM_JOYSTICK_SENSITIVITY);
        // }
        // else{
        //     arm.set(0.0);
        // }
        // ARM_JOYSTICK_SENSITIVITY = armController.get
        // if(Math.abs(armController.getRawAxis(1))< ARM_OUTPUT_POWER && Math.abs(armController.getRawAxis(1))>0.1){
        //     SmartDashboard.putNumber("Raw arm controller value", armController.getRawAxis(1));
        //     SmartDashboard.putNumber("arm voltage in v", (Double)arm.getBusVoltage());
        //     SmartDashboard.putNumber("Arm output current in ams", arm.getOutputCurrent());
        //     arm.set((Double)armController.getRawAxis(1));
        //     // System.out.println(armController.getRawAxis(1));
        // }
        // else{
        //     arm.set(0.0);
        // }
        // arm.set(armController.getRawAxis(1));
        // if (j.getRawButton(7)) {
        //     // lower the arm
        //     arm.set(-ARM_OUTPUT_POWER);
        //     // System.out.println(armEncoder);
        // } else if (j.getRawButton(5)) {
        //     // raise the arm
        //     arm.set(ARM_OUTPUT_POWER);
        //     // System.out.println(armEncoder);
        // } else {
        //     // do nothing and let it sit where it is
        //     arm.set(0.0);
        // }
        SmartDashboard.putNumber("Raw encoder value Spark max arm", armEncoder.getPosition());
        // setArmMotor(armPower);
        //zero
        // FIXME: getRawbutton pressed here
        // if (j.getRawButtonPressed(7)){
        //     arm.set(-ARM_OUTPUT_POWER);
        //     //     armPIDCalculation(
        // //     zeron,
        // //     () -> m_js.getThrottle()
        // // );
        // }


        // new Trigger(() -> m_js2.getRawButtonPressed(7)).onTrue(new armPIDCalculation(
        //     zeron,
        //     () -> m_js.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButtonPressed(8)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     flatn,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(7)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     zerop,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButtonPressed(3)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     pickupn,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // //mid cone (negative direction)
        // new Trigger(() -> m_js2.getRawButtonPressed(4)).onTrue(
        //     new ATWPositionCmd(atwSubsystem,
        //      autocubehigh,
        //      () -> m_js.getThrottle(),
        //      () -> m_js2.getThrottle())
        // );
        
        // //high cone (negative direction)
        // new Trigger(() -> m_js2.getRawButtonPressed(5)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     highn,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(3)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     pickupp,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(4)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     telecubehigh,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(5)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     highp,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButtonPressed(6)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     stationn,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(6)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     stationp,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(11)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     hoverp,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(12)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     sidep,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButtonPressed(11)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     hovern,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButtonPressed(12)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     siden,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js2.getRawButton(10)).onTrue(new ATPositionCmd(
        //   atwSubsystem, zeron, () -> m_js.getThrottle(),
        //   () -> m_js2.getThrottle())
        // );
        // new Trigger(() -> m_js2.getRawButtonPressed(9)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     midn,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));
        // new Trigger(() -> m_js.getRawButtonPressed(9)).onTrue(new ATWPositionCmd(
        //     atwSubsystem,
        //     midp,
        //     () -> m_js.getThrottle(),
        //     () -> m_js2.getThrottle()
        // ));

        double intakePower;
        int intakeAmps;
        // if (j.getRawButton(8)) {
        //     // cube in or cone out
        //     intakePower = INTAKE_OUTPUT_POWER;
        //     intakeAmps = INTAKE_CURRENT_LIMIT_A;
        //     lastGamePiece = CUBE;
        // } else if (j.getRawButton(6)) {
        //     // cone in or cube out
        //     intakePower = -INTAKE_OUTPUT_POWER;
        //     intakeAmps = INTAKE_CURRENT_LIMIT_A;
        //     lastGamePiece = CONE;
        // } else if (lastGamePiece == CUBE) {
        //     intakePower = INTAKE_HOLD_POWER;
        //     intakeAmps = INTAKE_HOLD_CURRENT_LIMIT_A;
        // } else if (lastGamePiece == CONE) {
        //     intakePower = -INTAKE_HOLD_POWER;
        //     intakeAmps = INTAKE_HOLD_CURRENT_LIMIT_A;
        // } else {
        //     intakePower = 0.0;
        //     intakeAmps = 0;
        // }
        // setIntakeMotor(intakePower, intakeAmps);

        /*
         * Negative signs here because the values from the analog sticks are backwards
         * from what we want. Forward returns a negative when we want it positive.
         */
        // setDriveM//otors(-j.getRawAxis(1), -j.getRawAxis(5));
    }

    // Sean's OG code for the arm
    // public void setArmMotors(double input){
    // //stop it from going too far

    // //reduce input because adding the holding value could make it over 1
    // input *= .9;
    // //get holding output flips itself so we just add this
    // //input += getArmHoldingOutput();
    // if(Math.abs(getArmPositionDegrees()) >= k_SOFT_LIMIT &&
    // !((getArmPositionDegrees() < 0) ^ (input < 0))){
    // input = 0;
    // }
    // //we only set the leader cuz the follower will just do the same
    // arm.set(input);
    // //updating the shuffle board output
    // // armOutput.setDouble(input);
    // }

    public double getArmPositionDegrees() {
        double angle = (armEncoder.getPosition() * 3);// Sean just played around with this so just use 3 as of 9/19/2023
        return angle + 0;
    }

    public double getArmHoldingOutput() {
        // first formula is just slope-intercept as .023 is the min and .1 is the max and it should increase linearly from there
        // FIXME: CHANGE THE FORMULA!!!!!! this is not accurate at ALL, so you must
        // chagne it before using it... otherwise it could potentially cause unwatned/severe damages.
        // double out = .023 + (getTelescopePosition()/TelescopeConstants.k_FULL_EXTENSION)*(.09-.023);
        // torque exerted by gravity changes with angle, this should account for that
        // out *= Math.sin(Math.abs(Math.toRadians(getArmPositionDegrees())));
        // out *= (getArmPositionDegrees() > 0)?-1:1;
        return 0.0;
    }

    public void stopArm() {
        // this will actually output the holding value not 0
        setArmMotor(0);
    }
}
