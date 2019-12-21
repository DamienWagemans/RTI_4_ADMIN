

#include "SocketsUtilities.h"
#include "AccessPort.h"
#include "file_manager.h"
#include "liste.h"

#define SERVEUR_EN_PAUSE 100
#define SERVEUR_EN_MARCHE 200

#define affThread(num, msg) printf("th_%s> %s\n", num, msg)

//liste
typedef struct adresseclient adresseclient;
struct adresseclient
{
	char adresse_ip[20];
    adresseclient *suivant;
};
typedef struct listclient listclient;
struct listclient
{
    adresseclient *premier;
};
listclient *initialisation();
void afficherListe(listclient *liste, char * list_tostring);
void insertion(listclient *liste, char *adresse);
char * client_connecte;
listclient *maListe;
int stop = 0;



//------------Thread---------------
//variables
pthread_mutex_t mutexIndiceCourant;
pthread_mutex_t mutex_FILE;
pthread_mutex_t mutex_BUSY_TERM;
pthread_cond_t condIndiceCourant;
pthread_t threadHandle[NB_MAX_CLIENTS]; 

pthread_t thread_Admin;
pthread_t thread_Secour;
 
//fonctions
void * fctThread(void * param);
void * fct_Admin(void * param);

char * getThreadIdentity();
void creation_threads();
void association_client_thread(int);


//signal ctrl-c
void handlerSigint(int sig);
//signal sig trap pour tuer thread
void handlerSigTrap( int sig);

//pour la lecture fichier config
long PORT;
char ip[20];
char ip_secour[20];

int indiceCourant;
int hSocketConnectee[NB_MAX_CLIENTS]; /* Sockets pour clients*/
int hSocketEcoute, hSocketEcouteAdmin, hSocketEcouteSecour;
int BusyTerm[NB_MAX_CLIENTS];

int mtu;

struct Message msg_envoi;
struct Message msg_reception;


struct sockaddr_in adresseSocket;
struct sockaddr_in adresseSocketAdmin;


int hSocketService = -1, hSocketServiceAdmin =-1;
int etat_serveur = SERVEUR_EN_MARCHE;

char sep [3]= ";";
char log_term[100];

int main() 
{
	maListe = initialisation();
	client_connecte = (char *) malloc(200);

	
	int i,j, retRecv; 
	

	
	int tailleSockaddr_in;
	int ret, * retThread;

	
	char * login;
	
	
	//lecture fichier config
	char *config_lect;
	//PORT
	config_lect = lect_config("Port_service");
	PORT = strtol(config_lect, NULL, 10);
	//IP
	config_lect = lect_config("ip_address");
	strcpy(ip,config_lect);
	//ipsecour
	config_lect = lect_config("ip_secour");
	strcpy(ip_secour,config_lect);
	
	ecrire_log("LOG_TERM","Récuperation des informations de le fichier de configuration\n");
	
	free(config_lect);
	
	//armement SIGINT
	struct sigaction act;
	act.sa_handler = handlerSigint;
	sigemptyset(&act.sa_mask);
	act.sa_flags = 0;
	sigaction(SIGINT, &act, 0);
	
	indiceCourant = -1;
	
	//init mutex / var de cond
	pthread_mutex_init(&mutex_FILE, NULL);
	pthread_mutex_init(&mutexIndiceCourant, NULL);
	pthread_mutex_init(&mutex_BUSY_TERM, NULL);
	pthread_cond_init(&condIndiceCourant,NULL);
	
	//creation de la socket
	hSocketEcoute = create_socket(ip, PORT, &adresseSocket);
	hSocketEcouteAdmin = create_socket(ip_secour, PORT+1, &adresseSocketAdmin);
	
	check_erreur(hSocketEcoute, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketEcoute);
	sprintf(log_term,"Creation de la socket sur le port %d avec l ip %s\n",PORT, ip);
	ecrire_log("LOG_TERM", log_term);
	printf("SERVEUR : socket ok\n");
	

	
	//Var de cond, mutex, thread creation/initialisation
	creation_threads();
	
	
	//association de l'ip et du port a la socket
	ret = bind(hSocketEcoute,(struct sockaddr *)&adresseSocket,sizeof(struct sockaddr_in));
	check_erreur(ret, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketEcoute);
	ecrire_log("LOG_TERM", "Bind OK");
	printf("SERVEUR : bind ok\n");
	
	do
	{
		ret = mise_en_reseau(&hSocketService, &hSocketEcoute , (struct sockaddr *)&adresseSocket);
		check_erreur(ret, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketEcoute);
		//Mise en ecoute sur le port puis en attente d'un client
		insertion(maListe,inet_ntoa(adresseSocket.sin_addr));	
   		afficherListe(maListe,client_connecte);
   		printf("Voila liste : \n%s !",client_connecte);
		//recuperation du MTU
		mtu = rech_MTU(hSocketService);
		//lier un thread a un client, le nom parle de lui meme
		association_client_thread(hSocketService);

			

	}
	while (stop==0);
	
	/* 10. Fermeture de la socket d'ecoute */ 
	close(hSocketEcoute); /* Fermeture de la socket */ 
	printf("Socket serveur fermee\n");
	puts("Fin du thread principal");
	return 0; 
}
		/* -------------------------------------------------------- */ 
