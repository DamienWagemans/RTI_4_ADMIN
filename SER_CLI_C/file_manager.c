#include "file_manager.h"


char * SelectLigne(char * fichier, int ligne)
{
	FILE *fp = NULL;
	fp = fopen(fichier,"r");
	char * Word = (char *)malloc(80);
	char Buffer[80];
	int i = 0;
	while (fgets(Buffer,sizeof Buffer,fp),!feof(fp))
	{
		if(i == ligne)
		{
			strcpy(Word, Buffer);
		}
		i++;
	}
	fclose(fp);
	return Word;
}

//cette fonction ouvre un fichier present dans Chemin (param 1), recherche un la ligne du fichier commencant par le mot rech (param 2)
//et renvoit en suite toute la ligne
char * Lect_Ligne_FichierCSV(char * rech, char * Chemin, char * sep)
{
	FILE *fp = NULL;
	char Buffer[80];
	char Buffer2[80];
	char * token;
	char * pointeur = NULL;
	char B1[40],B2[40];
	char * Word = (char *)malloc(80);

	
	strcpy(Word,"Not found");

	fp = fopen(Chemin,"r");
	if(fp == NULL)
	{
		perror("Echec Ouverture fichier\n");
		exit(1);
	}
	
	while (fgets(Buffer,sizeof Buffer,fp),!feof(fp))
	{
		strcpy(Buffer2, Buffer);

		token = strtok(Buffer, sep);
		
		if(strcmp (rech, Buffer)==0)
		{
			strcpy(Word, Buffer2);
			fclose(fp);
			return Word;
		}
	}
	if(strcmp(Word,"Not found"))
	{
		fclose(fp);
		return NULL;
	}
	
	//return liste;
}


char * lect_config(char * mot)
{
	char * token;
	char * pointeur = NULL;
	char B1[40],B2[40];
	char * Word ;

	Word = Lect_Ligne_FichierCSV(mot, "CONFIG", ";");

	if(Word == NULL)
	{
		printf("Erreur fichier config\n");
		exit(1);
	}
	else
	{
		token = strtok(Word, ";");
		token = strtok(NULL, ";");
		strcpy(Word,token);
		return Word;		
	}
}


void ecrire_log(char * chemin, char * txt)
{
	FILE * pf = NULL;
	char * heure;
	char to_write[200];
	
	heure = get_time();
	sprintf(to_write,"%s  ---  %s",heure, txt);
	

	free(heure);
	
	pf = fopen(chemin, "a");
	
	if(pf == NULL)
	{
		printf("Fichier log introuvable...\n");
	}
	else
	{
		fwrite(to_write, sizeof(char), strlen(to_write), pf);
	}
	fclose(pf);
}

char * get_time()
{
	char * tmp;
	time_t t = time(NULL);

	
	tmp = (char*) malloc(10);
	strftime(tmp, 10, "%X", localtime(&t));


	return tmp;
}

int EcrireDansFichier(Element * liste)
{
	FILE * fp;
	fp = fopen("F_WAITING.csv", "w");
	if(fp == NULL)
		return -1;
	struct Element  * elm;
	elm = liste;
	while(elm != NULL)
	{
		fputs(elm->Mot, fp);
		elm = elm->svt;
	}
	fclose(fp);
}

int remplace_ligne_fichier (char* chemin, char * cible, char * nouveau)
{
	char Buffer[256];
	char * ptr, * Ligne;
	char NameTerm[50], term[10], ferry[80];
	strcpy(ferry, nouveau);
	Ligne = SelectLigne(chemin, strtol(cible,NULL,10)-1);
	printf("Numéro du terminal: %s", Ligne);
	FILE * fp = fopen(chemin, "r");
	FILE * fpTemp = fopen("temp.csv", "w+");
	if (fp == NULL)
		return -1;
	while(fgets(Buffer, sizeof(Buffer), fp)!= NULL)
	{
		if(strcmp(Ligne, Buffer) == 0)
			fputs(ferry, fpTemp);
		else
			fputs(Buffer, fpTemp);
	}
	fclose(fp);
	fp = fopen(chemin, "w");
	if(fp == NULL)
		return -1;
	fseek(fpTemp, 0, SEEK_SET);
	while(fgets(Buffer,sizeof(Buffer),fpTemp) != NULL)
		fputs(Buffer, fp);
	fclose(fp);
	fclose(fpTemp);
	remove("temp.csv");
	return 0;
}
