package com.dabi.habitv.tray.controller.todl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import com.dabi.habitv.api.plugin.dto.EpisodeDTO;
import com.dabi.habitv.api.plugin.dto.StatusEnum;
import com.dabi.habitv.api.plugin.exception.TechnicalException;
import com.dabi.habitv.api.plugin.pub.UpdatablePluginEvent;
import com.dabi.habitv.core.event.RetreiveEvent;
import com.dabi.habitv.core.event.SearchCategoryEvent;
import com.dabi.habitv.core.event.SearchEvent;
import com.dabi.habitv.core.event.UpdatePluginEvent;
import com.dabi.habitv.framework.FrameworkConf;
import com.dabi.habitv.framework.plugin.tpl.TemplateUtils;
import com.dabi.habitv.framework.plugin.utils.DownloadUtils;
import com.dabi.habitv.framework.plugin.utils.RetrieverUtils;
import com.dabi.habitv.tray.Popin;
import com.dabi.habitv.tray.PopinController.ButtonHandler;
import com.dabi.habitv.tray.controller.BaseController;
import com.dabi.habitv.tray.controller.todl.CategoryTreeItem.SelectionChangeHandler;
import com.dabi.habitv.tray.subscriber.CoreSubscriber;
import com.dabi.habitv.utils.FilterUtils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ToDownloadController extends BaseController implements CoreSubscriber {

	private Button refreshCategoryButton;

	private Button cleanCategoryButton;

	private TreeView<CategoryDTO> toDLTree;

	private Map<String, CategoryDTO> plugins;

	private Label indicationText;

	private ProgressIndicator searchCategoryProgress;

	private ListView<EpisodeDTO> episodeListView;

	private TextField episodeFilter;

	private Collection<EpisodeDTO> currentEpisodes;

	private TextField categoryFilter;

	private Set<String> downloadedEpisodes;

	private ChoiceBox<IncludeExcludeEnum> filterTypeChoice;

	private HBox currentFilterVBox;

	private Button addFilterButton;

	private CheckBox applySavedFilters;

	public ToDownloadController(ProgressIndicator searchCategoryProgress, Button refreshCategoryButton, Button cleanCategoryButton,
	        TreeView<CategoryDTO> toDLTree, Label indicationTextFlow, ListView<EpisodeDTO> episodeListView, TextField episodeFilter,
	        TextField categoryFilter, CheckBox applySavedFilters, ChoiceBox<IncludeExcludeEnum> filterTypeChoice, Button addFilterButton,
	        HBox currentFilterVBox) {
		super();
		this.refreshCategoryButton = refreshCategoryButton;
		this.cleanCategoryButton = cleanCategoryButton;
		this.toDLTree = toDLTree;
		this.indicationText = indicationTextFlow;
		this.searchCategoryProgress = searchCategoryProgress;
		this.episodeListView = episodeListView;
		this.episodeFilter = episodeFilter;
		this.categoryFilter = categoryFilter;
		this.filterTypeChoice = filterTypeChoice;
		this.applySavedFilters = applySavedFilters;
		this.addFilterButton = addFilterButton;
		this.currentFilterVBox = currentFilterVBox;
		this.currentFilterVBox.setVisible(false);
		final TreeView<CategoryDTO> toDLTree2 = toDLTree;

		addSpaceHandler(toDLTree, toDLTree2);
		initContextOp(toDLTree);
	}

	@Override
	protected void init() {
		loadTree();
		addButtonsActions();
		addTooltips();
		initFilters();
		initIncludeExcludeFilterHandler();
	}

	private void initIncludeExcludeFilterHandler() {
		applySavedFilters.setSelected(true);

		filterTypeChoice.getItems().addAll(IncludeExcludeEnum.values());
		filterTypeChoice.setValue(IncludeExcludeEnum.INCLUDE);

		filterTypeChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<IncludeExcludeEnum>() {

			@Override
			public void changed(ObservableValue<? extends IncludeExcludeEnum> observable, IncludeExcludeEnum oldValue, IncludeExcludeEnum newValue) {
				filterEpisodeListView(episodeFilter.getText());
			}
		});

		addFilterButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (toDLTree.getSelectionModel().getSelectedItem() != null) {
					CategoryDTO category = toDLTree.getSelectionModel().getSelectedItem().getValue();
					if (filterTypeChoice.getValue() == IncludeExcludeEnum.INCLUDE) {
						category.getInclude().add(toRegExp(episodeFilter.getText()));
					} else {
						category.getExclude().add(toRegExp(episodeFilter.getText()));
					}
					episodeFilter.clear();
					saveTree();
					fillIncludeExcludePatterns(category);
					fillEpisodeList(category);
				}
			}

		});
		currentFilterVBox.setVisible(false);
	}

	private String toRegExp(String text) {
		return ".*" + text + ".*";
	}

	private void addSpaceHandler(TreeView<CategoryDTO> toDLTree, final TreeView<CategoryDTO> toDLTree2) {
		toDLTree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.SPACE) {
					TreeItem<CategoryDTO> selectedItem = toDLTree2.getSelectionModel().getSelectedItem();
					CategoryTreeItem CategoryTreeItem = (CategoryTreeItem) selectedItem;
					CategoryTreeItem.setSelected(!CategoryTreeItem.isSelected());
				}
			}
		});
	}

	private void initContextOp(TreeView<CategoryDTO> toDLTree) {
		toDLTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<CategoryDTO>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<CategoryDTO>> arg0, TreeItem<CategoryDTO> oldValue,
		            TreeItem<CategoryDTO> newValue) {
				if (newValue != null) {
					buildContextMenu(newValue);
					CategoryDTO category = newValue.getValue();
					if (category.isDownloadable()) {
						fillEpisodeList(category);
						fillIncludeExcludePatterns(category);
					} else {
						emptyEpisodeList();
						currentFilterVBox.setVisible(false);
					}
				}
				episodeFilter.clear();
			}

		});
	}

	private void fillIncludeExcludePatterns(CategoryDTO category) {
		final Pane reCallVBox = (Pane) currentFilterVBox.getChildren().get(2);
		reCallVBox.getChildren().clear();
		fillPatterns(category, reCallVBox, category.getInclude(), true);
		fillPatterns(category, reCallVBox, category.getExclude(), false);

		currentFilterVBox.setVisible(!category.getInclude().isEmpty() || !category.getExclude().isEmpty());
	}

	private void fillPatterns(final CategoryDTO category, final Pane reCallVBox, List<String> patterns, boolean include) {
		for (String pattern : patterns) {
			final IncludeExcludeReCall includeExcludeBox = new IncludeExcludeReCall(category, pattern, include);
			EventHandler<ActionEvent> deleteHandler = new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					reCallVBox.getChildren().remove(includeExcludeBox);
					fillEpisodeList(category);
					saveTree();
				}
			};
			includeExcludeBox.setOnAction(deleteHandler);
			reCallVBox.getChildren().add(includeExcludeBox);
		}
	}

	private void emptyEpisodeList() {
		ObservableList<EpisodeDTO> obsEp = FXCollections.observableArrayList();
		episodeListView.setItems(obsEp);
	}

	private void buildContextMenu(final TreeItem<CategoryDTO> treeItem) {
		final CategoryDTO category = treeItem.getValue();
		ContextMenu contextMenu = new ContextMenu();
		if (category.isTemplate()) {
			MenuItem ajoutMenu = new MenuItem("Ajouter une catégorie " + category.getName());
			ajoutMenu.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					formulaireAjout(treeItem, category);
				}

			});
			contextMenu.getItems().add(ajoutMenu);
		}

		if (treeItem.getParent() != null) {
			MenuItem indexMenu = new MenuItem("Ouvrir l'index");
			indexMenu.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					getController().openIndex(category);
				}
			});
			contextMenu.getItems().add(indexMenu);

			MenuItem supprimerMenu = new MenuItem("Supprimer");
			supprimerMenu.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					category.setDeleted(true);
					planTaskIfNot(new Runnable() {

				        @Override
				        public void run() {
					        saveTree();
				        }
			        });
					toDLTree.getSelectionModel().getSelectedItem().getParent().getChildren().remove(toDLTree.getSelectionModel().getSelectedItem());
				}
			});
			contextMenu.getItems().add(supprimerMenu);

			final MenuItem figerMenu = new MenuItem(category.getState() == StatusEnum.USER ? "Défiger" : "Figer");
			figerMenu.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					if (category.getState() == StatusEnum.USER) {
						category.setState(StatusEnum.EXIST);
						figerMenu.setText("Figer");
					} else {
						category.setState(StatusEnum.USER);
						figerMenu.setText("Défiger");
					}
					planTaskIfNot(new Runnable() {

				        @Override
				        public void run() {
					        saveTree();
				        }
			        });
				}
			});
			contextMenu.getItems().add(figerMenu);
		}

		toDLTree.setContextMenu(contextMenu);
	}

	private void formulaireAjout(final TreeItem<CategoryDTO> treeItem, final CategoryDTO templateCategory) {
		final CategoryForm categoryForm = new CategoryForm(templateCategory);
		Double width = categoryForm.getAdvisedWidth();
		Double height = categoryForm.getAdvisedHeight();
		new Popin(width, height).show("Ajout d'une catégorie " + templateCategory.getName(), categoryForm).setOkButtonHandler(new ButtonHandler() {

			@Override
			public void onAction() {
				CategoryDTO newCategory = buildCategoryFromTemplate(templateCategory, categoryForm.getValues());
				templateCategory.addSubCategory(newCategory);

				addCategoryToTree((CategoryTreeItem) treeItem, newCategory);
				saveTree();
			}

		});
	}

	private CategoryDTO buildCategoryFromTemplate(CategoryDTO templateCategory, Map<String, String> values) {
		CategoryDTO categoryDTO;
		if (templateCategory.getId().contains(TemplateUtils.TEMPLATE_ID_COMMENT_SEP)) {
			categoryDTO = buildCategoryFromTemplateV3(templateCategory, values);
		} else {
			categoryDTO = buildCategoryFromTemplateV2(templateCategory, values.get("ID"));
		}
		return categoryDTO;
	}

	private CategoryDTO buildCategoryFromTemplateV3(CategoryDTO templateCategory, Map<String, String> values) {
		CategoryDTO categoryDTO = new CategoryDTO(templateCategory.getPlugin(), findNameById(values.get("ID"), values.get("NAME")),
		        TemplateUtils.buildIdValues(values), FrameworkConf.MP4);
		categoryDTO.setState(StatusEnum.USER);
		categoryDTO.setDownloadable(true);
		return categoryDTO;
	}

	private CategoryDTO buildCategoryFromTemplateV2(CategoryDTO templateCategory, String text) {
		String id = templateCategory.getId().split("!!")[0].replace("§ID§", text);
		CategoryDTO categoryDTO = new CategoryDTO(templateCategory.getPlugin(), findNameById(id), id, FrameworkConf.MP4);
		categoryDTO.setState(StatusEnum.USER);
		categoryDTO.setDownloadable(true);
		return categoryDTO;
	}

	private String findNameById(String id) {
		return findNameById(id, null);
	}

	private String findNameById(String id, String defaultName) {
		String name;
		if (defaultName == null) {
			if (DownloadUtils.isHttpUrl(id)) {
				name = RetrieverUtils.getTitleByUrl(id);
			} else {
				File file = new File(id);
				if (file.exists()) {
					name = file.getName();
				} else {
					name = defaultName;
				}
			}
		} else {
			name = defaultName;
		}
		return name;
	}

	private void fillEpisodeList(final CategoryDTO category) {
		ObservableList<EpisodeDTO> obsEp = FXCollections.observableArrayList();
		obsEp.addAll(Arrays.asList(new EpisodeDTO(null, "Chargement...", "")));

		episodeListView.setItems(obsEp);
		downloadedEpisodes = getController().getManager().findDownloadedEpisodes(category);

		new Thread(new Runnable() {

			@Override
			public void run() {
				currentEpisodes = getController().findEpisodeByCategory(category);
				Platform.runLater(new Runnable() {

			        @Override
			        public void run() {
				        initListView(currentEpisodes);
				        filterEpisodeListView("");
			        }
		        });
			}

		}).start();

	}

	private void initListView(final Collection<EpisodeDTO> episodes) {
		ObservableList<EpisodeDTO> obsEp = FXCollections.observableArrayList();
		obsEp.addAll(episodes);

		episodeListView.setItems(obsEp);

		episodeListView.setCellFactory(new Callback<ListView<EpisodeDTO>, ListCell<EpisodeDTO>>() {
			@Override
			public ListCell<EpisodeDTO> call(ListView<EpisodeDTO> param) {
				return new ListCell<EpisodeDTO>() {

			        @Override
			        protected void updateItem(EpisodeDTO episode, boolean empty) {
				        super.updateItem(episode, empty);

				        if (!empty) {
					        setText(episode.getName());

					        if (downloadedEpisodes.contains(episode.getName())) {
						        setTextFill(Color.GRAY);
					        } else {
						        setTextFill(Color.BLACK);
					        }
				        } else {
					        setText(null);
				        }
			        }
		        };
			}
		});

		episodeListView.setContextMenu(buildEpisodeContextMenu());

		episodeListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EpisodeDTO>() {

			@Override
			public void changed(ObservableValue<? extends EpisodeDTO> observable, EpisodeDTO oldValue, EpisodeDTO newValue) {
				if (ouvrirUrl != null && observable.getValue() != null) {
					ouvrirUrl.setDisable(!observable.getValue().getId().startsWith("http:"));
				}
			}
		});
	}

	MenuItem ouvrirUrl = new MenuItem("Ouvrir dans le navigateur");

	private ContextMenu buildEpisodeContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		MenuItem telecharger = new MenuItem("Télécharger");
		telecharger.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().downloadEpisode(episodeListView.getSelectionModel().getSelectedItem());
			}
		});
		contextMenu.getItems().add(telecharger);

		MenuItem urlCopie = new MenuItem("Copier l'URL/Id");
		urlCopie.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().copyUrl(episodeListView.getSelectionModel().getSelectedItem());
			}
		});
		contextMenu.getItems().add(urlCopie);

		ouvrirUrl.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().openInBrowser(episodeListView.getSelectionModel().getSelectedItem());
			}
		});
		contextMenu.getItems().add(ouvrirUrl);

		MenuItem marquerTelecharger = new MenuItem("Marquer comme téléchargé");
		marquerTelecharger.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				EpisodeDTO episode = episodeListView.getSelectionModel().getSelectedItem();
				getController().setDownloaded(episode);
				filterEpisodeListView(episodeFilter.getText());
				downloadedEpisodes.add(episode.getName());
			}
		});

		contextMenu.getItems().add(marquerTelecharger);

		return contextMenu;
	}

	private void initFilters() {
		applySavedFilters.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				filterEpisodeListView(episodeFilter.getText());
			}
		});

		episodeFilter.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				filterEpisodeListView(episodeFilter.getText());
			}
		});

		categoryFilter.setOnKeyReleased(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				filterTree(categoryFilter.getText());
			}

		});
	}

	private void filterTree(String text) {
		categoriesToHide.clear();
		String textUpper = text.toUpperCase();
		filterCategories(plugins, textUpper);
		loadTree(plugins);

		if (!text.isEmpty()) {
			expandAll(toDLTree.getRoot().getChildren());
		}
	}

	private void expandAll(Collection<TreeItem<CategoryDTO>> observableList) {
		for (TreeItem<CategoryDTO> treeItem : observableList) {
			treeItem.setExpanded(true);
			expandAll(treeItem.getChildren());
		}
	}

	private void filterCategories(Map<String, CategoryDTO> plugins, String textUpper) {
		for (Entry<String, CategoryDTO> pluginEntry : plugins.entrySet()) {
			if (!categoryPassFilter(pluginEntry.getValue(), textUpper)) {
				categoriesToHide.add(pluginEntry.getValue());
			}
		}

	}

	private final Set<CategoryDTO> categoriesToHide = new HashSet<>();

	private boolean categoryPassFilter(CategoryDTO category, String textUpper) {

		boolean passFilter = category.getName().toUpperCase().contains(textUpper);
		boolean hasChildrenToShow = categoriesPassFilter(category.getSubCategories(), textUpper);
		boolean show = passFilter || hasChildrenToShow;

		if (!show) {
			categoriesToHide.add(category);
		}

		return show;
	}

	private boolean categoriesPassFilter(Set<CategoryDTO> subCategories, String textUpper) {
		boolean catToShow = false;
		if (subCategories != null) {
			for (CategoryDTO categoryDTO : subCategories) {
				catToShow = categoryPassFilter(categoryDTO, textUpper) || catToShow;
			}
		}
		return catToShow;
	}

	private void filterEpisodeListView(String text) {
		initListView(filterEpisodeList(text, this.filterTypeChoice.getValue().isInclude()));
	}

	private Collection<EpisodeDTO> filterEpisodeList(String text, boolean include) {
		Collection<EpisodeDTO> filteredList = new LinkedList<>();
		if (currentEpisodes != null) {
			for (EpisodeDTO episodeDTO : currentEpisodes) {
				List<String> includeList = new ArrayList<>();
				List<String> excludeList = new ArrayList<>();
				if (applySavedFilters.isSelected()) {
					includeList.addAll(episodeDTO.getCategory().getInclude());
					excludeList.addAll(episodeDTO.getCategory().getExclude());
				}
				if (!text.isEmpty()) {
					if (include) {
						includeList.add(text);
					} else {
						excludeList.add(text);
					}
				}

				if (FilterUtils.filterByIncludeExcludeAndDownloaded(episodeDTO, includeList, excludeList)) {
					filteredList.add(episodeDTO);
				}
			}
		}

		return filteredList;
	}

	private void addTooltips() {
		refreshCategoryButton.setTooltip(new Tooltip("Rafraichir l'arbre des catégories."));
		cleanCategoryButton.setTooltip(new Tooltip("Enlever les catégories périmées."));
		indicationText.setText(
		        "Sélectionner les catégories à surveiller pour le téléchargement automatique \n et cliquer sur les épisodes à droite pour le téléchargement manuel.");
	}

	private void addButtonsActions() {
		refreshCategoryButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				new Thread(new Runnable() {

			        @Override
			        public void run() {
				        getController().getManager().updateGrabConfig();
				        Platform.runLater(new Runnable() {

			                @Override
			                public void run() {
				                loadTree();
			                }
		                });
			        }
		        }).start();
			}
		});
		cleanCategoryButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				getController().getManager().cleanCategories();
				loadTree();
			}
		});
	}

	private void loadTree() {
		plugins = new TreeMap<>(getController().loadCategories());
		loadTree(plugins);
	}

	private void loadTree(Map<String, CategoryDTO> pluginsToDisplay) {
		TreeItem<CategoryDTO> root = new CategoryTreeItem(new CategoryDTO(null, "Chaines", "root", null));
		toDLTree.setRoot(root);
		toDLTree.setShowRoot(false);
		toDLTree.setCellFactory(forTreeView());
		for (CategoryDTO plugin : pluginsToDisplay.values()) {
			if (!categoriesToHide.contains(plugin)) {
				TreeItem<CategoryDTO> channelTreeItem = buildCategoryTreeItem(plugin);
				root.getChildren().add(channelTreeItem);
				addCategoriesToTree(channelTreeItem, plugin.getSubCategories());
			}
		}
	}

	private void addCategoriesToTree(TreeItem<CategoryDTO> treeItem, Collection<CategoryDTO> categories) {
		for (CategoryDTO category : categories) {
			addCategoryToTree(treeItem, category);
		}
	}

	private void addCategoryToTree(TreeItem<CategoryDTO> treeItem, CategoryDTO category) {
		if (!category.isDeleted() && !categoriesToHide.contains(category)) {
			final TreeItem<CategoryDTO> categoryTreeItem = buildCategoryTreeItem(category);
			treeItem.getChildren().add(categoryTreeItem);
			if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
				addCategoriesToTree(categoryTreeItem, category.getSubCategories());
			}
		}
	}

	private TreeItem<CategoryDTO> buildCategoryTreeItem(CategoryDTO category) {
		final CategoryTreeItem categoryTreeItem = new CategoryTreeItem(category);
		categoryTreeItem.setSelectionChangeHandler(new SelectionChangeHandler() {

			@Override
			public void onSelectionChange(CategoryTreeItem categoryTreeItem) {
				planTaskIfNot(new Runnable() {

			        @Override
			        public void run() {
				        saveTree();
			        }

		        });

			}
		});
		return categoryTreeItem;
	}

	private void saveTree() {
		if (plugins != null) {
			getController().getManager().saveGrabConfig(plugins);
		}
	}

	@Override
	public void update(UpdatePluginEvent event) {
	}

	@Override
	public void update(UpdatablePluginEvent event) {
	}

	@Override
	public void update(SearchEvent event) {
	}

	@Override
	public void update(RetreiveEvent event) {
	}

	private int searchCount;
	private int searchSize;

	@Override
	public void update(final SearchCategoryEvent event) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				switch (event.getState()) {
				case STARTING:
					refreshCategoryButton.setDisable(true);
					searchCount = 0;
					searchSize = Integer.parseInt(event.getInfo());
					searchCategoryProgress.setProgress((double) searchCount / searchSize);
					break;
				case CATEGORIES_BUILT:
					searchCount++;
					searchCategoryProgress.setProgress((double) searchCount / searchSize);
					break;
				case DONE:
					refreshCategoryButton.setDisable(false);
					searchCategoryProgress.setProgress(1);
					loadTree();
					break;
				default:
					break;
				}
			}

		});
	}

	private static final StringConverter STR_CONVERTER = new StringConverter<TreeItem>() {
		@Override
		public String toString(TreeItem treeItem) {
			CategoryTreeItem categoryTreeItem = (CategoryTreeItem) treeItem;
			return (treeItem == null || treeItem.getValue() == null) ? "" : categoryTreeItem.getValue().getName();
			// + (hasSelectedChild(categoryTreeItem.getValue()) ? "*"
	        // : "");
		}

		// private boolean hasSelectedChild(CategoryDTO categoryDTO) {
	    // for (CategoryDTO subCategoryDTO : categoryDTO.getSubCategories()) {
	    // if (subCategoryDTO.isSelected()) {
	    // return true;
	    // } else {
	    // if (hasSelectedChild(subCategoryDTO)) {
	    // return true;
	    // }
	    // }
	    // }
	    // return false;
	    // }

		@Override
		public TreeItem fromString(String string) {
			return new TreeItem(string);
		}
	};

	private static Callback<TreeView<CategoryDTO>, TreeCell<CategoryDTO>> forTreeView(
	        final Callback<TreeItem<CategoryDTO>, ObservableValue<Boolean>> getSelectedProperty) {
		return new Callback<TreeView<CategoryDTO>, TreeCell<CategoryDTO>>() {
			@Override
			public TreeCell<CategoryDTO> call(TreeView<CategoryDTO> list) {
				return new MyCheckBoxTreeCell<CategoryDTO>(STR_CONVERTER) {

			        @Override
			        protected boolean showCheckBox(CategoryDTO item) {
				        return item.isDownloadable();
			        }

			        @Override
			        protected boolean isDeleted(CategoryDTO item) {
				        return item.getState() == StatusEnum.DELETED;
			        }

			        @Override
			        protected boolean isBold(CategoryDTO item) {
				        return item.isSelected() || item.hasSelectedSubCategory();
			        }

			        @Override
			        protected boolean isNew(CategoryDTO item) {
				        return item.getState() == StatusEnum.NEW || item.hasSubCategoryWithState(StatusEnum.NEW);
			        }

			        @Override
			        protected boolean isFailed(CategoryDTO item) {
				        return item.getState() == StatusEnum.DELETED;
			        }

		        };
			}
		};
	}

	public static Callback<TreeView<CategoryDTO>, TreeCell<CategoryDTO>> forTreeView() {
		Callback<TreeItem<CategoryDTO>, ObservableValue<Boolean>> getSelectedProperty = new Callback<TreeItem<CategoryDTO>, ObservableValue<Boolean>>() {
			@Override
			public ObservableValue<Boolean> call(TreeItem<CategoryDTO> item) {
				if (item instanceof CheckBoxTreeItem<?>) {
					return ((CheckBoxTreeItem<?>) item).selectedProperty();
				}
				return null;
			}
		};
		return forTreeView(getSelectedProperty);
	}
}
