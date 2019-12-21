#include "SocketsUtilities.h"
#include "file_manager.h"


#define affThread(num, msg) printf("th_%s> %s\n", num, msg)


int login(int);
int AskDeparture(int);
int DepartureKnown();
int NoFerry();



//structure de transfert
struct Message msg_envoi;
struct Message msg_reception;
struct User user;  
int MTU;

//signal ctrl-c
void handlerSigint(int sig);


//fichier config
long PORT;
char ip[20];

int hSocket;
int authentification = 0;
char NumTerm [10];
char HeureSys[20];


pthread_t thread_test_conn;
void * fctTest_conn(void * param);

int main()
{
	//armement SIGINT
	struct sigaction act;
	act.sa_handler = handlerSigint;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGINT, &act, 0);
	
	
	//lecture dans fichier config
	char *config_lect;
	//PORT
	config_lect = lect_config("Port_service");
	PORT = strtol(config_lect, NULL, 10);
	//IP
	config_lect = lect_config("ip_address");
	strcpy(ip,config_lect);
	
	
	//Variables pour la socket/IP
	struct sockaddr_in adresseSocket;
	int tailleSockaddr_in;
		
	//utilisé pour les valeurs de retour
	int ret;
	int tentative = 0;
	char choix;
	//creation de la socket
	hSocket = create_socket(ip, PORT, &adresseSocket);
	check_erreur(hSocket, NULL, 0, &hSocket);

	//connection au server + gestion d'erreur
	ret = connect_to_server(&hSocket, (struct sockaddr *)&adresseSocket);
	check_erreur(ret, NULL, 0, &hSocket);
	
	
	
	MTU = rech_MTU(hSocket);
	check_erreur(MTU, NULL, 0, &hSocket);
	printf("Valeur du MTU : %d\n", MTU);

	/* 5. Envoi d'un message client */ 
	do
	{
		while(authentification==0)
		{	
			ret = login(hSocket);
			if(ret == -1)
			{
				tentative++;
				if(tentative==3)check_erreur(ERROR_BAD_LOGIN, NULL, 0, &hSocket);
				ret = 0;
			}
		}
		//creation du thread test connectivity
		pthread_create(&thread_test_conn, NULL, fctTest_conn ,NULL);
		sleep(2);
		system("clear");
		ret = AskDeparture(hSocket);
		switch(ret)
		{
			case NO_FERRY:
				ret = NoFerry();
				break;
			case DEPARTURE_KNOWN:
				ret = DepartureKnown();
				//msg_reception.TypeRequete = END_OF_CONNEXION;
				//printf("\nDeconnexion en cours...\n");
				sleep(5);
				break;
			case DEPARTURE_UNKNOWN:
				do{
					system("clear");
					printf("Le depart pour ce bateau n'est pas encore connu\n"); 
					printf("Veuillez vous connecter sur un autre terminal\n");
					printf("Appuyer sur o pour quitter\n");
					fflush(stdin);
					choix = getchar();
				}while(choix != 'o');
				msg_reception.TypeRequete = DENY_OF_CONNEXION;
				break;

		}
				
	}while (msg_reception.TypeRequete != DENY_OF_CONNEXION && msg_reception.TypeRequete != END_OF_CONNEXION);
	
	/* 9. Fermeture de la socket */
	close(hSocket); 
	
	return 0;
}




