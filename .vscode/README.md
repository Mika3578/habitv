# 🛠️ Configuration VS Code pour HabiTV

Ce dossier contient la configuration optimisée de VS Code/Cursor pour le projet HabiTV.

## 📁 Fichiers de configuration

### `tasks.json`
Contient toutes les tâches Maven et Git :
- **Maven** : clean, build, test, install, deps, profiles, verify, compile, package
- **Git** : refresh (fetch + reset + clean)
- **Présentation** : Terminal partagé avec focus automatique

### `settings.json`
Configuration optimisée pour :
- **Java/Maven** : Compilation automatique, analyse null
- **Git** : Auto-fetch toutes les 3 minutes
- **Terminal** : PowerShell par défaut sur Windows
- **Fichiers** : Auto-save, exclusion des dossiers target
- **Interface** : Raccourcis pour sidebar et panel

### `commands.json`
Commandes pour Custom Buttons (extension) :
- Toutes les tâches Maven avec icônes
- Commandes Git et IDE
- Navigation rapide vers les vues

### `keybindings.json`
Raccourcis clavier :
- **Ctrl+Shift+M** + lettre pour Maven
- **Ctrl+Shift+G** pour Git
- **Ctrl+Shift+R** pour reload IDE
- **Ctrl+Shift+** + touche pour navigation

## 🎯 Raccourcis principaux

### Maven
- `Ctrl+Shift+M C` : Clean
- `Ctrl+Shift+M B` : Build (clean + package)
- `Ctrl+Shift+M T` : Test
- `Ctrl+Shift+M I` : Install
- `Ctrl+Shift+M D` : Dependency tree
- `Ctrl+Shift+M P` : Active profiles
- `Ctrl+Shift+M V` : Verify

### Git
- `Ctrl+Shift+G R` : Refresh branch (fetch + reset + clean)

### IDE
- `Ctrl+Shift+R` : Reload window
- `Ctrl+Shift+\`` : Nouveau terminal
- `Ctrl+Shift+E` : Explorer
- `Ctrl+Shift+G` : Source Control
- `Ctrl+Shift+D` : Run and Debug
- `Ctrl+Shift+X` : Extensions

## 🔧 Installation des extensions

### Extensions recommandées (à installer manuellement)

1. **Custom Buttons** (`antfu.custom-buttons`)
   - Boutons dans la status bar
   - Utilise le fichier `commands.json`

2. **Action Buttons** (`adrianwilczynski.action-buttons`)
   - Boutons dans la toolbar
   - Intégration avec les tâches

### Installation manuelle
1. Ouvrir VS Code/Cursor
2. `Ctrl+Shift+X` pour ouvrir les extensions
3. Rechercher et installer les extensions
4. Recharger la fenêtre (`Ctrl+Shift+R`)

## 🚀 Utilisation

### Via les tâches
1. `Ctrl+Shift+P` → "Tasks: Run Task"
2. Sélectionner la tâche souhaitée

### Via les raccourcis
Utiliser directement les raccourcis clavier listés ci-dessus.

### Via Custom Buttons (si installé)
Les boutons apparaîtront dans la status bar avec les icônes définies.

## 🔄 Mise à jour

Pour mettre à jour la configuration :
1. Modifier les fichiers dans `.vscode/`
2. Recharger la fenêtre (`Ctrl+Shift+R`)
3. Les changements sont appliqués automatiquement

## 📝 Notes

- Configuration optimisée pour Windows PowerShell
- Compatible avec Cursor et VS Code
- Auto-save activé (1 seconde de délai)
- Git auto-fetch toutes les 3 minutes
- Exclusion automatique des dossiers `target/` 