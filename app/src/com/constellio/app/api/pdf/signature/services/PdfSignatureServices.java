package com.constellio.app.api.pdf.signature.services;

import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotCreateTempFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadKeystoreFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignatureFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSignedFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotReadSourceFileException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSaveNewVersionException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_CannotSignDocumentException;
import com.constellio.app.api.pdf.signature.exceptions.PdfSignatureException.PdfSignatureException_NothingToSignException;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.pdf.signature.CreateVisibleSignature;
import com.constellio.model.services.pdf.signature.PdfSignatureAnnotation;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class PdfSignatureServices {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private ContentManager contentManager;

	public PdfSignatureServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.contentManager = modelLayerFactory.getContentManager();
	}

	public void signAndCertify(Record record, Metadata metadata, User user, List<PdfSignatureAnnotation> signatures)
			throws PdfSignatureException {
		signAndCertify(record, metadata, user, signatures, null);
	}

	public void signAndCertify(Record record, Metadata metadata, User user, List<PdfSignatureAnnotation> signatures,
							   String base64PdfContent)
			throws PdfSignatureException {
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		String tempFilename = "docToSign.pdf";
		String docToSignFilePath;
		if (base64PdfContent != null) {
			docToSignFilePath = createTempFileFromBase64(tempFilename, base64PdfContent);
		} else {
			Content content = record.get(metadata);
			if (content != null) {
				ContentVersion contentVersion = content.getCurrentVersion();
				String extension = FilenameUtils.getExtension(contentVersion.getFilename()).toLowerCase();
				String hash = contentVersion.getHash();

				InputStream pdfInputStream;
				if ("pdf".equals(extension)) {
					pdfInputStream = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
				} else {
					pdfInputStream = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".signAndCertify");
				}
				try {
					docToSignFilePath = createTempFileFromInputStream(tempFilename, pdfInputStream);
				} catch (ContentManagerRuntimeException_NoSuchContent e) {
					throw new PdfSignatureException_CannotReadSourceFileException();
				} finally {
					ioServices.closeQuietly(pdfInputStream);
				}
			} else {
				throw new PdfSignatureException_CannotReadSourceFileException();
			}
		}

		if (StringUtils.isBlank(docToSignFilePath)) {
			throw new PdfSignatureException_CannotReadSourceFileException();
		}

		String keystorePath = createTempKeystoreFile("keystore");
		String keystorePass = modelLayerFactory.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE_PASSWORD);

		if (signatures.size() < 1) {
			throw new PdfSignatureException_NothingToSignException();
		}
		Collections.sort(signatures);

		File signedDocument = null;
		for (PdfSignatureAnnotation signature : signatures) {
			String signaturePath = createTempFileFromBase64("signature", signature.getImageData());
			if (StringUtils.isBlank(signaturePath)) {
				throw new PdfSignatureException_CannotReadSignatureFileException();
			}

			try {
				signedDocument = CreateVisibleSignature.signDocument(keystorePath, keystorePass, docToSignFilePath, signaturePath, signature);
			} catch (Exception e) {
				throw new PdfSignatureException_CannotSignDocumentException(e);
			}
		}

		uploadNewVersion(signedDocument, record, metadata, user);
	}

	private void uploadNewVersion(File signedPdf, Record record, Metadata metadata, User user)
			throws PdfSignatureException {
		Content content = record.get(metadata);
		ContentVersion currentVersion = content.getCurrentVersion();

		String oldFilename = currentVersion.getFilename();
		String substring = oldFilename.substring(0, oldFilename.lastIndexOf('.'));
		String newFilename = substring + ".pdf";

		ContentVersionDataSummary version;
		try {
			InputStream signedStream = new FileInputStream(signedPdf);
			version = contentManager.upload(signedStream, new ContentManager.UploadOptions(newFilename)).getContentVersionDataSummary();
			contentManager.createMajor(user, newFilename, version);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadSignedFileException(e);
		}

		content.updateContentWithName(user, version, true, newFilename);

		try {
			RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
			recordServices.update(record, user);
		} catch (RecordServicesException e) {
			throw new PdfSignatureException_CannotSaveNewVersionException(e);
		}

		//		ConstellioUI.getCurrent().updateContent();
	}

	@SuppressWarnings("rawtypes")
	private String createTempKeystoreFile(String filename) throws PdfSignatureException {
		FileService fileService = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename);

		StreamFactory keystore = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.SIGNING_KEYSTORE);
		if (keystore == null) {
			throw new PdfSignatureException_CannotReadKeystoreFileException();
		}

		byte[] data;
		try {
			InputStream inputStream = (InputStream) keystore.create("keystore-stream");
			data = new byte[inputStream.available()];
			inputStream.read(data);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotReadKeystoreFileException(e);
		}

		try {
			OutputStream outputStream = new FileOutputStream(tempFile);
			outputStream.write(data);
			outputStream.close();
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

	/**
	 * FIXME PdfTron specific code
	 *
	 * @param filename
	 * @param fileAsBase64Str
	 * @return
	 * @throws PdfSignatureException
	 */
	private String createTempFileFromBase64(String filename, String fileAsBase64Str) throws PdfSignatureException {
		if (StringUtils.isBlank(fileAsBase64Str)) {
			return null;
		}

		String[] parts = fileAsBase64Str.split(",");
		if (parts.length != 2) {
			return null;
		}

		// FIXME!
		String encodedText;
		try {
			encodedText = new String(parts[1].getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		encodedText = "iVBORw0KGgoAAAANSUhEUgAAAk4AAAClCAYAAACqRXKdAAAdxklEQVR4nO3dy4ok153H8d8j5Bt0PEFTDzCCWAstemeBNsFgtNCqzGhjGESgTQu06EEIy4sRNQvLNoihbSEwvZBS0AvbDKahjZBBQoVozwjbiNbFcusyUs0iKiZP/ONEZlzPOZH1/UDSUmVmxD9vJ/7xP5eQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABXx0ZSLumWpCxqJAAQz0ZSKem2qvYQALxuSLq4vJ1HjgUAYrmpXVt4IenVuOEASNVWu4biYeRYACCGjaSv1EycPokaEYAknajZUNyOGw4ARHGmZltI4gTA61TNhuI0bjgAEJw9gaxvn8cMCrgqClXjhNaSgNxWs6G4ETccAAiulD9xuhcxJuDKONd6BllvJH2h5vimTdSIACC8ut22t23MoICrwv0Bpu4FNRuJm3HDAYDgnpA/aWLMJxDIQ62n4mQTp6fjhgMAwb2h7sTpTsS4gCtho13itIZp/XZ8UxY1GgAIayPpU3UnTm/FCw24Ota0HpLbrcggSABXjbv4r+/G6uFAAGtKnOjLB3CV2aq7vZ3FCw24GjZaz6VL7LolMc+sCq1rCQcA6+cOrahvduVwKk7AwtzEKfWKk02cioixrGkJBwDHoVC7wvSuSJyAoNacOMVc+LJU9X5RFgcQiq+b7i3z/3ejRQdcEZmaP7qU2bVLno0bDgAEZRe9vCfp12onUgAWZKs4KXtKzVifihsOAATjuzZdqaprjkkzQECZ1rMuUqF0uuoAICS7DMG5qqEWZyJxAoKyZzEnccPZ61QkTgDWYePcTlRVyLeS/qAq2SlVtWFZz+3Z9q+8/DuJExCYPYvJo0azX6H1xArg6vl3Sd9K+kTtZQP23d6U9Jz2X7DcHRjuXtzcJk5MWAEWViidKf6HFKLiBCBd36p/stR1O1eVJJ2qeXL4sfzJkb1uHbPqgIUVav7oUl7Q0c6qY3A4gJRsJX2vXVVoSNVp3+2upP91/v8xZ5+/MY9lVh2wsK5+8xSROAFYoxuSnpH0kaRHmp5IPVBVlbqlakkC977X9sSxrysQQE9rKvPagex51GgAYLxMVRuWS3pa0os6fB26IbdvVHX7nauqfNX/3lPas6eB5N1R88d2L244e7mrnJM4AThWJ6qqVKWqCtIcY6fWMiQDSN5r6l/mjS1TM9YsZjAAEECu5oltpiqpOlXVVfdAw5Kmh0p72RkgebY0fBY3nL1sxSmLGg0ALM8dh+prn+3K4bfUXkPqRLtuQcY5AROdqf2jS5VNnGgAABw79+TW1z6/oma7mPI4VeAorK3i5E7vBYBj567f5Fu77jk12/DfhAsNuJq2Wk/iJFFxAnC1fK1dm3fdc3+hZrvIJVeAha0tcXLPvgDgmGU6fLJYiMQJCGptiVO92Nt57EAAYGF27Tpf4mSvN5p6Gw6s3toSp3pMVsrrTQHAHHINrzilPMEHOAp2Vl3qZd463m3sQABgYYWa7bOPvWwWiROwMLsGSOoVJxInAFdFocNr19nHlEEiA64wmzilXnGqrwT+u9iBAMDC+lzY/EXzmOeDRQdcUYXWmTj9MXYgALAwO/Dbt47TM+YxPwoWHXBF2R9m6l1gdVdd6gkeAEyV63DiVJjHsL4dsDB7GZPUZ6uROAG4KnIdTpzcweGpt9/A0XAvY7KWilPqg9gBYKpczcSp8DzmTef+MlBcwJVXLyq5hjOWOnFiyi2AY5frcMXpW+f+k2CRAVecO7Mu9cSpXgCzjBwHACzNrhxuEyf3/r+FDQ242kqtL3E6jR1IwgpJd8V7BKydTZxyc787uacIGRhw1bk/vtQTp/oSMUXkOFL2par36C+xAwEwSaZm4mS74uoTyXMxmw4IKtN6Boefq7uvH80z1G8ixwJgOjdxuub8PXf+TnUZiGAtFad9lx5As3r4ReRYAEyTyb9G00bSx5d/eyCqTUAUnyv9xMldcyqLG0qy6tL9haplJpAWDnAYwq6zl13+vXT+9kKMwABIH2jXV56quhuKhKCbu7QECWY8G0kvS/o3VbNW76m5XtpdSXck/VpV9/i9y39vX95ek3Rf0q9UVRHzoNGnaaPqfTjV1emqz9T+PWfafZdYzw6IqF6SIOWkJFf6VbHY3IPzsYwF26h6HTdVJfbvqjpg3Lr897Z2ycdHqta1ibWyfH4Z1z/U/Bzmut1VdYHr26qqDlehgnVd1Wutxze6t/9RdZHbY5WpnTjVE2QeihMjIKpS60mcUh/AHkum6v35TscxRXmjKgmxyWCf27eRYl0iWTp0O9aBwRtJP1Xz+9x1O9aTqUzN1/mi899FtKgASGoOKs7ihtKpUPjy9B1Jf9c6ro1Xd2U+0vob18ckfahxicT3kt4PHG+spKm+HdtEgFztbudDt4c6vpWzMzVfYz0WlZNHIAG5dj/OPGok3V5WFd/rgfZ3Tc1GKwu037FeVRXn19rFvLauuo2qZO8b9TtQ3r58/ImqzydG19VTHbG9J+lnqpKqU1W/q1xVvKeqxjDdV3Wtsbq7casqYfiThnf3Pb3oqwzjUOXuoXZdVV23tX3n98nkfw/yeCEBqLmzN1It/d9RFd/zgfb3hJoNVhFov2PV789aDyK5Dh8Uv1J1aYlnlcb4no3aXYn3NG/lI1P1OZaqPuPbkp5RNd7J3W+o34VPoV0SO8Wp/J/7Xy/vqz/zE1WJ6bEnT5nar43rdAIJSX2mxrnCVsQKNRusMtB+x6rfn1QOpn0dGsv0X6re+xOlkSy5bHXkQ4WL0U5Vj3VA/bGJ48cjt+MOF3CrK27CZJWe59TPy0bGkRJ7yZXPld5vALjS6jEFKY7nyRS+y8weFFN8X2qZ/AeQsQexUHL5E766cvPDaJEdZg/0X0v6p8AxuO9drAHSf1DzffjziG1cU3NsXv16sh7PzeX//qR6AjiETZxejRsOAOtM1Y/zPHYgHrnCx2YTp5RnHNYH8Xo14dSTvY2qaoGvynSoypCCjdqDl2N0cdvvaAz2Ozfmd3LfbKNv0lQ7kT8BH7KNFP2zmq/nybjhALBK7Rq+1A5a9diHbcB9fqR2Q5yq+v2x415SnKZ9XdLb8lcJtlrHwa5UO+4Yvxk7JihGDO5khDEnN75xTWPGiGVqJ+JrrjptVK1Z5r6eImZAANpSXpKgPrMO2RDaM+kU35dafakVO1YopSpZPWPOdsnUcRaxAhsoU/s9jjUNvlTcz/pxtT/LIWOtTtROdqYsaFl44gndfToXX0KZ6sQd4MpKeWadmxiEYAfeTjkTDqHupijU7LJIJXE6Ufe6PLGqNWOVasZfRozFvTZhjOrim2p/nkNmtG3Nc29p+nfhfc821yaXvxu7iBcSgC71QTe1EncdV6hpxrn8B/k5939T0meqDn73Lv/NRmzHHUCaqX0wiu1U/oPA90pnWYG+NmompueKG/+nipsgfKX255r1fK4dXH9P87yX18121zbDbqNqyY0LSV+KihOQvDOlVamQmjPG8kD77FpPpphp+xt1X0oiH7itQs3PzA4YzqYGO9K+xQy3qlYHXxt7sC8jxmLXGQu9dtELan+uj3o+d6PmOkxzj6us27E1Vp3ctuc/1XwdRbywAHQptPuRplIJyBU+CbANb32ba10k35o17u3xEbFuL///h2ZbT8wT8iDX1R6oXh8giwjxzMV2TWURY7FT1UN+zhs1q1317c2ez3/dPK+cOb5M7e/dGrgLqm4l/YuWaX8AzCjFcU71GVjIxs8dO+Le5lhHxU5lf0n+RC3rub2666g+q7bVsr7bmcuJ/APAtxFimdu32r2e2Es95JpWqRxrI/8q9Z+q3xhA+/38RsucpNnfVLHAPubmVmgLtRcXfSFaZAD2qg/EZ7EDuVQ3JiEHv9bvgb3NUfJ3G8dz7Q4an4zYl5vo1l01Mdf2uSH/eKY5Bv3Glqv5ml6KGk17BlkeYJ92jNfQqpF9/t+1XKXMTvCInege4lYQ6+qxPYE7lkvJAEenPvCmUt6uG49QiZzbgNklCabGkJvtuVU9233X5/13t5dd/q002wmVsOTyJ03H0r1gKyWxx2iFHsu2kb/7tf6u9vmeleZ5S489chOPVNozn42akzryy7/bJDWLEBuAHlJbz6luPEJ1HebavX57IdGpZ61uQ35b7YONbSiLA9vzdWPaBCyfGHMfvpW0H0p6KsC+Q7FdP1nUaJoH2hDV2K7u676VkFzNxHquWXT7pLBAaB+l/G2MbQ8AJGqj6mKSKVQLMoUvUxfOPt9Rs+GaUnFyB35eyD8exFYRDu2vPpi5ja0dNBzifbMzrGIuCrkU9yAWe0X20Bf47ZpleiHpuZ7bsOsrhfheXjP7zALsc6hMu3bBVu5sogkgYXX14G7kOGJUv0pnnz9Xs+GdUnFyX8u24zE/Nfu7c2Cb9cHcPXDaaerPjg+5l+tqL61QLLzP0GwyGnt6+5MK9377Vveub31n0dmZnlNOQIb670j77cutHJbO3zPFe88AjFAojfJwfaYb8mzL7ZK4qfkaL/esvevA+7zZ377XnTmPs2fv7tispQfF2mrEewvvLwb7GmMP0n1GzXiuLbSfjdqVojHvg1tJ/V7LxXto36mNc3K/V1s1q032BOhm8OgADOJ2BRQR4wg9MFxqngE+rWbjNaXS4CZkXQccOz7p0z3by53HZea+kAOH7diXHy24r1jsIOPYY2XceD5acD8vq/nZurdz9XsfbBd13yrVXDKF+y0M4b4vD9Uei2jbgmMaLwgcrbq7LmaJuO6KCnmGX1/m4DPNu1L0fR1uvH3XyOsaK1SfrZ577svMNopxIR9kD4opHZjm5L7GFKa117+LJX+fmfyLXA79LRTmeTGqde7+U1mf7kz738tc8d83AAPFWHjSZa/BFkLm7PMTtcvl/zpyuxtVXRT1dvadqbsVr30N/TuX97/dcb97cF3qYO+73tixydR8jTFPJKRwB1T7PRxbbXJnW8aq1rkxpJD45jr8XtrfFokTsAJu9SPGDKlC4RM3O4B7rsardLZxqGvFdrN1NfT1OKau1cxDdNfZwexzrKyemseUTsXCrvfTN4EZ6tAlgcqe2ynN82IlnSmNc7KfYVebUqj53uUBYgMwg7pqUUbY9zuX+w45s6/UrqG6pfbZfTFim7Y7Kz/w+D4LYbrVuK6G13b7FcPC7sUuiPjGAvuIzU4QGFt1nENpYikX2s++atOn6pes+Wbj5QvE2sdPTBxZpDik5mVU9lW/7ISEY1veAzhaMVcRr6cRvx5wn+6g27kSp9J5/rbH4zOzT997724z27Mtt4uiz76H8I1vOrYBrPYSIf9QvIOuTUSWWkDyB2p+pvb2q57bsclXzC4yuxxCrO/piXbXO/xM+5MhmzjlSwcHYB4xVxFf+qzaxz1IFmpXf4oR23STlz7P9w0QtwfIOsE7NKaoMNvJ+oXci23Yj3F805yTA6Y4kfSBE4dvFtYcNpL+rPb3r749UrVu1yH2uxF7QdTCxBOju9WO9zrUrU3FCVgpt6oQsrHp0xU1t0zNhirX9MTJff6QgbFuAmcbTTexOhu4rT6P7+ttE2M547ZTYVdEj3HwKtWe3VYutK99l1Xp+1vM1K5Exl4wNFf876o7zmqrw21BqWbM+YKxAZjZmcKX2t2zrVCJU65mQ5V5/jY0FvdAVA543ptmv08797nJWNFjWzZ5ywbE0eVxE9+xnhF/oXgVtY3a18e70HIDwvddVmVI8lMqTLxD2JXf5zyB6MP+Bvv8Vuwip8yqA1ak0PCKyVRuwpEF2qc9cEjTZtXZMSlDEgt7qZcXnfvcM9e+25x7OrYdMP3BDNtMTa7ma/xlgH1eu9xvqXbV8ULSb7TMqtuZui+rMqTi4dtOn+ctLeYlc+xYwLLn8+xSKE/vfziAlMRYRdw9aITint3X1QXb4OYDtufO5BmarDxl9ls699XvzZBENjPbywbGY9mDesyZZksp1HyNSw4ozlV9X+qBw1MqPmMc6qJ70HM7NqFOYc0kqb2kRBlw3+57u1X/3+wc4ysBRBRyFXE3UTsPsL+a28DVDX6mZuPVt+JkF7zMB8aSm/3W77sbz9Cuo1c0zwHNJpMhq4IhFVq+cpKrWQ303R5q2fGFvjWbHpj//8WBbWxUJX7ud/5C6XTfzrWQ7VBuFXvoAPlczZiLmWMDsLBCux//0tyGPOS4EvcA5ktUhhw83dfwyYhYbHJSJzqF87ehFQg7tT4bEZeNIXRyG9KSs5qeUHuBUns7V1UZyWbcr+VbUuJCVZdg3wrNjY5tpFJtktrf2SLAPjM135ehyS8VJ2DlQnbXlc6+QlS4ar7KUmb+nvXclq96NYTd7/by72eeGIcoNP29/aWJ7b2R20ndEiuvbyQ9Z7Zrq0u/UtW1FGI8oa+L7rbn713fNd8il6lVm6TwiZNdemC7/+FehcLGDGAB9UF76WRmq35nunO6rmYj9aRz39CDZ6bmwWRMgmPXcqorb1MrRna7Y7ZhVws/xvWbpPbssKmJzEb7xxJtFbbL80VPDPW4uXPzd993+MTzuLpS9tjCsQ8VunrjnuCca9x3x8Yc81I/AEYqtGtcl2IP7LGWInjGue9L5+99lGZb2Yh47Ptwrmb33ZRkxW3U3x/xfHuwjL1Gz1IKzVdx6koy6s+ymLDtsf7miaX+vdlY7TpipfyVpmL5sEeZMjt2KPeSKlP2VWgd7y2APdwz0WKhfeSa72A1hB3P4p7dPbr826Me27Fn61PGebgHpnM1G9Ip27UzjLIBz/UNDD/W9WXs+zS2itLVnfWVpJcUZ42j3BOPe53BrbmvTpwy7X4P9pZyAh2q4nQi6TtnP1OqRIxxAo5EPe5jqYGfbgITsgvIrcJcyD/9v0/FySZgU5IKd7/nanbzFBO2K0l/0bjG3TcDK5sYS6pydVdd+upKmraK975t1E6MPlNzfag75v7nL//+qtqvJfWkSQpTcbLjmqa2XyROwJGou5DmWoHamjqoeiw7Jbx07qsPfH26KN3tTF0w1Fac3EQqm7BdqX35h758q0sfq0LTEqeupKlU3JW0fZ+hTZ7tAqyvqIr5vvn7A62j4miXI3hy/8NHcU++5mgf7crhjHECVqw+gC/xQ3aTg3KB7fsc6n7qmzjZM8RyYlzue/En57/nqMSVasaa9Xyer+J0rOxrHZI4XVO7S+uh4icZvuUHfImzPWiXan9n1lQFsb/xuT8Hm4yWM2wz1tpTABZQaFcFmVOm5cvpPr5kIHfu75s4zT193U2c3Jlsc3SL2APjsz2fd5W66gp1fycOecc8N4WkSWp/R7sWZfQlAnZG4FJV5yXkGv9ZHmIri/c0T0WxUDNmKk7Aim20u1r79Rm3aw/KobozfF0XmXO/2yh22ag5+26ObkY3cfrQ+e85DsB2KvorPZ9nZ/uFTHBDs8nl8/sf/v9836digfiGelzVgPQ+B2Nf4vSR+ZuvUpWqXMt8Z+24poeaLykr1O+zArAS9dnnnINCS83bHdXX62o2ULaS1idx+g/N38i5iVN9DbOvNU9CaZPUIYmeG9cxN+jPq/k6X+jxnELtrrCfLhTfEBtJf1b/xKc0j/2JpC/M3z5aLtzZ2a66fKbt2ircnO3hvpm+AFaobojmXNPJbYTOZtzuIR+o2UC9a+4/VAU70a4Cd6Hqel3XZojLdw2zMZdv8bEHkiGJql0A862ZYkpNqWFVCt9g8DPFHQgudS+8me95ju3Se8Pz/DV97rnmrzjZxGareT/rJcZNAYisrjxkM23P7QoI2f1jD3a2+uLel5n7fFO778wUl2+BwrlmGtoutyEJsL2O2SPFTw6WUKr/Gb9vte1bSuN9KdX+Hv3swHPsZ/yp2tt4YplwFzH31H6bJA+9gG8fZ2rGHPJkEsBC6rPSOX7Q19RsJOZuhLrYystXaleLuhKnrjP5uZK+umvEveL8nF0BNmHMej7PzvY51ka9UPM1Puh43Imk3yrN98M33qrP4GW7jpO9bReKdylzJk4btauuS5zoFWYf5QL7ABBYpvkSnULNRiKbuL2+DpXDM3O/+zp9V7efa2yWb4mEOc6UXbZSNuQzXPMMq758A+GfNveXaiegU9fvmosvaeo7ePlNz3OXThSWNOeaSHbs21JJ8uZy2w81fzcggIjOVTUe5xO3Yw/E2cTtjdmv7+BvE5h6FqFtPOc+oPim/c99wPqJ2XbfWWOSP6lIpcoyp3fUfp0vS/pY7RlqF6oqEddiBGr8QP7vT9+EwTe+LrXEcIin1HwNPxq5ncJs5zut770AEFmhXSPy0sht+BblC6VO/C7kHz9kExipSq6+UfuAstQMw6USSrs69NDE58wTX6gu1lDs9er2JROnSuMgeiLpH5r2/fSteF7fyhljDcV+V7MR28jVfl/6zLQEgJavtWtI8hHPt10KU6tXfdmqia+aUzr31+Vy39n4meY9aPrGTs29RMOUJQkkf9XpXGkkD1OdqPrs78lfWXI/k1QSJsl/cB+aNPk+17V/vu4J0pjf0an8XbLZTPEBuGLcxGdoGd83G2lo5WMsN3HoStbcBOY1+ROa32r+g4lvP3Nfu2/KzLpaqXacX2qdlad6zJJdr+jQ7R2l8Xp9B/cLjVs2wLedVFY/H2Ps76j+ThxL5Q1AQsZenLdUu0HKZ46tz77Ljse41SV3dpv7Wpc4A7fJ5FINtd1HNmIbviRv7LZiyFVVZPZ1T12omlW37/57l9s5VTUO6nlVJwG3L/+9pWqx1Z9r3u/4RtXyAr6YxlZFfMljOT3UKMaeIFyT9At1f9ZrrLwBSIgdp9TnDNw3tinkiuFuctIV7wvaf6BcqvH0JU5LnO27l3K5UHVQH6qe/WPjPZspxqXkai9+6vuMS+2+H6fyfzZjbg9VvUdTZnidSHpvz/bHfmfsavp3J8SYgiGTTzaqPpNP5H9f7x14PgD0VmjYWd1NtRulcqHYLLebbrvncV2z57ZaLmnyjTFZajzFu2Y/v5uwrR+rHXcKXVhWrvZSDO77vJX0qqqB4V1OJd3v2MbY2x31r0Tl2l8lm9qtVifDX6hKmtZeXbEzSN9R9d18QtVin3dUfe5b7a88kjQBmJ3btXWoy86ujB1ymrNbISn2PM4mMbe0fDKQy99gL8EeUKZef8x9Xy+U1mDiXN3T7P+uKmkfGuvjqk4Azju2O/b2UNXv49eXcZ2q+qzOdXgM1rvi4G69rOmfyTEkkAASlKnZ2HR1/fjWKboZID6pirE+q+yTrNUzrEJVT3wLF54ttK/Ss68pfNWy2GfpuZqX87EJysuaZ+2lXNX7eSbpj5J+ryrZKVUl3FtJ7+vwWKopt4/Ewd1n30zBQ7fvVFWkeF8BLKZUs+GxDY5vSv9fPY8LEV8ZaJ9D+C538eRC+/IlsHOvAl8nKCG77TaXcXRVmGIvI5Cre7bWmFu9jhS6ncg/Fm/fbas0u5sBHKE/atf4ZOY+X0WlCBjbuXYHGxtbCj5W+/3JFtqX70x8jgOw7wD1QMsnKrn2j/35UGmtu3RNVWVqStLEwX2YG2qOcfta1YWq76tKqLcKW2EGAEnNA6d7IM7VPqjNvT7RIaV2M5pS5DbqdXVkSUt9Hr4E+WPNnyRnl/vaN/Znzi65pdxQ9d28q/2v5XtVM+lKcXCfKpUEGgAa3TXnqmav+CoBfS86epWETixtd9acCeWJpM/kT2RuqfqeFJKeVTWDsVD1fcgubxvndqLqmmO5qqRhq8ODsrdKq8I0VKYqobqh6vWv9XUAAA7oOxizjBRfqjaSPlfYbszc2dc9zV/F2Kh7TZwlbo9UJX9UYwAAq3JL+w9wt8QZtJWp/T5lgfa9dEXjhuaftm+rWK8q7e44AAD28g183YpqQJcTxR3/FcINDZ/dtC9Z2qqqypGEAwBW75qaB7o1jzcJIVP7/TpWG+0GRL8l6Q1VA6O3aidH59qt6P1zVat6Z6EDBgAghNALSK5djG66VJFkAwCAvf6kKml6O3YgAAAAa5DFDgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACYyf8BxcyucYnOhooAAAAASUVORK5CYII=";
		byte[] data = Base64.getDecoder().decode(encodedText);
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		File tempFile = fileService.newTemporaryFile(filename);

		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromBase64")) {
			outputStream.write(data);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

	private String createTempFileFromInputStream(String filename, InputStream inputStream)
			throws PdfSignatureException {
		FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		File tempFile = fileService.newTemporaryFile(filename);

		try (OutputStream outputStream = ioServices.newFileOutputStream(tempFile, getClass().getSimpleName() + ".createTempFileFromInputStream")) {
			ioServices.copy(inputStream, outputStream);
		} catch (IOException e) {
			throw new PdfSignatureException_CannotCreateTempFileException(e);
		}

		return tempFile.getPath();
	}

}