int login(int hSocket)
{
	static int tentative = 3;
	char login[50],MDP[50];

	
	int taille_paquet;
	
	//char sendMsg[100];
	
	int ret;

	
	
	printf("--------------------LOGIN--tentative(s) restante(s) %d--------------------", tentative);
	
	printf("\nEntrez le nom de l'utilisateur: ");
	fflush(stdin);
	gets(user.login);
	
	printf("\nEntrez le mot de passe: ");
	fflush(stdin);
	gets(user.MDP);
	
	do
	{
		printf("\nEntrez le numéro de terminal: ");
		fflush(stdin);
	}while ((scanf("%d",&user.NumeroTerm)==0) || user.NumeroTerm<0 || user.NumeroTerm>NB_MAX_CLIENTS);
	sprintf(NumTerm, "%d", user.NumeroTerm);
	sprintf(msg_envoi.Msg,"%s;%s;%s\r\n",user.login,user.MDP,NumTerm);
	printf("Donnée Utilisateur:%s %s %d\n", user.login,user.MDP,NumTerm);
	msg_envoi.TypeRequete = LOGIN;
	
	//envoie de la requete
	ret = Envoie_Recevoir(msg_envoi, hSocket, &msg_reception, MTU);
	
	system("clear");
	tentative--;
	if(msg_reception.TypeRequete == PAUSE)
	{
		printf("Serveur en pause !\n\n");
		tentative = 3;
		return -1;
	}
	if(msg_reception.TypeRequete == END_OF_CONNEXION)
	{
		close(hSocket);
		exit(0);
	}
	if(msg_reception.TypeRequete == LOGIN_OK)
	{
		printf("Authentification réussie sur le terminal %d\n", user.NumeroTerm);
		authentification = 1;
		return 1;
	}
	else
	{
		printf("%s\n\n", msg_reception.Msg);
		return -1;
	}	
}

int AskDeparture(int hSocket)
{
	printf("\n------------Ask Next Departure------------\n");
	int taille_paquet;
	int ret;
	msg_envoi.TypeRequete = ASK_NEXT_DEPARTURE;
	sprintf(msg_envoi.Msg,"%s\r\n",NumTerm);
	
	ret = Envoie_Recevoir(msg_envoi, hSocket, &msg_reception, MTU);
	
	if(msg_reception.TypeRequete == PAUSE)
	{
		printf("Serveur en pause !\n");
		return PAUSE;
	}
	if(msg_reception.TypeRequete == END_OF_CONNEXION)
	{
		close(hSocket);
		exit(0);
	}
	
	if(msg_reception.TypeRequete == DEPARTURE_KNOWN)
		return DEPARTURE_KNOWN;
	else
		if(msg_reception.TypeRequete == DEPARTURE_UNKNOWN)
			return DEPARTURE_UNKNOWN;
		else
			return NO_FERRY;
}

int DepartureKnown()
{
	int valeur = 0;
	int ret = 0;
	time_t timestamp = time(NULL);
	do
	{
		system("clear");
		printf("\n------------Departure known------------");
		printf("\nDemander l'embarquement-1");
		printf("\nDemander l'autorisation de partir-2");
		printf("\nNotifie que le ferry est partie-3");
		printf("\nQuitter-4");
		printf("\nChoix : ");
		fflush(stdin);
		scanf("%d", &valeur);
		switch(valeur)
		{
			case 1:
				msg_envoi.TypeRequete = ASK_BEGIN_LOADING;
				break;
			case 2:
				msg_envoi.TypeRequete = NOTIFY_END_LOADING;
				break;
			case 3:
				msg_envoi.TypeRequete = FERRY_LEAVING;
				break;
			case 4:
				close(hSocket);
				exit(1);
				
		}
		if(valeur == 1 || valeur == 2 || valeur == 3)
		{
			//récupérer l'heure du système
			strcpy(msg_envoi.Msg, "\0");
			strcpy(msg_envoi.Msg, NumTerm);
			strcat(msg_envoi.Msg, ":");
			strftime(HeureSys, sizeof(msg_envoi.Msg), "%X", localtime(&timestamp));
			strcat(msg_envoi.Msg, HeureSys);
			strcat(msg_envoi.Msg, "\r\n");
			printf("Msg Envoie: %s\n",msg_envoi.Msg);
			ret = Envoie_Recevoir(msg_envoi, hSocket, &msg_reception, MTU);
			if(msg_reception.TypeRequete == PAUSE)
			{
				printf("Serveur en pause !\n");
				break;
			}
			if(msg_reception.TypeRequete == END_OF_CONNEXION)
			{
				close(hSocket);
				exit(0);
			}
			switch(msg_envoi.TypeRequete)
			{
				case ASK_BEGIN_LOADING:
					if(msg_reception.TypeRequete == ACK)
						printf("\nDebut du chargement autoriser");
					else
						printf("\nDebut du chargement non autoriser");
					break;
				case NOTIFY_END_LOADING:
					if(msg_reception.TypeRequete == ACK)
						printf("\nFerry Chargé");
					else
						printf("\nFerry Pas Chargé");
					break;
				case FERRY_LEAVING:
					if(msg_reception.TypeRequete == ACK)
						printf("\nCe ferry a quitter le terminal");
					if(msg_reception.TypeRequete == FAIL)
						printf("\nCe ferry n'a pas quitter le terminal l'heure actuelle doit etre supérieure a l'heure de depart\n");
					break;
			}
		}
		sleep(3);
	}while(msg_envoi.TypeRequete != FERRY_LEAVING || msg_reception.TypeRequete == FAIL);
}

