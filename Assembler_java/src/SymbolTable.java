import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	ArrayList<Integer> literalValue;
	ArrayList<Integer> literalSize;
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

	public String getSymbol(int index) {
		return symbolList.get(index);
	}

	public int getAddress(int index) {
		return locationList.get(index);
	}

	public int getListSize() {
		return symbolList.size();
	}

	public int getLiteralValue(int index) {
		return literalValue.get(index);
	}

	public int getLiteralSize(int index) {
		return literalSize.get(index);
	}
	
	public int getModifyPoint (int index) {
		return modifyPointList.get(index);
	}
	public void putLiteral(String symbol, int location, String value, int size) {
		int litValue = 0;
		String litValue_s = new String();
		if (!symbolList.contains(symbol)) {
			if (symbol.charAt(0) == 'C') {
				for (int i = 0; i < value.length(); i++) {
					litValue_s = litValue_s + Integer.toHexString((int) value.charAt(i));
				}

				litValue = Integer.parseInt(litValue_s, 16);
			}
			else {
				litValue = Integer.parseInt(value);
			}

			symbolList.add(symbol);
			locationList.add(location);
			literalValue.add(litValue);
			literalSize.add(size);
		}
	}

	public void putModifySymbol(String symbol, int location, int modifyPoint) {
		symbolList.add(symbol);
		locationList.add(location);
		modifyPointList.add(modifyPoint);
	}
}
