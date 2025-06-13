/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.dabi.habitv.tray.controller.todl;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.StringConverter;

public abstract class MyCheckBoxTreeCell<T> extends TreeCell<T> {

	private final CheckBox checkBox;

	private ObservableValue<Boolean> booleanProperty;

	private BooleanProperty indeterminateProperty;

	public MyCheckBoxTreeCell(StringConverter<TreeItem<T>> strConverter) {
		// getSelectedProperty as anonymous inner class to deal with situation
		// where the user is using CheckBoxTreeItem instances in their tree
		this(new Callback<TreeItem<T>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TreeItem<T> item) {
				if (item instanceof CheckBoxTreeItem<?>) {
					return ((CheckBoxTreeItem<?>) item).selectedProperty();
				}
				return null;
			}
		}, strConverter);
	}

	private static <T> StringConverter<TreeItem<T>> createDefaultTreeItemStringConverter() {
		return new StringConverter<TreeItem<T>>() {
			@Override
			public String toString(TreeItem<T> treeItem) {
				return (treeItem == null || treeItem.getValue() == null) ? ""
						: treeItem.getValue().toString();
			}

			@Override
			public TreeItem<T> fromString(String string) {
				return new TreeItem<T>((T) string);
			}
		};
	}

	public MyCheckBoxTreeCell(
			final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty) {
		this(getSelectedProperty, MyCheckBoxTreeCell.<T>createDefaultTreeItemStringConverter());
	}

	public MyCheckBoxTreeCell(
			final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedProperty,
			final StringConverter<TreeItem<T>> converter) {
		if (getSelectedProperty == null) {
			throw new NullPointerException(
					"getSelectedProperty can not be null");
		}
		this.getStyleClass().add("choice-box-tree-cell");
		setSelectedStateCallback(getSelectedProperty);
		setConverter(converter);

		this.checkBox = new CheckBox();
		this.checkBox.setAllowIndeterminate(false);
		setGraphic(checkBox);
	}

	/***************************************************************************
	 * * Properties * *
	 **************************************************************************/

	// --- converter
	private ObjectProperty<StringConverter<TreeItem<T>>> converter = new SimpleObjectProperty<StringConverter<TreeItem<T>>>(
			this, "converter");

	/**
	 * The {@link StringConverter} property.
	 */
	public final ObjectProperty<StringConverter<TreeItem<T>>> converterProperty() {
		return converter;
	}

	/**
	 * Sets the {@link StringConverter} to be used in this cell.
	 */
	public final void setConverter(StringConverter<TreeItem<T>> value) {
		converterProperty().set(value);
	}

	/**
	 * Returns the {@link StringConverter} used in this cell.
	 */
	public final StringConverter<TreeItem<T>> getConverter() {
		return converterProperty().get();
	}

	// --- selected state callback property
	private ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallback = new SimpleObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>>(
			this, "selectedStateCallback");

	/**
	 * Property representing the {@link Callback} that is bound to by the
	 * CheckBox shown on screen.
	 */
	public final ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
		return selectedStateCallback;
	}

	/**
	 * Sets the {@link Callback} that is bound to by the CheckBox shown on
	 * screen.
	 */
	public final void setSelectedStateCallback(
			Callback<TreeItem<T>, ObservableValue<Boolean>> value) {
		selectedStateCallbackProperty().set(value);
	}

	/**
	 * Returns the {@link Callback} that is bound to by the CheckBox shown on
	 * screen.
	 */
	public final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedStateCallback() {
		return selectedStateCallbackProperty().get();
	}

	/***************************************************************************
	 * * Public API * *
	 **************************************************************************/

	/** {@inheritDoc} */
	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			StringConverter c = getConverter();
			Callback<TreeItem<T>, ObservableValue<Boolean>> callback = getSelectedStateCallback();

			// update the node content
			String value = c.toString(getTreeItem());
			if (isNew(item)) {
				value += "*";
				setTooltip(new Tooltip(
						"Cette catégorie a été ajoutée ou modifiée lors de la dernière mise à jour."));
			}
			setText(value);
			if (isBold(item)) {
				setFont(Font.font(null, FontWeight.BOLD, getFont().getSize()));
				setTooltip(new Tooltip(
						"Des sous-catégories sont sélectionnées pour le téléchargement auto."));
			} else {
				setFont(Font.font(null, FontWeight.NORMAL, getFont().getSize()));
			}

			if (isDeleted(item)) {
				setTextFill(Color.GRAY);
				setTooltip(new Tooltip(
						"Cette catégorie n'est plus présente chez le fournisseur."));
			} else {
				if (isFailed(item)) {
					setTextFill(Color.RED);
				} else {
					setTextFill(Color.BLACK);
				}
			}

			if (showCheckBox(item)) {
				setGraphic(checkBox);
			} else {
				setGraphic(new Label("     "));
			}

			// uninstall bindings
			if (booleanProperty != null) {
				checkBox.selectedProperty().unbindBidirectional(
						(BooleanProperty) booleanProperty);
			}
			if (indeterminateProperty != null) {
				checkBox.indeterminateProperty().unbindBidirectional(
						indeterminateProperty);
			}

			// install new bindings.
			// We special case things when the TreeItem is a CheckBoxTreeItem
			if (getTreeItem() instanceof CheckBoxTreeItem) {
				CheckBoxTreeItem<T> cbti = (CheckBoxTreeItem<T>) getTreeItem();
				booleanProperty = cbti.selectedProperty();
				checkBox.selectedProperty().bindBidirectional(
						(BooleanProperty) booleanProperty);

				indeterminateProperty = cbti.indeterminateProperty();
				checkBox.indeterminateProperty().bindBidirectional(
						indeterminateProperty);
			} else {
				booleanProperty = callback.call(getTreeItem());
				if (booleanProperty != null) {
					checkBox.selectedProperty().bindBidirectional(
							(BooleanProperty) booleanProperty);
				}
			}
		}
	}

	protected abstract boolean isFailed(T item);

	protected abstract boolean isNew(T item);

	protected abstract boolean isDeleted(T item);

	protected abstract boolean isBold(T item);

	protected abstract boolean showCheckBox(T item);
}