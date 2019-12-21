#ifndef        FILE_MANAGER_H
#define        FILE_MANAGER_H

#include <stdio.h> 
#include <stdlib.h> 
#include <string.h>
#include <ctype.h>
#include <unistd.h> 

#include "liste.h"



char * SelectLigne(char * fichier, int ligne);
char * Lect_Ligne_FichierCSV(char * rech, char * Chemin, char * sep);
char * lect_config(char * mot);
void ecrire_log(char * chemin, char * txt);
char * get_time();
int EcrireDansFichier(Element * liste);
int remplace_ligne_fichier (char*, char *, char * );

#endif

