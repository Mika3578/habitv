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

habiTv est développé en Java 21, il se base sur différents outils externes pour réaliser le téléchargement (youtube-dl, rtmpDump, curl, aria2c). Il utilise les modules JavaFX 21.0.2 pour l'interface graphique et est personnalisable grâce à un système de plugin : 

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

## Configuration Maven

Le projet utilise Maven pour la gestion des dépendances et la construction. Tous les modules dépendent du POM parent (com.dabi.habitv:parent:4.1.0) qui est disponible dans le dépôt Maven miroir :

```xml
<repositories>
  <repository>
    <id>habitv-mirror</id>
    <url>https://mika3578.github.io/habitv/repository/</url>
  </repository>
</repositories>
```

Si vous rencontrez des problèmes de résolution du POM parent, assurez-vous que :
1. La section repositories est présente dans tous les pom.xml
2. La section parent inclut l'élément relativePath vide pour forcer la recherche dans le dépôt distant :
   ```xml
   <parent>
     <groupId>com.dabi.habitv</groupId>
     <artifactId>parent</artifactId>
     <version>4.1.0</version>
     <relativePath/>
   </parent>
   ```
3. Vous pouvez également essayer de supprimer le cache Maven local pour le POM parent (~/.m2/repository/com/dabi/habitv/parent/4.1.0)

## JavaFX et Java 21

Le projet a été migré vers Java 21 et utilise les modules JavaFX 21.0.2 pour l'interface graphique. Les principales modifications sont :

1. Remplacement des dépendances système `javafx:jfxrt` par les modules org.openjfx :
   ```xml
   <dependency>
     <groupId>org.openjfx</groupId>
     <artifactId>javafx-controls</artifactId>
     <version>21.0.2</version>
   </dependency>
   <dependency>
     <groupId>org.openjfx</groupId>
     <artifactId>javafx-fxml</artifactId>
     <version>21.0.2</version>
   </dependency>
   <dependency>
     <groupId>org.openjfx</groupId>
     <artifactId>javafx-graphics</artifactId>
     <version>21.0.2</version>
   </dependency>
   ```

2. Utilisation du plugin JavaFX Maven pour la construction :
   ```xml
   <plugin>
     <groupId>org.openjfx</groupId>
     <artifactId>javafx-maven-plugin</artifactId>
     <version>0.0.8</version>
     <configuration>
       <mainClass>com.dabi.habitv.tray.HabiTvSplashScreen</mainClass>
     </configuration>
   </plugin>
   ```

Cette migration permet une meilleure compatibilité avec les JDK modernes et simplifie la gestion des dépendances.

## Génération de code JAXB

Le module core utilise JAXB pour générer des classes Java à partir de fichiers XSD. Les packages suivants sont générés automatiquement lors de la compilation :

- `com.dabi.habitv.grabconfig.entities` (à partir de grab-config.xsd)
- `com.dabi.habitv.config.entities` (à partir de config.xsd)
- `com.dabi.habitv.configuration.entities` (à partir de configuration.xsd)

La génération est configurée dans le pom.xml du module core avec le plugin maven-jaxb-plugin :

```xml
<plugin>
  <groupId>com.sun.tools.xjc.maven2</groupId>
  <artifactId>maven-jaxb-plugin</artifactId>
  <version>1.1.1</version>
  <executions>
    <execution>
      <id>grabconfig</id>
      <goals>
        <goal>generate</goal>
      </goals>
      <configuration>
        <generatePackage>com.dabi.habitv.grabconfig.entities</generatePackage>
        <schemaDirectory>xsd</schemaDirectory>
        <generateDirectory>generated</generateDirectory>
        <includeSchemas>
          <includeSchema>grab-config.xsd</includeSchema>
        </includeSchemas>
      </configuration>
    </execution>
    <!-- Autres exécutions pour config.xsd et configuration.xsd -->
  </executions>
</plugin>
```

Si vous rencontrez des erreurs de compilation liées à ces packages, assurez-vous que :
1. Les fichiers XSD existent dans le répertoire `application/core/xsd`
2. Le plugin maven-jaxb-plugin est correctement configuré avec une version spécifiée
3. Les repositories Maven Central et Java.net sont accessibles pour résoudre le plugin

## Modernization (2025)
- Requires Java 21 (LTS) and JavaFX 21.0.2
- All modules now use Java 21 for compilation
- Legacy Java 1.7/1.8 support and references removed
- Maven build system updated for modern Java
- Network-dependent tests are now marked as integration tests and skipped in CI
- Duplicate entity sources (manual/generated) have been resolved; only generated entities are used

## Entity Class Regeneration (2025-06-13)
- Regenerated all JAXB entity classes from XSD schemas to fix compilation issues
- Fixed boolean getter method names in generated classes (using 'is' prefix instead of 'get' for boolean properties)
- Updated code to use correct method names: `isUpdateOnStartup()`, `isAutoriseSnapshot()`, `isDownload()`, etc.
- Fixed JAXB marshalling to use FileOutputStream instead of File directly
- Added missing maven-compiler-plugin configurations to ensure Java 21 compatibility
- Core and consoleView modules now compile successfully
- Resolved "cannot resolve type" and "syntax error" issues in generated entity classes