void * fctThread (void *param)
{
	char *buf = (char*)malloc(100);
	char copie_reception[100];
	char * vect[3];//contiendra le num de term, login, mdp
	int vr = (int)(param), finDialogue=0, i, iCliTraite;
	int temps, ret, numterm = -1;
	char * numThr = getThreadIdentity();
	int hSocketServ, taille_paquet;
	int authentification = 0;
	struct Heure times;
	
	//armement sigtrap
	struct sigaction A;
	A.sa_handler = handlerSigTrap;
	A.sa_flags = 0;
	sigemptyset (&A.sa_mask);
	sigaction (SIGTRAP, &A, NULL);
	
	//masquage de tt les signaux sauf SIGTRAP
  	sigset_t masque;
  	sigfillset(&masque);
  	sigdelset(&masque, SIGTRAP);
  	sigprocmask(SIG_SETMASK, &masque, NULL);
	
	while(1)
	{
		/* 1. Attente d'un client à traiter */
		pthread_mutex_lock(&mutexIndiceCourant); 
		while (indiceCourant == -1)
			pthread_cond_wait(&condIndiceCourant, &mutexIndiceCourant); 
		iCliTraite = indiceCourant; 
		indiceCourant=-1;
		hSocketServ = hSocketConnectee[iCliTraite]; 
		pthread_mutex_unlock(&mutexIndiceCourant);
		sprintf(buf,"Je m'occupe du numero %d ...", iCliTraite);
		affThread(numThr, buf); 
		
		/* 2. Dialogue thread-client */
		finDialogue=0; 
		do
		{
			memset(&msg_reception,0,sizeof(struct Message));
			ret = ReceptionMsg((void*) &msg_reception,&hSocketConnectee[0],hSocketServ,mtu);
			
			if (ret==0) 
			{
				sprintf(buf,"Le client est parti !!!");
				
				//signaler que le terminal se libère si il a été connecté
				if(numterm != -1)
				{
					pthread_mutex_lock(&mutex_BUSY_TERM);
					BusyTerm[numterm-1] = -1;
					pthread_mutex_unlock(&mutex_BUSY_TERM);
				}

				affThread(numThr, buf); 
				finDialogue=1;
				break;
			} 
			
			if(etat_serveur == SERVEUR_EN_MARCHE && stop == 0)
			{

				switch (msg_reception.TypeRequete)
				{
					case LOGIN:
						printf("Dans LOGIN : receive : %s", msg_reception.Msg);
					
						//car MiseVecteurdetruit la chaine de base
						strcpy(copie_reception, msg_reception.Msg);
						//recup du num de term, login, mdp
						MiseVecteur(copie_reception, &vect[0], sep);


						numterm = strtol(vect[2], NULL, 10);
						pthread_mutex_lock(&mutex_BUSY_TERM);
					
						if(BusyTerm[numterm-1] == 1)
						{
							//cela signifie que ce terminal est occupé..
							ret = -3;
							pthread_mutex_unlock(&mutex_BUSY_TERM);
						}
						else
						{
							pthread_mutex_unlock(&mutex_BUSY_TERM);
							pthread_mutex_lock(&mutex_FILE); 
							ret = verif_login(msg_reception.Msg, sep);
							pthread_mutex_unlock(&mutex_FILE); 
						}	
					
					

						switch(ret)
						{
							case(1):
							{
								printf("Dans LOGIN : Username/MDP Ok !\n");
								msg_envoi.TypeRequete = LOGIN_OK;
								authentification = 1;
								BusyTerm[numterm-1] = 1;
								break;
							}
							case(-1):
							{
								printf("Dans LOGIN : Username incorrect\n");
								msg_envoi.TypeRequete = LOGIN;
								strcpy(msg_envoi.Msg, "Anthentification : Username incorrect\n");
								authentification = 0;
								break;
							}
							case(-2):
								printf("Dans LOGIN : MDP incorrect\n");
								strcpy(msg_envoi.Msg, "Authentification : MDP incorrect\n");
								msg_envoi.TypeRequete = LOGIN;
								authentification = 0;
								break;
							case(-3):
								printf("Dans LOGIN : terminal occupé\n");
								strcpy(msg_envoi.Msg, "Authentification : terminal occupé\n");
								msg_envoi.TypeRequete = LOGIN;
								authentification = 0;
								break;
							
						}	
						strcat(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case ASK_NEXT_DEPARTURE:
						printf("------------Ask Next Departure------------\n");
						ecrire_log("LOG_TERM", "Getting Next Departure\n");
					
						pthread_mutex_lock(&mutex_FILE); 
						ret = AskNextDeparture(msg_reception.Msg, ";");
						pthread_mutex_unlock(&mutex_FILE); 
					
						msg_envoi.TypeRequete = ret;
						strcat(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case ASK_BEGIN_LOADING:
						printf("ASK_BEGIN_LOADING\n");
						ecrire_log("LOG_TERM", "Getting begin loading\n");
					
						RecupererHeure(msg_reception.Msg, &times);
					
						pthread_mutex_lock(&mutex_FILE); 
						msg_envoi.TypeRequete = AskNotifyLoading(times,45);  
						pthread_mutex_unlock(&mutex_FILE);
					
						strcpy(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case NOTIFY_END_LOADING:
						printf("NOTIFY_END_LOADING\n");
						ecrire_log("LOG_TERM", "Notify End Loading\n");
					
						RecupererHeure(msg_reception.Msg, &times);
					
						pthread_mutex_lock(&mutex_FILE);
						msg_envoi.TypeRequete = AskNotifyLoading(times,15); 
						pthread_mutex_unlock(&mutex_FILE);
					
						strcpy(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case FERRY_LEAVING:
						printf("FERRY_LEAVING\n");
						ecrire_log("LOG_TERM", "Ferry Leaving\n");
					
						RecupererHeure(msg_reception.Msg, &times);
					
						pthread_mutex_lock(&mutex_FILE);
						msg_envoi.TypeRequete = FerryLeaving(times, msg_reception.Msg);
						pthread_mutex_unlock(&mutex_FILE);
					
						strcpy(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case ASK_FOR_FERRY:
						printf("ASK_FOR_FERRY\n");
						ecrire_log("LOG_TERM", "Ask for ferry\n");
					
						pthread_mutex_lock(&mutex_FILE);
						msg_envoi.TypeRequete = AskForFerry(msg_reception.Msg);
						pthread_mutex_unlock(&mutex_FILE);
					
						strcpy(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					case FERRY_ARRIVING:
						printf("FERRY ARRIVING\n");
						ecrire_log("LOG_TERM", "Ferry Arriving\n");
				
						pthread_mutex_lock(&mutex_FILE);
						msg_envoi.TypeRequete = FerryArriving(msg_reception.Msg);
						pthread_mutex_unlock(&mutex_FILE);
					
						strcpy(msg_envoi.Msg, "\r\n");
						taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
						ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
						check_erreur(ret, NULL, 0, &hSocketServ);
						break;
					
					case END_OF_CONNEXION:
						close(hSocketServ);
						hSocketConnectee[iCliTraite] = -1;
						finDialogue = 1;
						break;

				}
			}
			else
			{
				if(etat_serveur == SERVEUR_EN_PAUSE)
				{
					printf("Le serveur est en pause \n");
					msg_envoi.TypeRequete = PAUSE;	
					strcpy(msg_envoi.Msg, "\r\n");
					taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
					ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketServ);
					check_erreur(ret, NULL, 0, &hSocketServ);				
				}
				else
				{
					printf("Serveur STOP");
					msg_envoi.TypeRequete = END_OF_CONNEXION;	
					close(hSocketServ);
					exit(0);
				}	
				
			}
			
		}
		while (!finDialogue);
		
		
		/* 3. Fin de traitement */ 
		pthread_mutex_lock(&mutexIndiceCourant);
		hSocketConnectee[iCliTraite]=-1;
		pthread_mutex_unlock(&mutexIndiceCourant); 
	}
	
	free(buf);
	close (hSocketServ);
	return (void *)vr; 
}




void creation_threads()
{
	int ret;
	
	for(int i = 0;i < NB_MAX_CLIENTS; i++)
	{
		hSocketConnectee[i] = -1;
		BusyTerm[i] = -1;
	}
			
	for(int i = 0;i < NB_MAX_CLIENTS; i++)
	{
		ret = pthread_create(&threadHandle[i], NULL, fctThread,NULL);
		ret = pthread_detach(threadHandle[i]);
		printf("Thread secondaire %d créé\n", i);
	}
	
	pthread_create(&thread_Admin, NULL, fct_Admin,NULL);
	pthread_detach(thread_Admin);
	
}

void association_client_thread(int hSocketService)
{
	int j, taille_paquet,ret;
	
	for (j=0; j<NB_MAX_CLIENTS && hSocketConnectee[j] !=-1; j++);
	if (j == NB_MAX_CLIENTS) 
	{
		printf("Plus de connexion disponible\n");
		
		msg_envoi.TypeRequete = DENY_OF_CONNEXION;
		taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
		ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocketService);
		check_erreur(ret, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketService);
		
	} 
	else 
	{
		// Il y a une connexion de libre  
		printf("Connexion sur la socket num. %d\n", j); 
		pthread_mutex_lock(&mutexIndiceCourant); 
		hSocketConnectee[j] = hSocketService; 
		indiceCourant=j; 
		pthread_mutex_unlock(&mutexIndiceCourant); 
		pthread_cond_signal(&condIndiceCourant);	
	}
}


char * getThreadIdentity() 
{
	unsigned long numSequence; char *buf = (char *)malloc(30);
	numSequence = ( pthread_self( ) ); 
	sprintf(buf, "%d.%u", getpid(), numSequence);
	return buf;
} 


//signal ctrl-c
void handlerSigint(int sig)
{
	ecrire_log("LOG_TERM", "Interruption par le client : SIGINT\n");
	for(int j = 0; j< NB_MAX_CLIENTS; j++)
	{
		if(hSocketConnectee[j]!=-1)
		{
			msg_envoi.TypeRequete = END_OF_CONNEXION;
			EnvoieMsg ((void*)&msg_envoi, sizeof(int), hSocketConnectee[j]);
		}
	}
	for(int i = 0; i<NB_MAX_CLIENTS; i++)
	{
		pthread_kill(threadHandle[i], SIGTRAP);
	}
	check_erreur(ERROR_SIGINT, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketEcoute);
}

void handlerSigTrap( int sig)
{
	printf("Je suis le thread %s et je meurs\n", getThreadIdentity());
	pthread_exit(NULL);	
}









//ADMIN////////////////////////////


void * fct_Admin (void *param)
{
	//hSocketEcouteAdmin = create_socket(ip_secour, PORT+1, &adresseSocketAdmin);
	//check_erreur(hSocketEcouteAdmin, &hSocketConnectee[0], NB_MAX_CLIENTS, &hSocketEcouteAdmin);
	//printf("SERVEUR ADMIN: socket ok\n");
	
	struct Message msg_envoi_Admin;
	struct Message msg_reception_Admin;
	
	//initialisation_reseau_serveur(ip_secour,&hSocketEcouteAdmin,&adresseSocketAdmin,PORT+1);
	//mise_en_reseau_serveur(&hSocketServiceAdmin, &hSocketEcouteAdmin, &adresseSocketAdmin);
	
	bind(hSocketEcouteAdmin,(struct sockaddr *)&adresseSocketAdmin,sizeof(struct sockaddr_in));
	
		
		

	


	
	int tmp;
	int type;

	char message_reception[200];
	char message_reponse[200];
		int choix = 1;
	char * type_message = (char *) malloc(200);
	char * data_message = (char *) malloc(200);
	printf("je suis le thread administrateur");
	while(1)
	{
		mise_en_reseau(&hSocketServiceAdmin, &hSocketEcouteAdmin , (struct sockaddr *)&adresseSocketAdmin);
		while(choix)
		{
	
			printf("1\n");
			memset(message_reception,0,200);
			memset(message_reponse,0,200);
			memset(type_message,0,200);
			memset(data_message,0,200);
			printf("2\n");
			recv(hSocketServiceAdmin,&message_reception,200, 0);
			printf("Message recu : %s\n", message_reception);
			printf("3\n");
			type_message = strtok(message_reception,";");
			data_message = strtok(NULL,"");		
			printf("Voila le type %s de taille %d, voila les data %s de taille %d!\n",type_message,strlen(type_message),message_reception,strlen(data_message));
				
			if(strcmp(type_message,"logina") == 0)type = 1;		
			else if(strcmp(type_message,"lclients") == 0)type = 2;		
			else if(strcmp(type_message,"pause") == 0)type = 3;		
			else if(strcmp(type_message,"stop") == 0)type = 4;		
			else if(strcmp(type_message,"close") == 0)type = 5;					
		
			switch(type)
			{
				case 1 : 
					printf("Je suis la %s\n", data_message);

					if(strcmp(data_message,"Admin;Admin") == 0)
						strcpy(message_reponse,"ack,login\n");

					else
						strcpy(message_reponse,"fail,login\n");					

					if(send(hSocketServiceAdmin,&message_reponse,strlen(message_reponse)+1,0) == -1)
						exit(0);
					else 
						printf("Send socket connectee OK\n");				
				break;
				case 2 : 
				   		afficherListe(maListe,client_connecte);
				   		printf("Voila liste : \n%s !",client_connecte);
				   		strcat(client_connecte,"\n");
				   		strcpy(message_reponse,client_connecte);
						if(send(hSocketServiceAdmin,&message_reponse,strlen(message_reponse)+1,0) == -1)
							exit(0);
						else 
							printf("Send socket connectee OK\n");	
				break;
				case 3 : 
					if(etat_serveur == SERVEUR_EN_MARCHE)
					{
						etat_serveur = SERVEUR_EN_PAUSE;
						strcpy(message_reponse,"pause;LE SERVEUR EST EN PAUSE !\n");
					}
					else
					{
						etat_serveur = SERVEUR_EN_MARCHE;
						strcpy(message_reponse,"pause;LE SERVEUR EST RELANCE !\n");
					}		

					if(send(hSocketServiceAdmin,&message_reponse,strlen(message_reponse)+1,0) == -1)
						exit(0);
					else 
						printf("Send socket connectee OK\n");			
					break;
				case 4 : 
					stop = 1;
					strcpy(message_reponse,"ack;requete stop bien recu !\n");
					
					if(send(hSocketServiceAdmin,&message_reponse,strlen(message_reponse)+1,0) == -1)
						exit(0);
					else 
						printf("Send socket connectee OK\n");	
					
					
					close(hSocketEcoute);
					//exit(1);			
					break;					
			}
		}	
	}
	
}

listclient *initialisation()
{
    listclient *Listclient = malloc(sizeof(*Listclient));
    adresseclient *Adresseclient = malloc(sizeof(*Adresseclient));

    if (Listclient == NULL || Adresseclient == NULL)
    {
        exit(EXIT_FAILURE);
    }

    strcpy(Adresseclient->adresse_ip,"");
    Adresseclient->suivant = NULL;
    Listclient->premier = Adresseclient;

    return Listclient;
}

void insertion(listclient *liste, char *adresse)
{
    /* Création du nouvel élément */
    adresseclient *nouveau = malloc(sizeof(*nouveau));
    if (liste == NULL || nouveau == NULL)
    {
        exit(EXIT_FAILURE);
    }
    strcpy(nouveau->adresse_ip,adresse);

    /* Insertion de l'élément au début de la liste */
    nouveau->suivant = liste->premier;
    liste->premier = nouveau;
}
void afficherListe(listclient *liste, char * list_tostring)
{
    if (liste == NULL)
    {
        exit(EXIT_FAILURE);
    }

    adresseclient *actuel = liste->premier;
	strcpy(list_tostring,"");
    while (actuel != NULL)
    {
        printf("%s\n", actuel->adresse_ip);
        strcat(list_tostring,actuel->adresse_ip);
        strcat(list_tostring,",");
        actuel = actuel->suivant;
    }
    		printf("Je suis sorti de la boucle\n");
}










	
