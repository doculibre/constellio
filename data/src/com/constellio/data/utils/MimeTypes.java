package com.constellio.data.utils;

import java.util.HashMap;

/**
 * Classe listant les différents MimeTypes.
 *
 * @author Hery Raveloson (NURUN)
 * @version 1.0
 */
public final class MimeTypes {

	public static final String MIME_APPLICATION_ANDREW_INSET = "application/andrew-inset";
	public static final String MIME_APPLICATION_JSON = "application/json";
	public static final String MIME_APPLICATION_ZIP = "application/zip";
	public static final String MIME_APPLICATION_X_GZIP = "application/x-gzip";
	public static final String MIME_APPLICATION_TGZ = "application/tgz";
	public static final String MIME_APPLICATION_MSWORD = "application/msword";
	public static final String MIME_APPLICATION_POSTSCRIPT = "application/postscript";
	public static final String MIME_APPLICATION_PDF = "application/pdf";
	public static final String MIME_APPLICATION_JNLP = "application/jnlp";
	public static final String MIME_APPLICATION_MAC_BINHEX40 = "application/mac-binhex40";
	public static final String MIME_APPLICATION_MAC_COMPACTPRO = "application/mac-compactpro";
	public static final String MIME_APPLICATION_MATHML_XML = "application/mathml+xml";
	public static final String MIME_APPLICATION_MSONENOTE = "application/msonenote";
	public static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";
	public static final String MIME_APPLICATION_ODA = "application/oda";
	public static final String MIME_APPLICATION_RDF_XML = "application/rdf+xml";
	public static final String MIME_APPLICATION_JAVA_ARCHIVE = "application/java-archive";
	public static final String MIME_APPLICATION_RDF_SMIL = "application/smil";
	public static final String MIME_APPLICATION_SRGS = "application/srgs";
	public static final String MIME_APPLICATION_SRGS_XML = "application/srgs+xml";
	public static final String MIME_APPLICATION_VND_MIF = "application/vnd.mif";
	public static final String MIME_APPLICATION_VND_MSEXCEL = "application/vnd.ms-excel";
	public static final String MIME_APPLICATION_VND_MS = "application/vnd.ms";
	public static final String MIME_APPLICATION_VND_MSPOWERPOINT = "application/vnd.ms-powerpoint";
	public static final String MIME_APPLICATION_VND_MSWORD = "application/vnd.ms-word";
	public static final String MIME_APPLICATION_VND_OPENXMLFORMATS = "application/vnd.openxmlformats";
	public static final String MIME_APPLICATION_VND_RNREALMEDIA = "application/vnd.rn-realmedia";
	public static final String MIME_APPLICATION_X_BCPIO = "application/x-bcpio";
	public static final String MIME_APPLICATION_X_CDLINK = "application/x-cdlink";
	public static final String MIME_APPLICATION_X_CHESS_PGN = "application/x-chess-pgn";
	public static final String MIME_APPLICATION_X_CPIO = "application/x-cpio";
	public static final String MIME_APPLICATION_X_CSH = "application/x-csh";
	public static final String MIME_APPLICATION_X_DIRECTOR = "application/x-director";
	public static final String MIME_APPLICATION_X_DVI = "application/x-dvi";
	public static final String MIME_APPLICATION_X_FUTURESPLASH = "application/x-futuresplash";
	public static final String MIME_APPLICATION_X_GTAR = "application/x-gtar";
	public static final String MIME_APPLICATION_X_HDF = "application/x-hdf";
	public static final String MIME_APPLICATION_X_JAVASCRIPT = "application/x-javascript";
	public static final String MIME_APPLICATION_X_KOAN = "application/x-koan";
	public static final String MIME_APPLICATION_X_LATEX = "application/x-latex";
	public static final String MIME_APPLICATION_X_NETCDF = "application/x-netcdf";
	public static final String MIME_APPLICATION_X_OGG = "application/x-ogg";
	public static final String MIME_APPLICATION_X_SH = "application/x-sh";
	public static final String MIME_APPLICATION_X_SHAR = "application/x-shar";
	public static final String MIME_APPLICATION_X_SHOCKWAVE_FLASH = "application/x-shockwave-flash";
	public static final String MIME_APPLICATION_X_STUFFIT = "application/x-stuffit";
	public static final String MIME_APPLICATION_X_SV4CPIO = "application/x-sv4cpio";
	public static final String MIME_APPLICATION_X_SV4CRC = "application/x-sv4crc";
	public static final String MIME_APPLICATION_X_TAR = "application/x-tar";
	public static final String MIME_APPLICATION_X_RAR_COMPRESSED = "application/x-rar-compressed";
	public static final String MIME_APPLICATION_X_TCL = "application/x-tcl";
	public static final String MIME_APPLICATION_X_TEX = "application/x-tex";
	public static final String MIME_APPLICATION_X_TEXINFO = "application/x-texinfo";
	public static final String MIME_APPLICATION_X_TROFF = "application/x-troff";
	public static final String MIME_APPLICATION_X_TROFF_MAN = "application/x-troff-man";
	public static final String MIME_APPLICATION_X_TROFF_ME = "application/x-troff-me";
	public static final String MIME_APPLICATION_X_TROFF_MS = "application/x-troff-ms";
	public static final String MIME_APPLICATION_X_USTAR = "application/x-ustar";
	public static final String MIME_APPLICATION_X_WAIS_SOURCE = "application/x-wais-source";
	public static final String MIME_APPLICATION_VND_MOZZILLA_XUL_XML = "application/vnd.mozilla.xul+xml";
	public static final String MIME_APPLICATION_XHTML_XML = "application/xhtml+xml";
	public static final String MIME_APPLICATION_XSLT_XML = "application/xslt+xml";
	public static final String MIME_APPLICATION_XML = "application/xml";
	public static final String MIME_APPLICATION_XML_DTD = "application/xml-dtd";
	public static final String MIME_IMAGE_BMP = "image/bmp";
	public static final String MIME_IMAGE_CGM = "image/cgm";
	public static final String MIME_IMAGE_GIF = "image/gif";
	public static final String MIME_IMAGE_IEF = "image/ief";
	public static final String MIME_IMAGE_JPEG = "image/jpeg";
	public static final String MIME_IMAGE_TIFF = "image/tiff";
	public static final String MIME_IMAGE_PNG = "image/png";
	public static final String MIME_IMAGE_SVG_XML = "image/svg+xml";
	public static final String MIME_IMAGE_VND_DJVU = "image/vnd.djvu";
	public static final String MIME_IMAGE_WAP_WBMP = "image/vnd.wap.wbmp";
	public static final String MIME_IMAGE_X_CMU_RASTER = "image/x-cmu-raster";
	public static final String MIME_IMAGE_X_ICON = "image/x-icon";
	public static final String MIME_IMAGE_X_PORTABLE_ANYMAP = "image/x-portable-anymap";
	public static final String MIME_IMAGE_X_PORTABLE_BITMAP = "image/x-portable-bitmap";
	public static final String MIME_IMAGE_X_PORTABLE_GRAYMAP = "image/x-portable-graymap";
	public static final String MIME_IMAGE_X_PORTABLE_PIXMAP = "image/x-portable-pixmap";
	public static final String MIME_IMAGE_X_RGB = "image/x-rgb";
	public static final String MIME_AUDIO_BASIC = "audio/basic";
	public static final String MIME_AUDIO_MIDI = "audio/midi";
	public static final String MIME_AUDIO_MPEG = "audio/mpeg";
	public static final String MIME_AUDIO_X_AIFF = "audio/x-aiff";
	public static final String MIME_AUDIO_X_MPEGURL = "audio/x-mpegurl";
	public static final String MIME_AUDIO_X_PN_REALAUDIO = "audio/x-pn-realaudio";
	public static final String MIME_AUDIO_X_WAV = "audio/x-wav";
	public static final String MIME_CHEMICAL_X_PDB = "chemical/x-pdb";
	public static final String MIME_CHEMICAL_X_XYZ = "chemical/x-xyz";
	public static final String MIME_MODEL_IGES = "model/iges";
	public static final String MIME_MODEL_MESH = "model/mesh";
	public static final String MIME_MODEL_VRLM = "model/vrml";
	public static final String MIME_TEXT_PLAIN = "text/plain";
	public static final String MIME_TEXT_RICHTEXT = "text/richtext";
	public static final String MIME_TEXT_RTF = "text/rtf";
	public static final String MIME_TEXT_HTML = "text/html";
	public static final String MIME_TEXT_CALENDAR = "text/calendar";
	public static final String MIME_TEXT_CSS = "text/css";
	public static final String MIME_TEXT_SGML = "text/sgml";
	public static final String MIME_TEXT_TAB_SEPARATED_VALUES = "text/tab-separated-values";
	public static final String MIME_TEXT_VND_WAP_XML = "text/vnd.wap.wml";
	public static final String MIME_TEXT_VND_WAP_WMLSCRIPT = "text/vnd.wap.wmlscript";
	public static final String MIME_TEXT_X_SETEXT = "text/x-setext";
	public static final String MIME_TEXT_X_COMPONENT = "text/x-component";
	public static final String MIME_VIDEO_QUICKTIME = "video/quicktime";
	public static final String MIME_VIDEO_MPEG = "video/mpeg";
	public static final String MIME_VIDEO_VND_MPEGURL = "video/vnd.mpegurl";
	public static final String MIME_VIDEO_X_MSVIDEO = "video/x-msvideo";
	public static final String MIME_VIDEO_X_MS_WMV = "video/x-ms-wmv";
	public static final String MIME_VIDEO_X_SGI_MOVIE = "video/x-sgi-movie";
	public static final String MIME_X_CONFERENCE_X_COOLTALK = "x-conference/x-cooltalk";
	private static HashMap<String, String> mimeTypeMapping;

