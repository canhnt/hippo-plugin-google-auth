/*
 *  Copyright 2011-2014 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugins.gallery.editor;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.editor.plugins.resource.ResourceHelper;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.gallery.editor.crop.ImageCropBehavior;
import org.hippoecm.frontend.plugins.gallery.editor.crop.ImageCropSettings;
import org.hippoecm.frontend.plugins.gallery.imageutil.ImageUtils;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScaleImageOperation;
import org.hippoecm.frontend.plugins.gallery.imageutil.ScalingParameters;
import org.hippoecm.frontend.plugins.gallery.model.GalleryException;
import org.hippoecm.frontend.plugins.gallery.model.GalleryProcessor;
import org.hippoecm.frontend.plugins.standards.image.JcrImage;
import org.hippoecm.frontend.resource.JcrResourceStream;
import org.hippoecm.repository.gallery.HippoGalleryNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * ImageCropEditorDialog shows a modal dialog with simple image editor in it, the only operation available currently is
 * "cropping". The ImageCropEditorDialog will replace the current variant with the result of the image editing actions.
 */
public class ImageCropEditorDialog extends AbstractDialog<Node> {

    private static final Logger log = LoggerFactory.getLogger(ImageCropEditorDialog.class);

    private static final int MAX_PREVIEW_WIDTH = 200;
    private static final int MAX_PREVIEW_HEIGHT = 300;

    @SuppressWarnings("unused")
    private String region;
    private GalleryProcessor galleryProcessor;
    private Dimension originalImageDimension;
    private Dimension configuredDimension;
    private Dimension thumbnailDimension;
    private float compressionQuality;

    public ImageCropEditorDialog(IModel<Node> jcrImageNodeModel, GalleryProcessor galleryProcessor) {
        super(jcrImageNodeModel);

        this.galleryProcessor = galleryProcessor;
        Node thumbnailImageNode = jcrImageNodeModel.getObject();

         HiddenField<String> regionField = new HiddenField<>("region", new PropertyModel<String>(this, "region"));
        regionField.setOutputMarkupId(true);
        add(regionField);

        Component originalImage, imgPreview;
        try {
            Node originalImageNode = thumbnailImageNode.getParent().getNode(HippoGalleryNodeType.IMAGE_SET_ORIGINAL);
            originalImageDimension = new Dimension(
                    (int) originalImageNode.getProperty(HippoGalleryNodeType.IMAGE_WIDTH).getLong(),
                    (int) originalImageNode.getProperty(HippoGalleryNodeType.IMAGE_HEIGHT).getLong()
            );

            JcrNodeModel originalNodeModel = new JcrNodeModel(originalImageNode);
            originalImage = new JcrImage("image", new JcrResourceStream(originalNodeModel));
            imgPreview = new JcrImage("imagepreview", new JcrResourceStream(originalNodeModel));

        } catch (RepositoryException e) {
            log.error("Cannot retrieve original image", e);
            error(e);
            originalImage = new EmptyPanel("image");
            imgPreview = new EmptyPanel("imagepreview");
        }

        WebMarkupContainer imagePreviewContainer = new WebMarkupContainer("previewcontainer");
        imagePreviewContainer.setOutputMarkupId(true);
        try {
            configuredDimension = galleryProcessor.getDesiredResourceDimension(thumbnailImageNode);
            thumbnailDimension = handleZeroValueInDimension(originalImageDimension, configuredDimension);

            final double previewCropFactor = determinePreviewScalingFactor(thumbnailDimension.getWidth(), thumbnailDimension.getHeight());
            final double previewWidth = Math.floor(previewCropFactor * thumbnailDimension.getWidth());
            final double previewHeight = Math.floor(previewCropFactor * thumbnailDimension.getHeight());

            imagePreviewContainer.add(new AttributeAppender("style", Model.of("width:" + previewWidth + "px"), ";"));
            imagePreviewContainer.add(new AttributeAppender("style", Model.of("height:" + previewHeight + "px"), ";"));

        } catch (RepositoryException | GalleryException e) {
            log.error("Cannot retrieve thumbnail dimensions", e);
            error(e);
        }

        boolean isUpscalingEnabled = true;
        try {
            isUpscalingEnabled = galleryProcessor.isUpscalingEnabled(thumbnailImageNode);
        } catch (GalleryException | RepositoryException e) {
            log.error("Cannot retrieve Upscaling configuration option", e);
            error(e);
        }

        Label thumbnailSize = new Label("thumbnail-size", new StringResourceModel("thumbnail-size", this, null));
        thumbnailSize.setOutputMarkupId(true);
        add(thumbnailSize);

        ImageCropSettings cropSettings = new ImageCropSettings(regionField.getMarkupId(),
                imagePreviewContainer.getMarkupId(),
                originalImageDimension,
                configuredDimension,
                thumbnailDimension,
                isUpscalingEnabled,
                thumbnailSize.getMarkupId(true));
        originalImage.add(new ImageCropBehavior(cropSettings));
        originalImage.setOutputMarkupId(true);

        add(originalImage);
        imgPreview.add(new AttributeAppender("style", Model.of("position:absolute"), ";"));
        imagePreviewContainer.add(imgPreview);
        imagePreviewContainer.setVisible(cropSettings.isPreviewVisible());
        add(imagePreviewContainer);

        add(new Label("preview-description", cropSettings.isPreviewVisible() ?
                        new StringResourceModel("preview-description-enabled", this, null) :
                        new StringResourceModel("preview-description-disabled", this, null))
        );

        compressionQuality = 1.0f;
        try{
            compressionQuality = galleryProcessor.getScalingParametersMap().get(thumbnailImageNode.getName()).getCompressionQuality();
        } catch (RepositoryException e) {
            log.info("Cannot retrieve compression quality.", e);
        }
    }

