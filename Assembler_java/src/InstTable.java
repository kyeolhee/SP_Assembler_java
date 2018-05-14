import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;



/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap = new HashMap();
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			
			String instName;
			String line;
			StringTokenizer token;
			while ((line = bufReader.readLine()) != null) {
				token = new StringTokenizer(line);
				instName = token.nextToken();
				instMap.put(instName, new Instruction(line));
			}
			bufReader.close();
		}
		catch (FileNotFoundException e) {
			System.out.println(e);
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	//get, set, search ���� �Լ��� ���� ����
	public int getFormat(String instName) {
		int format;
		
		if (instMap.containsKey(instName)) {
			format = instMap.get(instName).format;
		}
		else
			format = -1;
		
		return format;
	}
	
	public int getOpcode (String instName) {
		int opcode;
		
		if (instMap.containsKey(instName)) {
			opcode = instMap.get(instName).opcode;
		}
		else
			opcode = -1;
		
		return opcode;
	}
	
	public int getNumberOfOperand (String instName) {
		int numberOfOperand;
		
		if (instMap.containsKey(instName)) {
			numberOfOperand = instMap.get(instName).numberOfOperand;
		}
		else
			numberOfOperand = -1;
		
		return numberOfOperand;
	}
}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	String instruction;
	int format;
	int opcode;
	int numberOfOperand;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		String[] token = line.split("\t");
		
		instruction = token[0];
		format = Integer.parseInt(token[1]);
		opcode = Integer.parseInt(token[2], 16);
		numberOfOperand = Integer.parseInt(token[3]);
	}
}
