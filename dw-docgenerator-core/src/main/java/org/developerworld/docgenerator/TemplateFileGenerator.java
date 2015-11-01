package org.developerworld.docgenerator;

import java.io.File;
import java.util.Map;

/**
 * 模板生成器接口
 * 
 * @author Roy Huang
 * @version 20130728
 * 
 */
public interface TemplateFileGenerator {

	/**
	 * 判断是否支持该文件
	 * 
	 * @param file
	 * @return
	 */
	public boolean isSupport(File file);

	/**
	 * 执行文件生成
	 * 
	 * @param templateFile
	 * @param outputFile
	 * @param envVar
	 * @throws Exception
	 */
	public void generate(File templateFile, File outputFile, Map envVar)
			throws Exception;
}
