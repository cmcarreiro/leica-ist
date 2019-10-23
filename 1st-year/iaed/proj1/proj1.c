/*
 * File:  proj1.c
 * Author:  Catarina Carreiro ist192438
 * Description: program to schedule events
 * Language : C
*/


#include <stdio.h>
#include <string.h>

/* maximum number of characters in a string */
#define MAXSTR 64
/* maximum number of rooms */
#define MAXROOM 10
/* maximum number of participants */
#define MAXPARTS 4
/*maximum number of events at all times (+1 because i always have the last one empty)*/
#define MAXALL 1001
/* maximum number of characters in the string with guests */
#define MAXPARTSR 192
/*maximum size a string can have(from description to participants,/n/0) */
#define STRINGSIZE 343
/*for certain function returns*/
#define TRUE 1
#define FALSE 0
/* index that could never be in the function(for when it is necessary to check
 if something exists, returning its index if it does) */
#define FALSEINDEX 1002

/*
 * brief: defining the Event structure
 */
typedef struct event{
  /* description */
  char desc[MAXSTR];
  /* participants */
  char parts[MAXPARTS][MAXSTR];
  /* day, month, year */
  int day; int month; int year;
  /* hour and minutes */
  int hour; int mins;
  /* duration */
  int dur;
  /* room */
  int room;
  /* number of participants */
  int numparts;
  /* ending hours and ending minutes */
  int endhours; int endmins;
}Event;

/* Auxiliary functions */
void cleanstr(char s[], int size);
void getstring(char s[], char delim);
int atoi(char c);
Event ending(Event ev);
Event toparse(Event ev);
Event resetevent(Event ev);
void printevent(Event ev);
int overlappingevents(Event ev1, Event ev2);
void printerror(int i, char desc[], char part[], int room);
int available_room(Event ev1, Event ev2[], int len);
int available_people(Event ev1, Event ev2[], int len, int code);
void order(Event ev[], int len);
int existencecheck(char s[], Event ev[], int len);


/*
 * brief : main function that gets a character, checks which command it is,
 and does what that command asks
 */
