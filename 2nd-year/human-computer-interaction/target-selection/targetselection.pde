// Bakeoff #2 - Seleção de Alvos e Fatores Humanos //<>// //<>//
// IPM 2019-20, Semestre 2
// Bake-off: durante a aula de lab da semana de 20 de Abril
// Submissão via Twitter: exclusivamente no dia 24 de Abril, até às 23h59

// Processing reference: https://processing.org/reference/

import java.util.Collections;

// Target properties
float PPI, PPCM;
float SCALE_FACTOR;
float TARGET_SIZE;
float TARGET_PADDING, MARGIN, LEFT_PADDING, TOP_PADDING;

// Study properties
ArrayList<Integer> trials  = new ArrayList<Integer>();    // contains the order of targets that activate in the test
int trialNum               = 0;                           // the current trial number (indexes into trials array above)
final int NUM_REPEATS      = 3;                           // sets the number of times each target repeats in the test - FOR THE BAKEOFF NEEDS TO BE 3!
boolean ended              = false;
ArrayList<Float> fitts     = new ArrayList<Float>();

// Performance variables
int startTime              = 0;      // time starts when the first click is captured
int finishTime             = 0;      // records the time of the final click
int hits                   = 0;      // number of successful clicks
int misses                 = 0;      // number of missed clicks

// Animation
int animStartTime = 0;
int animDur = 80;

// Class used to store properties of a target
class Target
{
  int x, y;
  float w;
  
  Target(int posx, int posy, float twidth) 
  {
    x = posx;
    y = posy;
    w = twidth;
  }
}

// Setup window and vars - runs once
void setup()
{
  //size(900, 900);              // window size in px (use for debugging)
  fullScreen();                // USE THIS DURING THE BAKEOFF!
  
  SCALE_FACTOR    = 1.0 / displayDensity();            // scale factor for high-density displays
  String[] ppi_string = loadStrings("ppi.txt");        // The text from the file is loaded into an array.
  PPI            = float(ppi_string[1]);               // set PPI, we assume the ppi value is in the second line of the .txt
  PPCM           = PPI / 2.54 * SCALE_FACTOR;          // do not change this!
  TARGET_SIZE    = 1.5 * PPCM;                         // set the target size in cm; do not change this!
  TARGET_PADDING = 1.5 * PPCM;                         // set the padding around the targets in cm; do not change this!
  MARGIN         = 1.5 * PPCM;                         // set the margin around the targets in cm; do not change this!
  LEFT_PADDING   = width/2 - TARGET_SIZE - 1.5*TARGET_PADDING - 1.5*MARGIN;        // set the margin of the grid of targets to the left of the canvas; do not change this!
  TOP_PADDING    = height/2 - TARGET_SIZE - 1.5*TARGET_PADDING - 1.5*MARGIN;       // set the margin of the grid of targets to the top of the canvas; do not change this!
  
  noStroke();        // draw shapes without outlines
  frameRate(60);     // set frame rate

  // Text and font setup
  textFont(createFont("Arial", 16));    // sets the font to Arial size 16
  textAlign(CENTER);                    // align text
  
  randomizeTrials();    // randomize the trial order for each participant
}

// Updates UI - this method is constantly being called and drawing targets
void draw()
{
  if(hasEnded()) return; // nothing else to do; study is over
    
  background(0);       // set background to black

  // Print trial count
  fill(255);          // set text fill color to white
  text("Trial " + (trialNum + 1) + " of " + trials.size(), 50, 20);    // display what trial the participant is on (the top-left corner)

  // Draw targets
  for (int i = 0; i < 16; i++) drawTarget(i);
  if(trialNum < trials.size()-1)
  {
    if(trials.get(trialNum+1) != trials.get(trialNum))
    {
      drawArrow(trials.get(trialNum), trials.get(trialNum+1));
    }
    else
    {
      drawSelfArrow(trials.get(trialNum));
    }
  }
}

boolean hasEnded() {
  if(ended) return true;    // returns if test has ended before
   
  // Check if the study is over
  if (trialNum >= trials.size())
  {
    float timeTaken = (finishTime-startTime) / 1000f;     // convert to seconds - DO NOT CHANGE!
    float penalty = constrain(((95f-((float)hits*100f/(float)(hits+misses)))*.2f),0,100);    // calculate penalty - DO NOT CHANGE!
    
    printResults(timeTaken, penalty);    // prints study results on-screen
    ended = true;
  }
  
  return ended;
}

// Randomize the order in the targets to be selected
// DO NOT CHANGE THIS METHOD!
void randomizeTrials()
{
  for (int i = 0; i < 16; i++)             // 4 rows times 4 columns = 16 target
    for (int k = 0; k < NUM_REPEATS; k++)  // each target will repeat 'NUM_REPEATS' times
      trials.add(i);
  Collections.shuffle(trials);             // randomize the trial order
  
  System.out.println("trial order: " + trials);    // prints trial order - for debug purposes
}

