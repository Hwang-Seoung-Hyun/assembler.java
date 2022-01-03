import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다 또한 instruction 관련 연산,
 * 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/**
	 * inst.data 파일을 불러와 저장하는 공간. 명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;

	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * 
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */

	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}

	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
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

	// get, set, search 등의 함수는 자유 구현
	public boolean containInst(String instruction) {
		return instMap.containsKey(instruction);
	}
	public Instruction get(String instruction) {
		return instMap.get(instruction);
	}

}

/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다. instruction과 관련된 정보를 저장하고 기초적인 연산을
 * 수행한다.
 */
class Instruction {

	/*
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 * 
	 * ex) String instruction; int opcode; int numberOfOperand; String comment;
	 */
	String instruction; int opcode; int numberOfOperand;

	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format;

	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}

	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * 
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		String instToken[]=line.split(",");
		instruction=instToken[0];
		format=Integer.parseInt(instToken[1]);
		opcode=Integer.decode("0x".concat(instToken[2]));
		numberOfOperand=Integer.parseInt(instToken[3]);
		
	}

	// 그 외 함수 자유 구현

}