int main() {
  int i, j=0 , r, k;
  /* len keeps track of the amount of events there; flag is used as a flag in
  a couple of functions; hours, minute, dur and room are used for some calculations
  */
  int len=0, flag=0, hours, minute, dur, room;
  /* s and p are used for getting strings */
  char s[MAXSTR], p[MAXSTR], c;
  /* ev has all the events in it; temp is for temporary events */
  Event ev[MAXALL], temp;
  c = getchar();
  ev[0] = resetevent(ev[0]);

  while(c!='x'){
    temp = resetevent(temp);
    hours = 0; minute = 0; dur = 0; k=0; flag=0;
    cleanstr(s, MAXSTR); cleanstr(p, MAXSTR);
    switch(c){
      /*add event*/
      case 'a':
        temp = toparse(temp);
        if(available_room(temp, ev, len) && available_people(temp, ev, len, 1)){
          ev[len] = resetevent(ev[len]); ev[len] = temp;
          len = len + 1; ev[len] = resetevent(ev[len]);
        }
        c = getchar();
        break;
      /* list all events*/
      case 'l':
        for (j=0; j<len && j<MAXALL; j++){
          printevent(ev[j]);
        }
        c = getchar();
        break;
      /* list all events in a room */
      case 's':
        c = getchar(); c = getchar();
        r = atoi(c);
        c = getchar();
        if (c == '0'){
          r = 10;
          c = getchar();
        }
        for(j=0;j<len && j<MAXALL;j++){
          if(ev[j].room == r){
            printevent(ev[j]);
          }
        }
        break;
      /* remove an event*/
      case 'r':
        c = getchar();
        getstring(s, '\n');
        if ( existencecheck(s, ev, len) != FALSEINDEX){
          for (j=0; j<len && j<MAXALL; j++){
            if (strcmp(s, ev[j].desc)==0){
              for (i=j; i<len;i++){
                ev[i] = ev[i+1];
              }
              len--;
              break;
            }
          }
        }
        break;
      /* change the beginning of an event */
      case 'i':
        c = getchar();
        getstring(s, ':');
        c = getchar();
        /* getting the hours and minutes */
        hours = atoi(c)*10; c = getchar(); hours += atoi(c); c = getchar();
        minute = atoi(c)*10; c = getchar(); minute += atoi(c); c = getchar();
        j = existencecheck(s, ev, len);
        if (j!=FALSEINDEX){
          /*
           * places the beginning hours and minutes in a temporary event, and
          calculates the ending hours
          */
          temp = ev[j];
          temp.hour = 0; temp.mins = 0;
          temp.hour = hours; temp.mins = minute;
          temp.endhours = 0; temp.endmins = 0;
          temp = ending(temp);
          if (available_room(temp, ev, len) && available_people(temp, ev, len, 1)){
            /* places the event at the end of list of events so it can be ordered */
            ev[j] = resetevent(ev[j]);
            for (i=j; i<len-1;i++){
              ev[i] = ev[i+1];
            }
            ev[len-1] = resetevent(ev[len-1]);
            ev[len-1] = temp;
          }
        }
        break;
      /* change the duration of an event */
      case 't':
        /* get the description */
        c = getchar();
        getstring(s, ':');
        c = getchar();
        /* get the duration */
        while (c != '\n'){
          dur = dur * 10 + atoi(c);
          c = getchar();
        }
        j = existencecheck(s, ev, len);
        if (j!=FALSEINDEX){
          temp = resetevent(temp);
          temp = ev[j];
          temp.dur = 0;
          temp.dur = dur;
          temp = ending(temp);
          if (available_room(temp, ev, len) && available_people(temp, ev, len, 1)){
            ev[j].endhours = temp.endhours; ev[j].endmins = temp.endmins;
            ev[j].dur = dur;
          }
        }
        break;
      /* change the room of an event */
      case 'm':
        c = getchar(); getstring(s, ':'); c = getchar();
        room = atoi(c); c = getchar();
        if (c !='\n'){
          room = 10;
          c = getchar();
        }
        j = existencecheck(s, ev, len);
        if (j!=FALSEINDEX){
          temp = ev[j];
          temp.room = 0;
          temp.room = room;
          if (available_room(temp, ev, len)){
            /* places the event at the end of list of events so it can be ordered */
            ev[j] = resetevent(ev[j]);
            for (i=j; i<len-1;i++){
              ev[i] = ev[i+1];
            }
            ev[len-1] = resetevent(ev[len-1]);
            ev[len-1] = temp;
          }
        }
        break;
      /* add a participant to an event */
      case 'A':
        c = getchar(); getstring(s, ':'); getstring(p, '\n');
        j = existencecheck(s, ev, len);
        if (j!=FALSEINDEX){
          /* if there are already 3 participants */
          if (ev[j].numparts == 3) {
            printerror(3, s, ev[0].parts[0], 0);
          } else {
            flag = 0;
            for(i=0; i<ev[j].numparts+1; i++){
              if(strcmp(p,ev[j].parts[i])==0){
                flag++;
                break;
              }
            }
            if (flag == 0){
              temp = ev[j];
              /* cleaning out the participants in the temporary event, leaving
              only the one we want to check*/
              for(i=0; i<MAXPARTS; i++){
                cleanstr(temp.parts[i], MAXSTR);
              }
              strcpy(temp.parts[0], p);
              /* if the person is available, add them to to the participants
              and increment the number of participants */
              if (available_people(temp, ev, len, 5)){
                ev[j].numparts +=1;
                strcpy(ev[j].parts[ev[j].numparts], p);
              }
            }
          }
        }
        break;
      /* remove a participant */
      case 'R':
        c = getchar(); getstring(s, ':'); getstring(p, '\n');
        j = existencecheck(s, ev, len);
        if (j!=FALSEINDEX){
          k=0;
          /* checks if the participant actually exists */
          for(i=1;i<=ev[j].numparts;i++){
            if(strcmp(ev[j].parts[i], p)==0){
              r = i; k = 1;
              break;
            }
          }
          if (ev[j].numparts == 1 && k!=0) {
            printerror(4, s, p, 0);
          } else if (k!=0){
            for (i=r;i<ev[j].numparts;i++){
              strcpy(ev[j].parts[i],ev[j].parts[i+1]);
            }
            /* removes participant */
            cleanstr(ev[j].parts[ev[j].numparts], MAXSTR);
            ev[j].numparts--;
          }
          break;
        }
      }
    order(ev, len-1);
    c = getchar();
  }
  return 0;
}


/*
 * brief : function to clean a string.
 * input : the string and the size of the string
 */
void cleanstr(char s[], int size){
  int i;
  for(i=0; i < size; i++){
    s[i] = '\0';
  }
}


/*
 * brief: function to get a string until a certain delimiter
 * input: string to be stored, delimiter
 */
void getstring(char s[], char delim){
  char c;
  int i;
  c = getchar();
  for (i=0; c != delim; i++){
    s[i] = c;
    c = getchar();
  }
}


/*
 * brief: i couldnt figure out how to use atoi, and either way i was already parsing
 numbers one digit at a time, so this is my makeshift atoi
 * input: a character
 * note : in hindsight, this function isn't the most efficient way to deal with
 this problem
 */
