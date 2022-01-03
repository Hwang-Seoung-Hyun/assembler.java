import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
 * 
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다. section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	/**object program M Record를 저장하는 공간*/
	ArrayList<String> mRecord;
	
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab    : 해당 section과 연결되어있는 symbol table
	 * @param literaTab : 해당 section과 연결되어있는 literal table
	 * @param instTab   : instruction 명세가 정의된 instTable
	 */
	public TokenTable(LabelTable symTab, LabelTable literalTab, InstTable instTab) {
		// ...
		this.symTab=symTab;
		this.literalTab=literalTab;
		this.instTab=instTab;
		tokenList= new ArrayList<Token>();
		this.mRecord=new ArrayList<String>();
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
		// ...
		String objectCode="";
		Token token=getToken(index);
		
		////////////////////////////
		////////////WORD////////////
		////////////////////////////
		if(token.operator.equals("WORD")) {
			if(token.operand[0].contains("-")) {
				String operand[]=token.operand[0].split("-");
				for(int i=0;i<operand.length;i++) {
					if(this.symTab.search(operand[i])<0) {
						//EXTREF찾기 구현
						int ext=0;
						for(ext=0;ext<this.symTab.extRef.size();ext++) {
							if(this.symTab.extRef.get(ext).equals(operand[i]))
								break;
						}
						if(ext==this.symTab.extRef.size()) {
							System.err.println("non-exitent symbol : "+operand[i]);
						}
						else {
							if(i==0) {
								String location=Integer.toHexString(token.location);
								int k=6-location.length();
								for(int a=0;a<k;a++)
									location="0".concat(location);
								this.mRecord.add(location+"06"+"+"+operand[i]);
							}
							else if(i==1) {
								String location=Integer.toHexString(token.location);
								int k=6-location.length();
								for(int a=0;a<k;a++)
									location="0".concat(location);
								this.mRecord.add(location+"06"+"-"+operand[i]);
							}
							
							
							objectCode=objectCode.concat("000");
						}
					}
					
				}
				
			}
			
			
		}//end WORD
		///////////////////////
		
		else if(token.operator.equals("BYTE")) {
			String llabel[]=token.operand[0].split("'");
			if(token.operand[0].charAt(0)=='X') {
				token.byteSize=(llabel[1].length()+1)%2;
				
				objectCode=objectCode.concat(llabel[1]);
				
			}
			else if(token.operand[0].charAt(0)=='C') {
				token.byteSize=llabel[1].length();
				for(int k=0;k<token.byteSize;k++)
					objectCode=objectCode.concat(Integer.toHexString(llabel[1].charAt(k)));
			}
		}
		else if(token.label.equals("*")) {
			
				String label=token.operator;
				String llabel[]=label.split("'");
				if(label.charAt(1)=='X') {
					token.byteSize=(llabel[1].length()+1)%2;
					
					objectCode=objectCode.concat(llabel[1]);
					
				}	
				else if(label.charAt(1)=='C') {
					token.byteSize=llabel[1].length();
					for(int k=0;k<token.byteSize;k++)
						objectCode=objectCode.concat(Integer.toHexString(llabel[1].charAt(k)));
				}
				else {
					String literal=token.operator.substring(1);
					int iLiteral=Integer.parseInt(literal);
					literal=Integer.toHexString(iLiteral);
					for(int i=literal.length();i<6;i++)
						literal="0".concat((literal));
					objectCode=literal;
				}
				
		}
		///////////////////////////////
		/////////////opcode////////////
		///////////////////////////////
		else {
			if(this.instTab.containInst(token.operator)) {
				Instruction inst= this.instTab.get(token.operator);
				token.byteSize=inst.format;
				
				if(inst.format==1) {
					objectCode=Integer.toHexString(inst.opcode);
				}
				
				else if(inst.format==2) {
					objectCode=Integer.toHexString(inst.opcode);
					int i=0;
					try {
						for(i=0;i<2;i++) {
							if(token.operand[i].equals("A"))
								objectCode=objectCode.concat("0");
							else if(token.operand[i].equals("X"))
								objectCode=objectCode.concat("1");
							else if(token.operand[i].equals("L"))
								objectCode=objectCode.concat("2");
							else if(token.operand[i].equals("B"))
								objectCode=objectCode.concat("3");
							else if(token.operand[i].equals("S"))
								objectCode=objectCode.concat("4");
							else if(token.operand[i].equals("T"))
								objectCode=objectCode.concat("5");
							else if(token.operand[i].equals("F"))
								objectCode=objectCode.concat("6");
							else if(token.operand[i].equals("PC"))
								objectCode=objectCode.concat("8");
							else if(token.operand[i].equals("SW"))
								objectCode=objectCode.concat("9");
						}
					}
					catch(Exception e) {
						for(int ii=i;ii<2;ii++)
							objectCode=objectCode.concat("0");
					}

				}
				else if(inst.format==3) {
					String disp="";
					objectCode=Integer.toHexString(inst.opcode/16);
					objectCode=objectCode.concat(Integer.toHexString(inst.opcode%16+token.getFlag(nFlag|iFlag)/16));
					objectCode=objectCode.concat(Integer.toHexString(token.getFlag(xFlag|bFlag|pFlag|eFlag)));
					////////////////////////////////////////////
					/////////////simple addressing//////////////
					////////////////////////////////////////////
					try {
						if(token.getFlag(nFlag|iFlag)==48) {
							///////////////////////
							////////literal////////
							///////////////////////
							
							if(token.operand[0].charAt(0)=='=') {
								int litAddress;
								if((litAddress= this.literalTab.search(token.operand[0]))<0)
									System.err.println("non-existent literal : "+token.operand[0]);
								else {
									disp=Integer.toHexString(litAddress-(token.location+token.byteSize));
									if(disp.length()==1)
										disp="00".concat(disp);
									else if(disp.length()==2)
										disp="0".concat(disp);
									else if(disp.length()>3) {
										disp=disp.substring(disp.length()-3, disp.length());
									}
								}
							}
							///////////////////////
							else {
								int symaddress;
								if((symaddress=this.symTab.search(token.operand[0]))<0)
									System.err.println("non-existent symbol : "+token.operand[0]);
								else {
									disp=Integer.toHexString(symaddress-(token.location+token.byteSize));
									if(disp.length()==1)
										disp="00".concat(disp);
									else if(disp.length()==2)
										disp="0".concat(disp);
									else if(disp.length()>3) {
										disp=disp.substring(disp.length()-3, disp.length());
									}
								}
							}
						}
						//////////////////////////////////////////
						///////////immediate addressing///////////
						//////////////////////////////////////////
						else if(token.getFlag(nFlag|iFlag)==16) {
							int symaddress;
							if(Character.isAlphabetic(token.operand[0].charAt(1))) {
								if((symaddress=this.symTab.search(token.operand[0]))<0)
									System.err.println("non-existent symbol : "+token.operand[0]);
								else {
									disp=Integer.toHexString(symaddress-(token.location+token.byteSize));
									if(disp.length()==1)
										disp="00".concat(disp);
									else if(disp.length()==2)
										disp="0".concat(disp);
									else if(disp.length()>3) {
										disp=disp.substring(disp.length()-3, disp.length());
									}
								}
							}
							else {
								String operand[]=token.operand[0].split("#");
								disp=operand[1];
								if(disp.length()==1)
									disp="00".concat(disp);
								else if(disp.length()==2)
									disp="0".concat(disp);
								else if(disp.length()>3) {
									disp=disp.substring(disp.length()-3, disp.length());
								}
										
							}
							
						}
						//////////////////////////////////////////
						///////////indirect addressing///////////
						//////////////////////////////////////////
						else if(token.getFlag(nFlag|iFlag)==32) {
							int symaddress;
							String operand[]=token.operand[0].split("@");
							if((symaddress=this.symTab.search(operand[1]))<0)
								System.err.println("non-existent symbol : "+token.operand[0]);
							else {
								disp=Integer.toHexString(symaddress-(token.location+token.byteSize));
								if(disp.length()==1)
									disp="00".concat(disp);
								else if(disp.length()==2)
									disp="0".concat(disp);
								else if(disp.length()>3) {
									disp=disp.substring(disp.length()-3, disp.length());
								}
							}
								
								
						}
					}
					catch(StringIndexOutOfBoundsException e) {//if no operand
						disp="000";
					}
					objectCode=objectCode.concat(disp);
				}
				else if(inst.format==4) {
					String disp="";
					objectCode=Integer.toHexString(inst.opcode/16);
					objectCode=objectCode.concat(Integer.toHexString(inst.opcode%16+token.getFlag(nFlag|iFlag)/16));
					objectCode=objectCode.concat(Integer.toHexString(token.getFlag(xFlag|bFlag|pFlag|eFlag)));
					if(this.symTab.search(token.operand[0])<0) {
						//EXTREF찾기 구현
						String location=Integer.toHexString(token.location+1);
						int k=6-location.length();
						for(int i=0;i<k;i++)
							location="0".concat(location);
						this.mRecord.add(location+"05"+"+"+token.operand[0]);
						
						objectCode=objectCode.concat("00000");
					}
					
				}
				
			}
		}//end opcede
		/////////////////////////////////
		
		
		token.objectCode=objectCode.toUpperCase();
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		// initialize ???
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String lineToken[]=line.split("\t");
		if(lineToken[0].equals(".")) {//assembly 주석
			this.label=lineToken[0];
			this.operator="";
			if(lineToken.length>1)
				comment=lineToken[1];
		}
		else {
			label=lineToken[0];
			operator=lineToken[1];
			if(lineToken.length>2) {
				operand=lineToken[2].split(",");
			if(operand.length>1)
				if(operand[1].equals("X")) {
					this.setFlag(TokenTable.xFlag, 1);
					if( operand[0].equals("A")|| operand[0].equals("X")|| operand[0].equals("L")
							|| operand[0].equals("B")|| operand[0].equals("S")|| operand[0].equals("T")
							|| operand[0].equals("T")|| operand[0].equals("F")|| operand[0].equals("PC")
							|| operand[0].equals("SW"))
						this.setFlag(TokenTable.xFlag, 0);
				}
			}
			if(lineToken.length>3)
				comment=lineToken[3];
			/**    nixbpe      */
			this.setFlag(TokenTable.nFlag,1);
			this.setFlag(TokenTable.iFlag, 1);
			try {
				if(operand[0].charAt(0)=='@') {
					this.setFlag(TokenTable.iFlag, 0);
				}
				else if(operand[0].charAt(0)=='#') {
					this.setFlag(TokenTable.nFlag, 0);
				}
			}
			catch(Exception e) {
				
			}
			
			try {
				if(this.getFlag(TokenTable.nFlag)==TokenTable.nFlag) {//n=1
					try{
						if(operator.charAt(0)=='+') {
							this.setFlag(TokenTable.eFlag, 1);
						}
						else if(!operand[0].equals(""))
						this.setFlag(TokenTable.pFlag, 1);
					}
					catch(Exception e) {
						
					}
					
				}
				else if(this.getFlag(TokenTable.iFlag)==TokenTable.iFlag){//n=0,i=1
					if(Character.isAlphabetic(operand[0].charAt(1))) {
						this.setFlag(TokenTable.pFlag, 1);
					}
				}
			}
			catch(Exception e) {
				
			}
			
		}//end not comment line

	}

	/**
	 * n,i,x,b,p,e flag를 설정한다.
	 * 
	 * 
	 * 사용 예 : setFlag(nFlag, 1) 또는 setFlag(TokenTable.nFlag, 1)
	 * 
	 * @param flag  : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		// ...
		if(flag==TokenTable.nFlag) {//n
			if (value==1)
				this.nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		else if(flag==TokenTable.iFlag) {//i
			if (value==1)
				nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		else if(flag==TokenTable.xFlag) {//x
			if (value==1)
				this.nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		else if(flag==TokenTable.bFlag) {//b
			if (value==1)
				this.nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		else if(flag==TokenTable.pFlag) {//p
			if (value==1)
				this.nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		else if(flag==TokenTable.eFlag) {//e
			if (value==1)
				this.nixbpe=(char)(nixbpe|flag);
			else
				nixbpe=(char)(nixbpe&~flag);
		}
		
	
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다.
	 * 
	 * 사용 예 : getFlag(nFlag) 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
