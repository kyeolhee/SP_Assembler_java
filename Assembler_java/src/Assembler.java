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
 * Assembler : 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다. 프로그램의 수행 작업은 다음과
 * 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br>
 * <br>
 * 작성중의 유의사항 : <br>
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.<br>
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br>
 * <br>
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/** 프로그램의 section별로 literal을 저장하는 공간 */
	ArrayList<SymbolTable> literalList;
	/** 프로그램의 section별로 external reference을 저장하는 공간 */
	ArrayList<SymbolTable> externalList;
	/** 프로그램의 section별로 modification을 저장하는 공간 */
	ArrayList<SymbolTable> modifyList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	int numSection;
	int loccount;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile
	 *            : instruction 명세를 작성한 파일 이름.
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
	 * 어셈블러의 메인 루틴
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		try {
			File file = new File(fileName);
			FileWriter filewriter = new FileWriter(file);

			for (int i = 0; i < codeList.size(); i++) {
				filewriter.write(codeList.get(i));
				filewriter.write("\r\n"); // filewirter.write("\n");하면 개행이 깨짐

			}
			filewriter.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * 
	 * @param fileName
	 *            : 저장되는 파일 이름
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
				filewriter.write("\r\n"); // filewirter.write("\n");하면 개행이 깨짐
			}
			filewriter.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass1 과정을 수행한다.<br>
	 * 1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 * 2) label을 symbolTable에 정리<br>
	 * <br>
	 * <br>
	 * 주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		String line;
		int TokenIndex = 0;
		Token ParsedToken;

		// TODO Auto-generated method stub
		for (int i = 0; i < lineList.size(); i++) {
			line = lineList.get(i);

			/* START 가 들어올 시에 모든 List를 초기화해준다 */
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
			/* CSECT 가 들어올 시에 새로운 section이 시작되므로 새로운 list를 만들어준다 */
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

			/* 입력받은 line을 token으로 나누어 tokenList에 저장 */
			TokenList.get(numSection).putToken(line, loccount);
			/* 바로 전에 parsing한 token */
			ParsedToken = TokenList.get(numSection).getToken(TokenIndex);

			/* symbol table 저장 - symbol명과 location */
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

			/* literal table 저장 - literal명과 location(0으로 통일), literal의 value와 크기 */
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

			/* literal table에 저장된 literal에 주소 저장 - 0으로 통일한 주소를 알맞은 주소로 수정 */
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

			/* externalList 저장 - external reference의 명 저장 */
			if (ParsedToken.operator != null && ParsedToken.operator.equals("EXTREF")) {
				String[] exToken = ParsedToken.operand[0].split(",");

				for (int exIndex = 0; exIndex < exToken.length; exIndex++) {
					externalList.get(numSection).putSymbol(exToken[exIndex], 0);
				}
			}

			/*
			 * modifyList 저장 - externalList를 바탕으로 modification명, 호출되는 location, 수정할 address의
			 * 위치
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
	 * pass2 과정을 수행한다.<br>
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		String codeLine;
		Token currentToken;
		int sectionSize;
		int endFlag;

		/* token의 object code를 완성 */
		for (int secIndex = 0; secIndex <= numSection; secIndex++) {
			for (int tokenIndex = 0; tokenIndex < TokenList.get(secIndex).getSize(); tokenIndex++) {
				TokenList.get(secIndex).makeObjectCode(tokenIndex);
			}
		}

		/*
		 * object code를 바탕으로 codelist를 작성한다 이때 section은 따로 나눠주지 않고 출력할 때 section이 바뀌면 빈
		 * 문장이 출력되도록 한다
		 */
		for (int secIndex = 0; secIndex <= numSection; secIndex++) {
			sectionSize = 0;
			endFlag = 0;
			for (int tokenIndex = 0; tokenIndex < TokenList.get(secIndex).getSize(); tokenIndex++) {
				codeLine = new String();
				currentToken = TokenList.get(secIndex).getToken(tokenIndex);

				/* 프로그램의 시작을 받은 경우 endFlag를 1로 하여 object program을 출력할 때 End record의 값을 출력 */
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
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * 
	 * @param inputFile
	 *            : input 파일 이름.
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
