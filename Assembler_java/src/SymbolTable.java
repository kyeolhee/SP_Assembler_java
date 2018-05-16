import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	/* literal�� ������ �� ����Ѵ� */
	ArrayList<Integer> literalValue;
	/* literal�� ������ �� ����Ѵ� */
	ArrayList<Integer> literalSize;
	/* modification�� ������ �� ����Ѵ� */
	ArrayList<Integer> modifyPointList;

	public SymbolTable() {
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		literalValue = new ArrayList<>();
		literalSize = new ArrayList<>();
		modifyPointList = new ArrayList<>();
	}

	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol
	 *            : ���� �߰��Ǵ� symbol�� label
	 * @param location
	 *            : �ش� symbol�� ������ �ּҰ� <br>
	 *            <br>
	 *            ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����.
	 *            ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) {
		if (!symbolList.contains(symbol)) {
			symbolList.add(symbol);
			locationList.add(location);
		}
	}

	/**
	 * ���ο� literal�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol
	 *            : ���� �߰��Ǵ� literal�� label
	 * @param location
	 *            : �ش� literal�� �ּҰ� <br>
	 * @param value
	 *            : �ش� literal�� ��
	 * @param size
	 *            : �ش� literal�� ũ�� ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷�
	 *            �ڵ忡 ������ ������ ��Ÿ����. ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String symbol, int location, String value, int size) {
		int litValue = 0;
		String litValue_s = new String();
		if (!symbolList.contains(symbol)) {
			if (symbol.charAt(0) == 'C') {
				for (int i = 0; i < value.length(); i++) {
					litValue_s = litValue_s + Integer.toHexString((int) value.charAt(i));
				}

				litValue = Integer.parseInt(litValue_s, 16);
			} else {
				litValue = Integer.parseInt(value);
			}

			symbolList.add(symbol);
			locationList.add(location);
			literalValue.add(litValue);
			literalSize.add(size);
		}
	}

	/**
	 * ���ο� modification�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol
	 *            : ���� �߰��Ǵ� modification�� label
	 * @param location
	 *            : �ش� modification�� ȣ��� �ּҰ� <br>
	 * @param modifyPoint
	 *            : �ش� modification�� �� bit ��ġ
	 */
	public void putModifySymbol(String symbol, int location, int modifyPoint) {
		symbolList.add(symbol);
		locationList.add(location);
		modifyPointList.add(modifyPoint);
	}

	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param symbol
	 *            : ������ ���ϴ� symbol�� label
	 * @param newLocation
	 *            : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				locationList.set(i, newLocation);
				break;
			}
		}
	}

	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param symbol
	 *            : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;

		for (int i = 0; i < symbolList.size(); i++) {
			if (symbolList.get(i).equals(symbol)) {
				address = locationList.get(i);
				return address;
			}
		}

		return -1;
	}

	/**
	 * ���ڷ� ���޵� index�� �ش��ϴ� symbol�� �̸��� ��ȯ�Ѵ�.
	 * 
	 * @param index
	 *            : �˻��� ���ϴ� symbol�� index
	 */
	public String getSymbol(int index) {
		return symbolList.get(index);
	}

	/**
	 * ���ڷ� ���޵� index�� �ش��ϴ� symbol�� �ּ��� ��ȯ�Ѵ�.
	 * 
	 * @param index
	 *            : �˻��� ���ϴ� symbol�� index
	 */
	public int getAddress(int index) {
		return locationList.get(index);
	}

	/**
	 * �ش��ϴ� symbolTable�� List���� ��ȯ�Ѵ�.
	 */
	public int getListSize() {
		return symbolList.size();
	}

	/**
	 * ���ڷ� ���޵� index�� �ش��ϴ� literal�� value�� ��ȯ�Ѵ�.
	 * 
	 * @param index
	 *            : �˻��� ���ϴ� literal�� index
	 */
	public int getLiteralValue(int index) {
		return literalValue.get(index);
	}

	/**
	 * ���ڷ� ���޵� index�� �ش��ϴ� literal�� ũ�⸦ ��ȯ�Ѵ�.
	 * 
	 * @param index
	 *            : �˻��� ���ϴ� literal�� index
	 */
	public int getLiteralSize(int index) {
		return literalSize.get(index);
	}

	/**
	 * ���ڷ� ���޵� index�� �ش��ϴ� modification�� modifyPoint�� ��ȯ�Ѵ�.
	 * 
	 * @param index
	 *            : �˻��� ���ϴ� modification�� index
	 */
	public int getModifyPoint(int index) {
		return modifyPointList.get(index);
	}
}
