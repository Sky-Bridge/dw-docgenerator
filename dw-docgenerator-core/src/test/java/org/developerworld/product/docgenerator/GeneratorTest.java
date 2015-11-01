package org.developerworld.product.docgenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.developerworld.commons.dbutils.info.DBInfo;
import org.developerworld.commons.dbutils.info.TablenameFilter;
import org.developerworld.commons.dbutils.info.object.Column;
import org.developerworld.commons.dbutils.info.object.ForeignKey;
import org.developerworld.commons.dbutils.info.object.Index;
import org.developerworld.commons.dbutils.info.object.Table;
import org.developerworld.commons.lang.time.DateUtils;
import org.developerworld.docgenerator.Generator;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 生成器单元测试类
 * 
 * @author Roy Huang
 * @version 20130728
 * 
 */
public class GeneratorTest {

	private static Generator generator = null;
	private static DBInfo dbInfo = null;
	private static Connection connection;

	@BeforeClass
	public static void beforeClass() throws ClassNotFoundException,
			SQLException {
		generator = new Generator();
		// 设置模板目录
		generator
				.setTemplateFilePath("E:\\eclipse_workspace\\dw-product-docgenerator\\src\\test\\resources\\template");
		// 设置输出目录
		generator.setOutputFilePath("e:\\docgenerator");
		// 设置文件过滤器
		generator.setTemplateFileFilter(new FileFilter() {

			public boolean accept(File pathname) {
				return true;
			}

		});
		// // 创建dbinfo对象
		// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// Connection connection = DriverManager.getConnection(
		// "jdbc:sqlserver://localhost:1433;DatabaseName=gagc3161", "gagc3161",
		// "gagc3161");
		Class.forName("com.mysql.jdbc.Driver");
//		Connection connection = DriverManager
//				.getConnection(
//						"jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=UTF-8",
//						"root", "root");
		Connection connection = DriverManager
				.getConnection(
						"jdbc:mysql://localhost:3306/wms?useUnicode=true&amp;characterEncoding=UTF-8",
						"root", "root");
		dbInfo = new DBInfo(connection);
	}

	@AfterClass
	public static void afterClass() throws SQLException, IOException {
		if (connection != null && !connection.isClosed())
			connection.close();
		Runtime.getRuntime().exec("cmd.exe /c start " + "e:\\docgenerator");
	}

	@Test
	public void testGenerate() throws Exception {
		Set<Table> tables = dbInfo.getAllTableInfos(new TablenameFilter() {

			public boolean accept(String tableName) {
				tableName = tableName.toLowerCase();
//				return true;
				return tableName.startsWith("pms_")
						|| tableName.startsWith("hrr_");
			}

		});
		for (Table table : tables) {
			Map envVar = buildEnvVar(table);
			generator.generate(envVar);
		}
		Assert.assertTrue(true);
	}

	/**
	 * 创建环境变量
	 * 
	 * @param table
	 * @return
	 */
	private Map buildEnvVar(Table table) {
		Map envVar = new HashMap();
		// 文档信息
		Map docMap = new HashMap();
		envVar.put("doc", docMap);
		docMap.put("projectName", "网站管理系统WMS");
		docMap.put("createDate", DateUtils.formatDate(new Date()));
		docMap.put("createUser", "黄若儒");
		docMap.put("updateDate", DateUtils.formatDate(new Date()));
		docMap.put("updateUser", "黄若儒");
		docMap.put("version", "V1.0");
		// 数据库表信息
		Map tableMap = new HashMap();
		//强制输出大写表名
		table.setName(table.getName().toUpperCase());
		envVar.put("table", tableMap);
		tableMap.put("name", table.getName());
		tableMap.put("remarks", table.getRemarks());
		if (table.getName().indexOf("PMS_") == 0)
			tableMap.put("moduleName", "问卷模块");
		else
			if (table.getName().indexOf("HRR_") == 0)
			tableMap.put("moduleName", "人才招聘模块");
		// 数据库字段信息
		List<Map> columns = new ArrayList<Map>();
		tableMap.put("columns", columns);
		for (Column _column : table.getColumns()) {
			Map column = new HashMap();
			column.put("name", _column.getName());
			column.put("remarks", _column.getRemarks());
			column.put("isNullable", _column.isNullable());
			column.put("defaultValue", _column.getDefaultValue() == null ? ""
					: _column.getDefaultValue());
			column.put("isAutoIncrement", _column.isAutoIncrement());
			if (_column.getType() == Types.DECIMAL)
				column.put("typeName",
						_column.getTypeName() + "(" + _column.getLength() + ","
								+ _column.getDecimalDigits() + ")");
			else
				column.put("typeName",
						_column.getTypeName() + "(" + _column.getLength() + ")");
			// 判断是否主键
			column.put("isPk", false);
			if (table.getPrimaryKey() != null) {
				for (Column pkColumn : table.getPrimaryKey().getColumns()) {
					if (pkColumn.getName().equals(_column.getName())) {
						column.put("isPk", true);
						break;
					}
				}
			}
			// 判断是否外键
			column.put("isFk", false);
			for (ForeignKey foreignKey : table.getForeignKeys()) {
				for (Column fkColumn : foreignKey.getColumns()) {
					if (fkColumn.getName().equals(_column.getName())) {
						column.put("isFk", true);
						break;
					}
				}
			}
			// 判断是否唯一
			column.put("isUnique", false);
			for (Index index : table.getIndexs()) {
				if (!index.isUnique())
					continue;
				for (Column ixColumn : index.getColumns()) {
					if (ixColumn.getName().equals(_column.getName())) {
						column.put("isUnique", true);
						break;
					}
				}
			}
			// 获取索引信息
			String indexDescription = "";
			for (Index index : table.getIndexs()) {
				for (Column ixColumn : index.getColumns()) {
					if (ixColumn.getName().equals(_column.getName())) {
						indexDescription += "关联索引：" + index.getName();
						if (index.getColumns().size() > 1) {
							indexDescription += ",且为组合索引(";
							for (Column _ixColumn : index.getColumns())
								indexDescription += _ixColumn.getName() + ",";
							indexDescription = indexDescription.substring(0,
									indexDescription.length() - 1) + ")";
						}
						indexDescription += ";\n";
						break;
					}
				}
			}
			column.put("indexDescription", indexDescription);
			// 备注信息
			String otherRemark = "";
			for (ForeignKey foreignKey : table.getForeignKeys()) {
				int i = 0;
				for (Column fkColumn : foreignKey.getColumns()) {
					if (fkColumn.getName().equals(_column.getName())) {
						otherRemark += "关联"
								+ foreignKey.getPrimaryTable().getName().toUpperCase() + ".";
						int j = 0;
						for (Column fpc : foreignKey.getPrimaryColumns()) {
							if (j == i) {
								otherRemark += fpc.getName() + ";\n";
								break;
							}
							j++;
						}
						break;
					}
					++i;
				}
			}
			column.put("otherRemark", otherRemark);
			columns.add(column);
		}
		return envVar;
	}

}
