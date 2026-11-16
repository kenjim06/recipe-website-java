package application.ui;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public final class RecipeImageLoader {
    private RecipeImageLoader() {
    }

    public static void loadCover(ImageView imageView, StackPane frame, String imageUrl) {
        imageView.fitWidthProperty().bind(frame.widthProperty());
        imageView.fitHeightProperty().bind(frame.heightProperty());
        imageView.setPreserveRatio(true);
        imageView.setViewport(null);
        if (imageUrl == null || imageUrl.isBlank()) {
            imageView.setImage(null);
            return;
        }

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        clip.setArcWidth(18.0);
        clip.setArcHeight(18.0);
        imageView.setClip(clip);

        Image image = new Image(imageUrl.strip(), true);
        imageView.setImage(image);

        ChangeListener<Number> updateViewport =
                (observable, oldValue, newValue) -> cropToFit(imageView, image);
        image.widthProperty().addListener(updateViewport);
        image.heightProperty().addListener(updateViewport);
        imageView.fitWidthProperty().addListener(updateViewport);
        imageView.fitHeightProperty().addListener(updateViewport);
        cropToFit(imageView, image);
    }

    private static void cropToFit(ImageView imageView, Image image) {
        if (imageView.getImage() != image) {
            return;
        }

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double frameWidth = imageView.getFitWidth();
        double frameHeight = imageView.getFitHeight();
        if (!Double.isFinite(imageWidth) || !Double.isFinite(imageHeight)
                || !Double.isFinite(frameWidth) || !Double.isFinite(frameHeight)
                || imageWidth <= 0.0 || imageHeight <= 0.0
                || frameWidth <= 0.0 || frameHeight <= 0.0) {
            return;
        }

        double frameRatio = frameWidth / frameHeight;
        double imageRatio = imageWidth / imageHeight;
        if (imageRatio > frameRatio) {
            double visibleWidth = imageHeight * frameRatio;
            imageView.setViewport(new Rectangle2D(
                    (imageWidth - visibleWidth) / 2.0,
                    0.0,
                    visibleWidth,
                    imageHeight
            ));
        } else {
            double visibleHeight = imageWidth / frameRatio;
            imageView.setViewport(new Rectangle2D(
                    0.0,
                    (imageHeight - visibleHeight) / 2.0,
                    imageWidth,
                    visibleHeight
            ));
        }
    }
}