int NoFerry()
{
	int valeur, ret, h, m;
	char ferry[80], heure[10], minute[10], dest[80];
	do
	{
		system("clear");
		printf("\n------------No Ferry------------");
		printf("\nNotifier qu'un ferry peut être accepté'-1");
		printf("\nNotifie que le ferry arrive au terminal-2");
		printf("\nQuitter-3");
		printf("\nChoix : ");
		fflush(stdin);
		scanf("%d", &valeur);
		switch(valeur)
		{
			case 1:
				msg_envoi.TypeRequete = ASK_FOR_FERRY;
				strcpy(msg_envoi.Msg, NumTerm);
				strcat(msg_envoi.Msg, "\r\n");
				break;
			case 2:
				msg_envoi.TypeRequete = FERRY_ARRIVING;
				system("clear");
				printf("Entrez les paramètres du ferry a ajouté:");
				printf("\nLe nom du ferry: ");
				fflush(stdin);
				gets(ferry);
				do
				{
					printf("\nL'heure de départ(HH:MM): ");
					ret = scanf("%d:%d", &h , &m);
					
				}while(ret == EOF || h>24 || m>60 );
				sprintf(heure, "%d:%d", h, m);
				printf("\nLe nom de destination: \n");
				fflush(stdin);
				gets(dest);
				sprintf(msg_envoi.Msg, "%s;%s;%s;%s\r\n",NumTerm, ferry, heure, dest); 
				break;
		}
		ret = Envoie_Recevoir(msg_envoi, hSocket, &msg_reception, MTU);
		if(msg_reception.TypeRequete == PAUSE)
		{
			printf("Le serveur est en pause! ");
		}
		if(msg_reception.TypeRequete == END_OF_CONNEXION)
		{
			close(hSocket);
			exit(0);
		}
		if(msg_reception.TypeRequete == FAIL)
			printf("Problème lors de la modification d'un terminal\n");
		sleep(5);
		
	}while(valeur != 3 && msg_reception.TypeRequete == FAIL );
	printf("Le ferry a accosté avec succes!\n");
	return msg_reception.TypeRequete;
}

//signal ctrl-c
void handlerSigint(int sig)
{
	printf("Client interrompu par utilisateur");
	close(hSocket);
	exit(1);
}

void * fctTest_conn(void * param)
{
/*	while(1)*/
/*	{*/
/*		sleep(5);*/
/*		msg_envoi.TypeRequete = TEST_CONNECTIVITY;*/
/*		strcpy(msg_envoi.Msg,""); */
/*		*/
/*		int taille_paquet;*/
/*		int ret;*/
/*		taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	*/
/*		ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocket);*/
/*		check_erreur(ret, NULL, 0, &hSocket);*/
/*		*/
/*		ret = send(hSocket, "coucou", 6, 0);*/
/*		if(ret == -1)*/
/*		{*/
/*			printf("Fermeture du client !!");*/
/*			close(hSocket);*/
/*			exit(0);*/
/*		}*/
/*	}	*/
}
