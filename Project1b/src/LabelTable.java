import java.util.ArrayList;

/**
 * symbol, literal�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LabelTable {
	ArrayList<String> label;
	ArrayList<Integer> locationList;
	// external ���� �� ó������� �����Ѵ�.
	ArrayList<String> extDef;
	ArrayList<String> extRef;
	
	
	/**
	 * ���ο� symbol�� literal�� table�� �߰��Ѵ�.
	 * 
	 * @param label    : ���� �߰��Ǵ� symbol Ȥ�� literal�� lable
	 * @param location : �ش� symbol Ȥ�� literal�� ������ �ּҰ� ���� : ���� �ߺ��� symbol, literal��
	 *                 putName�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. ��Ī�Ǵ� �ּҰ��� ������
	 *                 modifylable()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putName(String label, int location) {
		
		this.label.add(label);
		this.locationList.add(location);
	}

	/**
	 * ������ �����ϴ� symbol, literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param lable       : ������ ���ϴ� symbol, literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyName(String lable, int newLocation) {
		int index = this.label.indexOf(lable);
		this.locationList.set(index, newLocation);
	}

	/**
	 * ���ڷ� ���޵� symbol, literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param label : �˻��� ���ϴ� symbol Ȥ�� literal�� label
	 * @return address: ������ �ִ� �ּҰ�. �ش� symbol, literal�� ���� ��� -1 ����
	 */
	public int search(String label) {
		int address = 0;
		// ...
		int index;
		if((index = this.label.indexOf(label))>=0) {
			Integer obj=this.locationList.get(index);
			address=obj.intValue();
		}
		else 
			address=-1;
		return address;
	}
	
	//�߰� ����
	/**
	 * 
	 * �ɹ� ���� ArrayList�� ��ü�� �����Ѵ�.
	 * 
	 */
	public LabelTable() {
		label = new ArrayList<String>() ;
		locationList = new ArrayList<Integer>() ;
		extDef = new ArrayList<String>();
		extRef = new ArrayList<String>();
		
	}

}
