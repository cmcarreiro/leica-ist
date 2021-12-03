// Bakeoff #3 - Escrita de Texto em Smartwatches
// IPM 2019-20, Semestre 2
// Entrega: exclusivamente no dia 22 de Maio, até às 23h59, via Discord

// Processing reference: https://processing.org/reference/

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.List;

// Screen resolution vars;
float PPI, PPCM;
float SCALE_FACTOR;

// Finger parameters
PImage fingerOcclusion;
int FINGER_SIZE;
int FINGER_OFFSET;

// Arm/watch parameters
PImage arm;
int ARM_LENGTH;
int ARM_HEIGHT;

// Arrow parameters
PImage delete, space, enter;

// Study properties
String[] phrases;                   // contains all the phrases that can be tested
int NUM_REPEATS            = 2;     // the total number of phrases to be tested
int currTrialNum           = 0;     // the current trial number (indexes into phrases array above)
String currentPhrase       = "";    // the current target phrase
String currentTyped        = "";    // what the user has typed so far
String[] typed = new String[NUM_REPEATS];

String suggWord = "";
List<String> alreadySugg = new ArrayList<String>(); 
String[] freqList;

char currLetter = ' ';
//int currKey = 0;
int lastClickTime = 0;
int maxClickTime = 400;

// Performance variables
float startTime            = 0;     // time starts when the user clicks for the first time
float finishTime           = 0;     // records the time of when the final trial ends
float lastTime             = 0;     // the timestamp of when the last trial was completed
float lettersEnteredTotal  = 0;     // a running total of the number of letters the user has entered (need this for final WPM computation)
float lettersExpectedTotal = 0;     // a running total of the number of letters expected (correct phrases)
float errorsTotal          = 0;     // a running total of the number of errors (when hitting next)

//Setup window and vars - runs once
void setup()
{
  //size(900, 900);
  fullScreen();
  textFont(createFont("Arial", 24));  // set the font to arial 24
  noCursor();                         // hides the cursor to emulate a watch environment
  
  // Load images
  arm = loadImage("arm_watch.png");
  fingerOcclusion = loadImage("finger.png");
  delete = loadImage("delete.png");
  space = loadImage("space.png");
  enter = loadImage("enter.png");
  
  // Load phrases
  phrases = loadStrings("phrases.txt");                       // load the phrase set into memory
  Collections.shuffle(Arrays.asList(phrases), new Random());  // randomize the order of the phrases with no seed
  
  freqList = loadStrings("count_1w.txt");
  
  // Scale targets and imagens to match screen resolution
  SCALE_FACTOR = 1.0 / displayDensity();          // scale factor for high-density displays
  String[] ppi_string = loadStrings("ppi.txt");   // the text from the file is loaded into an array.
  PPI = float(ppi_string[1]);                     // set PPI, we assume the ppi value is in the second line of the .txt
  PPCM = PPI / 2.54 * SCALE_FACTOR;               // do not change this!
  
  FINGER_SIZE = (int)(11 * PPCM);
  FINGER_OFFSET = (int)(0.8 * PPCM);
  ARM_LENGTH = (int)(19 * PPCM);
  ARM_HEIGHT = (int)(11.2 * PPCM);
  
  arm.resize(ARM_LENGTH, ARM_HEIGHT);
  fingerOcclusion.resize(FINGER_SIZE, FINGER_SIZE);
}