int atoi(char c){
  int i;
  i = c - 48;
  return i;
}

/*
 * bried : calculates when an event is going to end
 * input : event
 * output : event
 */
Event ending(Event ev){
  int hours, mins;
  hours = ev.dur/60; mins = ev.dur - (hours*60);
  ev.endhours = ev.hour + hours; ev.endmins = ev.mins + mins;
  if (ev.endmins > 59){
      ev.endhours++;
      ev.endmins = ev.endmins - 60;
  }
  if (ev.endhours >= 24){
    ev.endhours = 23;
    ev.endmins = 59;
  }
  return ev;
}


/*
 * brief : this function does the parsing for case 'a' (adding an event),
 one character at a time
 * input : event to be stored
 * output : event
 */
Event toparse(Event ev){
  int i=0, k=0, j=0, l=0;
  char s[STRINGSIZE];
  cleanstr(s, STRINGSIZE);
  ev.numparts = 1; ev.dur = 0;

  /*scans the entire add event function, including spaces */
  scanf(" %[^\n]s", s);
  for(k=0; s[j]!=':';k++){
    ev.desc[k] = s[j];
    j++;
  } ev.desc[k] = '\0'; j++;

  /*reading day, month and year. its multiplying the numbers by ten and adding
  them */
  ev.day = atoi(s[j])*10; j++; ev.day += atoi(s[j]); j++;
  ev.month = atoi(s[j])*10; j++; ev.month += atoi(s[j]); j++;
  ev.year = atoi(s[j])*1000; j++; ev.year += atoi(s[j])*100; j++;
  ev.year += atoi(s[j])*10; j++; ev.year += atoi(s[j]); j++; j++;
  ev.hour += atoi(s[j])*10; j++; ev.hour += atoi(s[j]); j++;
  ev.mins = atoi(s[j])*10; j++; ev.mins += atoi(s[j]); j++; j++;

  /* since i dont know how many digits there are in the duration, i'm first
  trying to find the lenght of that part of the string, and then adding it
  to the duration */
  for(k=j; s[k]!=':'; k++){
      l++;
  }
  for(k=0; k<l; k++){
      ev.dur = ev.dur*10 + atoi(s[j]);
      j++;
  } j++;

  ev.room = atoi(s[j]); j++;
  if (s[j]!=':'){
    ev.room = 10;
    j++; j++;
  } else {
    j++;
  }

  /*scanning the responsible*/
  for(k=0; s[j]!=':';k++){
    ev.parts[0][k] = s[j];
    j++;
  } j++;
  ev.parts[0][k] = '\0';

  /*scanning the participants */
  for(k=j; s[k]!='\0';k++){
    if (s[k]==':'){
        ev.numparts++;
    }
  }
  for(i=1;i<ev.numparts+1; i++){
    for(k=0; ( s[j]!=':' && s[j]!='\n' && s[j]!='\0' );k++){
      ev.parts[i][k] = s[j];
      j++;
    }
    j++;
  }
  /*calculting the ending hours*/
  ev = ending(ev);
  return ev;
}


/*
 * brief : cleans up the events
 * input : event to be reset
 * output : event
 */
Event resetevent(Event ev){
  int i;
  cleanstr(ev.desc, MAXSTR);
  for(i=0; i<MAXPARTS; i++){
    cleanstr(ev.parts[i], MAXSTR);
  }
  ev.day=0; ev.month=0; ev.year=0; ev.hour=0; ev.mins=0; ev.dur=0;
  ev.room = 0; ev.numparts = 0; ev.endhours = 0; ev.endmins=0;
  return ev;
}


/*
 * brief : prints event
 * input : event to be printed
 */
void printevent(Event ev){
    int i;
    printf("%s %02d%02d%04d %02d%02d %d Sala%d %s\n*", ev.desc, ev.day,
    ev.month, ev.year, ev.hour, ev.mins, ev.dur, ev.room, ev.parts[0]);
    for(i=1; i<=ev.numparts; i++){
        printf(" %s", ev.parts[i]);
    }
    printf("\n");
}


/*
 * brief : checks if there are any overlapping events
 * input : two events
 * output : TRUE if they overlap or FALSE if they don't
 */
int overlappingevents(Event ev1, Event ev2){
  int ev1bh = 0, ev1eh = 0, ev2bh=0, ev2eh=0;
  /*
   * bh -> beginning hour;
   * eh -> ending hour;
   */
  ev1bh = ev1.hour*100+ev1.mins; ev1eh = ev1.endhours*100+ev1.endmins;
  ev2bh = ev2.hour*100 + ev2.mins; ev2eh = ev2.endhours*100 + ev2.endmins;

  return (((ev1.day == ev2.day) && (ev1.month==ev2.month)
  && (ev1.year==ev2.year)) && ((ev1bh >= ev2bh && ev1bh < ev2eh)
  || (ev1eh > ev2bh && ev1eh <= ev2eh) || (ev1bh<=ev2bh && ev1eh >=ev2eh))
  && (strcmp(ev1.desc,ev2.desc)!=0));
}


