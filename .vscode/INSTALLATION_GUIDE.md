# 🚀 Guide d'Installation des Extensions HabiTV

## 📋 **ÉTAPES D'INSTALLATION**

### **1. Installer Custom Buttons (Status Bar)**

1. **Ouvrir Cursor/VS Code**
2. **Appuyer sur** `Ctrl+Shift+X` pour ouvrir les extensions
3. **Rechercher** : `Custom Buttons`
4. **Installer** l'extension de **antfu** (1M+ téléchargements)
5. **Recharger** la fenêtre : `Ctrl+Shift+R`

### **2. Installer Action Buttons (Toolbar)**

1. **Dans le même panneau** des extensions
2. **Rechercher** : `Action Buttons`
3. **Installer** l'extension de **adrianwilczynski** (500K+ téléchargements)
4. **Recharger** la fenêtre : `Ctrl+Shift+R`

### **3. Vérifier l'installation**

Après rechargement, vous devriez voir :
- **Status Bar** : Boutons colorés avec icônes (Custom Buttons)
- **Toolbar** : Boutons avec texte (Action Buttons)

---

## 🎯 **EMPLACEMENT DES BOUTONS**

### **Custom Buttons (Status Bar)**
- **Emplacement** : Barre de statut en bas à droite
- **Apparence** : Icônes colorées (🧹 🔨 🧪 📦 🔄)
- **Utilisation** : Clic gauche pour exécuter
- **Configuration** : Via `commands.json` ou interface GUI

### **Action Buttons (Toolbar)**
- **Emplacement** : Barre d'outils en haut
- **Apparence** : Boutons avec texte et couleurs
- **Utilisation** : Clic gauche pour exécuter
- **Configuration** : Via `settings.json`

---

## ⚙️ **CONFIGURATION GUI**

### **Custom Buttons - Interface Graphique**

1. **Ouvrir la palette** : `Ctrl+Shift+P`
2. **Rechercher** : `Edit Commands`
3. **Sélectionner** : `Custom Buttons: Edit Commands`
4. **Interface** : Éditeur JSON avec aperçu en temps réel
5. **Sauvegarder** : `Ctrl+S` pour appliquer

### **Action Buttons - Configuration**

1. **Ouvrir les paramètres** : `Ctrl+,`
2. **Rechercher** : `actionButtons`
3. **Modifier** : Configuration JSON
4. **Sauvegarder** : Changements appliqués automatiquement

---

## 🎨 **BOUTONS DISPONIBLES**

### **Maven Commands**
- 🧹 **Clean** : `mvn clean`
- 🔨 **Build** : `mvn clean package`
- 🧪 **Test** : `mvn test`
- 📦 **Install** : `mvn install`
- ✅ **Verify** : `mvn verify`
- 🌳 **Deps** : `mvn dependency:tree`
- 📄 **Profiles** : `mvn help:active-profiles`
- 🚀 **Deploy** : `mvn clean deploy`

### **Git Commands**
- 🔄 **Refresh** : `git fetch && git reset --hard && git clean -fd`
- 📝 **Status** : `git status`

### **IDE Commands**
- 🔄 **Reload** : `workbench.action.reloadWindow`
- 💻 **Terminal** : `workbench.action.terminal.new`

### **Navigation**
- 📁 **Explorer** : `workbench.view.explorer`
- 📝 **Source Control** : `workbench.view.scm`
- 🐛 **Debug** : `workbench.view.debug`
- 🔌 **Extensions** : `workbench.view.extensions`

---

## 🔧 **DÉPANNAGE**

### **Si les boutons n'apparaissent pas :**

1. **Vérifier l'installation** :
   - `Ctrl+Shift+X` → Vérifier que les extensions sont installées
   - Recharger la fenêtre : `Ctrl+Shift+R`

2. **Vérifier la configuration** :
   - `Ctrl+Shift+P` → `Custom Buttons: Edit Commands`
   - Vérifier `settings.json` pour Action Buttons

3. **Alternative manuelle** :
   - Utiliser les raccourcis clavier : `Ctrl+Shift+M` + lettre
   - Utiliser les tâches : `Ctrl+Shift+P` → `Tasks: Run Task`

### **Si les commandes ne fonctionnent pas :**

1. **Vérifier Maven** :
   ```bash
   mvn --version
   ```

2. **Vérifier Git** :
   ```bash
   git --version
   ```

3. **Vérifier le terminal** :
   - `Ctrl+Shift+\`` pour ouvrir un nouveau terminal
   - Tester une commande manuellement

---

## 📱 **RACCOURCIS CLAVIER**

### **Maven**
- `Ctrl+Shift+M C` : Clean
- `Ctrl+Shift+M B` : Build
- `Ctrl+Shift+M T` : Test
- `Ctrl+Shift+M I` : Install
- `Ctrl+Shift+M V` : Verify
- `Ctrl+Shift+M D` : Dependencies
- `Ctrl+Shift+M P` : Profiles

### **Git**
- `Ctrl+Shift+G R` : Refresh branch

### **IDE**
- `Ctrl+Shift+R` : Reload window
- `Ctrl+Shift+\`` : New terminal

---

## 🎉 **FÉLICITATIONS !**

Votre workspace HabiTV est maintenant configuré avec :
- ✅ **Boutons graphiques** dans la status bar et toolbar
- ✅ **Raccourcis clavier** pour accès rapide
- ✅ **Tâches Maven** optimisées pour le projet
- ✅ **Configuration Git** automatique
- ✅ **Interface intuitive** pour le développement

**Profitez de votre développement optimisé !** 🚀 