    /**
     * Determine the scaling factor of the preview image, so that it fits within the max boundaries of
     * the preview container (e.g. {@code #MAX_PREVIEW_WIDTH} by {@code #MAX_PREVIEW_HEIGHT}).
     * @param previewWidth width of preview image
     * @param previewHeight height of preview image
     * @return the scaling factor of the preview image
     */
    private double determinePreviewScalingFactor(final double previewWidth, final double previewHeight) {

        final double widthBasedScaling;
        if (previewWidth > MAX_PREVIEW_WIDTH) {
            widthBasedScaling = MAX_PREVIEW_WIDTH / previewWidth;
        } else {
            widthBasedScaling = 1D;
        }

        final double heightBasedScaling;

        if (previewHeight > MAX_PREVIEW_HEIGHT) {
            heightBasedScaling = MAX_PREVIEW_HEIGHT / previewHeight;
        } else {
            heightBasedScaling = 1D;
        }

        if (heightBasedScaling < widthBasedScaling) {
            return heightBasedScaling;
        } else {
            return widthBasedScaling;
        }
    }

    @Override
    public IValueMap getProperties() {
        return new ValueMap("width=855,height=565").makeImmutable();
    }

    @Override
    public IModel<String> getTitle() {
        return new StringResourceModel("edit-image-dialog-title", ImageCropEditorDialog.this, null);
    }

