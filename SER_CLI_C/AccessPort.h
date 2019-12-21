#ifndef        ACCESSPORT_H
#define        ACCESSPORT_H

#include <stdio.h> 
#include <stdlib.h> 
#include <string.h>
#include <ctype.h>
#include <unistd.h> 
#include <errno.h> 
#include <time.h> 

#include "SocketsUtilities.h"
#include "file_manager.h"
#include "liste.h"




int verif_login (char*, char *);
int AskNextDeparture(char *, char *);
void RecupererHeure(char *, struct Heure *);
int AskNotifyLoading(struct Heure, int);
int FerryLeaving(struct Heure, char *);
int AskForFerry(char *);
int FerryArriving(char*);



#endif

