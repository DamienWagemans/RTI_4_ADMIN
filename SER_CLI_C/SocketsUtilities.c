#include "SocketsUtilities.h"


//Cette fonction prend un paramètre l'hostname de la machine serveur, le port d'ecoute, ainsi que la structure sockaddr_in. 
//1 : creation de la socket
//2 : acquisition des infos de la machine
//3 : preparation de la structure sockaddr_in : copie de l'adresse IP ainsi que du port
//
// Valeur de retour :: NEGATIVE : ERREUR :  -100  -->   Erreur lors de la creation de la socket
//                       									  -101  -->   Erreur d'acquisition des infos sur la machine
//
//									   POSTIVIE : descripteur de la socket d'ecoute

int create_socket(char * hostname, unsigned long port , struct sockaddr_in* adresseSocket)
{
	int hSocket;
	struct hostent * Host;
	struct in_addr adresseIP; /* Adresse Internet au format reseau*/ 
	//creation de la socket
	hSocket = socket(AF_INET, SOCK_STREAM, 0);
	if (hSocket == -1)
	{
		return -100; // -1 = erreur creation socket
	}
	
	//acquisition des infos sur l'ordinateur distant
	if ( (Host = gethostbyname(hostname))==0)
	{
		return -101; // -2 = erreur acquisition infos sur l'ordinateur distant
	}
	memcpy(&adresseIP, Host->h_addr, Host->h_length); 

	//preparation de la structure 
	memset(adresseSocket, 0, sizeof(struct sockaddr_in)); 
	adresseSocket->sin_family = AF_INET; /* Domaine */
	adresseSocket->sin_port = htons(port);
	memcpy(&adresseSocket->sin_addr, Host->h_addr,Host->h_length);
	
	return hSocket;
}

//cette fonction prend en paramètre l'adresse d'un vecteur d'entier correspondant à des sockets
// chaque socket ouverte (!=-1) sera fermée
void fermeture_socket_tout(int * vec, int nbr, int * hSocketEcoute)
{
	for(int i = 0; i<nbr; i++)
	{
		if(((*vec)+i) != -1)
		{
				printf("Fermeture de la socket : %d\n", (*vec)+i);
				close((*vec)+i);
		}

	}
	if(*hSocketEcoute != -1)
	{
		close(*hSocketEcoute);
	}
}

//fonction de gestion d'erreur, elle s'occupe egalement de fermer les sockets si necessaire. si cette fonction est appelée 
// depuis le client, alors les paramètres 2 et 3 doivent etre respectivement a NULL et 0  . Le 4ieme paramtre correspond a 
//socket du client. SI appel depuis le serveur: paramètre -2 adresse du vecteur d'entrier correspond au socket hSocketConnectee
// -3: NB_MAX_CLIENTS  -4 hSocketEcoute
void check_erreur(int code, int* vec_socket, int nbr, int * hSocketEcoute)
{
		if(code < 0)
		{
			switch(code)
			{
				case ERROR_CREATING_SOCKET : printf("Erreur creation socket\n");break;
				case ERROR_GETHOSTBYNAME : printf("Erreur gethostbyname\n");break;
				case ERROR_BIND : printf("Erreur bind\n");break;
				case ERROR_LISTEN : printf("Erreur listen\n");break;
				case ERROR_ACCEPT : printf("Erreur accept\n");break;
				case ERROR_MTU : printf("Erreur lors de l'acquisition du MTU\n");break;
				case ERROR_BAD_LOGIN : printf("Trop de tentative de login\n");break;
				case ERROR_ENVOI : printf("Erreur lors de l'envoi\n");break;
				case ERROR_RCV : printf("Erreur lors du receive\n");break;
				
				case ERROR_CONNECT_BADF : printf("Erreur connect : descripteur invalide\n");break;
				case ERROR_CONNECT_NOTSOCK : printf("Erreur connect : le descripteur n'est pas associé a une socket\n");break;
				case ERROR_CONNECT_NETUNREACH : printf("Erreur connect : reseau injoignable\n");break;
				case ERROR_CONNECT_ISCONN : printf("Erreur connect : socket deja connectee\n");break;
				case ERROR_CONNECT_ADDRINUSE : printf("Erreur connect : addresse deja utilisee\n");break;
				case ERROR_CONNECT_FAULT : printf("Erreur connect : structure sockaddr incorrecte\n");break;
				case ERROR_CONNECT_INTR : printf("Erreur connect : fonction interrompue\n");break;
				case ERROR_CONNECT_TIMEDOUT : printf("Erreur connect : time out !\n");break;
				case ERROR_CONNECT_UNKNOWN : printf("Erreur connect : Pas de serveur\n");break;
				
				case ERROR_SIGINT : printf("Interrompu par l'utilisateur\n");break;

			}
			
			//dans serveur
			if( nbr != 0)
			{
				printf("Fermeture de toutes les sockets \n");
				fermeture_socket_tout(vec_socket, nbr, hSocketEcoute);
			}
			//dans thread/client
			else
			{
				printf("Fermeture socket ecoute\n");
				close(*hSocketEcoute);
			}
			exit(1);
		}
}




