#!/bin/bash

# Lancer master + slaves + dispatch avant ce script

echo "Lancement de 3 clients automatiques..."
java -cp build/classes/java/main Client.AutoClient Client1 10 &
java -cp build/classes/java/main Client.AutoClient Client2 10 &
java -cp build/classes/java/main Client.AutoClient Client3 10 &

wait
echo "Tous les clients ont terminé."