void draw()
{ 
  // Check if we have reached the end of the study
  if (finishTime != 0)  return;
 
  background(255);                                                         // clear background
  
  // Draw arm and watch background
  imageMode(CENTER);
  image(arm, width/2, height/2);
  
  // Check if we just started the application
  if (startTime == 0 && !mousePressed)
  {
    fill(0);
    textAlign(CENTER);
    text("Tap to start time!", width/2, height/2);
  }
  else if (startTime == 0 && mousePressed) nextTrial();                    // show next sentence
  
  // Check if we are in the middle of a trial
  else if (startTime != 0)
  {
    textAlign(LEFT);
    fill(100);
    text("Phrase " + (currTrialNum + 1) + " of " + NUM_REPEATS, width/2 - 4.0*PPCM, height/2 - 8.1*PPCM);   // write the trial count
    text("Target:    " + currentPhrase, width/2 - 4.0*PPCM, height/2 - 7.1*PPCM);                           // draw the target string
    fill(0);
    text("Entered:  " + currentTyped + "|", width/2 - 4.0*PPCM, height/2 - 6.1*PPCM);                      // draw what the user has entered thus far 
    
    // Draw very basic ACCEPT button - do not change this!
    textAlign(CENTER);
    noStroke();
    fill(0, 250, 0);
    rect(width/2 - 2*PPCM, height/2 - 5.1*PPCM, 4.0*PPCM, 2.0*PPCM);
    fill(0);
    text("ACCEPT >", width/2, height/2 - 4.1*PPCM);
    
    // Draw screen areas
    // simulates text box - not interactive
    noStroke();
    fill(180);
    rect(width/2 - 2*PPCM, height/2 - 2*PPCM, 4*PPCM, PPCM);
    textAlign(CENTER);
    fill(0);
    
    
    
    
    if(currLetter!=' ' && millis()-lastClickTime>maxClickTime) {
      currLetter = ' '; 
      suggWord = newSuggestion();
    }
    
    textFont(createFont("Arial", 26));  // set the font to arial 24
    text(suggWord, width/2, height/2 - 1.3 * PPCM);             // draw current letter
    
    
    // THIS IS THE ONLY INTERACTIVE AREA (4cm x 4cm); do not change size
    stroke(0, 255, 0);
    noFill();
    rect(width/2 - 2*PPCM, height/2 - PPCM, 4.0*PPCM, 3.0*PPCM);
    
    stroke(0);
    
    rect(width/2 - PPCM, height/2 - PPCM, PPCM, PPCM, 5);
    rect(width/2, height/2 - PPCM, PPCM, PPCM, 5);
    
    rect(width/2 - 2*PPCM, height/2, PPCM, PPCM, 5);
    rect(width/2 - PPCM, height/2, PPCM, PPCM, 5);
    rect(width/2, height/2, PPCM, PPCM, 5);
    
    rect(width/2 - 2*PPCM, height/2 + PPCM, PPCM, PPCM, 5);
    rect(width/2 - PPCM, height/2 + PPCM, PPCM, PPCM, 5);
    rect(width/2, height/2 + PPCM, PPCM, PPCM, 5);
    
    //orange
    fill(255, 165, 0);
    rect(width/2 - 2*PPCM, height/2 - PPCM, PPCM, PPCM, 5);
    rect(width/2 + PPCM, height/2 - PPCM, PPCM, PPCM, 5);
    rect(width/2 + PPCM, height/2, PPCM, 2*PPCM, 5);
    
    
    
    textFont(createFont("Arial", 25));
    textAlign(CENTER);
    fill(0);
    text("abc", width/2 - 0.5*PPCM, height/2 - 0.5*PPCM);
    text("def", width/2 + 0.5*PPCM, height/2 - 0.5*PPCM);
    text("ghi", width/2 - 1.5*PPCM, height/2 + 0.5*PPCM);
    text("jkl", width/2 - 0.5*PPCM, height/2 + 0.5*PPCM);
    text("mno", width/2 + 0.5*PPCM, height/2 + 0.5*PPCM);
    text("pqrs", width/2 - 1.5*PPCM, height/2 + 1.5*PPCM);
    text("tuv", width/2 - 0.5*PPCM, height/2 + 1.5*PPCM);
    text("wxyz", width/2 + 0.5*PPCM, height/2 + 1.5*PPCM);
    
    
    // Draw next and previous arrows
    noFill();
    imageMode(CORNER);
    image(space, width/2 - 2*PPCM, height/2 - PPCM, PPCM, PPCM);
    image(delete, width/2 + PPCM, height/2 - PPCM, PPCM, PPCM);
    image(enter, width/2 + PPCM, height/2, PPCM, 2*PPCM);
  }
  
  // Draw the user finger to illustrate the issues with occlusion (the fat finger problem)
  imageMode(CORNER);
  image(fingerOcclusion, mouseX - FINGER_OFFSET, mouseY - FINGER_OFFSET);
}

// Check if mouse click was within certain bounds
boolean didMouseClick(float x, float y, float w, float h)
{
  return (mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h);
}


String newSuggestion() {
  if(currentTyped.length()>0 && currentTyped.charAt(currentTyped.length() - 1) != ' ') {
    String[] currTypedParts = currentTyped.split(" ");
    String lastWord = currTypedParts[currTypedParts.length - 1];
    String[] pieces;
    String word;
    for(int i=0; i < freqList.length; i++) {
      pieces = split(freqList[i], TAB);
      word = pieces[0];
      if(word.startsWith(lastWord) && !alreadySugg.contains(word)) {
        alreadySugg.add(word);
        return word;
      }
    }
  }
  return "";
}




