package com.dabi.habitv.tray.controller;

import java.util.List;

import org.apache.log4j.Logger;

import com.dabi.habitv.core.config.CredentialConfig;
import com.dabi.habitv.core.service.CredentialManager;
import com.dabi.habitv.tray.Popin;
import com.dabi.habitv.tray.model.HabitTvViewManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.application.Platform;

/**
 * Controller for managing credentials in the habiTv GUI.
 */
public class CredentialController {

    private static final Logger LOG = Logger.getLogger(CredentialController.class);
    
    private HabitTvViewManager manager;
    private ViewController viewController;
    private Stage primaryStage;
    private CredentialManager credentialManager;
    
    private ObservableList<CredentialConfig> credentialsData;
    private boolean initialized = false;

    // UI Components - will be initialized manually
    private TableView<CredentialConfig> credentialsTable;
    private Button addCredentialButton;
    private Button editCredentialButton;
    private Button removeCredentialButton;
    private Button refreshCredentialsButton;

    public CredentialController() {
        this.credentialManager = CredentialManager.getInstance();
        this.credentialsData = FXCollections.observableArrayList();
    }

    public void init(ViewController viewController, HabitTvViewManager manager, Stage primaryStage) {
        if (initialized) {
            LOG.warn("CredentialController already initialized, skipping");
            return;
        }
        
        this.viewController = viewController;
        this.manager = manager;
        this.primaryStage = primaryStage;

        // Initialize UI components by finding them in the scene
        // Use Platform.runLater to ensure the scene is fully loaded
        Platform.runLater(() -> {
            try {
                // Try multiple times to find the components
                initializeUIComponentsWithRetry();
            } catch (Exception e) {
                LOG.error("Failed to initialize CredentialController: " + e.getMessage(), e);
                // Mark as initialized to prevent further attempts
                initialized = true;
            }
        });
    }

