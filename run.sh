#!/bin/bash

# Script de lancement FarmIQ
# Usage: ./run.sh [main|test|quick]

echo "╔═══════════════════════════════════════╗"
echo "║   FarmIQ - User Management System    ║"
echo "║   Script de Lancement                 ║"
echo "╚═══════════════════════════════════════╝"
echo ""

# Vérifier que Maven est installé
if ! command -v mvn &> /dev/null; then
    echo "❌ Erreur: Maven n'est pas installé"
    echo "   Installez Maven: sudo apt install maven"
    exit 1
fi

# Vérifier que MySQL est accessible
if ! command -v mysql &> /dev/null; then
    echo "⚠️  Attention: MySQL CLI n'est pas trouvé"
    echo "   Assurez-vous que MySQL est installé et démarré"
fi

# Compiler le projet si nécessaire
if [ ! -d "target/classes" ]; then
    echo "📦 Compilation du projet..."
    mvn clean compile
    if [ $? -ne 0 ]; then
        echo "❌ Erreur lors de la compilation"
        exit 1
    fi
    echo "✅ Compilation réussie"
    echo ""
fi

# Déterminer quelle classe principale lancer
MODE=${1:-main}

case $MODE in
    main)
        echo "🚀 Lancement de l'interface graphique..."
        mvn exec:java -Dexec.mainClass="com.farmiq.Main"
        ;;
    test)
        echo "🧪 Lancement des tests automatiques..."
        mvn exec:java -Dexec.mainClass="com.farmiq.TestMain"
        ;;
    quick)
        echo "⚡ Lancement du menu interactif..."
        mvn exec:java -Dexec.mainClass="com.farmiq.QuickTestMain"
        ;;
    *)
        echo "❌ Mode inconnu: $MODE"
        echo ""
        echo "Usage: ./run.sh [main|test|quick]"
        echo "  main  - Interface graphique (par défaut)"
        echo "  test  - Tests automatiques"
        echo "  quick - Menu interactif"
        exit 1
        ;;
esac