void mousePressed() {
  if (mouseButton == LEFT) {
    
    //accept
    if (didMouseClick(width/2 - 2*PPCM, height/2 - 5.1*PPCM, 4.0*PPCM, 2.0*PPCM)) {
      suggWord = "";
      alreadySugg.clear();
      nextTrial();                         // Test click on 'accept' button - do not change this!
    }
    
    //on keyboard
    else if(didMouseClick(width/2 - 2.0*PPCM, height/2 - 1.0*PPCM, 4.0*PPCM, 3.0*PPCM))  // Test click on 'keyboard' area - do not change this condition! 
    {
      // YOUR KEYBOARD IMPLEMENTATION NEEDS TO BE IN HERE! (inside the condition)
      
      lastClickTime = millis();
      
      //abc
      if (didMouseClick(width/2 - PPCM, height/2 - PPCM, PPCM, PPCM))
      {
        if(currLetter=='a' || currLetter=='b') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='c') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'a';
        }
        else currLetter = 'a';
        currentTyped += currLetter;
      }
      
      //def
      else if (didMouseClick(width/2, height/2 - PPCM, PPCM, PPCM))
      {
        if(currLetter=='d' || currLetter=='e') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='f') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'd';
        }
        else currLetter = 'd';
        currentTyped += currLetter;
      }
      
      //ghi
      else if (didMouseClick(width/2 - 2*PPCM, height/2, PPCM, PPCM))
      {
        if(currLetter=='g' || currLetter=='h') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='i') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'g';
        }
        else currLetter = 'g';
        currentTyped += currLetter;
      }
      
      //jkl
      else if (didMouseClick(width/2 - PPCM, height/2, PPCM, PPCM))
      {
        if(currLetter=='j' || currLetter=='k') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='l') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'j';
        }
        else currLetter = 'j';
        currentTyped += currLetter;
      }
      
      //mno
      else if (didMouseClick(width/2, height/2, PPCM, PPCM))
      {
        if(currLetter=='m' || currLetter=='n') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='o') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'm';
        }
        else currLetter = 'm';
        currentTyped += currLetter;
      }
      
      //pqrs
      else if (didMouseClick(width/2 - 2*PPCM, height/2 + PPCM, PPCM, PPCM))
      {
        if(currLetter=='p' || currLetter=='q' || currLetter=='r') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='s') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'p';
        }
        else currLetter = 'p';
        currentTyped += currLetter;
      }
      
      //tuv
      else if (didMouseClick(width/2 - PPCM, height/2 + PPCM, PPCM, PPCM))
      {
        if(currLetter=='t' || currLetter=='u') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='v') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 't';
        }
        else currLetter = 't';
        currentTyped += currLetter;
      }
      
      //wxyz
      else if (didMouseClick(width/2, height/2 + PPCM, PPCM, PPCM))
      {
        if(currLetter=='w' || currLetter=='x' || currLetter=='y') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter++;
        }
        else if(currLetter=='z') {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          currLetter = 'w';
        }
        else currLetter = 'w';
        currentTyped += currLetter;
      }
      
      //space
      else if (didMouseClick(width/2 - 2*PPCM, height/2 - PPCM, PPCM, PPCM))
      {
        currentTyped+=" ";
        suggWord = "";
        alreadySugg.clear();
      }
      
      //delete
      else if (didMouseClick(width/2 + PPCM, height/2 - PPCM, PPCM, PPCM))
      {
        if(currentTyped.length() > 0) {
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
          if(alreadySugg.size() > 0) alreadySugg.remove(alreadySugg.size()-1);
          if(alreadySugg.size() > 0) alreadySugg.remove(alreadySugg.size()-1);
          suggWord = newSuggestion();
        }
      }
      
      //enter
      else if (didMouseClick(width/2 + PPCM, height/2 , PPCM, 2*PPCM)) {
        if(suggWord != "") {
          currentTyped = currentTyped.substring(0, currentTyped.lastIndexOf(" ")<0?0:currentTyped.lastIndexOf(" ")+1);
          currentTyped += suggWord + " ";
          suggWord = "";
          alreadySugg.clear();
        }
      }
      
      
      
      
      // Test click on keyboard area (to confirm selection)
      /*else
      {
        if (currentLetter == '_') currentTyped+=" ";                   // if underscore, consider that a space bar
        else if (currentLetter == '`' && currentTyped.length() > 0)    // if `, treat that as a delete command
          currentTyped = currentTyped.substring(0, currentTyped.length() - 1);
        else if (currentLetter != '`') currentTyped += currentLetter;  // if not any of the above cases, add the current letter to the typed string
      }*/
    }
    else System.out.println("debug: CLICK NOT ACCEPTED");
  }
}