	static {
		mimeTypeMapping = new HashMap<String, String>(200) {
			private static final long serialVersionUID = 1661933272537286975L;

			private void putLocal(String key, String value) {
				if (put(key, value) != null) {
					throw new IllegalArgumentException("Duplicated extension: " + key);
				}
			}

			{
				putLocal("xul", MIME_APPLICATION_VND_MOZZILLA_XUL_XML);
				putLocal("json", MIME_APPLICATION_JSON);
				putLocal("ice", MIME_X_CONFERENCE_X_COOLTALK);
				putLocal("movie", MIME_VIDEO_X_SGI_MOVIE);
				putLocal("avi", MIME_VIDEO_X_MSVIDEO);
				putLocal("wmv", MIME_VIDEO_X_MS_WMV);
				putLocal("m4u", MIME_VIDEO_VND_MPEGURL);
				putLocal("mxu", MIME_VIDEO_VND_MPEGURL);
				putLocal("htc", MIME_TEXT_X_COMPONENT);
				putLocal("etx", MIME_TEXT_X_SETEXT);
				putLocal("wmls", MIME_TEXT_VND_WAP_WMLSCRIPT);
				putLocal("wml", MIME_TEXT_VND_WAP_XML);
				putLocal("tsv", MIME_TEXT_TAB_SEPARATED_VALUES);
				putLocal("sgm", MIME_TEXT_SGML);
				putLocal("sgml", MIME_TEXT_SGML);
				putLocal("css", MIME_TEXT_CSS);
				putLocal("ifb", MIME_TEXT_CALENDAR);
				putLocal("ics", MIME_TEXT_CALENDAR);
				putLocal("wrl", MIME_MODEL_VRLM);
				putLocal("vrlm", MIME_MODEL_VRLM);
				putLocal("silo", MIME_MODEL_MESH);
				putLocal("mesh", MIME_MODEL_MESH);
				putLocal("msh", MIME_MODEL_MESH);
				putLocal("iges", MIME_MODEL_IGES);
				putLocal("igs", MIME_MODEL_IGES);
				putLocal("rgb", MIME_IMAGE_X_RGB);
				putLocal("ppm", MIME_IMAGE_X_PORTABLE_PIXMAP);
				putLocal("pgm", MIME_IMAGE_X_PORTABLE_GRAYMAP);
				putLocal("pbm", MIME_IMAGE_X_PORTABLE_BITMAP);
				putLocal("pnm", MIME_IMAGE_X_PORTABLE_ANYMAP);
				putLocal("ico", MIME_IMAGE_X_ICON);
				putLocal("ras", MIME_IMAGE_X_CMU_RASTER);
				putLocal("wbmp", MIME_IMAGE_WAP_WBMP);
				putLocal("djv", MIME_IMAGE_VND_DJVU);
				putLocal("djvu", MIME_IMAGE_VND_DJVU);
				putLocal("svg", MIME_IMAGE_SVG_XML);
				putLocal("ief", MIME_IMAGE_IEF);
				putLocal("cgm", MIME_IMAGE_CGM);
				putLocal("bmp", MIME_IMAGE_BMP);
				putLocal("xyz", MIME_CHEMICAL_X_XYZ);
				putLocal("pdb", MIME_CHEMICAL_X_PDB);
				putLocal("ra", MIME_AUDIO_X_PN_REALAUDIO);
				putLocal("ram", MIME_AUDIO_X_PN_REALAUDIO);
				putLocal("m3u", MIME_AUDIO_X_MPEGURL);
				putLocal("aifc", MIME_AUDIO_X_AIFF);
				putLocal("aif", MIME_AUDIO_X_AIFF);
				putLocal("aiff", MIME_AUDIO_X_AIFF);
				putLocal("mp3", MIME_AUDIO_MPEG);
				putLocal("mp2", MIME_AUDIO_MPEG);
				putLocal("mp1", MIME_AUDIO_MPEG);
				putLocal("mpga", MIME_AUDIO_MPEG);
				putLocal("kar", MIME_AUDIO_MIDI);
				putLocal("mid", MIME_AUDIO_MIDI);
				putLocal("midi", MIME_AUDIO_MIDI);
				putLocal("dtd", MIME_APPLICATION_XML_DTD);
				putLocal("xsl", MIME_APPLICATION_XML);
				putLocal("xml", MIME_APPLICATION_XML);
				putLocal("xslt", MIME_APPLICATION_XSLT_XML);
				putLocal("xht", MIME_APPLICATION_XHTML_XML);
				putLocal("xhtml", MIME_APPLICATION_XHTML_XML);
				putLocal("src", MIME_APPLICATION_X_WAIS_SOURCE);
				putLocal("ustar", MIME_APPLICATION_X_USTAR);
				putLocal("ms", MIME_APPLICATION_X_TROFF_MS);
				putLocal("me", MIME_APPLICATION_X_TROFF_ME);
				putLocal("man", MIME_APPLICATION_X_TROFF_MAN);
				putLocal("roff", MIME_APPLICATION_X_TROFF);
				putLocal("tr", MIME_APPLICATION_X_TROFF);
				putLocal("t", MIME_APPLICATION_X_TROFF);
				putLocal("texi", MIME_APPLICATION_X_TEXINFO);
				putLocal("texinfo", MIME_APPLICATION_X_TEXINFO);
				putLocal("tex", MIME_APPLICATION_X_TEX);
				putLocal("tcl", MIME_APPLICATION_X_TCL);
				putLocal("sv4crc", MIME_APPLICATION_X_SV4CRC);
				putLocal("sv4cpio", MIME_APPLICATION_X_SV4CPIO);
				putLocal("sit", MIME_APPLICATION_X_STUFFIT);
				putLocal("swf", MIME_APPLICATION_X_SHOCKWAVE_FLASH);
				putLocal("shar", MIME_APPLICATION_X_SHAR);
				putLocal("sh", MIME_APPLICATION_X_SH);
				putLocal("cdf", MIME_APPLICATION_X_NETCDF);
				putLocal("nc", MIME_APPLICATION_X_NETCDF);
				putLocal("latex", MIME_APPLICATION_X_LATEX);
				putLocal("skm", MIME_APPLICATION_X_KOAN);
				putLocal("skt", MIME_APPLICATION_X_KOAN);
				putLocal("skd", MIME_APPLICATION_X_KOAN);
				putLocal("skp", MIME_APPLICATION_X_KOAN);
				putLocal("js", MIME_APPLICATION_X_JAVASCRIPT);
				putLocal("hdf", MIME_APPLICATION_X_HDF);
				putLocal("gtar", MIME_APPLICATION_X_GTAR);
				putLocal("spl", MIME_APPLICATION_X_FUTURESPLASH);
				putLocal("dvi", MIME_APPLICATION_X_DVI);
				putLocal("dxr", MIME_APPLICATION_X_DIRECTOR);
				putLocal("dir", MIME_APPLICATION_X_DIRECTOR);
				putLocal("dcr", MIME_APPLICATION_X_DIRECTOR);
				putLocal("csh", MIME_APPLICATION_X_CSH);
				putLocal("cpio", MIME_APPLICATION_X_CPIO);
				putLocal("pgn", MIME_APPLICATION_X_CHESS_PGN);
				putLocal("vcd", MIME_APPLICATION_X_CDLINK);
				putLocal("bcpio", MIME_APPLICATION_X_BCPIO);
				putLocal("rm", MIME_APPLICATION_VND_RNREALMEDIA);
				putLocal("ppt", MIME_APPLICATION_VND_MSPOWERPOINT);
				putLocal("mif", MIME_APPLICATION_VND_MIF);
				putLocal("grxml", MIME_APPLICATION_SRGS_XML);
				putLocal("gram", MIME_APPLICATION_SRGS);
				putLocal("smil", MIME_APPLICATION_RDF_SMIL);
				putLocal("smi", MIME_APPLICATION_RDF_SMIL);
				putLocal("rdf", MIME_APPLICATION_RDF_XML);
				putLocal("ogg", MIME_APPLICATION_X_OGG);
				putLocal("oda", MIME_APPLICATION_ODA);
				putLocal("dmg", MIME_APPLICATION_OCTET_STREAM);
				putLocal("lzh", MIME_APPLICATION_OCTET_STREAM);
				putLocal("so", MIME_APPLICATION_OCTET_STREAM);
				putLocal("lha", MIME_APPLICATION_OCTET_STREAM);
				putLocal("dms", MIME_APPLICATION_OCTET_STREAM);
				putLocal("bin", MIME_APPLICATION_OCTET_STREAM);
				putLocal("mathml", MIME_APPLICATION_MATHML_XML);
				putLocal("cpt", MIME_APPLICATION_MAC_COMPACTPRO);
				putLocal("hqx", MIME_APPLICATION_MAC_BINHEX40);
				putLocal("jnlp", MIME_APPLICATION_JNLP);
				putLocal("ez", MIME_APPLICATION_ANDREW_INSET);
				putLocal("txt", MIME_TEXT_PLAIN);
				putLocal("ini", MIME_TEXT_PLAIN);
				putLocal("c", MIME_TEXT_PLAIN);
				putLocal("h", MIME_TEXT_PLAIN);
				putLocal("cpp", MIME_TEXT_PLAIN);
				putLocal("cxx", MIME_TEXT_PLAIN);
				putLocal("cc", MIME_TEXT_PLAIN);
				putLocal("chh", MIME_TEXT_PLAIN);
				putLocal("java", MIME_TEXT_PLAIN);
				putLocal("csv", MIME_TEXT_PLAIN);
				putLocal("bat", MIME_TEXT_PLAIN);
				putLocal("cmd", MIME_TEXT_PLAIN);
				putLocal("asc", MIME_TEXT_PLAIN);
				putLocal("rtf", MIME_TEXT_RTF);
				putLocal("rtx", MIME_TEXT_RICHTEXT);
				putLocal("html", MIME_TEXT_HTML);
				putLocal("htm", MIME_TEXT_HTML);
				putLocal("zip", MIME_APPLICATION_ZIP);
				putLocal("rar", MIME_APPLICATION_X_RAR_COMPRESSED);
				putLocal("gzip", MIME_APPLICATION_X_GZIP);
				putLocal("gz", MIME_APPLICATION_X_GZIP);
				putLocal("tgz", MIME_APPLICATION_TGZ);
				putLocal("tar", MIME_APPLICATION_X_TAR);
				putLocal("gif", MIME_IMAGE_GIF);
				putLocal("jpeg", MIME_IMAGE_JPEG);
				putLocal("jpg", MIME_IMAGE_JPEG);
				putLocal("jpe", MIME_IMAGE_JPEG);
				putLocal("tiff", MIME_IMAGE_TIFF);
				putLocal("tif", MIME_IMAGE_TIFF);
				putLocal("png", MIME_IMAGE_PNG);
				putLocal("au", MIME_AUDIO_BASIC);
				putLocal("snd", MIME_AUDIO_BASIC);
				putLocal("wav", MIME_AUDIO_X_WAV);
				putLocal("mov", MIME_VIDEO_QUICKTIME);
				putLocal("qt", MIME_VIDEO_QUICKTIME);
				putLocal("mpeg", MIME_VIDEO_MPEG);
				putLocal("mpg", MIME_VIDEO_MPEG);
				putLocal("mpe", MIME_VIDEO_MPEG);
				putLocal("abs", MIME_VIDEO_MPEG);
				putLocal("doc", MIME_APPLICATION_MSWORD);
				putLocal("xls", MIME_APPLICATION_VND_MSEXCEL);
				putLocal("eps", MIME_APPLICATION_POSTSCRIPT);
				putLocal("ai", MIME_APPLICATION_POSTSCRIPT);
				putLocal("ps", MIME_APPLICATION_POSTSCRIPT);
				putLocal("pdf", MIME_APPLICATION_PDF);
				putLocal("exe", MIME_APPLICATION_OCTET_STREAM);
				putLocal("dll", MIME_APPLICATION_OCTET_STREAM);
				putLocal("class", MIME_APPLICATION_OCTET_STREAM);
				putLocal("jar", MIME_APPLICATION_JAVA_ARCHIVE);
				putLocal("docx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.wordprocessingml.document");
				putLocal("docm", MIME_APPLICATION_VND_MSWORD + ".document.macroEnabled.12");
				putLocal("dotx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.wordprocessingml.template");
				putLocal("dotm", MIME_APPLICATION_VND_MSWORD + ".template.macroEnabled.12");
				putLocal("xlsx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.spreadsheetml.sheet");
				putLocal("xlsm", MIME_APPLICATION_VND_MSEXCEL + ".sheet.macroEnabled.12");
				putLocal("xltx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.spreadsheetml.template");
				putLocal("xltm", MIME_APPLICATION_VND_MSEXCEL + ".template.macroEnabled.12");
				putLocal("xlsb", MIME_APPLICATION_VND_MSEXCEL + ".sheet.binary.macroEnabled.12");
				putLocal("xlam", MIME_APPLICATION_VND_MSEXCEL + ".addin.macroEnabled.12");
				putLocal("pptx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.presentationml.presentation");
				putLocal("pptm", MIME_APPLICATION_VND_MS + "-powerpoint.presentation.macroEnabled.12");
				putLocal("ppsx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.presentationml.slideshow");
				putLocal("ppsm", MIME_APPLICATION_VND_MS + "-powerpoint.slideshow.macroEnabled.12");
				putLocal("potx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.presentationml.template");
				putLocal("potm", MIME_APPLICATION_VND_MS + "-powerpoint.template.macroEnabled.12");
				putLocal("ppam", MIME_APPLICATION_VND_MS + "-powerpoint.addin.macroEnabled.12");
				putLocal("sldx", MIME_APPLICATION_VND_OPENXMLFORMATS + "-officedocument.presentationml.slide");
				putLocal("sldm", MIME_APPLICATION_VND_MS + "-powerpoint.slide.macroEnabled.12");
				putLocal("one", MIME_APPLICATION_MSONENOTE);
				putLocal("onetoc2", MIME_APPLICATION_MSONENOTE);
				putLocal("onetmp", MIME_APPLICATION_MSONENOTE);
				putLocal("onepkg", MIME_APPLICATION_MSONENOTE);
				putLocal("thmx", MIME_APPLICATION_VND_MS + "-officetheme");
			}
		};
	}

	/**
	 * Constructeur de {@link MimeTypes}
	 */
	private MimeTypes() {
	}

	public static void main(String[] args) {
		System.out.println(mimeTypeMapping.size());
	}

	/**
	 * Registers MIME type for provided extension. Existing extension type will
	 * be overriden.
	 */
	public static void registerMimeType(String ext, String mimeType) {
		mimeTypeMapping.put(ext, mimeType);
	}

	/**
	 * Returns the corresponding MIME type to the given extension. If no MIME
	 * type was found it returns 'application/octet-stream' type.
	 */
	public static String getMimeType(String ext) {
		String mimeType = lookupMimeType(ext);
		if (mimeType == null) {
			mimeType = MIME_APPLICATION_OCTET_STREAM;
		}
		return mimeType;
	}

	/**
	 * Simply returns MIME type or <code>null</code> if no type is found.
	 */
	public static String lookupMimeType(String ext) {
		return mimeTypeMapping.get(ext.toLowerCase());
	}
}