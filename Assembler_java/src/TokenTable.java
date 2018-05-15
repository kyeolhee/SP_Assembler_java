import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ�
 * �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit ������ �������� ���� ���� */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* register ���� */
	public static final int A_Reg = 0;
	public static final int X_Reg = 1;
	public static final int L_Reg = 2;
	public static final int B_Reg = 3;
	public static final int S_Reg = 4;
	public static final int T_Reg = 5;
	public static final int F_Reg = 6;
	public static final int PC_Reg = 8;
	public static final int SW_Reg = 9;

	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	SymbolTable literalTab;
	SymbolTable externalTab;
	InstTable instTab;

	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * 
	 * @param symTab
	 *            : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab
	 *            : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, SymbolTable literalTab, SymbolTable externalTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.externalTab = externalTab;
		this.instTab = instTab;
	}

	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * 
	 * @param line
	 *            : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line, int loccount) {
		tokenList.add(new Token(line, instTab, loccount));
	}

	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * 
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 �������� ����Ѵ�. instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸�
	 * �����Ѵ�.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		int op_ni;
		int xbpe;
		int address;

		if (tokenList.get(index).label != null) {
			/* 4������ ��� */
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
			/* 3������ ��� */
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

			/* 1, 2 ������ ��� */
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
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
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ �� �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. �ǹ� �ؼ��� ������ pass2����
 * object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token {
	InstTable instTable;
	// �ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand = new String[3];
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������
	String objectCode;
	int byteSize;

	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * 
	 * @param line
	 *            ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line, InstTable instTable, int location) {
		// initialize �߰�
		this.instTable = instTable;
		this.location = location;
		parsing(line);
	}

	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * 
	 * @param line
	 *            ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		String[] token = line.split("\t");

		if (!(token[0].equals("."))) {
			label = token[0];
			operator = token[1];

			if (token.length > 2) {
				if (instTable.getNumberOfOperand(operator) == 0) {
					operand = null; // ���߿� ��������� ' '�� �ٲٱ�
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

				/* 4������ ��� nixbpe ���� */
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

				/* 4������ �ƴ� inst�� ��� */
				else if (instTable.getOpcode(operator) >= 0) {
					/* x ���� */
					if (operand != null) {
						String[] operandToken = operand[0].split(",");
						if (operandToken.length == 2 && operandToken[1].equals("X")) {
							setFlag(TokenTable.xFlag, 1);
						}
					}

					/* n, i, b, p, e ���� */
					/* 3������ ��� */
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

					/* 1, 2������ ��� */
					else {
						byteSize = getByteSize(operator, operand);
					}
				}

				/* instTable�� ���� ��� */
				else {
					byteSize = getByteSize(operator, operand);
				}
			}
		}
	}

	/**
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br>
	 * <br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 * �Ǵ� setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag
	 *            : ���ϴ� ��Ʈ ��ġ
	 * @param value
	 *            : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		if (value == 1) {
			nixbpe |= flag;
		} else {
			nixbpe ^= flag;
		}
	}

	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br>
	 * <br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 * �Ǵ� getFlag(nFlag|iFlag)
	 * 
	 * @param flags
	 *            : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
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
