package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	// Aponta para o começo de um lexema
	private int start = 0;
	// Aponta para o caractere que está sendo analisado em um lexema
	private int current = 0;
	// Linha atual em que o caractere sendo analisado está
	private int line = 1;

	private static final Map<String, TokenType> keywords;
	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// Esse é o começo de um lexema
			start = current;
			scanToken();
		}

		// Ao final é adicionado um token EOF
		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();

		switch (c) {
			// Lexemas com 1 caractere
			case '(': addToken(LEFT_PAREN); break;
			case ')': addToken(RIGHT_PAREN); break;
			case '{': addToken(LEFT_BRACE); break;
			case '}': addToken(RIGHT_BRACE); break;
			case ',': addToken(COMMA); break;
			case '.': addToken(DOT); break;
			case '-': addToken(MINUS); break;
			case '+': addToken(PLUS); break;
			case ';': addToken(SEMICOLON); break;
			case '*': addToken(STAR); break;

			// Lexemas com 1 ou 2 caracteres
			case '!':
				addToken(match('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '>':
				addToken(match('=') ? GREATER_EQUAL : GREATER);
				break;
			case '<':
				addToken(match('=') ? LESS_EQUAL : LESS);
				break;
			case '/':
				if (match('/')) {
					// Consume os caracteres e os ignora
					while (peek() != '\n' && !isAtEnd()) advance();
				} else if (match('*')) {
					multiComments();
				} else {
					addToken(SLASH);
				}
				break;

			// Ignorar espaços em branco
			case ' ':
			case '\r':
			case '\t':
				break;

			// Incrementar linha
			case '\n':
				line++;
				break;

			// String literal
			case '"':
				string();
				break;

			default:
				// Número literal
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					Lox.error(line, "Unexpected character");
				}
				break;
		}
	}

	private void multiComments() {
		while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) {
			advance();
			
			if (peek() == '\n') line++;
		}

		// Ignorar * e /
		advance(); advance();
	}

	private void identifier() {
		while (isAlphaNumeric(peek())) advance();

		// Verifica se é uma palavra reservada antes de criar o token
		String value = source.substring(start, current);
		TokenType type = keywords.get(value);
		
		if (type == null) type = IDENTIFIER;
		addToken(type);
	}

	private void number() {
		while (isDigit(peek())) advance();

		// Procura por uma parte fracionária
		if (peek() == '.' && isDigit(peekNext())) {
			// Consumir o .
			advance();

			while (isDigit(peek())) advance();
		}

		// Converta a string para Double e cria um token do número
		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private void string() {
		// Itera pela string até encontrar " de fechamento
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++;
			advance();
		}

		// Tratamento de erro
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string");
		}

		// Consumir " de fechamento
		advance();

		// Corta as "" fora e cria um token da string
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	// Compara o caractere apontado por 'current' com um 'char'
	private boolean match(char expected) {
		if (isAtEnd()) return false;
		if (source.charAt(current) != expected) return false;

		current++;
		return true;
	}

	private char peek() {
		// Necessário para evitar acesso fora do limite de source
		if (isAtEnd()) return '\0';
		return source.charAt(current);
	}

	private char peekNext() {
		// Permite um lookahead de 2 caracteres no máximo
		if (current + 1 >= source.length()) return '\0';
		return source.charAt(current + 1);
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
			   (c >= 'A' && c <= 'Z') ||
			   (c == '_');
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private char advance() {
		return source.charAt(current++);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String lexeme = source.substring(start, current);
		tokens.add(new Token(type, lexeme, literal, line));
	}
}
