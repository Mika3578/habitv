# 🚀 Guide d'utilisation des boutons HabiTv

## 📍 Localisation des boutons

### 🔵 Status Bar (barre de statut en bas)
- **Action Buttons** : Boutons colorés avec icônes
- **Task Buttons** : Boutons de tâches avec compteur

### ⌨️ Raccourcis clavier
- `Ctrl+Shift+M + C` : Maven Clean
- `Ctrl+Shift+M + B` : Maven Build
- `Ctrl+Shift+M + T` : Maven Test
- `Ctrl+Shift+M + I` : Maven Install
- `Ctrl+Shift+G + R` : Git Refresh
- `Ctrl+Shift+F5` : Reload IDE

## 🎯 Boutons disponibles

### 🔨 Maven Commands
| Bouton | Commande | Description |
|--------|----------|-------------|
| 🧹 Clean | `mvn clean` | Nettoie le projet |
| 🔨 Build | `mvn clean package` | Compile et package |
| 🧪 Test | `mvn test` | Lance les tests |
| 📦 Install | `mvn install` | Install local repository |
| 🌳 Deps | `mvn dependency:tree` | Arbre des dépendances |
| ⚙️ Profiles | `mvn help:active-profiles` | Profils actifs |
| ✅ Verify | `mvn verify` | Vérifie le package |

### 🔄 Git Commands
| Bouton | Commande | Description |
|--------|----------|-------------|
| 🔄 Refresh | `git fetch && git reset --hard && git clean -fd` | Synchronise la branche |

### 💻 IDE Commands
| Bouton | Commande | Description |
|--------|----------|-------------|
| ♻️ Reload IDE | `workbench.action.reloadWindow` | Recharge VS Code |

## 🎨 Configuration des couleurs

- 🧹 Clean : Rouge (`#ff6b6b`)
- 🔨 Build : Turquoise (`#4ecdc4`)
- 🧪 Test : Bleu (`#45b7d1`)
- 📦 Install : Vert (`#96ceb4`)
- 🌳 Deps : Jaune (`#feca57`)
- ⚙️ Profiles : Rose (`#ff9ff3`)
- ✅ Verify : Bleu clair (`#54a0ff`)
- 🔄 Refresh : Violet (`#5f27cd`)
- ♻️ Reload : Orange (`#ff6348`)

## 🔧 Personnalisation

Pour modifier les boutons, éditez :
- `.vscode/settings.json` : Configuration Action Buttons et Task Buttons
- `.vscode/commands.json` : Commandes personnalisées
- `.vscode/keybindings.json` : Raccourcis clavier

## 🆘 Dépannage

1. **Boutons non visibles** : Redémarrez VS Code (`Ctrl+Shift+F5`)
2. **Commandes Maven échouent** : Vérifiez que Maven est installé et dans le PATH
3. **Git commands fail** : Vérifiez que vous êtes dans un repository Git
4. **Permissions PowerShell** : Exécutez `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`

## 📱 Interface utilisateur

```
┌─────────────────────────────────────────────────────────────────┐
│ VS Code - habitv                                            ─ □ ×│
├─────────────────────────────────────────────────────────────────┤
│ File  Edit  View  Go  Run  Terminal  Help                       │
├─────────────────────────────────────────────────────────────────┤
│ 📁 EXPLORER  🔍 SEARCH  📝 SOURCE CONTROL  🐛 RUN  🔌 EXTENSIONS │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                    CODE EDITOR                                  │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ 🧹Clean 🔨Build 🧪Test 📦Install 🌳Deps ⚙️Profiles ✅Verify 🔄Refresh ♻️Reload │
└─────────────────────────────────────────────────────────────────┘
           ↑ ACTION BUTTONS (Status Bar)
```
