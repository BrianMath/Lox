package com.craftinginterpreters.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		String outputDir = args[0];

		defineAST(outputDir, "Expr", Arrays.asList(
			"Unary    : Token operator, Expr right",
			"Binary   : Expr left, Token operator, Expr right",
			"Grouping : Expr expression",
			"Literal  : Object value"
		));
	}

	private static void defineAST(
			String outputDir, String baseName, List<String> types)
			throws IOException {

		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");

		// Código que será escrito no arquivo
		writer.println("package com.craftinginterpreters.lox;");
		writer.println();
		writer.println("import java.util.List;");
		writer.println();
		writer.println("abstract class " + baseName + " {");

		defineVisitor(writer, baseName, types);

		// Classes da AST
		for (String type : types) {
			String classname = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, classname, fields);
		}

		// Método base accept()
		writer.println("	abstract <R> R accept(Visitor<R> visitor);");

		writer.println("}");
		writer.close();
	}

	private static void defineVisitor(
			PrintWriter writer, String baseName, List<String> types) {
		
		writer.println("	interface Visitor<R> {");

		for (String type : types) {
			String typeName = type.split(":")[0].trim();
			writer.println("		R visit" + typeName + baseName + "(" + 
				typeName + " " + baseName.toLowerCase() + ");");
		}

		writer.println("	}\n");
	}

	private static void defineType(
			PrintWriter writer, String baseName, 
			String className, String fieldList) {
			
		writer.println("	static class " + className + " extends " + baseName + " {");
		
		// Criar construtor
		writer.println("		" + className + "(" + fieldList + ") {");
		
		// Armazenar campos nos atributos
		String[] fields = fieldList.split(", ");
		for (String field : fields) {
			String name = field.split(" ")[1].trim();
			writer.println("			this." + name + " = " + name + ";");
		}

		writer.println("		}\n");

		// Padrão Visitor
		writer.println("		@Override");
		writer.println("		<R> R accept(Visitor<R> visitor) {");
		writer.println("			return visitor.visit" + className + baseName + "(this);");
		writer.println("		}\n");

		// Criar atributos
		for (String field : fields) {
			writer.println("		final " + field + ";");
		}

		writer.println("	}\n");
	}
}