    private void initializeUIComponentsWithRetry() {
        int maxRetries = 10;
        int retryCount = 0;
        
        while (retryCount < maxRetries && credentialsTable == null) {
            try {
                initializeUIComponents();
                
                if (credentialsTable != null) {
                    LOG.info("Found UI components on attempt " + (retryCount + 1));
                    setupTable();
                    setupButtons();
                    loadCredentials();
                    initialized = true;
                    LOG.info("CredentialController initialized successfully");
                    return;
                }
            } catch (Exception e) {
                LOG.warn("Attempt " + (retryCount + 1) + " failed with exception: " + e.getMessage());
            }
            
            retryCount++;
            LOG.warn("Attempt " + retryCount + " failed to find UI components, retrying...");
            
            // Wait a bit before retrying
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (credentialsTable == null) {
            LOG.error("Failed to initialize CredentialController after " + maxRetries + " attempts - UI components not found");
            // Mark as initialized to prevent further attempts
            initialized = true;
        }
    }

    private void initializeUIComponents() {
        LOG.info("Starting UI component initialization...");
        
        // Find the credentials tab and its components
        if (primaryStage != null && primaryStage.getScene() != null) {
            LOG.info("Primary stage and scene are available");
            
            // Try to find the credentials table using different approaches
            credentialsTable = (TableView<CredentialConfig>) primaryStage.getScene().lookup("#credentialsTable");
            LOG.info("Direct lookup for credentialsTable: " + (credentialsTable != null ? "SUCCESS" : "FAILED"));
            
            // If not found, try to find it by traversing the scene graph
            if (credentialsTable == null) {
                LOG.info("Attempting to find TableView by traversing scene graph...");
                credentialsTable = findTableViewInScene(primaryStage.getScene().getRoot());
                LOG.info("Scene graph traversal for credentialsTable: " + (credentialsTable != null ? "SUCCESS" : "FAILED"));
            }
            
            // Try to find the buttons
            addCredentialButton = (Button) primaryStage.getScene().lookup("#addCredentialButton");
            editCredentialButton = (Button) primaryStage.getScene().lookup("#editCredentialButton");
            removeCredentialButton = (Button) primaryStage.getScene().lookup("#removeCredentialButton");
            refreshCredentialsButton = (Button) primaryStage.getScene().lookup("#refreshCredentialsButton");
            
            LOG.info("Button lookup results:");
            LOG.info("  addCredentialButton: " + (addCredentialButton != null ? "FOUND" : "NOT FOUND"));
            LOG.info("  editCredentialButton: " + (editCredentialButton != null ? "FOUND" : "NOT FOUND"));
            LOG.info("  removeCredentialButton: " + (removeCredentialButton != null ? "FOUND" : "NOT FOUND"));
            LOG.info("  refreshCredentialsButton: " + (refreshCredentialsButton != null ? "FOUND" : "NOT FOUND"));
            
            if (credentialsTable == null) {
                LOG.warn("Could not find credentialsTable in scene");
            }
            if (addCredentialButton == null) {
                LOG.warn("Could not find addCredentialButton in scene");
            }
            if (editCredentialButton == null) {
                LOG.warn("Could not find editCredentialButton in scene");
            }
            if (removeCredentialButton == null) {
                LOG.warn("Could not find removeCredentialButton in scene");
            }
            if (refreshCredentialsButton == null) {
                LOG.warn("Could not find refreshCredentialsButton in scene");
            }
        } else {
            LOG.warn("Primary stage or scene is null");
            if (primaryStage == null) {
                LOG.warn("Primary stage is null");
            }
            if (primaryStage != null && primaryStage.getScene() == null) {
                LOG.warn("Primary stage scene is null");
            }
        }
        
        // If any components are null, log a warning but don't fail
        if (credentialsTable == null) {
            LOG.error("Credentials table is null - credential management will be disabled");
        }
    }

    private TableView<CredentialConfig> findTableViewInScene(javafx.scene.Node node) {
        if (node instanceof TableView) {
            TableView<?> table = (TableView<?>) node;
            if ("credentialsTable".equals(table.getId())) {
                return (TableView<CredentialConfig>) table;
            }
        }
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                TableView<CredentialConfig> result = findTableViewInScene(child);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    private void setupTable() {
        try {
            if (credentialsTable == null) {
                LOG.error("Credentials table is null, cannot setup table");
                return;
            }

            // Clear existing columns
            credentialsTable.getColumns().clear();

            TableColumn<CredentialConfig, String> serviceColumn = new TableColumn<>("Service");
            serviceColumn.setCellValueFactory(new PropertyValueFactory<>("service"));
            serviceColumn.setPrefWidth(150);

            TableColumn<CredentialConfig, String> usernameColumn = new TableColumn<>("Username");
            usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            usernameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            usernameColumn.setPrefWidth(200);
            usernameColumn.setCellValueFactory(cellData -> {
                String username = cellData.getValue().getUsername();
                return new SimpleStringProperty(maskUsername(username));
            });

            TableColumn<CredentialConfig, Boolean> enabledColumn = new TableColumn<>("Enabled");
            enabledColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
            enabledColumn.setPrefWidth(80);

            TableColumn<CredentialConfig, String> descriptionColumn = new TableColumn<>("Description");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            descriptionColumn.setPrefWidth(200);

            credentialsTable.getColumns().addAll(serviceColumn, usernameColumn, enabledColumn, descriptionColumn);
            credentialsTable.setItems(credentialsData);
            
            // Enable/disable buttons based on selection
            credentialsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonStates());
                
            LOG.info("Table setup completed successfully");
        } catch (Exception e) {
            LOG.error("Failed to setup table: " + e.getMessage(), e);
        }
    }

    private void setupButtons() {
        try {
            if (addCredentialButton == null || editCredentialButton == null || 
                removeCredentialButton == null || refreshCredentialsButton == null) {
                LOG.error("One or more buttons are null, cannot setup buttons");
                return;
            }

            addCredentialButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    showAddCredentialDialog();
                }
            });

            editCredentialButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    editSelectedCredential();
                }
            });

            removeCredentialButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    removeSelectedCredential();
                }
            });

            refreshCredentialsButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadCredentials();
                }
            });
            
            LOG.info("Button setup completed successfully");
        } catch (Exception e) {
            LOG.error("Failed to setup buttons: " + e.getMessage(), e);
        }
    }

    private void loadCredentials() {
        try {
            if (credentialsData == null) {
                LOG.error("Credentials data is null");
                return;
            }
            
            credentialsData.clear();
            List<CredentialConfig> credentials = credentialManager.getAllCredentials();
            credentialsData.addAll(credentials);
            updateButtonStates();
            LOG.info("Loaded " + credentials.size() + " credentials");
        } catch (Exception e) {
            LOG.error("Failed to load credentials: " + e.getMessage(), e);
        }
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return username;
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 2);
    }

    private void showAddCredentialDialog() {
        CredentialDialog dialog = new CredentialDialog(primaryStage, null);
        dialog.showAndWait();
        
        if (dialog.isConfirmed()) {
            CredentialConfig newCredential = dialog.getCredential();
            credentialManager.addCredential(newCredential);
            loadCredentials();
            LOG.info("Added new credential for service: " + newCredential.getService());
        }
    }

    private void editSelectedCredential() {
        if (credentialsTable == null) {
            LOG.error("Credentials table is null");
            return;
        }
        
        CredentialConfig selected = credentialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a credential to edit.", AlertType.WARNING);
            return;
        }

        CredentialDialog dialog = new CredentialDialog(primaryStage, selected);
        dialog.showAndWait();
        
        if (dialog.isConfirmed()) {
            CredentialConfig updatedCredential = dialog.getCredential();
            credentialManager.addCredential(updatedCredential);
            loadCredentials();
            LOG.info("Updated credential for service: " + updatedCredential.getService());
        }
    }

    private void removeSelectedCredential() {
        if (credentialsTable == null) {
            LOG.error("Credentials table is null");
            return;
        }
        
        CredentialConfig selected = credentialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a credential to remove.", AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Credential");
        alert.setContentText("Are you sure you want to remove the credential for " + selected.getService() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                credentialManager.removeCredential(selected.getService());
                loadCredentials();
                LOG.info("Removed credential for service: " + selected.getService());
            }
        });
    }

    private void updateButtonStates() {
        if (credentialsTable == null || editCredentialButton == null || removeCredentialButton == null) {
            LOG.warn("UI components are null, cannot update button states");
            return;
        }
        
        boolean hasSelection = credentialsTable.getSelectionModel().getSelectedItem() != null;
        editCredentialButton.setDisable(!hasSelection);
        removeCredentialButton.setDisable(!hasSelection);
    }

    private void showAlert(String title, String content, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Dialog for adding/editing credentials
     */
    private class CredentialDialog extends javafx.scene.control.Dialog<CredentialConfig> {
        private boolean confirmed = false;
        private CredentialConfig credential;

        public CredentialDialog(Stage owner, CredentialConfig credential) {
            this.credential = credential;
            
            setTitle(credential == null ? "Add Credential" : "Edit Credential");
            setHeaderText(null);
            
            // Set up dialog content
            javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            javafx.scene.control.TextField serviceField = new javafx.scene.control.TextField();
            serviceField.setPromptText("Service name (e.g., m6plus)");
            
            javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField();
            usernameField.setPromptText("Username or email");
            
            javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
            passwordField.setPromptText("Password");
            
            javafx.scene.control.CheckBox enabledCheckBox = new javafx.scene.control.CheckBox();
            enabledCheckBox.setSelected(true);
            
            javafx.scene.control.TextField descriptionField = new javafx.scene.control.TextField();
            descriptionField.setPromptText("Description (optional)");

            grid.add(new javafx.scene.control.Label("Service:"), 0, 0);
            grid.add(serviceField, 1, 0);
            grid.add(new javafx.scene.control.Label("Username:"), 0, 1);
            grid.add(usernameField, 1, 1);
            grid.add(new javafx.scene.control.Label("Password:"), 0, 2);
            grid.add(passwordField, 1, 2);
            grid.add(new javafx.scene.control.Label("Enabled:"), 0, 3);
            grid.add(enabledCheckBox, 1, 3);
            grid.add(new javafx.scene.control.Label("Description:"), 0, 4);
            grid.add(descriptionField, 1, 4);

            getDialogPane().setContent(grid);

            // Load existing data if editing
            if (credential != null) {
                serviceField.setText(credential.getService());
                usernameField.setText(credential.getUsername());
                passwordField.setText(credential.getPassword());
                enabledCheckBox.setSelected(credential.getEnabled());
                descriptionField.setText(credential.getDescription());
            }

            // Add buttons
            javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

            // Set result converter
            setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    if (validateForm(serviceField, usernameField, passwordField)) {
                        confirmed = true;
                        CredentialConfig newCredential = new CredentialConfig(
                            serviceField.getText().trim(),
                            usernameField.getText().trim(),
                            passwordField.getText()
                        );
                        newCredential.setEnabled(enabledCheckBox.isSelected());
                        newCredential.setDescription(descriptionField.getText().trim());
                        return newCredential;
                    }
                }
                return null;
            });
        }

        private boolean validateForm(javafx.scene.control.TextField serviceField, 
                                   javafx.scene.control.TextField usernameField, 
                                   javafx.scene.control.PasswordField passwordField) {
            if (serviceField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Service name is required.", AlertType.ERROR);
                return false;
            }
            if (usernameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Username is required.", AlertType.ERROR);
                return false;
            }
            if (passwordField.getText().isEmpty()) {
                showAlert("Validation Error", "Password is required.", AlertType.ERROR);
                return false;
            }
            return true;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public CredentialConfig getCredential() {
            return getResult();
        }
    }
} 