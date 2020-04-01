package com.constellio.app.ui.framework.components.viewers.pdftron.signature;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSigBuilder;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.visible.PDVisibleSignDesigner;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class PDVisibleSigBuilderExtension extends PDVisibleSigBuilder {

	private String username;
	private String fontName;

	public PDVisibleSigBuilderExtension(String username) {
		super();
		this.username = username;
	}

	@Override
	public void createImageForm(PDResources imageFormResources, PDResources innerFormResource, PDStream imageFormStream,
								PDRectangle formrect, AffineTransform affineTransform, PDImageXObject img)
			throws IOException {
		super.createImageForm(imageFormResources, innerFormResource, imageFormStream, formrect, affineTransform, img);

		PDFont font = PDType1Font.TIMES_ROMAN;
		fontName = getStructure().getImageForm().getResources().add(font).getName();
	}

	@Override
	public void injectAppearanceStreams(PDStream holderFormStream, PDStream innerFormStream, PDStream imageFormStream,
										COSName imageFormName, COSName imageName, COSName innerFormName,
										PDVisibleSignDesigner properties)
			throws IOException {
		super.injectAppearanceStreams(holderFormStream, innerFormStream, imageFormStream, imageFormName, imageName, innerFormName, properties);

		// Use width and height of BBox as values for transformation matrix.
		int width = (int) this.getStructure().getFormatterRectangle().getWidth();
		int height = (int) this.getStructure().getFormatterRectangle().getHeight();

		/*String imgFormContent = "q " + width + " 0 0 " + height + " 0 0 cm /" + imageName.getName() + " Do Q\n";
		String holderFormContent = "q 1 0 0 1 0 0 cm /" + innerFormName.getName() + " Do Q\n";
		String innerFormContent = "q 1 0 0 1 0 0 cm /n0 Do Q q 1 0 0 1 0 0 cm /" + imageFormName.getName() + " Do Q\n";

		appendRawCommands(this.getStructure().getHolderFormStream().createOutputStream(), holderFormContent);
		appendRawCommands(this.getStructure().getInnerFormStream().createOutputStream(), innerFormContent);
		appendRawCommands(this.getStructure().getImageFormStream().createOutputStream(), imgFormContent);*/


		String signedBy = $("pdfTronViewer.signedByEntity");

		//String topText = "BT /" + fontName + " 10 Tf (" + signedBy + ") Tj ET\n";
		String imgFormComment = "q " + width + " 0 0 " + height + " 0 0 cm /" + imageName.getName() + " Do Q\n";
		String botText = "BT /" + fontName + " 10 Tf (" + signedBy + ") Tj ET\n";
		appendRawCommands(getStructure().getImageFormStream().createOutputStream(), imgFormComment + botText);
	}
}
