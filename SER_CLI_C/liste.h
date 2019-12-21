#ifndef        LISTE_H
#define        LISTE_H

#include <stdio.h> 
#include <stdlib.h> 
#include <string.h>
#include <ctype.h>
#include <unistd.h> 

typedef struct Element{
	char Mot[80];
	struct Element * svt;
}Element;

typedef struct Liste{
	Element * first;
}Liste;


void AfficherListe(Element * liste);
Liste * InitListe();
void AjouterElement(char * , Element ** liste);
void SupprimerLigneListe(Liste ** liste, char * Ligne);
void vider_liste(Element ** liste);




#endif

