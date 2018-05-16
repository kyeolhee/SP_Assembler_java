import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Assembler : �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�. ���α׷��� ���� �۾��� ������
 * ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br>
 * 
 * <br>
 * <br>
 * �ۼ����� ���ǻ��� : <br>
 * 1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ����
 * �ȵȴ�.<br>
 * 2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 * 3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 * 4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br>
 * <br>
 * + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� ��
 * �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ���� */
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ���� */
	ArrayList<TokenTable> TokenList;
	/** ���α׷��� section���� literal�� �����ϴ� ���� */
	ArrayList<SymbolTable> literalList;
	/** ���α׷��� section���� external reference�� �����ϴ� ���� */
	ArrayList<SymbolTable> externalList;
	/** ���α׷��� section���� modification�� �����ϴ� ���� */
	ArrayList<SymbolTable> modifyList;
	/**
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;

	int numSection;
	int loccount;

	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile
	 *            : instruction ���� �ۼ��� ���� �̸�.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		literalList = new ArrayList<SymbolTable>();
		externalList = new ArrayList<SymbolTable>();
		modifyList = new ArrayList<SymbolTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");

		assembler.pass1();
		assembler.printSymbolTable("symtab_20160259.txt");

		assembler.pass2();
		assembler.printObjectCode("output_20160259.txt");

	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName
	 *            : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);

			for (int i = 0; i < codeList.size(); i++) {
				filewriter.write(codeList.get(i));
				filewriter.write("\r\n"); // filewirter.write("\n");�ϸ� ������ ����

			}
			filewriter.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * 
	 * @param fileName
	 *            : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);

			for (int i = 0; i <= numSection; i++) {
				for (int j = 0; j < symtabList.get(i).getListSize(); j++) {
					String line = symtabList.get(i).getSymbol(j) + "\t"
							+ String.format("%X", symtabList.get(i).getAddress(j)) + "\r\n";
					filewriter.write(line);
				}
				filewriter.write("\r\n"); // filewirter.write("\n");�ϸ� ������ ����
			}
			filewriter.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass1 ������ �����Ѵ�.<br>
	 * 1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 * 2) label�� symbolTable�� ����<br>
	 * <br>
	 * <br>
	 * ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		String line;
		int TokenIndex = 0;
		Token ParsedToken;

		// TODO Auto-generated method stub
		for (int i = 0; i < lineList.size(); i++) {
			line = lineList.get(i);

			/* START �� ���� �ÿ� ��� List�� �ʱ�ȭ���ش� */
			if (line.contains("START")) {
				numSection = 0;
				loccount = 0;
				TokenIndex = 0;

				symtabList.clear();
				TokenList.clear();
				literalList.clear();
				externalList.clear();
				modifyList.clear();

				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(0), literalList.get(0), externalList.get(0), instTable));
				modifyList.add(new SymbolTable());
			}
			/* CSECT �� ���� �ÿ� ���ο� section�� ���۵ǹǷ� ���ο� list�� ������ش� */
			else if (line.contains("CSECT")) {
				numSection++;
				loccount = 0;
				TokenIndex = 0;

				symtabList.add(new SymbolTable());
				literalList.add(new SymbolTable());
				externalList.add(new SymbolTable());
				TokenList.add(new TokenTable(symtabList.get(numSection), literalList.get(numSection),
						externalList.get(numSection), instTable));
				modifyList.add(new SymbolTable());
			}

			/* �Է¹��� line�� token���� ������ tokenList�� ���� */
			TokenList.get(numSection).putToken(line, loccount);
			/* �ٷ� ���� parsing�� token */
			ParsedToken = TokenList.get(numSection).getToken(TokenIndex);

			/* symbol table ���� - symbol��� location */
			if (line.charAt(0) != '.' && !ParsedToken.label.equals(" ")) {
				if (ParsedToken.operator.equals("EQU")) {
					if (ParsedToken.operand[0].equals("*")) {
						symtabList.get(numSection).putSymbol(ParsedToken.label, ParsedToken.location);
					} else if (ParsedToken.operand[0].contains("-")) {
						String[] opToken = ParsedToken.operand[0].split("-");
						int calLocation;

						calLocation = symtabList.get(numSection).search(opToken[0])
								- symtabList.get(numSection).search(opToken[1]);

						symtabList.get(numSection).putSymbol(ParsedToken.label, calLocation);
						TokenList.get(numSection).getToken(TokenIndex).location = calLocation;
					}
				} else {
					symtabList.get(numSection).putSymbol(ParsedToken.label, ParsedToken.location);
				}
				loccount += ParsedToken.byteSize;
			}

			/* literal table ���� - literal��� location(0���� ����), literal�� value�� ũ�� */
			if (ParsedToken.operand != null) {
				if (ParsedToken.operand[0] != null && ParsedToken.operand[0].charAt(0) == '=') {
					int litLength = ParsedToken.operand[0].length();
					if (ParsedToken.operand[0].charAt(1) == 'C') {
						literalList.get(numSection).putLiteral(ParsedToken.operand[0].substring(1), 0,
								ParsedToken.operand[0].substring(3, litLength - 1), litLength - 4);
					} else if (ParsedToken.operand[0].charAt(1) == 'X') {
						literalList.get(numSection).putLiteral(ParsedToken.operand[0].substring(1), 0,
								ParsedToken.operand[0].substring(3, litLength - 1), 1);
					}
				}
			}

			/* literal table�� ����� literal�� �ּ� ���� - 0���� ������ �ּҸ� �˸��� �ּҷ� ���� */
			String literalName;
			if (line.charAt(0) != '.') {
				if (ParsedToken.operator.equals("LTORG") || ParsedToken.operator.equals("END")) {
					for (int litIndex = 0; litIndex < literalList.get(numSection).getListSize(); litIndex++) {
						literalName = literalList.get(numSection).getSymbol(litIndex);
						literalList.get(numSection).modifySymbol(literalName, loccount);
						if (literalName.charAt(0) == 'C') {
							int length = literalName.length();
							loccount += literalName.substring(2, length - 1).length();
							ParsedToken.byteSize = length - 3;
						} else if (literalName.charAt(0) == 'X') {
							loccount++;
						}
					}
				} else if (ParsedToken.label.equals(" ")) {
					loccount += ParsedToken.byteSize;
				}
			}

			/* externalList ���� - external reference�� �� ���� */
			if (ParsedToken.operator != null && ParsedToken.operator.equals("EXTREF")) {
				String[] exToken = ParsedToken.operand[0].split(",");

				for (int exIndex = 0; exIndex < exToken.length; exIndex++) {
					externalList.get(numSection).putSymbol(exToken[exIndex], 0);
				}
			}

			/*
			 * modifyList ���� - externalList�� �������� modification��, ȣ��Ǵ� location, ������ address��
			 * ��ġ
			 */
			if (ParsedToken.operand != null) {
				for (int modifIndex = 0; modifIndex < externalList.get(numSection).getListSize(); modifIndex++) {
					if (ParsedToken.operand[0] != null
							&& ParsedToken.operand[0].contains(externalList.get(numSection).getSymbol(modifIndex))) {
						if (ParsedToken.operator.charAt(0) == '+') {
							modifyList.get(numSection).putModifySymbol(
									"+" + externalList.get(numSection).getSymbol(modifIndex), ParsedToken.location + 1,
									5);
						} else if (ParsedToken.operand[0].contains("-")) {
							String[] modifToken = ParsedToken.operand[0].split("-");

							modifyList.get(numSection).putModifySymbol("+" + modifToken[0], ParsedToken.location, 6);
							modifyList.get(numSection).putModifySymbol("-" + modifToken[1], ParsedToken.location, 6);
							break;
						}
					}
				}
			}
			TokenIndex++;
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.<br>
	 * 1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		String codeLine;
		Token currentToken;
		int sectionSize;
		int endFlag;

		/* token�� object code�� �ϼ� */
		for (int secIndex = 0; secIndex <= numSection; secIndex++) {
			for (int tokenIndex = 0; tokenIndex < TokenList.get(secIndex).getSize(); tokenIndex++) {
				TokenList.get(secIndex).makeObjectCode(tokenIndex);
			}
		}

		/*
		 * object code�� �������� codelist�� �ۼ��Ѵ� �̶� section�� ���� �������� �ʰ� ����� �� section�� �ٲ�� ��
		 * ������ ��µǵ��� �Ѵ�
		 */
		for (int secIndex = 0; secIndex <= numSection; secIndex++) {
			sectionSize = 0;
			endFlag = 0;
			for (int tokenIndex = 0; tokenIndex < TokenList.get(secIndex).getSize(); tokenIndex++) {
				codeLine = new String();
				currentToken = TokenList.get(secIndex).getToken(tokenIndex);

				/* ���α׷��� ������ ���� ��� endFlag�� 1�� �Ͽ� object program�� ����� �� End record�� ���� ��� */
				if (currentToken.operator != null && currentToken.operator.equals("START")) {
					endFlag = 1;
				}

				/* header */
				if (tokenIndex == 0) {
					for (int i = 0; i < TokenList.get(secIndex).getSize(); i++) {
						sectionSize += TokenList.get(secIndex).getToken(i).byteSize;
					}
					codeLine = "H" + currentToken.label + "\t"
							+ String.format("%06X%06X", currentToken.location, sectionSize);
					codeList.add(codeLine);
				}

				/* define */
				else if (currentToken.operator != null && currentToken.operator.equals("EXTDEF")) {
					codeLine = "D";

					String[] defToken = currentToken.operand[0].split(",");
					for (int i = 0; i < defToken.length; i++) {
						codeLine = codeLine + defToken[i]
								+ String.format("%06X", symtabList.get(secIndex).search(defToken[i]));
					}
					codeList.add(codeLine);
				}

				/* refer */
				else if (currentToken.operator != null && currentToken.operator.equals("EXTREF")) {
					codeLine = "R";

					String[] refToken = currentToken.operand[0].split(",");
					for (int i = 0; i < refToken.length; i++) {
						codeLine = codeLine + refToken[i];
					}
					codeList.add(codeLine);
				}

				/* text */
				else if (currentToken.objectCode != null) {
					int startAddress = currentToken.location;
					int size;
					codeLine = "T";

					String obCodes = new String();
					for (size = 0; size < 32;) {
						if (tokenIndex >= TokenList.get(secIndex).getSize()) {
							break;
						}
						if ((size + TokenList.get(secIndex).getToken(tokenIndex).byteSize) > 31) {
							tokenIndex--;
							break;
						}
						if (TokenList.get(secIndex).getToken(tokenIndex).operator.equals("RESW")
								|| TokenList.get(secIndex).getToken(tokenIndex).operator.equals("RESB")) {
							break;
						}
						if (TokenList.get(secIndex).getToken(tokenIndex).objectCode != null)
							if (TokenList.get(secIndex).getToken(tokenIndex).objectCode.length() == 5) {
								TokenList.get(secIndex).getToken(tokenIndex).objectCode = "0"
										+ TokenList.get(secIndex).getToken(tokenIndex).objectCode;
							}
						obCodes = obCodes + String.format("%s",
								TokenList.get(secIndex).getToken(tokenIndex).objectCode.toUpperCase());
						size += TokenList.get(secIndex).getToken(tokenIndex).byteSize;
						tokenIndex++;
					}

					codeLine = codeLine + String.format("%06X", startAddress) + String.format("%02X", size)
							+ obCodes.toUpperCase();
					codeList.add(codeLine);
				}
			}

			/* modification */
			for (int modIndex = 0; modIndex < modifyList.get(secIndex).getListSize(); modIndex++) {
				codeLine = "M" + String.format("%06X", modifyList.get(secIndex).getAddress(modIndex))
						+ String.format("%02X", modifyList.get(secIndex).getModifyPoint(modIndex))
						+ modifyList.get(secIndex).getSymbol(modIndex);
				codeList.add(codeLine);
			}

			/* End */
			if (endFlag == 1) {
				codeLine = "E" + String.format("%06X", symtabList.get(secIndex).getAddress(0));
				codeList.add(codeLine);
			} else {
				codeLine = "E";
				codeList.add(codeLine);
			}
			codeList.add(" ");
		}
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * 
	 * @param inputFile
	 *            : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);

			String line;
			while ((line = bufReader.readLine()) != null) {
				lineList.add(line);
			}
			bufReader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
