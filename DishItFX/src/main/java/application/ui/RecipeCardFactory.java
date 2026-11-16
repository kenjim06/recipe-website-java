package application.ui;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import application.models.Recipe;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class RecipeCardFactory {
    private RecipeCardFactory() {
    }

    public static VBox create(Recipe recipe, List<String> details, Consumer<Node> onOpen) {
        Objects.requireNonNull(recipe, "recipe");
        Objects.requireNonNull(details, "details");
        Objects.requireNonNull(onOpen, "onOpen");

        VBox card = new VBox(10.0);
        card.setMinSize(205.0, 0.0);
        card.setPrefSize(220.0, 300.0);
        card.setMaxSize(240.0, Double.MAX_VALUE);
        card.getStyleClass().add("recipe-card");
        card.setAccessibleRole(AccessibleRole.BUTTON);
        card.setAccessibleText(recipe.name() + ", " + recipe.category()
                + ", " + recipe.totalTimeMinutes() + " minutes");
        card.setFocusTraversable(true);

        Label name;

        if(recipe.name().length() > 20){
            name = new Label(recipe.name().substring(0,18) + "...");
        } else {
            name = new Label(recipe.name());
        }
        name.getStyleClass().add("recipe-name");

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

        Label category = new Label(recipe.category().toUpperCase(Locale.ROOT));
        category.getStyleClass().add("recipe-category");
        card.getChildren().addAll(name, imageBox, category);

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
}