void nextTrial()
{
  if (currTrialNum >= NUM_REPEATS) return;                                            // check to see if experiment is done
  
  // Check if we're in the middle of the tests
  else if (startTime != 0 && finishTime == 0)                                         
  {
    System.out.println("==================");
    System.out.println("Phrase " + (currTrialNum+1) + " of " + NUM_REPEATS);
    System.out.println("Target phrase: " + currentPhrase);
    System.out.println("Phrase length: " + currentPhrase.length());
    System.out.println("User typed: " + currentTyped);
    System.out.println("User typed length: " + currentTyped.length());
    System.out.println("Number of errors: " + computeLevenshteinDistance(currentTyped.trim(), currentPhrase.trim()));
    System.out.println("Time taken on this trial: " + (millis() - lastTime));
    System.out.println("Time taken since beginning: " + (millis() - startTime));
    System.out.println("==================");
    lettersExpectedTotal += currentPhrase.trim().length();
    lettersEnteredTotal += currentTyped.trim().length();
    errorsTotal += computeLevenshteinDistance(currentTyped.trim(), currentPhrase.trim());
    typed[currTrialNum] = currentTyped;
  }
  
  // Check to see if experiment just finished
  if (currTrialNum == NUM_REPEATS - 1)                                           
  {
    finishTime = millis();
    System.out.println("==================");
    System.out.println("Trials complete!"); //output
    System.out.println("Total time taken: " + (finishTime - startTime));
    System.out.println("Total letters entered: " + lettersEnteredTotal);
    System.out.println("Total letters expected: " + lettersExpectedTotal);
    System.out.println("Total errors entered: " + errorsTotal);

    float wpm = (lettersEnteredTotal / 5.0f) / ((finishTime - startTime) / 60000f);   // FYI - 60K is number of milliseconds in minute
    float freebieErrors = lettersExpectedTotal * .05;                                 // no penalty if errors are under 5% of chars
    float penalty = max(0, (errorsTotal - freebieErrors) / ((finishTime - startTime) / 60000f));
    float cps = lettersEnteredTotal / ((finishTime - startTime) / 1000f);
    
    System.out.println("Raw WPM: " + wpm);
    System.out.println("Freebie errors: " + freebieErrors);
    System.out.println("Penalty: " + penalty);
    System.out.println("WPM w/ penalty: " + (wpm - penalty));                         // yes, minus, because higher WPM is better: NET WPM
    System.out.println("CPS: " + cps);
    System.out.println("==================");
    
    printResults(wpm, freebieErrors, penalty, cps);
    
    currTrialNum++;                                                                   // increment by one so this mesage only appears once when all trials are done
    return;
  }

  else if (startTime == 0)                                                            // first trial starting now
  {
    System.out.println("Trials beginning! Starting timer...");
    startTime = millis();                                                             // start the timer!
  } 
  else currTrialNum++;                                                                // increment trial number

  lastTime = millis();                                                                // record the time of when this trial ended
  currentTyped = "";                                                                  // clear what is currently typed preparing for next trial
  currentPhrase = phrases[currTrialNum];                                              // load the next phrase!
}

// Print results at the end of the study
void printResults(float wpm, float freebieErrors, float penalty, float cps)
{
  background(0);       // clears screen
  
  textFont(createFont("Arial", 16));    // sets the font to Arial size 16
  fill(255);    //set text fill color to white
  text(day() + "/" + month() + "/" + year() + "  " + hour() + ":" + minute() + ":" + second(), 100, 20);   // display time on screen
  
  text("Finished!", width / 2, height / 2); 
  
  int h = 20;
  for(int i = 0; i < NUM_REPEATS; i++, h += 40 ) {
    text("Target phrase " + (i+1) + ": " + phrases[i], width / 2, height / 2 + h);
    text("User typed " + (i+1) + ": " + typed[i], width / 2, height / 2 + h+20);
  }
  
  text("Raw WPM: " + wpm, width / 2, height / 2 + h+20);
  text("Freebie errors: " + freebieErrors, width / 2, height / 2 + h+40);
  text("Penalty: " + penalty, width / 2, height / 2 + h+60);
  text("WPM with penalty: " + max((wpm - penalty), 0), width / 2, height / 2 + h+80);
  text("CPS: " + cps, width / 2, height / 2 + h+100);

  saveFrame("results-######.png");    // saves screenshot in current folder    
}

// This computes the error between two strings (i.e., original phrase and user input)
int computeLevenshteinDistance(String phrase1, String phrase2)
{
  int[][] distance = new int[phrase1.length() + 1][phrase2.length() + 1];

  for (int i = 0; i <= phrase1.length(); i++) distance[i][0] = i;
  for (int j = 1; j <= phrase2.length(); j++) distance[0][j] = j;

  for (int i = 1; i <= phrase1.length(); i++)
    for (int j = 1; j <= phrase2.length(); j++)
      distance[i][j] = min(min(distance[i - 1][j] + 1, distance[i][j - 1] + 1), distance[i - 1][j - 1] + ((phrase1.charAt(i - 1) == phrase2.charAt(j - 1)) ? 0 : 1));

  return distance[phrase1.length()][phrase2.length()];
}
