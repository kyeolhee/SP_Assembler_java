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

	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
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
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
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
		// ...
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

					if (operand != null && operand.length == 2 && operand[1].equals("X")) {
						setFlag(TokenTable.xFlag, 1);
					}
					byteSize = 4;
				}

				/* 4������ �ƴ� inst�� ��� */
				else if (instTable.getOpcode(operator) >= 0) {
					/* x ���� */
					if (operand != null && operand.length == 2 && operand[1].equals("X")) {
						setFlag(TokenTable.xFlag, 1);
					}

					/* n, i, b, p, e ���� */
					/* 3������ ��� */
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
