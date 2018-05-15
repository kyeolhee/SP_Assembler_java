import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab
	 *            : 해당 section과 연결되어있는 symbol table
	 * @param instTab
	 *            : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.instTab = instTab;
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line
	 *            : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line, int loccount) {
		tokenList.add(new Token(line, instTab, loccount));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		// ...
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	InstTable instTable;
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand = new String[3];
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드
	 */
	public Token(String line, InstTable instTable, int location) {
		// initialize 추가
		this.instTable = instTable;
		this.location = location;
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line
	 *            문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String[] token = line.split("\t");

		if (!(token[0].equals("."))) {
			label = token[0];
			operator = token[1];

			if (token.length > 2) {
				if (instTable.getNumberOfOperand(operator) == 0) {
					operand = null; // 나중에 문제생기면 ' '로 바꾸기
				} else if (instTable.getNumberOfOperand(operator) > 0) {
					String[] opToken = token[2].split(",");

					for (int i = 0; i < instTable.getNumberOfOperand(operator); i++) {
						operand[i] = opToken[i];
					}
				} else if (instTable.getNumberOfOperand(operator) < 0) {
					operand[0] = token[2].toString();
				}
				if (token.length > 3) {
					comment = token[3];
				}

				/* 4형식인 경우 nixbpe 설정 */
				if (token[1].charAt(0) == '+') {
					setFlag(TokenTable.nFlag, 1);
					setFlag(TokenTable.iFlag, 1);
					setFlag(TokenTable.eFlag, 1);

					if (operand != null && operand.length == 2 && operand[1].equals("X")) {
						setFlag(TokenTable.xFlag, 1);
					}
					byteSize = 4;
				}

				/* 4형식이 아닌 inst인 경우 */
				else if (instTable.getOpcode(operator) >= 0) {
					/* x 설정 */
					if (operand != null && operand.length == 2 && operand[1].equals("X")) {
						setFlag(TokenTable.xFlag, 1);
					}

					/* n, i, b, p, e 설정 */
					/* 3형식인 경우 */
					else if (getByteSize(operator, operand) == 3) {
						byteSize = 3;
						if (operand != null) {
							if (operand[0].charAt(0) == '@') {
								setFlag(TokenTable.nFlag, 1);
								setFlag(TokenTable.pFlag, 1);

							} else if (operand != null && operand[0].charAt(0) == '#') {
								setFlag(TokenTable.iFlag, 1);
							}
						}

						else {
							setFlag(TokenTable.nFlag, 1);
							setFlag(TokenTable.iFlag, 1);
						}
					}

					/* 1, 2형식의 경우 */
					else {
						byteSize = getByteSize(operator, operand);
					}
				}

				/* instTable에 없는 경우 */
				else {
					byteSize = getByteSize(operator, operand);
				}
			}
		}
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다. <br>
	 * <br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 * 또는 setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag
	 *            : 원하는 비트 위치
	 * @param value
	 *            : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if (value == 1) {
			nixbpe |= flag;
		} else {
			nixbpe ^= flag;
		}
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br>
	 * <br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 * 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags
	 *            : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}

	public int getByteSize(String operator, String[] operand) {
		if (instTable.getFormat(operator) > 0) {
			return instTable.getFormat(operator);
		} else {
			if (operator.equals("RESW")) {
				return Integer.parseInt(operand[0]) * 3;
			} else if (operator.equals("RESB")) {
				return Integer.parseInt(operand[0]);
			} else if (operator.equals("BYTE")) {
				return 1;
			} else if (operator.equals("WORD")) {
				return 3;
			} else {
				return 0;
			}
		}
	}

}
