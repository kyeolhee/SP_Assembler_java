import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다. section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	/* literal을 저장할 때 사용한다 */
	ArrayList<Integer> literalValue;
	/* literal을 저장할 때 사용한다 */
	ArrayList<Integer> literalSize;
	/* modification을 저장할 때 사용한다 */
	ArrayList<Integer> modifyPointList;

	public SymbolTable() {
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		literalValue = new ArrayList<>();
		literalSize = new ArrayList<>();
		modifyPointList = new ArrayList<>();
	}

	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * 
	 * @param symbol
	 *            : 새로 추가되는 symbol의 label
	 * @param location
	 *            : 해당 symbol이 가지는 주소값 <br>
	 *            <br>
	 *            주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다.
	 *            매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location) {
		if (!symbolList.contains(symbol)) {
			symbolList.add(symbol);
			locationList.add(location);
		}
	}

	/**
	 * 새로운 literal을 table에 추가한다.
	 * 
	 * @param symbol
	 *            : 새로 추가되는 literal의 label
	 * @param location
	 *            : 해당 literal의 주소값 <br>
	 * @param value
	 *            : 해당 literal의 값
	 * @param size
	 *            : 해당 literal의 크기 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램
	 *            코드에 문제가 있음을 나타낸다. 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
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
	 * 새로운 modification을 table에 추가한다.
	 * 
	 * @param symbol
	 *            : 새로 추가되는 modification의 label
	 * @param location
	 *            : 해당 modification이 호출된 주소값 <br>
	 * @param modifyPoint
	 *            : 해당 modification이 들어갈 bit 위치
	 */
	public void putModifySymbol(String symbol, int location, int modifyPoint) {
		symbolList.add(symbol);
		locationList.add(location);
		modifyPointList.add(modifyPoint);
	}

	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * 
	 * @param symbol
	 *            : 변경을 원하는 symbol의 label
	 * @param newLocation
	 *            : 새로 바꾸고자 하는 주소값
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
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다.
	 * 
	 * @param symbol
	 *            : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
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
	 * 인자로 전달된 index에 해당하는 symbol의 이름을 반환한다.
	 * 
	 * @param index
	 *            : 검색을 원하는 symbol의 index
	 */
	public String getSymbol(int index) {
		return symbolList.get(index);
	}

	/**
	 * 인자로 전달된 index에 해당하는 symbol의 주소을 반환한다.
	 * 
	 * @param index
	 *            : 검색을 원하는 symbol의 index
	 */
	public int getAddress(int index) {
		return locationList.get(index);
	}

	/**
	 * 해당하는 symbolTable의 List수를 반환한다.
	 */
	public int getListSize() {
		return symbolList.size();
	}

	/**
	 * 인자로 전달된 index에 해당하는 literal의 value를 반환한다.
	 * 
	 * @param index
	 *            : 검색을 원하는 literal의 index
	 */
	public int getLiteralValue(int index) {
		return literalValue.get(index);
	}

	/**
	 * 인자로 전달된 index에 해당하는 literal의 크기를 반환한다.
	 * 
	 * @param index
	 *            : 검색을 원하는 literal의 index
	 */
	public int getLiteralSize(int index) {
		return literalSize.get(index);
	}

	/**
	 * 인자로 전달된 index에 해당하는 modification의 modifyPoint를 반환한다.
	 * 
	 * @param index
	 *            : 검색을 원하는 modification의 index
	 */
	public int getModifyPoint(int index) {
		return modifyPointList.get(index);
	}
}
