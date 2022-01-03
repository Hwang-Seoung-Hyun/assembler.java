import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
/**
 * Assembler: 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인루틴이다. 프로그램의 수행 작업은 다음과 같다.
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다.
 * 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다
 * 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1)
 * 
 * 4) 분석된 내용을바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 * 작성중의 유의사항:
 * 
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.
 * 
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨
 * 
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 * 
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<LabelTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LabelTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. 필요한 경우 String 대신 별도의 클래스를
	 * 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	/** 프로그램 section별로 프로그램 크기를 저장하는 공간*/
	ArrayList<Integer> progLength;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<LabelTable>();
		literaltabList = new ArrayList<LabelTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
		//...
		progLength=new ArrayList<Integer>();
		
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20180637");
		assembler.printLiteralTable("literaltab_20180637");
		assembler.pass2();
		assembler.printObjectCode("output_20180637");

	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		// TODO Auto-generated method stub
		String inputF="./";
		inputF= inputF.concat(inputFile);
		
		try {
		BufferedReader br= new BufferedReader(new FileReader(inputF));
		String line;
		while((line=br.readLine())!=null)
		lineList.add(line);
		
		br.close();
		}
		catch(Exception e){
			System.err.println(inputF);
		}
		
	}

	/**
	 * pass1 과정을 수행한다.
	 * 
	 * 1) 프로그램 소스를 스캔하여 토큰 단위로 분리한 뒤 토큰 테이블을 생성.
	 * 
	 * 2) symbol, literal 들을 SymbolTable, LiteralTable에 정리.
	 * 
	 * 주의사항: SymbolTable, LiteralTable, TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		int location=0;
		int startAddress=0;
		int tokenNum=0;
		int linenum=0;
		String label;
		String line;
		Token token;
		LabelTable symTable = new LabelTable();
		LabelTable litTable = new LabelTable();
		TokenTable tokenTable = new TokenTable(symTable,litTable,instTable);
		////////////////////////////////////
		///////parsing first line///////////
		////////////////////////////////////
		line=lineList.get(linenum);
		tokenTable.putToken(line);
		token=tokenTable.getToken(tokenNum++);
		label=token.label;
		if(token.operator.equals("START")){
			startAddress=Integer.parseInt(token.operand[0]);
			location=startAddress;
			symTable.putName(label, location);
		}
		for(linenum=1;linenum<lineList.size();linenum++) {
			
			line=lineList.get(linenum);
			
			tokenTable.putToken(line);
			token=tokenTable.getToken(tokenNum++);
			label=token.label;
			token.location=location;
			
			if(!label.equals(".")) {//if not comment
				if(token.operator.equals("CSECT")){
					//add last section 
					progLength.add(location-startAddress);
					symtabList.add(symTable);
					literaltabList.add(litTable);
					TokenList.add(tokenTable);
					
					//initial new section
					symTable = new LabelTable();
					litTable = new LabelTable();
					tokenTable = new TokenTable(symTable,litTable,instTable);
					tokenTable.putToken(line);
					startAddress=0;
					location=0;
					tokenNum=1;
					
				}
				/////////////////////////////
				////////symbol table/////////
				/////////////////////////////
				if(!label.equals("")){
					if(symTable.search(label)<0)
						symTable.putName(label, location);
					else
						System.err.println("twice define symbol: "+label);
				}
				/////////////////////////////
				////////literal table////////
				/////////////////////////////
				try {
					if(token.operand[0].charAt(0)=='='){
						
						if(litTable.search(token.operand[0])<0)
							litTable.putName(token.operand[0], location);
						else
							litTable.modifyName(token.operand[0], location);
					}
				}
				catch(Exception e) {
					
				}
				/////////////////////////////
				///////////EXTDEF////////////
				/////////////////////////////
				if(token.operator.equals("EXTDEF")){
					String extDef[]=new String[3];
					for(int i=0;i<token.operand.length;i++) {
						extDef[i]=token.operand[i];
						symTable.extDef.add(extDef[i]);
					}
				}
				/////////////////////////////
				///////////EXTDEF////////////
				/////////////////////////////
				else if(token.operator.equals("EXTREF")){
					String extRef[]=new String[3];
					for(int i=0;i<token.operand.length;i++) {
						extRef[i]=token.operand[i];
						symTable.extRef.add(extRef[i]);
					}
				}
				/////////////////////////////
				else if(token.operator.equals("RESW")){
					location+=3*Integer.parseInt(token.operand[0]);
				}
				else if(token.operator.equals("RESB")){
					location+=Integer.parseInt(token.operand[0]);
				}
				else if(token.operator.equals("EQU")){
					if(!token.operand[0].equals("*")) {
						if(token.operand[0].contains("-")) {
							String operand[]=token.operand[0].split("-");
							symTable.modifyName(label, symTable.search(operand[0])-symTable.search(operand[1]));
						}	
						
					}
				}//end if EQU
				else if(token.operator.equals("WORD")){
					location+=3;
				}
				else if(token.operator.equals("BYTE")){
					if(token.operand[0].charAt(0)=='X')
						location+=(token.operand[0].length()-3+1)/2;
					else if(token.operand[0].charAt(0)=='C')
						location+=token.operand[0].length()-3;
				}
				else if(token.operator.equals("LTORG")){
					for(int i=0;i<litTable.label.size();i++) {
						line="*\t".concat(litTable.label.get(i));
						tokenTable.putToken(line);
						litTable.modifyName(litTable.label.get(i), location);
						if(litTable.label.get(0).charAt(1)=='C')
							location+=litTable.label.get(0).length()-4;
						else if(litTable.label.get(0).charAt(1)=='X')
							location+=(litTable.label.get(0).length()-4+1)/2;
						else {
							location+=3;
						}
						tokenNum++;
					}
					
				}//LTORG
				else if(token.operator.equals("CSECT")){
					
				}
				else if(token.operator.equals("END")) {
					//flush literal table
					for(int i=0;i<litTable.label.size();i++) {
						line="*\t".concat(litTable.label.get(i));
						tokenTable.putToken(line);
						litTable.modifyName(litTable.label.get(i), location);
						if(litTable.label.get(0).charAt(1)=='C')
							location+=litTable.label.get(0).length()-4;
						else if(litTable.label.get(0).charAt(1)=='X')
							location+=(litTable.label.get(0).length()-4+1)/2;
						tokenNum++;
					}
					//add last section 
					progLength.add(location-startAddress);
					symtabList.add(symTable);
					literaltabList.add(litTable);
					TokenList.add(tokenTable);
					
				}
				else {//opcode
					Instruction inst;
					if(instTable.containInst(token.operator)) 
						location+=instTable.get(token.operator).format;
					
					else {
						if(!label.equals("*"))
							System.err.println("Non-existent operator: "+token.operator);
						
					}
				}
				
			}
			
			
		}//end for lineList.size()
		int a=1;

	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		// TODO Auto-generated method stub
		String outputF="./";
		outputF= outputF.concat(fileName);
		
		try {
			PrintWriter pw= new PrintWriter(outputF);
			for(int i=0;i<symtabList.size();i++) {
				for(int line=0;line<symtabList.get(i).label.size();line++) {
					/*pw.print(symtabList.get(i).label.get(line)+"\t");
					pw.println(symtabList.get(i).search(symtabList.get(i).label.get(line)));
					*/
					String label = symtabList.get(i).label.get(line);
					pw.print(label +"\t\t");
					int address= symtabList.get(i).search(label);
					pw.println(Integer.toHexString(address).toUpperCase());
				}
				pw.println("");
				//System.out.println("");
			}
			
			
			pw.close();
		}
		catch(Exception e){
			System.err.println(e + outputF);
		}
		

	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		// TODO Auto-generated method stub
		String outputF="./";
		outputF=outputF.concat(fileName);
		try {
			PrintWriter pw= new PrintWriter(outputF);
			for(int i=0;i<literaltabList.size();i++) {
				for(int line=0;line<literaltabList.get(i).label.size();line++) {
					String label=literaltabList.get(i).label.get(line);
					String literal[]=label.split("'");
					pw.print(literal[1]+"\t\t");
					pw.println(Integer.toHexString(literaltabList.get(i).search(label)).toUpperCase());
				}
				
			}
			pw.close();
		}
		catch(Exception e) {
			
		}

	}

	/**
	 * pass2 과정을 수행한다.
	 * 
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		// TODO Auto-generated method stub
		int startAddress=0;
		int tokenline=0;
		int tokennum=0;
		int linenum=0;
		TokenTable tokenTable=TokenList.get(tokennum);
		if(tokenTable.getToken(tokenline).operator.equals("START")) {

		}
		for(tokenline=0;tokenline<tokenTable.tokenList.size();tokenline++) {
			Token token=tokenTable.getToken(tokenline);
			
			if(!token.label.equals(".")) {
				if(token.operator.equals("CSECT")) {
					tokennum++;
					tokenline=1;
					tokenTable=TokenList.get(tokennum);
				}//if operator = CESCT
				else if(token.operator.equals("START")) {
					
				}
				else if(token.operator.equals("RESW")) {
					
				}
				else if(token.operator.equals("RESB")) {
					
				}
				else if(token.operator.equals("EXTDEF")) {
					
				}
				else if(token.operator.equals("EXTREF")) {
					
				}
				else if(token.operator.equals("EQU")) {
					
				}
				//else if(token.operator.equals("END")) {
					
				//}
				else {
					tokenTable.makeObjectCode(tokenline);
					//location+=token.byteSize;
				}
					
				codeList.add(tokenTable.getObjectCode(tokenline));
			}//not commend line
			
		}//for not end
		/////////////////////////////
		/////flush literal table/////
		/////////////////////////////
		try {
			tokenTable.makeObjectCode(tokenline);
			codeList.add(tokenTable.getObjectCode(tokenline));
		}
		catch(Exception e) {
			
		}

	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		// TODO Auto-generated method stub
		try {
			fileName="./".concat(fileName);
			PrintWriter pw= new PrintWriter(fileName);
			int codeLine=1;
			for(int tokenNum=0;tokenNum<this.TokenList.size();tokenNum++) {
				String buf;
				String bufSize;
				TokenTable tokenTable=this.TokenList.get(tokenNum);
				////////////////////
				//////H Record//////
				////////////////////
				buf="H"+tokenTable.symTab.label.get(0);
				if(tokenTable.symTab.label.get(0).length()<6)
					buf=buf.concat("\t");
				
				/**start address**/
				String startAddress=Integer.toHexString(tokenTable.symTab.locationList.get(0));
				int k=6-startAddress.length();
				for(int i=0;i<k;i++)
					startAddress="0".concat(startAddress);
				buf=buf.concat(startAddress);
				
				/**program size;**/
				String progSize=Integer.toHexString(this.progLength.get(tokenNum));
				k=6-progSize.length();
				for(int i=0;i<k;i++)
					progSize="0".concat(progSize);
				buf=buf.concat(progSize);
				
				//codeList.add(buf.toUpperCase());
				pw.println(buf.toUpperCase());

				
				/////////////////////
				//////D Record///////
				/////////////////////
				buf="D";
				if(tokenTable.symTab.extDef.size()>0) {
					for(int i=0;i<tokenTable.symTab.extDef.size();i++) {
						String label=tokenTable.symTab.extDef.get(i);
						buf=buf.concat(label);
						if(label.length()<6)
							buf=buf.concat("\t");
						String address=Integer.toHexString(tokenTable.symTab.search(label));
						int m=6-address.length();
						for(int t=0;t<m;t++)
							address="0".concat(address);
						buf=buf.concat(address);
						
					}
					//codeList.add(buf.toUpperCase());
					pw.println(buf.toUpperCase());

				}
				/////////////////////
				//////R Record///////
				/////////////////////
				buf="R";
				if(tokenTable.symTab.extRef.size()>0) {
					for(int i=0;i<tokenTable.symTab.extRef.size();i++) {
						String label=tokenTable.symTab.extRef.get(i);
						buf=buf.concat(label);
						if(label.length()<6)
							buf=buf.concat("\t");
					}
					//codeList.add(buf.toUpperCase());
					pw.println(buf.toUpperCase());
				}
				
				int tokenLine;
				for(tokenLine=1;tokenLine<tokenTable.tokenList.size();tokenLine++) {
					Token token=tokenTable.tokenList.get(tokenLine);
					
					
					
					/////////////////////
					//////T Record///////
					/////////////////////
					if(!token.label.equals(".")) {
						if(codeList.get(codeLine)!=null) {
							if(buf.charAt(0)!='T') {
								buf="T";
								startAddress=Integer.toHexString(token.location);
								k=6-startAddress.length();
								for(int i=0;i<k;i++)
									startAddress="0".concat(startAddress);
								buf=buf.concat(startAddress+"XX");
								
							}
							if(buf.length()+token.byteSize>69) {
								//flush buffer
								bufSize=Integer.toHexString((buf.length()-9)/2);
								if(bufSize.length()==1)
									bufSize="0".concat(bufSize);
								buf=buf.replaceFirst("XX",bufSize);
								//codeList.add(buf.toUpperCase());
								pw.println(buf.toUpperCase());
								
								//initial buffer
								buf="T";
								startAddress=Integer.toHexString(token.location);
								k=6-startAddress.length();
								for(int i=0;i<k;i++)
									startAddress="0".concat(startAddress);
								buf=buf.concat(startAddress+"XX");
							}
							buf=buf.concat(codeList.get(codeLine));
						}
						else {
							if(buf.length()>9&&buf.charAt(0)=='T') {//buffer에 objectcode 존재
								//flush buffer
								bufSize=Integer.toHexString((buf.length()-9)/2);
								if(bufSize.length()==1)
									bufSize="0".concat(bufSize);
								buf=buf.replaceFirst("XX",bufSize);
								//codeList.add(buf.toUpperCase());
								pw.println(buf.toUpperCase());
								buf="X";
							}
							
							else {//buffer에 objectcode 없음
								
							}
							
						}
						codeLine++;
					}
				}//for section
				////////////////////////////
				////////flush buffer////////
				////////////////////////////
				if((buf.length()-9)/2>0){
					bufSize=Integer.toHexString((buf.length()-9)/2);
					if(bufSize.length()==1)
						bufSize="0".concat(bufSize);
					buf=buf.replaceFirst("XX",bufSize);
					//codeList.add(buf.toUpperCase());
					pw.println(buf.toUpperCase());
				}
				/////////////////////////////
				
				/////////////////////
				//////M Record///////
				/////////////////////
			
				for(int i=0;i<tokenTable.mRecord.size();i++) {
					buf="M";
					buf=buf.concat(tokenTable.mRecord.get(i));
					//codeList.add(buf);
					pw.println(buf.toUpperCase());
				}	
				
				/////////////////////
				//////E Record///////
				/////////////////////
				buf="E";
				if(tokenNum==0) {
					startAddress=Integer.toHexString(tokenTable.symTab.locationList.get(0));
					k=6-startAddress.length();
					for(int i=0;i<k;i++)
						startAddress="0".concat(startAddress);
					buf=buf.concat(startAddress);
				}
				//codeList.add(buf.toUpperCase()+"\n");
				pw.println(buf.toUpperCase()+'\n');
					
				
			}//for tokenList
			
			
			
			pw.close();
		}
		catch(Exception e) {
			System.err.println(fileName+e);
		}
		


	}

}
