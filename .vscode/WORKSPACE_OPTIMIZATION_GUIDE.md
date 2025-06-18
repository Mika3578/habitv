# 🎯 HabiTV Workspace Optimization Guide

## 🚀 Overview
Your VS Code workspace has been optimized with graphical buttons and enhanced tools for Java/Maven development. This guide shows you where to find and how to use all the new features.

## 📍 Button Locations & Usage

### 1. **Status Bar Buttons** (Bottom of VS Code)
![Status Bar Location](https://code.visualstudio.com/assets/docs/getstarted/userinterface/hero.png)

**Extension:** StatusBar Commands
**Location:** Bottom status bar (blue bar at the bottom of VS Code)

**Available Actions:**
- 🧹 **Clean** - Maven clean
- 🔨 **Build** - Maven clean package  
- 🧪 **Test** - Run Maven tests
- 📦 **Install** - Install to local repository
- ✅ **Verify** - Maven verify
- 🌳 **Deps** - Show dependency tree
- 📄 **Profiles** - Show active profiles
- ♻️ **Refresh** - Git fetch + reset
- 🔄 **Reload** - Reload VS Code window

### 2. **Menu Bar Buttons** (Top Menu)
**Extension:** Shortcut Menu Bar
**Location:** Top menu bar (next to File, Edit, View, etc.)

**Features:**
- Quick access toolbar with custom commands
- Configurable button order and visibility
- Icons and colors for easy identification

### 3. **Action Buttons** (Editor Title Bar)
**Extension:** Action Buttons (already configured)
**Location:** Top-right of editor tabs

**Quick Actions:**
- Build, Test, Clean buttons directly in editor
- Context-sensitive based on file type

### 4. **Terminal Commands Panel**
**Extension:** Terminal Commands (already configured)
**Location:** Terminal view panel

**Features:**
- Predefined command shortcuts
- One-click execution of common tasks

## 🛠️ Installed Extensions & Features

### ✅ Successfully Installed:
1. **jerrygoyal.shortcut-menu-bar** - Toolbar buttons in menu bar
2. **anweber.statusbar-commands** - Status bar action buttons  
3. **trunk.io** - Advanced code quality and security analysis
4. **sourcery.sourcery** - AI-powered code review and suggestions

### ⚠️ Previously Configured:
- **Action Buttons** - Editor toolbar buttons
- **Task Buttons** - Task runner buttons
- **Activitus Bar** - Enhanced activity bar
- **Terminal Commands** - Quick terminal commands

### ❌ Not Available (Alternative solutions in place):
- gsppvo.vscode-commandbar
- spmeesseman.vscode-taskexplorer  
- shengchen.vscode-checkstyle
- spencerwmiles.vscode-task-buttons
- adrianwilczynski.terminal-commands

## 🎨 Code Quality Features

### **Trunk.io** - Advanced Analysis
- **Real-time code scanning** for 40+ languages
- **Security vulnerability detection**
- **Code formatting** and style enforcement
- **Custom rule configuration**

**Usage:**
1. Open any Java file
2. Issues appear in Problems panel
3. Right-click → "Trunk: Fix Issues" for auto-fixes
4. Status bar shows Trunk analysis status

### **Sourcery** - AI Code Review
- **Intelligent code suggestions** powered by AI
- **Refactoring recommendations**
- **Performance optimizations**
- **Code simplification**

**Usage:**
1. Highlight code block
2. Right-click → "Sourcery: Review Code"
3. View suggestions in hover tooltips
4. Accept/reject improvements with one click

## 📱 Platform-Specific Instructions

### **Windows (PowerShell)**
```powershell
# Your default shell is PowerShell - all commands work natively
# Maven commands: mvn clean, mvn test, etc.
# Git commands: git status, git fetch, etc.
# Custom scripts: .\scripts\deploy-and-publish.ps1
```

### **macOS/Linux (Bash/Zsh)**
```bash
# If working on other platforms, use:
# Maven: ./mvnw clean, ./mvnw test
# Scripts: ./scripts/deploy-and-publish.sh
```

## ⚡ Quick Start Guide

### First Time Setup:
1. **Restart VS Code** to activate all extensions
2. **Check Status Bar** - Look for new buttons at the bottom
3. **Check Menu Bar** - Look for "Shortcut Menu Bar" in top menu
4. **Open Java file** - Trunk.io and Sourcery will start analyzing

### Daily Workflow:
1. **Clean Build**: Click 🧹 Clean → 🔨 Build
2. **Run Tests**: Click 🧪 Test  
3. **Check Dependencies**: Click 🌳 Deps
4. **Git Refresh**: Click ♻️ Refresh before/after commits
5. **Code Review**: Highlight code → Right-click → Sourcery review

## 🔧 Customization

### Modify Button Configuration:
Edit `.vscode/settings.json` to customize:
- Button order and visibility
- Colors and icons
- Keyboard shortcuts
- Command arguments

### Add Custom Commands:
Edit `.vscode/commands.json` to add new actions:
```json
{
  "id": "custom.command",
  "command": "workbench.action.tasks.runTask", 
  "args": "your-task-name",
  "description": "Custom Action",
  "icon": "🎯",
  "color": "#42a5f5"
}
```

## 🚨 Troubleshooting

### Buttons Not Visible:
1. Restart VS Code completely
2. Check extension is enabled: Ctrl+Shift+X → Search for extension
3. Verify settings.json configuration

### Commands Not Working:
1. Check Maven is installed: `mvn -version`
2. Verify tasks.json contains the task
3. Check PowerShell execution policy if on Windows

### Code Quality Issues:
1. **Trunk.io**: Check internet connection for rule updates
2. **Sourcery**: Sign up for free account if needed
3. **Problems panel**: View → Problems to see all issues

## 📞 Support

- **VS Code docs**: https://code.visualstudio.com/docs
- **Maven docs**: https://maven.apache.org/guides/
- **Extension issues**: Check VS Code marketplace for specific extension

---

## 🎉 Summary

Your workspace now has:
- **9 status bar buttons** for common actions
- **Enhanced code quality** with Trunk.io + Sourcery  
- **Multiple button locations** for different workflows
- **Comprehensive task automation** for Maven/Git operations
- **Cross-platform compatibility** with proper shell detection

**Start developing with one-click actions! 🖱️✨**