/*
 * brief : prints the error messages
 * input : an integer (the error code), the event, and another integer
 (for the messages the involve participants)
 */
void printerror(int i, char desc[], char part[], int room){
  if (i==0){
    printf("Impossivel agendar evento %s. Sala%d ocupada.\n", desc, room);
  } else if (i==1){
    printf("Impossivel agendar evento %s. Participante %s tem um evento sobreposto.\n", desc, part);
  } else if(i==2){
    printf("Evento %s inexistente.\n", desc);
  } else if(i==3){
    printf("Impossivel adicionar participante. Evento %s ja tem 3 participantes.\n", desc);
  } else if(i==4){
    printf("Impossivel remover participante. Participante %s e o unico participante no evento %s.\n", part, desc);
  } else if(i==5){
    printf("Impossivel adicionar participante. Participante %s tem um evento sobreposto.\n", part);
  }
}


/*
 * brief : checks if the room is available
 * input : event1, list of events, lenght of the list of events
 * output : returns TRUE if the person is available and FALSE if it's not
 */
int available_room(Event ev1, Event ev2[], int len){
    int i=0;
    for (i=0; i < len; i++){
      /*if the room is the same and as long as they don't have the same
        description*/
      if (ev1.room == ev2[i].room && overlappingevents(ev1, ev2[i])){
        printerror(0, ev1.desc, ev1.parts[0], ev1.room);
        return FALSE;
      }
    }
  return TRUE;
}


/*
 * brief : similar to available_room, checks if a person is available
 * input : event1, list of events, length, and character to check which error to send
 * output: returns TRUE if the person is available and FALSE if it isn't
 */
int available_people(Event ev1, Event ev2[], int len, int code){
  int i, j, k, flag=0;
  for (i=0;i<=len+1;i++){
    if (overlappingevents(ev1,ev2[i])){
      for (j=0;j<=ev1.numparts;j++){
        for (k=0;k<=ev2[i].numparts; k++){
          if(strcmp(ev1.parts[j],ev2[i].parts[k])==0){
            flag++;
            printerror(code, ev1.desc, ev1.parts[j], 0);
          }
        }
      }
    }
  }
  if (flag>=1){return FALSE;}
  return TRUE;
}


/*
 * brief : orders my list of events. places the last event in the right place,
  then shifts the rest of the events
 * input:  arrary of events and the lenght of the list
 * output: ordered array of events
 */
void order(Event ev[], int len){
  /*
  11 -> year, month and day all in one integer;
  12 -> hour and minutes all in one integer;
  so as to be able to compare the dates and because if i put it all in the same
  variable it'd be too long of a number
  */
  int e11, e12, e21, e22;
  int i, j;
  Event temp;

  temp = resetevent(temp);

  e11 = ev[len].year*1000 + ev[len].month*100 + ev[len].day;
  e12= ev[len].hour *100 + ev[len].mins;

  if (len>0){
    for (i=0; i < len ; i++){
      e21 = ev[i].year*1000 + ev[i].month*100 + ev[i].day;
      e22= ev[i].hour * 100 + ev[i].mins;
      /*checks if they start on the same day or not, and if so, do they begin at
      the same hour and minutes, and if so, what is their room*/
      if((e11<e21)||(e11==e21 && e12<e22)
      ||(e11==e21 && e12==e22 && ev[len].room<ev[i].room)){
          /*passes the last event (the one im ordering) to a temporary one,
          then shifts everything one position down, and passes my temporary event
          to the index i want*/
          temp = ev[len];
          for(j=len-1; j>=i; j--){
            ev[j+1] = resetevent(ev[j+1]);
            ev[j+1] = ev[j];
          }
          ev[i] =  temp;
          break;
      }
    }
  }
}

/*
 * brief : checks if an event actually exists
 * input : string with description of event, list of events, lenght of list of
 events
 * output : if the event exists returns its index, if it doesn't returns a number
 that could never be in the index
 */
int existencecheck(char s[], Event ev[], int len){
  int i, flag=0;

  for (i=0; i<len && i<MAXALL; i++){
    if (strcmp(ev[i].desc, s)==0){
      flag = 1;
      break;
    }
  }

  if (flag==0) {
    printerror(2, s, ev[0].parts[0], ev[0].room);
    return FALSEINDEX;
  } else { return i; }

}
