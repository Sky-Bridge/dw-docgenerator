package org.developerworld.docgenerator;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.developerworld.docgenerator.excel.ExcelTemplateFileGenerator;

/**
 * 生成器
 * 
 * @author Roy Huang
 * @version 20130728
 * 
 */
public class Generator {

	private final static Log log = LogFactory.getLog(Generator.class);
	private final static List<TemplateFileGenerator> TEMPLATE_FILE_GENERATORS=new ArrayList<TemplateFileGenerator>();
	static{
		TEMPLATE_FILE_GENERATORS.add(new ExcelTemplateFileGenerator());
	}
	private static final JexlEngine jexl = new JexlEngine();
	
	private String templateFilePath;
	private String outputFilePath;
	private FileFilter templateFileFilter;
	private File templateFileDirectory;
	private List<TemplateFileGenerator> templateFileGenerators = new ArrayList<TemplateFileGenerator>(TEMPLATE_FILE_GENERATORS);

	public void setTemplateFilePath(String templateFilePath) {
		this.templateFilePath = templateFilePath;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public void setTemplateFileFilter(FileFilter templateFileFilter) {
		this.templateFileFilter = templateFileFilter;
	}

	public void setTemplateFileGenerators(
			List<TemplateFileGenerator> templateFileGenerators) {
		this.templateFileGenerators = templateFileGenerators;
	}

	/**
	 * 添加新模板生成
	 * 
	 * @param templateFileGenerator
	 * @return
	 */
	public Generator addTemplateFileGenerator(
			TemplateFileGenerator templateFileGenerator) {
		if (templateFileGenerators == null)
			templateFileGenerators = new ArrayList<TemplateFileGenerator>();
		templateFileGenerators.add(templateFileGenerator);
		return this;
	}

	public void generate(Map envVar) throws Exception {
		Date beginTime = new Date();
		// 定义模板目录对象
		log.info("开始执行生成");
		log.info("模板目录为：" + templateFilePath);
		templateFileDirectory = new File(templateFilePath);
		if (!templateFileDirectory.exists())
			throw new Exception("templateFilePath is not found!");
		else if (!templateFileDirectory.isDirectory())
			throw new Exception("templateFilePath must is a directory!");
		log.info("输出目录为：" + outputFilePath);
		File outputDirectory = new File(outputFilePath);
		// 清空输出路径
		if (!outputDirectory.exists())
			outputDirectory.mkdirs();
		// 遍历模板目录，进行文件生成
		File[] templateFiles = null;
		if (templateFileFilter != null)
			templateFiles = templateFileDirectory.listFiles(templateFileFilter);
		else
			templateFiles = templateFileDirectory.listFiles();
		for (File templateFile : templateFiles)
			generate(templateFile, outputDirectory, envVar);
		log.info("完成生成,用时:"
				+ (System.currentTimeMillis() - beginTime.getTime()) / 1000
				+ "秒");
	}

	/**
	 * 生成模板文件
	 * 
	 * @param templateFile
	 * @param outputDirectory
	 * @param envVar
	 * @throws Exception 
	 */
	private void generate(File templateFile, File outputDirectory, Map envVar) throws Exception {
		if (templateFile.isDirectory()) {
			outputDirectory = new File(outputDirectory, getElName(templateFile.getName(),envVar));
			if (!outputDirectory.exists())
				outputDirectory.mkdirs();
			// 遍历模板目录，进行文件生成
			File[] templateFiles = null;
			if (templateFileFilter != null)
				templateFiles = templateFileDirectory
						.listFiles(templateFileFilter);
			else
				templateFiles = templateFileDirectory.listFiles();
			for (File _templateFile : templateFiles)
				generate(_templateFile, outputDirectory, envVar);
		} else {
			File outputFile = new File(outputDirectory, getElName(templateFile.getName(),envVar));
			boolean isG = false;
			for (int i = templateFileGenerators.size() - 1; i >= 0; i--) {
				TemplateFileGenerator templateFileGenerator = templateFileGenerators
						.get(i);
				if (templateFileGenerator.isSupport(templateFile)) {
					templateFileGenerator.generate(templateFile, outputFile,
							envVar);
					isG = true;
					break;
				}
			}
			if (!isG)
				log.warn("can not find TemplateFileGenerator to generator template file:"
						+ templateFile.getPath());
		}
	}
	
	/**
	 * 获取表达式名称
	 * @param str
	 * @param envVar
	 * @return
	 */
	private String getElName(String str,Map envVar){
		String rst=str;
		//根据内容，获取表达式内容
		int begin=rst.indexOf("${");
		while(begin>=0){
			int end=rst.indexOf("}",begin);
			if(end>begin){
				String eStr=rst.substring(begin+2,end);
				//创建表达式
				Expression e = jexl.createExpression( eStr );
				//创建环境变量
				JexlContext context = new MapContext(envVar);
				//运行表达式
				String tmp=(String) e.evaluate(context);
				//替换原来的内容
				rst=rst.substring(0,begin)+tmp+rst.substring(end+1);
				begin=begin+tmp.length();
			}
			else
				break;
		}
		return rst;
	}

}
