# habiTv

[![Java](https://img.shields.io/badge/Java-1.7+-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.0+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-lightgrey.svg)]()

habiTv est un logiciel permettant de télécharger automatiquement et régulièrement des vidéos sur les sites de Replay TV.

## 📋 Description

Le but de habiTv est de ne pas avoir à télécharger puis exporter manuellement via une interface graphique des vidéos disponibles régulièrement sur le Replay, mais que tout soit géré automatiquement en tâche de fond.

Avec habiTv, vous spécifiez les séries/documentaires/programmes que vous souhaitez récupérer et habiTv vérifie régulièrement si de nouveaux épisodes sont disponibles. Si c'est le cas, il les télécharge automatiquement.

Il est ensuite possible de spécifier une série de commandes à exécuter dès qu'un épisode est disponible pour par exemple l'exporter vers un support (encodage de la vidéo, transfert FTP, rangement, ...).

## ✨ Fonctionnalités

### Modes d'utilisation

- **IHM (Interface Graphique)** : habiTv propose une interface visuelle pour sélectionner les programmes à télécharger et suivre les téléchargements. L'application se loge dans la barre des tâches et affiche des notifications pour prévenir qu'un nouvel épisode est téléchargé.

- **CLI (Ligne de Commande)** : habiTv propose plusieurs paramètres pour rechercher et télécharger des épisodes en ligne de commande. L'application peut se lancer en mode démon depuis la ligne de commande et logger dans un fichier.

### Fournisseurs supportés

habiTv supporte actuellement les fournisseurs suivants :

- **CanalPlus** (Canal+, D8, D17)
- **Pluzz** (France 2, 3, 4, ô)
- **Arte**
- **BeinSport**
- **Lequipe.fr**
- **6play**
- **SFR**
- **WAT**
- **GlobalNews**
- **MLSSoccer**
- **Footyroom**
- **Clubic**
- **RSS** : n'importe quel flux RSS contenant des liens vers des vidéos à télécharger (HTTP, FTP, Bittorent, Youtube, Dailymotion ...)
- **File** : téléchargement depuis des fichiers locaux
- **Email** : téléchargement depuis des emails
- **Youtube**

### Système de plugins

habiTv est personnalisable grâce à un système de plugin modulaire :

- **Plugin Provider** : listent les catégories disponibles et gèrent le téléchargement des épisodes (ex: Arte, CanalPlus, Pluzz)
- **Plugin Downloader** : encapsulent les utilitaires de téléchargement pour une meilleure interaction avec habiTv (youtube-dl, rtmpDump, curl, aria2c, ffmpeg, adobeHDS)
- **Plugin Exporter** : améliorent l'interaction entre les utilitaires permettant d'exporter les vidéos et habiTv (ffmpeg, curl, cmd)

## 🚀 Installation

### Prérequis

- Java 1.7 ou supérieur
- Maven 3.0+ (pour la compilation)
- Outils externes (selon les plugins utilisés) :
  - youtube-dl
  - rtmpDump
  - curl
  - aria2c
  - ffmpeg

### Compilation

```bash
# Cloner le repository
git clone https://github.com/Mika3578/habitv.git
cd habitv

# Compiler le projet
mvn clean install
```

### Exécution

```bash
# Mode IHM (interface graphique)
java -jar application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar

# Mode CLI
java -jar application/habiTv/target/habiTv-4.1.0-SNAPSHOT.jar [options]
```

## 📖 Utilisation

### Configuration

habiTv utilise deux fichiers de configuration XML :

- `config.xml` : configuration générale de l'application
- `grabconfig.xml` : configuration des catégories à télécharger

Les fichiers de configuration sont placés dans `%USER_DIR%/habitv` sauf si un des fichiers est présent dans le répertoire contenant l'exécutable.

### Exemple de configuration

Voir les fichiers d'exemple dans `application/core/xsd/` pour la structure des fichiers de configuration.

## 🏗️ Architecture

Le projet est organisé en modules Maven :

```
habitv/
├── application/          # Application principale
│   ├── core/            # Cœur métier
│   ├── consoleView/     # Interface ligne de commande
│   ├── trayView/        # Interface graphique (systray)
│   └── habiTv/          # Launcher principal
├── fwk/                 # Framework
│   ├── api/             # Interfaces et DTOs
│   └── framework/       # Utilitaires et helpers
└── plugins/              # Plugins (providers, downloaders, exporters)
```

## 🛠️ Technologies

- **Java 1.7+**
- **Maven** (gestion des dépendances)
- **Log4j** (logging)
- **JavaFX** (interface graphique)
- **Jsoup** (parsing HTML)
- **Jackson** (JSON)
- **JAXB** (XML)
- **Rome** (RSS)
- **Guava** (utilitaires)
- **Commons CLI** (ligne de commande)

## 🤝 Contribution

Les contributions sont les bienvenues ! Veuillez consulter le fichier [CONTRIBUTING.md](CONTRIBUTING.md) pour plus d'informations.

## 📝 TODO

- Support torrent
- RSS content matcher
- Plugins TMC, NT1, Eurosport
- Internationalisation (français/anglais)
- Tests d'automatisation des téléchargements

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👤 Auteur

Développé par dabi

## 🙏 Remerciements

Merci à tous les contributeurs qui ont aidé à améliorer habiTv !
