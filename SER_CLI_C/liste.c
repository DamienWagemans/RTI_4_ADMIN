#include "liste.h"

void AjouterElement(char * msg, Element ** liste)
{
	struct Element * elm = (struct Element *)malloc(sizeof(struct Element));
	struct Element * temp;
	strcpy(elm->Mot, msg);
	if(*liste == NULL)
	{
		elm->svt = *liste; 
		*liste = elm;
	}
	else
	{
		temp = *liste;
		while(temp->svt != NULL)
			temp = temp->svt;	
		elm->svt = temp->svt;
		temp->svt = elm;
	}
}

void AfficherListe(Element * liste)
{
	struct Element * new = NULL;
	if(liste == NULL)
	{
		printf("Liste Vide\n");
		exit(0);
	}
	while(liste->svt != NULL){
		liste = liste->svt;
		printf("Ligne du fichier: %s", liste->Mot);
	}
}


Liste * InitListe()
{
	struct Liste * liste = (struct Liste*) malloc(sizeof(struct Liste));
	struct Element * element = (struct Element *) malloc(sizeof(struct Element));
	if(liste == NULL || element == NULL)
	{
		printf("Echec de l'allocation dynamique\n");
		return 0;
	}
	
	strcpy(element->Mot, "");
	element->svt = NULL;
	liste->first = element;
	
	return liste;
}

void vider_liste(Element ** liste)
{
	Element * ptemp;
	do
	{
		ptemp = *liste;
		ptemp = ptemp-> svt;
		free(liste);
		*liste = ptemp;
	}while(*liste != NULL);
}

void SupprimerLigneListe(Liste ** liste, char * Ligne)
{
	char * ptr;
	struct Element * temp = (struct Element *)malloc(sizeof(struct Element));
	if(temp == NULL)
	{
		printf("Echec de l'allocation dynamique\n");
		exit(0);
	}
	if((*liste)->first == NULL)
	{
		printf("Liste Vide\n");
		exit(0);
	}
	temp = (*liste)->first;
	while(strcmp(temp->Mot, Ligne) != 0 && temp->svt != NULL)
	{
		temp = temp->svt;
	}
	if(strcmp(temp->Mot, Ligne) == 0){
		ptr = strchr(temp->Mot, ';');
		*ptr = '\0';
		strcat(temp->Mot, ";-;NA;-\0");
			
	}
	free(temp);
}