    @Override
    protected void onOk() {
        JSONObject jsonObject = JSONObject.fromObject(region);

        int top = jsonObject.getInt("top");
        int height = jsonObject.getInt("height");
        int left = jsonObject.getInt("left");
        int width = jsonObject.getInt("width");
        try {
            Node originalImageNode = getModelObject().getParent().getNode(HippoGalleryNodeType.IMAGE_SET_ORIGINAL);
            String mimeType = originalImageNode.getProperty(JcrConstants.JCR_MIMETYPE).getString();
            ImageReader reader = ImageUtils.getImageReader(mimeType);
            if (reader == null) {
                throw new GalleryException("Unsupported MIME type for reading: " + mimeType);
            }
            ImageWriter writer = ImageUtils.getImageWriter(mimeType);
            if (writer == null) {
                throw new GalleryException("Unsupported MIME type for writing: " + mimeType);
            }

            Binary binary = originalImageNode.getProperty(JcrConstants.JCR_DATA).getBinary();
            MemoryCacheImageInputStream imageInputStream = new MemoryCacheImageInputStream(binary.getStream());
            reader.setInput(imageInputStream);
            BufferedImage original = reader.read(0);
            Dimension thumbnailDimension = galleryProcessor.getDesiredResourceDimension(getModelObject());
            Dimension dimension = handleZeroValueInDimension(new Dimension(width, height), thumbnailDimension);
            Object hints;
            boolean highQuality;
            if (Math.min(width / reader.getWidth(0), height / reader.getHeight(0)) < 1.0) {
                hints = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                highQuality = true;
            } else {
                hints = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
                highQuality = false;
            }
            BufferedImage thumbnail = ImageUtils.scaleImage(original, left, top, width, height,
                    (int) dimension.getWidth(), (int) dimension.getHeight(), hints, highQuality);
            ByteArrayOutputStream bytes = ImageUtils.writeImage(writer, thumbnail, compressionQuality);

            //CMS7-8544 Keep the scaling of the image when cropping, to avoid a resulting image with bigger size than the original
            InputStream stored = new ByteArrayInputStream(bytes.toByteArray());
            final ScalingParameters parameters = galleryProcessor.getScalingParametersMap().get(getModelObject().getName());
            if (parameters != null) {
                try {
                    final ScaleImageOperation scaleOperation = new ScaleImageOperation(parameters.getWidth(), parameters.getHeight(),
                            parameters.getUpscaling(), parameters.getStrategy(), parameters.getCompressionQuality());
                    scaleOperation.execute(stored, mimeType);
                    stored = scaleOperation.getScaledData();
                } catch (GalleryException e) {
                    log.warn("Scaling failed, using original image instead", e);
                }
            } else {
                log.debug("No scaling parameters specified for {}, using original image", galleryProcessor.getScalingParametersMap().get(getModelObject().getName()));
            }


            final Node cropped = getModelObject();
            cropped.setProperty(JcrConstants.JCR_DATA, ResourceHelper.getValueFactory(cropped).createBinary(stored));//newBinaryFromBytes(cropped, bytes));
            cropped.setProperty(JcrConstants.JCR_LASTMODIFIED, Calendar.getInstance());
            cropped.setProperty(HippoGalleryNodeType.IMAGE_WIDTH, dimension.getWidth());
            cropped.setProperty(HippoGalleryNodeType.IMAGE_HEIGHT, dimension.getHeight());

        } catch (GalleryException | IOException | RepositoryException ex) {
            log.error("Unable to create thumbnail image", ex);
            error(ex);
        }
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;

    }

    /**
     * If height or width in the thumbnailDimension is equal to 0 it is a special case. 
     * The value 0 represents a value that according to the dimension of the original image.
     *
     * With this function a new dimension is created according to the original dimension
     *
     * @param originalDimension dimension of the original image
     * @param thumbnailDimension dimension of the thumbnail image
     * @return scaled dimension based on width or height value
     */
    private Dimension handleZeroValueInDimension(Dimension originalDimension, Dimension thumbnailDimension) {
        Dimension normalized = new Dimension(thumbnailDimension);
        if(thumbnailDimension.height == 0) {
            int height = (int) ((thumbnailDimension.getWidth() / originalDimension.getWidth()) * originalDimension.getHeight());
            normalized.setSize(thumbnailDimension.width, height);
        }
        if(thumbnailDimension.width == 0) {
            int width = (int) ((thumbnailDimension.getHeight() / originalDimension.getHeight()) * originalDimension.getWidth());
            normalized.setSize(width, thumbnailDimension.height);
        }
        return normalized;
    }

    private Binary newBinaryFromBytes(final Node node, final ByteArrayOutputStream baos) throws RepositoryException {
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return ResourceHelper.getValueFactory(node).createBinary(is);
    }
}