// Print results at the end of the study
void printResults(float timeTaken, float penalty)
{
  background(0);       // clears screen
  
  fill(255);    //set text fill color to white
  text(day() + "/" + month() + "/" + year() + "  " + hour() + ":" + minute() + ":" + second() , 100, 20);   // display time on screen
  
  text("Finished!", width / 2, height / 4); 
  text("Hits: " + hits, width / 2, height / 4 + 20);
  text("Misses: " + misses, width / 2, height / 4 + 40);
  text("Accuracy: " + (float)hits*100f/(float)(hits+misses) +"%", width / 2, height / 4 + 60);
  text("Total time taken: " + timeTaken + " sec", width / 2, height / 4 + 80);
  text("Average time for each target: " + nf((timeTaken)/(float)(hits+misses),0,3) + " sec", width / 2, height / 4 + 100);
  text("Average time for each target + penalty: " + nf(((timeTaken)/(float)(hits+misses) + penalty),0,3) + " sec", width / 2, height / 4 + 140);
  text("Fitts Index of Performance", width/2, height/4+180);
  text("Target 1: ---", width*2/5, height/4+200);
  for (int i=1; i<16*NUM_REPEATS; i++)
    text("Target " + str(i+1) + ": " + str(fitts.get(i-1)), width*2/5+width*1/5*(i/24), height/4+200+((20*i)%(20*24)));
  
  saveFrame("results-######.png");    // saves screenshot in current folder
}

float log2(float x)
{
  return (log(x) / log(2));
}

float calcFitts()
{
  Target target = getTargetBounds(trials.get(trialNum+1)); //next target
  return log2(dist(mouseX, mouseY, target.x, target.y)/(target.w*2) + 1);
}

// Mouse button was released - lets test to see if hit was in the correct target
void mouseReleased() 
{
  if (trialNum >= trials.size()) return;      // if study is over, just return
  if (trialNum < trials.size()-1) fitts.add(calcFitts()); // calc next target's fitts id
  if (trialNum == 0) startTime = millis();    // check if first click, if so, start timer
  if (trialNum == trials.size()-1)          // check if final click
  {
    finishTime = millis();    // save final timestamp
    println("We're done!");
  }
  
  animStartTime = millis();
  
  Target target = getTargetBounds(trials.get(trialNum));    // get the location and size for the target in the current trial
  
  // Check to see if mouse cursor is inside the target bounds
  if(dist(target.x, target.y, mouseX, mouseY) < target.w/2)
  {
    System.out.println("HIT! " + trialNum + " " + (millis() - startTime));     // success - hit!
    hits++; // increases hits counter 
  }
  else
  {
    System.out.println("MISSED! " + trialNum + " " + (millis() - startTime));  // fail
    misses++;   // increases misses counter
  }

  trialNum++;   // move on to the next trial; UI will be updated on the next draw() cycle
}  

// For a given target ID, returns its location and size
Target getTargetBounds(int i)
{
  int x = (int)LEFT_PADDING + (int)((i % 4) * (TARGET_SIZE + TARGET_PADDING) + MARGIN);
  int y = (int)TOP_PADDING + (int)((i / 4) * (TARGET_SIZE + TARGET_PADDING) + MARGIN);
  
  return new Target(x, y, TARGET_SIZE);
}

// Draw target on-screen
// This method is called in every draw cycle; you can update the target's UI here //<>//
void drawTarget(int i)
{
  Target target = getTargetBounds(i);   // get the location and size for the circle with ID:i
  
  noStroke();
  alpha(100);
  
  
  // check whether current circle is the intended target
  if (trials.get(trialNum) == i) 
    {
      fill(255);
    }
      else if (trialNum+1<trials.size() && trials.get(trialNum+1)==i && trials.get(trialNum+1)!=trials.get(trialNum)) // check if next target
    {
      fill(0, 200, 255);
    }
    else
    {
      fill(0, 5, 102);           // fill dark gray
    }
  if(trialNum>0 && trials.get(trialNum-1)==i && millis()-animStartTime<animDur) {
    fill(0, 5, 102);
  }
    //<>//
  circle(target.x, target.y, target.w);   // draw target
}

void drawArrow(int num_orig, int num_dest) {
  Target target_orig = getTargetBounds(num_orig);
  Target target_dest = getTargetBounds(num_dest);
  int x1 = target_orig.x;
  int y1 = target_orig.y;
  int x2 = target_dest.x;
  int y2 = target_dest.y;
  stroke(255);
  strokeWeight(4);
  line(x1, y1, x2, y2);
  pushMatrix();
  translate(x2, y2);
  float a = atan2(x1-x2, y2-y1);
  rotate(a);
  line(0, 0, -10, -10);
  line(0, 0, 10, -10);
  popMatrix();
}

void drawSelfArrow(int num_orig) {
  Target target = getTargetBounds(num_orig);
  int x = target.x;
  int y = target.y;
  stroke(255);
  strokeWeight(4);
  noFill();
  ellipse(x, y-TARGET_SIZE/2, TARGET_SIZE*3/4, TARGET_SIZE);
  pushMatrix();
  translate(x+sqrt(TARGET_SIZE*2*TARGET_SIZE/16), y-sqrt(TARGET_SIZE*2*TARGET_SIZE/16));
  float a = PI/8;
  rotate(a);
  line(0, 0, -10, -10);
  line(0, 0, 10, -10);
  popMatrix();
} 
