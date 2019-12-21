#ifndef        SOCKETSUTILITIES_H
#define        SOCKETSUTILITIES_H

#include <stdio.h> 
#include <stdlib.h> 
#include <string.h>
#include <ctype.h>
#include <unistd.h> 
#include <sys/types.h> 
#include <sys/socket.h> 
#include <netdb.h> 
#include <errno.h> 
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <time.h> 
#include <pthread.h>
#include <signal.h>


#include "protocol_SFVMP.ini"




//Fonction reseau
int create_socket(char * , unsigned long , struct sockaddr_in* );
int EnvoieMsg(void*, int, int);
int ReceptionMsg(void *, int* ,int ,int );
void fermeture_socket_tout(int *, int, int*);
void check_erreur(int, int* , int, int *);
int mise_en_reseau(int * , int * , struct sockaddr *);
int connect_to_server(int*, struct sockaddr *);
int rech_MTU (int);
char marqueurRecu (char *, int );
int Envoie_Recevoir(struct Message, int, struct Message *, int);


int ReceptionMsg_taille(int socket, void *message, int taille_msg, int *hSocketConnectee);

void MiseVecteur(char *, char **, char *);



//void AfficheMenu();



#endif

