all:	Ser2 Cli2 SocketsUtilities.o AccessPort.o file_manager.o liste.o

SocketsUtilities.o:	SocketsUtilities.c
	echo "Creation de SocketsUtilities"
	gcc -c SocketsUtilities.c -lnsl -lsocket -lpthread -D SUN -m64
	
AccessPort.o:	AccessPort.c
	echo "Creation de AccessPort"
	gcc -c AccessPort.c -lnsl -lsocket -lpthread -D SUN -m64
	
file_manager.o:	file_manager.c
	echo "Creation de file_manager"
	gcc -c file_manager.c -lnsl -lsocket -lpthread -D SUN -m64
	
liste.o:	liste.c
	echo "Creation de liste"
	gcc -c liste.c -lnsl -lsocket -lpthread -D SUN -m64

Ser2:	Ser2.c SocketsUtilities.o AccessPort.o file_manager.o liste.o
	echo "Creation du Serveur"
	gcc Ser2.c SocketsUtilities.o AccessPort.o file_manager.o liste.o -lnsl -lsocket -lpthread -o Ser2

Cli2:	Cli2.c SocketsUtilities.o file_manager.o
	echo "Creation du client"
	gcc Cli2.c SocketsUtilities.o file_manager.o -lnsl -lsocket -lpthread -o Cli2
	
clean:	
	rm *.o Cli2 Ser2

open:
	gedit Cli2.c Ser2.c *.c *.h makefile protocol_SFVMP.ini &
