import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ� ���� instruction ���� ����,
 * ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/**
	 * inst.data ������ �ҷ��� �����ϴ� ����. ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * 
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
		// ...
		String inputF="./";
		inputF= inputF.concat(fileName);
		try {
			BufferedReader br= new BufferedReader(new FileReader(inputF));
			String line;
			while((line=br.readLine())!=null) {
				Instruction inst= new Instruction(line);
				instMap.put(inst.instruction,inst);
				
			}
			
			br.close();
			}
			catch(Exception e){
				System.err.println(inputF);
			}
		
	}

	// get, set, search ���� �Լ��� ���� ����
	public boolean containInst(String instruction) {
		return instMap.containsKey(instruction);
	}
	public Instruction get(String instruction) {
		return instMap.get(instruction);
	}

}

/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����. instruction�� ���õ� ������ �����ϰ� �������� ������
 * �����Ѵ�.
 */
class Instruction {

	/*
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */
	String instruction; int opcode; int numberOfOperand;

	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;

	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * 
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		String instToken[]=line.split(",");
		instruction=instToken[0];
		format=Integer.parseInt(instToken[1]);
		opcode=Integer.decode("0x".concat(instToken[2]));
		numberOfOperand=Integer.parseInt(instToken[3]);
		
	}

	// �� �� �Լ� ���� ����

}
