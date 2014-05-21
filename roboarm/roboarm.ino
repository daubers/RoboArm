#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include <EEPROM.h>

const int NUM_SERVOS = 5;

// called this way, it uses the default address 0x40
Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver();
int currentPosition[NUM_SERVOS];
int max_position[NUM_SERVOS];
int min_position[NUM_SERVOS];
int up_position[NUM_SERVOS];

int bytes_read = 0;
byte in[10];

void setup() {
  Serial.begin(9600);
  Serial.println("Welcome To RoboArm Control");
  pwm.begin();
  pwm.setPWMFreq(60);  // Analog servos run at ~60 Hz updates
  //check the eeprom has been initialised
  //if (is_eeprom_ready() == false){
    init_eeprom();
  //}
  //get the min/max positions for the servo's from the eeprom
  init_data();
  //set the arm upright, we'll need the servo positions for this from the eeprom
  set_start_pos();
  Serial.println("ready");
}

boolean is_eeprom_ready(){
  byte value = EEPROM.read(0);
  if (value == 0x12){
    return true;
  }
  else {
    return false;
  }
}

void init_eeprom(){
  Serial.println("Initialising EEPROM data");
  //clear it all out
  for (int i = 0; i < 512; i++)
   EEPROM.write(i, 0);
  //now we need to put in our datastructure
  EEPROM.write(0,0x12);
  //each servo has a max/min position, we'll default these to 150 and 600, it also has an upright position, which will default to 300
  //each of these require 2 bytes
  for (int i=0; i<NUM_SERVOS; i++){
    int pos = i*6;
    EEPROMWriteInt(pos+1, 150);
    EEPROMWriteInt(pos+3, 600);
    EEPROMWriteInt(pos+5, 400);
  }
}

void init_data(){
  Serial.println("Reading Saved Data");
  for (int i=0; i<NUM_SERVOS; i++){
    int pos = i*6;
    min_position[i] = EEPROMReadInt(pos+1);
    max_position[i] = EEPROMReadInt(pos+3);
    up_position[i] = EEPROMReadInt(pos+5);
  }
}

void set_start_pos(){
  Serial.println("Setting Start Position");
   for (int i=0; i<NUM_SERVOS; i++){
      currentPosition[i] = up_position[i];
      pwm.setPWM(i, 0, up_position[i]);
   }   
}

void move_servo(int servo, int pos){
   if (pos > min_position[servo] && pos < max_position[servo]){
     currentPosition[servo] = pos;
     pwm.setPWM(servo, 0, (uint16_t) pos);
   }
}

void set_servo_max(int servo, int max_pos){
  if (servo <= NUM_SERVOS){
    int pos = servo*6;
    EEPROMWriteInt(pos+3, max_pos);
    max_position[servo] = max_pos;
  }
}

void set_servo_min(int servo, int min_pos){
  if (servo <= NUM_SERVOS){
    int pos = servo*6;
    EEPROMWriteInt(pos+1, min_pos);
    min_position[servo] = min_pos;
  }
}

//taken from http://forum.arduino.cc/index.php/topic,37470.0.html
void EEPROMWriteInt(int p_address, int p_value)
      {
      byte lowByte = ((p_value >> 0) & 0xFF);
      byte highByte = ((p_value >> 8) & 0xFF);

      EEPROM.write(p_address, lowByte);
      EEPROM.write(p_address + 1, highByte);
      }

//This function will read a 2 byte integer from the eeprom at the specified address and address + 1
unsigned int EEPROMReadInt(int p_address)
      {
      byte lowByte = EEPROM.read(p_address);
      byte highByte = EEPROM.read(p_address + 1);

      return ((lowByte << 0) & 0xFF) + ((highByte << 8) & 0xFF00);
      }

void loop(){
    
    
    if (Serial.available() > 0) {
      // read the incoming byte:
      byte incomingByte = Serial.read();
      //Serial.print(incomingByte);
      if (incomingByte == ';'){
        // say what you got:
        process_command(in);
        bytes_read = 0;
        for (int i=0; i<=10; i++){
          in[i] = 0x0;
        }
      }
      else{
        in[bytes_read] = incomingByte;
        bytes_read++;
      }
    }
}

void process_command(byte* bytes){
  int command = bytes[0]-48;
  if (command == 0){
    int servo = bytes[2]-48;
    int pos = (((bytes[4]-48)*100)+((bytes[5]-48)*10)+(bytes[6]-48));
    Serial.print("Servo: ");
    Serial.print(servo);
    Serial.print("\n");
    Serial.print("Position: ");
    Serial.print(pos);
    Serial.print("\n"); 
    move_servo(servo,pos);
  }
  else if (command==1){
    int servo = bytes[2]-48;
    int max_pos = (((bytes[4]-48)*100)+((bytes[5]-48)*10)+(bytes[6]-48));
    set_servo_max(servo, max_pos);
    Serial.print("Maximum Position for servo ");
    Serial.print(servo);
    Serial.print(" set to ");
    Serial.print(max_pos);
    Serial.println();
  }
  else if (command==2){
    int servo = bytes[2]-48;
    int min_pos = (((bytes[4]-48)*100)+((bytes[5]-48)*10)+(bytes[6]-48));
    set_servo_min(servo, min_pos);
    Serial.print("Minimum Position for servo ");
    Serial.print(servo);
    Serial.print(" set to ");
    Serial.print(min_pos);
    Serial.println();
  }
  else if (command==3){
    Serial.println("Servo,Current Position,Minimum Position,Maximum Position,Up Position");
    for (int i=0; i<NUM_SERVOS; i++){
      Serial.print(i);
      Serial.print(",");
      Serial.print(currentPosition[i]);
      Serial.print(",");
      Serial.print(min_position[i]);
      Serial.print(",");
      Serial.print(max_position[i]);
      Serial.print(",");
      Serial.print(up_position[i]);
      Serial.println();
    }
  }
  else if (command==4){
    //We want a machine readable response from these, so responses are in the form
    //subcommand (1byte) data tt (end of data)
    int subcommand = bytes[1]-48;
    if (subcommand==1) {
      //get current positions
      Serial.print("1");
      for (int i=0; i<NUM_SERVOS; i++){
        Serial.print(i);
        Serial.print(",");
        Serial.print(currentPosition[i]);
        Serial.print(",");
        Serial.print(min_position[i]);
        Serial.print(",");
        Serial.print(max_position[i]);
        Serial.print(",");
        Serial.print(up_position[i]);
        Serial.print("\t");
      }
      Serial.print("tt");
    }
  }
}
