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

	/* register 선언 */
	public static final int A_Reg = 0;
	public static final int X_Reg = 1;
	public static final int L_Reg = 2;
	public static final int B_Reg = 3;
	public static final int S_Reg = 4;
	public static final int T_Reg = 5;
	public static final int F_Reg = 6;
	public static final int PC_Reg = 8;
	public static final int SW_Reg = 9;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	SymbolTable literalTab;
	SymbolTable externalTab;
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
	public TokenTable(SymbolTable symTab, SymbolTable literalTab, SymbolTable externalTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.externalTab = externalTab;
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
		int op_ni;
		int xbpe;
		int address;

		if (tokenList.get(index).label != null) {
			/* 4형식의 경우 */
			if (tokenList.get(index).operator.charAt(0) == '+') {
				op_ni = instTab.getOpcode(tokenList.get(index).operator.substring(1))
						+ tokenList.get(index).getFlag(nFlag) / 16 + tokenList.get(index).getFlag(iFlag) / 16;
				xbpe = tokenList.get(index).getFlag(xFlag) + tokenList.get(index).getFlag(bFlag)
						+ tokenList.get(index).getFlag(pFlag) + tokenList.get(index).getFlag(eFlag);
				tokenList.get(index).objectCode = Integer.toHexString(op_ni) + Integer.toHexString(xbpe) + "00000";
			}

			else if (tokenList.get(index).operator.equals("END")) {
				for (int litIndex = 0; litIndex < literalTab.getListSize(); litIndex++) {
					if (literalTab.getLiteralSize(litIndex) == 3) {
						tokenList.get(index).objectCode = String.format("%06X", literalTab.getLiteralValue(litIndex));
						tokenList.get(index).byteSize = literalTab.getLiteralSize(litIndex);
					} else {
						tokenList.get(index).objectCode = String.format("%02X", literalTab.getLiteralValue(litIndex));
						tokenList.get(index).byteSize = literalTab.getLiteralSize(litIndex);
					}
				}
			}
			/* 3형식의 경우 */
			else if (instTab.getFormat(tokenList.get(index).operator) == 3) {
				op_ni = instTab.getOpcode(tokenList.get(index).operator)
						+ (tokenList.get(index).getFlag(nFlag) + tokenList.get(index).getFlag(iFlag)) / 16;
				xbpe = tokenList.get(index).getFlag(xFlag) + tokenList.get(index).getFlag(bFlag)
						+ tokenList.get(index).getFlag(pFlag) + tokenList.get(index).getFlag(eFlag);

				if (instTab.getNumberOfOperand(tokenList.get(index).operator) > 0) {
					if (tokenList.get(index).operand[0].charAt(0) == '#') {
						address = Integer.parseInt(tokenList.get(index).operand[0].substring(1));
					} else if (tokenList.get(index).operand[0].charAt(0) == '@') {
						address = 0;
					} else if (tokenList.get(index).operand[0].charAt(0) == '=') {
						address = literalTab.search(tokenList.get(index).operand[0].substring(1))
								- tokenList.get(index + 1).location;
					} else if (symTab.search(tokenList.get(index).operand[0]) >= 0) {
						address = symTab.search(tokenList.get(index).operand[0]) - tokenList.get(index + 1).location;
					} else {
						address = 0;
					}
				} else {
					address = 0;
				}

				String strAddress = Integer.toHexString(address);
				int addLength = strAddress.length();
				if (addLength > 3) {
					strAddress = strAddress.substring(addLength - 3);
				} else if (addLength < 3) {
					strAddress = String.format("%03X", address);
				}
				tokenList.get(index).objectCode = Integer.toHexString(op_ni) + Integer.toHexString(xbpe) + strAddress;
			}

			/* 1, 2 형식인 경우 */
			else if (instTab.getFormat(tokenList.get(index).operator) >= 0) {
				int register_1 = 0;
				int register_2 = 0;
				
				if (tokenList.get(index).operand[0].equals("A")) {
					register_1 = A_Reg;
				} else if (tokenList.get(index).operand[0].equals("X")) {
					register_1 = X_Reg;
				} else if (tokenList.get(index).operand[0].equals("L")) {
					register_1 = L_Reg;
				} else if (tokenList.get(index).operand[0].equals("B")) {
					register_1 = B_Reg;
				} else if (tokenList.get(index).operand[0].equals("S")) {
					register_1 = S_Reg;
				} else if (tokenList.get(index).operand[0].equals("T")) {
					register_1 = T_Reg;
				} else if (tokenList.get(index).operand[0].equals("F")) {
					register_1 = F_Reg;
				} else if (tokenList.get(index).operand[0].equals("PC")) {
					register_1 = PC_Reg;
				} else if (tokenList.get(index).operand[0].equals("SW")) {
					register_1 = SW_Reg;
				}

				if (tokenList.get(index).operand[1] != null) {
					if (tokenList.get(index).operand[1].equals("A")) {
						register_2 = A_Reg;
					} else if (tokenList.get(index).operand[1].equals("X")) {
						register_2 = X_Reg;
					} else if (tokenList.get(index).operand[1].equals("L")) {
						register_2 = L_Reg;
					} else if (tokenList.get(index).operand[1].equals("B")) {
						register_2 = B_Reg;
					} else if (tokenList.get(index).operand[1].equals("S")) {
						register_2 = S_Reg;
					} else if (tokenList.get(index).operand[1].equals("T")) {
						register_2 = T_Reg;
					} else if (tokenList.get(index).operand[1].equals("F")) {
						register_2 = F_Reg;
					} else if (tokenList.get(index).operand[1].equals("PC")) {
						register_2 = PC_Reg;
					} else if (tokenList.get(index).operand[1].equals("SW")) {
						register_2 = SW_Reg;
					}
				}
				tokenList.get(index).objectCode = Integer.toHexString(instTab.getOpcode(tokenList.get(index).operator))
						+ Integer.toHexString(register_1) + Integer.toHexString(register_2);
			} else if (tokenList.get(index).operator.equals("BYTE")) {
				int opLength = tokenList.get(index).operand[0].length();
				tokenList.get(index).objectCode = tokenList.get(index).operand[0].substring(2, opLength - 1);
			} else if (tokenList.get(index).operator.equals("WORD")) {
				for (int exIndex = 0; exIndex < externalTab.getListSize(); exIndex++) {
					if (tokenList.get(index).operand[0].contains(externalTab.getSymbol(exIndex))) {
						tokenList.get(index).objectCode = "000000";
					} else {
						int opLength = tokenList.get(index).operand[0].length();
						tokenList.get(index).objectCode = tokenList.get(index).operand[0].substring(2, opLength - 1);
					}
				}
			} else if (tokenList.get(index).operator.equals("LTORG")) {
				for (int litIndex = 0; litIndex < literalTab.getListSize(); litIndex++) {
					if (literalTab.getLiteralSize(litIndex) == 3) {
						tokenList.get(index).objectCode = String.format("%06X", literalTab.getLiteralValue(litIndex));
					} else {
						tokenList.get(index).objectCode = String.format("%02X", literalTab.getLiteralValue(litIndex));
					}
				}
			}
		}
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

	public int getSize() {
		return tokenList.size();
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

					String[] operandToken = operand[0].split(",");
					if (operand != null && operandToken.length == 2 && operandToken[1].equals("X")) {
						setFlag(TokenTable.xFlag, 1);
					}
					byteSize = 4;
				}

				/* 4형식이 아닌 inst인 경우 */
				else if (instTable.getOpcode(operator) >= 0) {
					/* x 설정 */
					if (operand != null) {
						String[] operandToken = operand[0].split(",");
						if (operandToken.length == 2 && operandToken[1].equals("X")) {
							setFlag(TokenTable.xFlag, 1);
						}
					}

					/* n, i, b, p, e 설정 */
					/* 3형식인 경우 */
					if (getByteSize(operator, operand) == 3) {
						byteSize = 3;
						if (operand != null) {
							if (operand[0].charAt(0) == '@') {
								setFlag(TokenTable.nFlag, 1);
								setFlag(TokenTable.pFlag, 1);

							} else if (operand != null && operand[0].charAt(0) == '#') {
								setFlag(TokenTable.iFlag, 1);
							} else {
								setFlag(TokenTable.nFlag, 1);
								setFlag(TokenTable.iFlag, 1);
								setFlag(TokenTable.pFlag, 1);
							}
						}
						if (operator.equals("RSUB")) {
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
