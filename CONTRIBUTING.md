# Guide de contribution à habiTv

Merci de votre intérêt pour contribuer à habiTv ! Ce document fournit des directives pour contribuer au projet.

## 🚀 Comment contribuer

### Signaler un bug

Si vous trouvez un bug, veuillez créer une [issue](../../issues) avec les informations suivantes :

- Description claire du bug
- Étapes pour reproduire le problème
- Comportement attendu
- Comportement observé
- Version de Java utilisée
- Système d'exploitation
- Logs pertinents (si disponibles)

### Proposer une fonctionnalité

Pour proposer une nouvelle fonctionnalité :

1. Créez une [issue](../../issues) avec le label `enhancement`
2. Décrivez clairement la fonctionnalité et son utilité
3. Expliquez comment elle s'intègre dans l'architecture existante

### Soumettre une Pull Request

1. **Fork** le repository
2. **Créez une branche** pour votre fonctionnalité (`git checkout -b feature/ma-fonctionnalite`)
3. **Commitez** vos changements (`git commit -m 'Ajout de ma fonctionnalité'`)
4. **Pushez** vers la branche (`git push origin feature/ma-fonctionnalite`)
5. Ouvrez une **Pull Request**

## 📋 Standards de code

### Style de code

- Suivez les conventions de nommage Java standard
- Utilisez des noms de variables et méthodes descriptifs
- Commentez le code en anglais (selon les règles du projet)
- Maintenez une indentation cohérente (4 espaces)

### Structure des commits

Utilisez des messages de commit clairs et descriptifs :

```
feat: Ajout du support pour le plugin TMC
fix: Correction du bug de téléchargement pour Arte
docs: Mise à jour du README
refactor: Refactorisation du PluginManager
test: Ajout de tests pour YoutubePluginDownloader
```

Préfixes recommandés :
- `feat:` : Nouvelle fonctionnalité
- `fix:` : Correction de bug
- `docs:` : Documentation
- `style:` : Formatage, point-virgule manquant, etc.
- `refactor:` : Refactorisation du code
- `test:` : Ajout ou modification de tests
- `chore:` : Maintenance (dépendances, build, etc.)

## 🧪 Tests

- Ajoutez des tests pour les nouvelles fonctionnalités
- Assurez-vous que tous les tests existants passent
- Exécutez les tests avec `mvn test`

## 🔌 Développement de plugins

### Créer un nouveau plugin Provider

1. Créez un nouveau module dans `plugins/`
2. Implémentez `PluginProviderInterface`
3. Ajoutez les tests dans le répertoire `test/`
4. Documentez le plugin dans le README

### Créer un nouveau plugin Downloader

1. Créez un nouveau module dans `plugins/`
2. Implémentez `PluginDownloaderInterface`
3. Ajoutez les tests dans le répertoire `test/`
4. Documentez le plugin dans le README

### Exemple de structure

```
plugins/mon-plugin/
├── pom.xml
├── src/
│   └── com/dabi/habitv/
│       └── provider/
│           └── MonPluginManager.java
└── test/
    └── com/dabi/habitv/
        └── provider/
            └── MonPluginManagerTest.java
```

## 📝 Documentation

- Mettez à jour la documentation si nécessaire
- Ajoutez des commentaires Javadoc pour les nouvelles classes et méthodes publiques
- Mettez à jour le README si vous ajoutez une fonctionnalité majeure

## ✅ Checklist avant de soumettre une PR

- [ ] Le code suit les standards du projet
- [ ] Les tests passent (`mvn test`)
- [ ] Le code compile sans erreurs
- [ ] La documentation est à jour
- [ ] Les messages de commit sont clairs
- [ ] La PR est liée à une issue (si applicable)

## 🐛 Problèmes connus

Consultez les [issues](../../issues) pour voir les problèmes connus et les fonctionnalités en cours de développement.

## 📞 Questions ?

Si vous avez des questions, n'hésitez pas à ouvrir une issue ou à contacter les mainteneurs du projet.

Merci de contribuer à habiTv ! 🎉

