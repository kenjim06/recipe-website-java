package application.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import application.models.Recipe;
import application.services.RecipeService;
import application.services.SupabaseException;
import application.services.UserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class RecipeCardFactory {
    private RecipeCardFactory() {
    }

    public static VBox create(Recipe recipe, List<String> details, Consumer<Node> onOpen) {
        return create(recipe, details, onOpen, new HashSet<>(), null);
    }

    public static VBox create(Recipe recipe, List<String> details, Consumer<Node> onOpen,
            Consumer<Node> onDelete) {
        return create(recipe, details, onOpen, new HashSet<>(), onDelete);
    }

    public static VBox create(Recipe recipe, List<String> details, Consumer<Node> onOpen,
            Set<Long> favoriteRecipeIds, Consumer<Node> onDelete) {
        return create(recipe, details, onOpen, favoriteRecipeIds, onDelete, null);
    }

    public static VBox create(Recipe recipe, List<String> details, Consumer<Node> onOpen,
            Set<Long> favoriteRecipeIds, Consumer<Node> onDelete, Consumer<Boolean> onFavoriteChanged) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(details, "details");
        Objects.requireNonNull(onOpen, "onOpen");
        Objects.requireNonNull(favoriteRecipeIds, "favoriteRecipeIds");

        VBox card = new VBox(10.0);
        card.setMinSize(220.0, 0.0);
        card.setPrefSize(250.0, 360.0);
        card.setMaxSize(260.0, Double.MAX_VALUE);
        card.getStyleClass().add("recipe-card");
        card.setAccessibleRole(AccessibleRole.BUTTON);
        card.setAccessibleText(recipe.name() + ", " + recipe.category()
                + ", " + recipe.totalTimeMinutes() + " minutes");
        card.setFocusTraversable(true);

        Label name = new Label(recipe.name());
        name.getStyleClass().add("recipe-name");
        name.setWrapText(true);
        name.setMaxWidth(Double.MAX_VALUE);

        StackPane imageBox = new StackPane();
        imageBox.setMinHeight(128.0);
        imageBox.setPrefHeight(145.0);
        imageBox.setMaxHeight(160.0);
        imageBox.getStyleClass().add("recipe-image-box");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(194.0);
        imageView.setFitHeight(145.0);
        imageView.setPreserveRatio(true);
        imageView.setAccessibleText(recipe.name() + " image");
        RecipeImageLoader.loadCover(imageView, imageBox, recipe.imageUrl());
        imageBox.getChildren().add(imageView);

        boolean initiallyFavorite = favoriteRecipeIds.contains(recipe.id());
        Button favoriteButton = new Button(initiallyFavorite ? "★" : "☆");
        favoriteButton.getStyleClass().add("favorite-star-button");
        if (initiallyFavorite) {
            favoriteButton.getStyleClass().add("favorited");
        }
        updateFavoriteAccessibility(favoriteButton, recipe, initiallyFavorite);
        StackPane.setAlignment(favoriteButton, Pos.TOP_RIGHT);
        StackPane.setMargin(favoriteButton, new Insets(7.0));
        favoriteButton.setOnMouseClicked(event -> event.consume());
        favoriteButton.setOnAction(event -> {
            event.consume();
            if (!UserService.isLoggedIn()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Log in to save favorite recipes.", ButtonType.OK).showAndWait();
                return;
            }
            boolean nextState = !favoriteRecipeIds.contains(recipe.id());
            try {
                RecipeService.setFavorite(recipe.id(), nextState);
                if (nextState) {
                    favoriteRecipeIds.add(recipe.id());
                } else {
                    favoriteRecipeIds.remove(recipe.id());
                }
                favoriteButton.setText(nextState ? "★" : "☆");
                favoriteButton.getStyleClass().remove("favorited");
                if (nextState) {
                    favoriteButton.getStyleClass().add("favorited");
                }
                updateFavoriteAccessibility(favoriteButton, recipe, nextState);
                if (onFavoriteChanged != null) {
                    onFavoriteChanged.accept(nextState);
                }
            } catch (SupabaseException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
            }
        });
        imageBox.getChildren().add(favoriteButton);

        Label category = new Label(recipe.category().toUpperCase(Locale.ROOT));
        category.getStyleClass().add("recipe-category");
        String author = recipe.userName().isBlank() ? "Community" : recipe.userName();
        Label authorLabel = new Label("By " + author);
        authorLabel.getStyleClass().add("recipe-card-author");
        authorLabel.setWrapText(true);

        HBox recipeStats = new HBox(10.0);
        recipeStats.setAlignment(Pos.CENTER_LEFT);
        Label cost = new Label(recipe.estimatedCostDollars() > 0
                ? "Cost: $" + recipe.estimatedCostDollars()
                : "Cost: Not listed");
        cost.getStyleClass().add("recipe-card-meta");
        Label saves = new Label((recipe.popularity() >= 100 ? "Popular | " : "")
                + recipe.popularity() + " saves");
        saves.getStyleClass().add("recipe-card-meta");
        HBox.setHgrow(cost, javafx.scene.layout.Priority.ALWAYS);
        recipeStats.getChildren().addAll(cost, saves);

        card.getChildren().addAll(name, imageBox, category, authorLabel, recipeStats);

        details.stream()
                .filter(detail -> detail != null && !detail.isBlank())
                .map(detail -> {
                    Label label = new Label(detail);
                    label.getStyleClass().add("recipe-time");
                    label.setWrapText(true);
                    return label;
                })
                .forEach(card.getChildren()::add);

        Button openButton = new Button("See complete recipe");
        openButton.getStyleClass().add("recipe-card-action");
        openButton.setMaxWidth(Double.MAX_VALUE);
        openButton.setOnAction(event -> onOpen.accept(openButton));
        card.getChildren().add(openButton);

        if (onDelete != null) {
            card.getStyleClass().add("manager-recipe-card");
            card.setPrefHeight(420.0);
            Button deleteButton = new Button("Delete recipe");
            deleteButton.getStyleClass().add("secondary-button");
            deleteButton.setMaxWidth(Double.MAX_VALUE);
            deleteButton.setOnMouseClicked(event -> event.consume());
            deleteButton.setOnAction(event -> {
                event.consume();
                onDelete.accept(deleteButton);
            });
            card.getChildren().add(deleteButton);
        }

        card.setOnMouseClicked(event -> {
            if (!openButton.isHover()) {
                onOpen.accept(card);
            }
        });
        card.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER, SPACE -> {
                    onOpen.accept(card);
                    event.consume();
                }
                default -> {
                }
            }
        });
        return card;
    }

    private static void updateFavoriteAccessibility(Button button, Recipe recipe, boolean favorite) {
        button.setAccessibleText((favorite ? "Remove " : "Add ") + recipe.name() + " "
                + (favorite ? "from" : "to") + " favorites");
    }
}
