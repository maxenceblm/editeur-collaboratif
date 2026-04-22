# Éditeur Collaboratif

## Membre de l'équipe
- Maxence Balme

## Compilation
cd editor
./gradlew classes


# Lancement

### Tâche 1 — Serveur centralisé PULL
./gradlew runServer
./gradlew run  (client GUI simple )

### Tâche 2 — Mode PUSH
./gradlew runServerPush
./gradlew runClientPush 

### Tâche 3 & 4 — Fédération 
./gradlew runServerFederate
./gradlew runServerFederate -Pport=12346
./gradlew runServerFederate -Pport=12347

### Tâche 5 — Fédération avec  Master/Slave
./gradlew runServerMaster 
./gradlew runServerSlave
./gradlew runServerSlave -Pport=12348
./gradlew runServerDispatch 
./gradlew runClientPush

### Tâche 6 — Tests de charge
./test_convergences.sh

### Tâche 7 — Tolérance aux pannes
./gradlew runServerReplicated -Pid=1
./gradlew runServerReplicated -Pid=2
./gradlew runServerReplicated -Pid=3