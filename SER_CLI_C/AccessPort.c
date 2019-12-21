#include "AccessPort.h"


//fonction de verification du login, prend en parametre le chaine de caractère recue du clients
//ensuite elle va voir dans le fichier F_AGENTS.CSC si les identifiants/MDP correspondent
//renvoit : 1 : login ok, -1: username incorrect, -2: password_incorrect
int verif_login(char * login_password_client, char * sep)
{
	char * ptr;
	char log_term[100];
	
	char login_password_fichier[160], NumTerm[100];
	struct User user;
	struct User U_from_Fich;
	
	//decomposition du login_password_client
	// lire dans fichier config le séparateur !!!!!
	ptr = strtok(login_password_client, sep);
	strcpy(user.login, ptr);
	
	ptr = strtok(NULL, sep);
	strcpy(user.MDP, ptr);
	
	ptr = strtok(NULL, sep);
	strcpy(NumTerm, ptr);
	
	user.NumeroTerm = strtol(NumTerm, NULL, 10);
	
	
	ptr = Lect_Ligne_FichierCSV(user.login, "F_AGENTS.csv", sep);
	if(ptr == NULL)
	{
		sprintf(log_term,"Login invalide de %s\n",user.login);
		ecrire_log("LOG_TERM", log_term);
		return -1;
	}
	else
	{
		//important car ptr pointe vers une chaine de caractère allouée dans Lect_Ligne_FichierCSV
		free(ptr);
	
		strcpy(login_password_fichier, ptr);
		
		ptr = strtok(login_password_fichier, sep);
		strcpy(U_from_Fich.login, ptr);
	
		ptr = strtok(NULL, sep);
		strcpy(U_from_Fich.MDP, ptr);
		strcat(user.MDP, "\n");
		
		//printf("PASS fichier : %s, PASS client : %sK\n", password_fichier, password_client);
		
		if (strcmp(U_from_Fich.MDP, user.MDP)==0)
		{
			sprintf(log_term,"Login valide de %s\n",user.login);
			ecrire_log("LOG_TERM", log_term);
			return 1;
		}
		else
		{
			sprintf(log_term,"Login invalide de %s\n",user.login);
			ecrire_log("LOG_TERM", log_term);
			return -2;
		}
	}
}

int AskNextDeparture(char * NumTerm,char * sep)
{
	int Num = strtol(NumTerm, NULL, 10);
	char * Ligne;
	char* vect[10];
	
	
	//Ligne = Lect_Ligne_FichierCSV (rech, "F_TERM.csv", ";");
	Ligne = SelectLigne("F_TERM.csv", Num-1);
	printf("La ligne est: %s\n", Ligne);
	MiseVecteur(Ligne, &vect[0], sep);
	if(strcmp(vect[1],"-") == 0 && strcmp(vect[2], "NA") == 0){
		return NO_FERRY;
	}
	else{
		if(strcmp(vect[2], "NA") == 0 && strcmp(vect[1],"-") != 0){
			return DEPARTURE_UNKNOWN;
		}
		else{
			return DEPARTURE_KNOWN;
		}
	}
	free(vect);
}


int AskNotifyLoading(struct Heure temps, int time)
{
	int TempsSys = 0, TempsTerm = 0;
	TempsSys = temps.HeureSys * 60 + temps.MinuteSys;
	TempsTerm = temps.HeureTerm * 60 + temps.MinuteTerm;
	printf("HeureS en Minute: %d || HeureT en Minute: %d\n", TempsSys, TempsTerm);	
	if(TempsSys > TempsTerm - time && TempsSys < TempsTerm)
		return ACK;
	else
		return FAIL;
}

int FerryLeaving(struct Heure times, char * Msg)
{
	FILE * fp;
	char * vect[10], * ptr, * Ligne;
	char rech[80];
	int offset = 0, offset_temp, numterm, TailleLigne, TailleRech;
	MiseVecteur(Msg, &vect[0],":");
	numterm = strtol(vect[0], NULL, 10);
	printf("Numéro du TERMINAL: %d", numterm);
	strcpy(rech, "term_");
	sprintf(vect[0], "%d", numterm);
	strcat(rech, vect[0]);
	Ligne = Lect_Ligne_FichierCSV(&rech[0], "F_TERM.csv", ";");
	if(times.HeureSys > times.HeureTerm || (times.HeureSys == times.HeureTerm && times.MinuteSys > times.MinuteTerm))
	{
		
		ptr = strchr(Ligne, ';');
		*ptr = '\0';
		strcat(Ligne, ";-;NA;-");
		strcat(Ligne, "\n");
		printf("Avant remplace ligne fichier \n");
		int ret = remplace_ligne_fichier("F_TERM.csv", vect[0], Ligne);
		printf("Apres remplace ligne fichier \n");
		if(ret == 0)
			return ACK;
		else
			return FAIL;
	}
	else
		return FAIL;
	free(vect);
}

int AskForFerry(char * NumTerm)
{
	char Buffer[80], Buffer2[80], temp[80];
	char Ferry[80];
	char * ptr = NULL;
	FILE * fp = NULL;
	fp = fopen("F_WAITING.csv", "r");
	if(fp == NULL)
		return -1;
	fgets(Ferry, sizeof Ferry, fp);
	printf("Ferry = %s\n", Ferry);
	struct Element * liste = NULL;
	while(fgets(Buffer, sizeof Buffer, fp), !feof(fp))
	{
		AjouterElement(Buffer, &liste);
	}
	fclose(fp);	
	EcrireDansFichier(liste);	
	strcpy(temp, NumTerm);
	ptr = strchr(temp, '\r');
	*ptr = '\0';
	sprintf(Buffer2, "term_%s;%s",temp, Ferry);
	printf("Buffer2: %s", Buffer2);

	vider_liste(&liste);

	int ret = remplace_ligne_fichier("F_TERM.csv", NumTerm, Buffer2);
	if(ret == 0)
		return ACK;
	else
		return FAIL;
}

int FerryArriving(char * Msg)
{
	char * Ligne, * vect[10], *ptr;
	char rech[80], Ferry[80];
	MiseVecteur(Msg, &vect[0], ";");
	sprintf(Ferry, "term_%s;%s;%s;%s", vect[0], vect[1], vect[2], vect[3]);
	ptr = strchr(Ferry, '\n');
	*ptr = '\0';
	int ret = remplace_ligne_fichier("F_TERM.csv", vect[0], Ferry);
	if(ret == 0)
		return ACK;
	else
		return FAIL;
	free(vect);
}

void RecupererHeure(char * Msg, struct Heure * temps)
{
	char * Ligne;
	char * HeureSys[10];
	char * HeureTerm[10];
	char * temp[10];
	
	MiseVecteur(Msg,&HeureSys[0],":");
	Ligne = SelectLigne("F_TERM.csv", (strtol(HeureSys[0], NULL, 10))-1);
	MiseVecteur(Ligne, &temp[0], ";");
	MiseVecteur(temp[2],&HeureTerm[0], ":");

	temps->HeureSys = strtol(HeureSys[1], NULL, 10);
	temps->HeureTerm = strtol(HeureTerm[0], NULL, 10);
	temps->MinuteSys = strtol(HeureSys[2], NULL, 10);
	temps->MinuteTerm = strtol(HeureTerm[1], NULL, 10);
	printf("HeureSystème: %d:%d || HeureTerminal: %d:%d\n", temps->HeureSys,temps->MinuteSys,temps->HeureTerm,temps->MinuteTerm);

}

