# habivt

habiTv est un logiciel permettant de télécharger automatiquement et régulièrement des vidéos sur les sites de Replay TV.

Le but étant de ne pas avoir à télécharger puis exporter manuellement via une interface graphique des vidéos disponibles régulièrement sur le Replay mais que tout soit géré automatiquement en tâche de fond. 


Avec habiTv, vous spécifiez les séries/documentaires/programmes que vous souhaitez récupérer et habiTv vérifie régulièrement si de nouveaux épisodes sont disponibles, si c'est le cas il les télécharge.

Il est ensuite possible de spécifier une série de commande à exécuter dès qu'un épisode est disponible pour par exemple l'exporter vers un support (encodage de la vidéo, transfert FTP, rangement, ...).



habiTv est utilisable de 2 manières : 

    IHM : IHM
        habiTv propose une interface visuelle pour sélectionner les programmes à télécharger et suivre les téléchargements
        habiTv se loge dans la barre des tâches et affiche des notifications pour prévenir qu'un nouvel épisode est téléchargé
    CLI : CLI
        habiTv propose plusieurs paramètres pour rechercher et télécharger des épisodes en ligne de commande
        habiTv peut se lancer en mode démon depuis la ligne de commande et log dans un fichier



Il supporte actuellement les fournisseurs suivant : 

    canalPlus
    pluzz  (france 2,3,4,ô)
    arte
    D8
    D17
    nrj12
    lequipe.fr
    beinsport
    tf1
    RSS : n'importe quel flux RSS contenant des liens vers des vidéos à télécharger (HTTP, FTP, Bittorent, Youtube, Dailymotion ...)

habiTv est développé en Java 1.7, il se base sur différents outils externes pour réaliser le téléchargement (youtube-dl, rtmpDump, curl, aria2c). Il est personnalisable grâce à un système de plugin : 

    plugin de fournisseur de contenu (arte, canalPlus) : ils listent les catégories disponibles et gèrent le téléchargement des épisodes
    plugin de téléchargement (rtmpDump, curl, aria2c) : encapsule les utilitaires de téléchargement pour une meilleure interaction avec habiTv.
    plugin d'export (ffmpeg, curl) : améliore l'interaction entre les utilitaires permettant d'exporter les vidéos et habiTv



habiTv est actuellement développé et testé sous Windows et linux.

## Structure du projet

habiTv est un projet Maven multi-modules avec la structure suivante :

### Modules principaux
- **application** : Contient les modules d'application
  - core : Cœur de l'application
  - consoleView : Interface en ligne de commande
  - trayView : Interface graphique dans la barre des tâches
  - habiTv : Application principale
  - habiTv-linux : Distribution pour Linux
  - habiTv-windows : Distribution pour Windows
- **fwk** : Contient les modules du framework
  - api : API du framework
  - framework : Implémentation du framework
- **plugins** : Contient tous les plugins pour les différents fournisseurs et outils
  - 6play, adobeHDS, aria2, arte, beinsport, canalPlus, clubic, cmd, curl, email, ffmpeg, file, footyroom, globalnews, lequipe, mlssoccer, pluzz, RSS, rtmpDump, sfr, youtube, wat, plugin-tester
