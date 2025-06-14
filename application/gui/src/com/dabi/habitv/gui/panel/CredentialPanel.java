package com.dabi.habitv.gui.panel;

import com.dabi.habitv.core.config.CredentialConfig;
import com.dabi.habitv.core.service.CredentialManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel for managing credentials in the habiTv GUI.
 */
public class CredentialPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(CredentialPanel.class);
    
    private JTable credentialTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton removeButton;
    private JButton refreshButton;
    
    private CredentialManager credentialManager;

    public CredentialPanel() {
        this.credentialManager = CredentialManager.getInstance();
        initComponents();
        loadCredentials();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Service Credentials"));

        // Create table
        String[] columnNames = {"Service", "Username", "Enabled", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        credentialTable = new JTable(tableModel);
        credentialTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(credentialTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Add components to panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addButton = new JButton("Add Credential");
        editButton = new JButton("Edit Credential");
        removeButton = new JButton("Remove Credential");
        refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddCredentialDialog();
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedCredential();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedCredential();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCredentials();
            }
        });
        
        panel.add(addButton);
        panel.add(editButton);
        panel.add(removeButton);
        panel.add(refreshButton);
        
        return panel;
    }

    private void loadCredentials() {
        tableModel.setRowCount(0);
        List<CredentialConfig> credentials = credentialManager.getAllCredentials();
        
        for (CredentialConfig credential : credentials) {
            Object[] row = {
                credential.getService(),
                maskUsername(credential.getUsername()),
                credential.getEnabled(),
                credential.getDescription()
            };
            tableModel.addRow(row);
        }
        
        updateButtonStates();
    }

    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return username;
        }
        return username.substring(0, 2) + "***" + username.substring(username.length() - 2);
    }

    private void showAddCredentialDialog() {
        CredentialDialog dialog = new CredentialDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            CredentialConfig newCredential = dialog.getCredential();
            credentialManager.addCredential(newCredential);
            loadCredentials();
            LOG.info("Added new credential for service: " + newCredential.getService());
        }
    }

    private void editSelectedCredential() {
        int selectedRow = credentialTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a credential to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String service = (String) tableModel.getValueAt(selectedRow, 0);
        CredentialConfig credential = credentialManager.getCredential(service);
        
        if (credential != null) {
            CredentialDialog dialog = new CredentialDialog((Frame) SwingUtilities.getWindowAncestor(this), credential);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                CredentialConfig updatedCredential = dialog.getCredential();
                credentialManager.addCredential(updatedCredential);
                loadCredentials();
                LOG.info("Updated credential for service: " + updatedCredential.getService());
            }
        }
    }

    private void removeSelectedCredential() {
        int selectedRow = credentialTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a credential to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String service = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to remove the credential for " + service + "?", 
            "Confirm Removal", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            credentialManager.removeCredential(service);
            loadCredentials();
            LOG.info("Removed credential for service: " + service);
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = credentialTable.getSelectedRow() != -1;
        editButton.setEnabled(hasSelection);
        removeButton.setEnabled(hasSelection);
    }

    /**
     * Inner dialog class for adding/editing credentials
     */
    private class CredentialDialog extends JDialog {
        private JTextField serviceField;
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JCheckBox enabledCheckBox;
        private JTextField descriptionField;
        private boolean confirmed = false;
        private CredentialConfig credential;

        public CredentialDialog(Frame parent, CredentialConfig credential) {
            super(parent, credential == null ? "Add Credential" : "Edit Credential", true);
            this.credential = credential;
            initDialog();
        }

        private void initDialog() {
            setLayout(new BorderLayout());
            setSize(400, 300);
            setLocationRelativeTo(getParent());

            // Create form panel
            JPanel formPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // Service field
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Service:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            serviceField = new JTextField(20);
            formPanel.add(serviceField, gbc);

            // Username field
            gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            formPanel.add(new JLabel("Username:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            usernameField = new JTextField(20);
            formPanel.add(usernameField, gbc);

            // Password field
            gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            formPanel.add(new JLabel("Password:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            passwordField = new JPasswordField(20);
            formPanel.add(passwordField, gbc);

            // Enabled checkbox
            gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            formPanel.add(new JLabel("Enabled:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            enabledCheckBox = new JCheckBox();
            formPanel.add(enabledCheckBox, gbc);

            // Description field
            gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
            formPanel.add(new JLabel("Description:"), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
            descriptionField = new JTextField(20);
            formPanel.add(descriptionField, gbc);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (validateForm()) {
                        confirmed = true;
                        dispose();
                    }
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            // Add components to dialog
            add(formPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            // Load existing data if editing
            if (credential != null) {
                loadCredentialData();
            } else {
                enabledCheckBox.setSelected(true);
            }
        }

        private void loadCredentialData() {
            serviceField.setText(credential.getService());
            usernameField.setText(credential.getUsername());
            passwordField.setText(credential.getPassword());
            enabledCheckBox.setSelected(credential.getEnabled());
            descriptionField.setText(credential.getDescription());
        }

        private boolean validateForm() {
            if (serviceField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Service name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (passwordField.getPassword().length == 0) {
                JOptionPane.showMessageDialog(this, "Password is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public CredentialConfig getCredential() {
            if (!confirmed) {
                return null;
            }
            
            return new CredentialConfig(
                serviceField.getText().trim(),
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                enabledCheckBox.isSelected(),
                descriptionField.getText().trim()
            );
        }
    }
} 