//le serveur se met en ecoute et se met en attente/accepte la connexion du client
int mise_en_reseau(int * hSocketService, int * hSocketEcoute, struct sockaddr * adresseIP )
{
		int tailleSockaddr_in;
		tailleSockaddr_in = sizeof(struct sockaddr_in);
		
		if (listen(*hSocketEcoute,SOMAXCONN) == -1)
		{
			return -102;
		}
		else printf("Listen socket OK\n");
	
	
		if ( (* hSocketService = accept(* hSocketEcoute, adresseIP, &tailleSockaddr_in) ) == -1) 
		{
			return -103;
		}
		else printf("Accept socket OK\n");
}





//connection du client vers le server
int connect_to_server(int * hSocket, struct sockaddr * adresseSocket)
{
		int tailleSockaddr_in = sizeof(struct sockaddr_in);
		if ((connect(*
		hSocket, (struct sockaddr *)adresseSocket, tailleSockaddr_in) )== -1) 
		{
			switch(errno)
			{
				case EBADF : return -104;
							 break;
				case ENOTSOCK : return -105;
							 break;
				case ENETUNREACH : return -106;
							 break;   
				case EISCONN : return -107;
							 break; 
				case EADDRINUSE : return -108;
							 break; 
				case EFAULT : return -109;
							 break; 
				case EINTR : return -110;
							 break; 
				case ETIMEDOUT : return -111;
							 break; 		 
				default : return -112; 
			} 
		}
}

//envoi d'un message d'un type quelconque
int EnvoieMsg (void* Message, int longueur_msg, int hSocket)
{
	int nbr_envoi;
	
	//envoi message 
	if (nbr_envoi = send(hSocket, Message, longueur_msg, 0) == -1)  
	{
		return -115;
	}
	else 
		printf("Send socket OK\n");
	return nbr_envoi;
}




//recherche la valeur du MTU dans la socket
int rech_MTU(int hSocket)
{
	unsigned int tailleO=sizeof(int);
	int tailleS = 0;
	
	if (getsockopt(hSocket, IPPROTO_TCP, TCP_MAXSEG,&tailleS,&tailleO) == -1)
	{
		return -113;
	}
		
	return tailleS;
}


int ReceptionMsg_taille(int socket, void *message, int taille_msg, int *hSocketConnectee) {
    int byteReceive = 0;
    int byteTotal = 0;

    do  {
        if((byteReceive = recv(socket, ((char *)message + byteTotal), taille_msg, 0)) == -1) {
            check_erreur(ERROR_RCV, hSocketConnectee, NB_MAX_CLIENTS, &socket);
        } else {
            byteTotal += byteReceive;
        }
    } while(byteReceive != 0 && byteTotal < taille_msg);

    return byteReceive;
}

//reception d'un message avec boucle de reception tant que caractère de fin de chaines pas trouvés
int ReceptionMsg(void *data, int* hSocketConnectee,int hSocket,int mtu)
{
	int taille_message,taille_reseau;
	int finDetectee = 0;
	char buf [1000];
	char * msgClient;

	taille_message = 0;
	
	do
	{
		memset(buf,0,1000);
		if ( (taille_reseau = recv(hSocket, (char *)buf, mtu, 0)) == -1)
			check_erreur(ERROR_RCV, hSocketConnectee, NB_MAX_CLIENTS, &hSocket);
		else
		{
			finDetectee = marqueurRecu (buf, taille_reseau);
			memcpy((void *)data + taille_message, buf,taille_reseau);
			taille_message += taille_reseau;
			//printf("Nombre de bytes recus = %d | ", taille_reseau );
			//printf("Taille totale msg recu = %d\n", taille_message );
		}
	}while(taille_reseau !=0 && taille_reseau != -1 && !finDetectee);
	
	return taille_reseau;
}

//fonction qui determine si les caractères \r\n sont présents ou non a la fin du paquet recu
char marqueurRecu (char *m, int nc)
{
	static char demiTrouve=0;
	int i;
	char trouve=0;
	if (demiTrouve==1 && m[0]=='\n') return 1;
	else demiTrouve=0;
	for (i=0; i<nc-1 && !trouve; i++)
	if (m[i]=='\r' && m[i+1]=='\n') trouve=1;
	if (trouve) return 1;
	else if (m[nc]=='\r')
	{
	demiTrouve=1;
	return 0;
	}
	else return 0;
}



void MiseVecteur(char * chaine, char ** vect, char * sep)
{
	int i = 0;
	char * Mot;
	Mot = strtok(chaine, sep);
	vect[i] = Mot;
	i++;
	while(Mot != NULL){
		Mot = strtok(NULL, sep);
		vect[i] = Mot;
		i++;
	}
	free(Mot);
}

int Envoie_Recevoir(struct Message msg_envoi, int hSocket, struct Message * msg_reception, int MTU)
{
	int taille_paquet;
	int ret;
	taille_paquet = (sizeof(int) + strlen(msg_envoi.Msg));	
	ret = EnvoieMsg ((void*)&msg_envoi, taille_paquet, hSocket);
	check_erreur(ret, NULL, 0, &hSocket);

	//reception 
	memset(msg_reception,0,sizeof(struct Message));
	ret = ReceptionMsg((void*) msg_reception,NULL,hSocket,MTU);
	check_erreur(ret, NULL, 0, &hSocket);
	
	return ret;
}





