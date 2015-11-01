package org.developerworld.docgenerator.excel;

import java.io.File;
import java.util.Map;

import net.sf.jxls.transformer.XLSTransformer;

import org.apache.commons.io.FilenameUtils;
import org.developerworld.docgenerator.TemplateFileGenerator;

/**
 * excel模板文件生成
 * 
 * @author Roy Huang
 * @version 20130728
 * 
 */
public class ExcelTemplateFileGenerator implements TemplateFileGenerator {

	public boolean isSupport(File file) {
		String extension = FilenameUtils.getExtension(file.getName());
		return extension.equalsIgnoreCase("xls")
				|| extension.equalsIgnoreCase("xlsx");
	}

	public void generate(File templateFile, File outputFile, Map envVar)
			throws Exception {
		XLSTransformer xlsTransformer = new XLSTransformer();
		xlsTransformer.transformXLS(templateFile.getPath(), envVar,
				outputFile.getPath());
	}

}
