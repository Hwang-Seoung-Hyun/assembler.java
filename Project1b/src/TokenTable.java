import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�.
 * 
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ�
 * �̸� ��ũ��Ų��. section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
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
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;

	/**object program M Record�� �����ϴ� ����*/
	ArrayList<String> mRecord;
	
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * 
	 * @param symTab    : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literaTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab   : instruction ���� ���ǵ� instTable
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
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * 
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
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
						//EXTREFã�� ����
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
						//EXTREFã�� ����
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
	// �ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������
	String objectCode;
	int byteSize;

	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�.
	 * 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		// initialize ???
		parsing(line);
	}

	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * 
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		String lineToken[]=line.split("\t");
		if(lineToken[0].equals(".")) {//assembly �ּ�
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
	 * n,i,x,b,p,e flag�� �����Ѵ�.
	 * 
	 * 
	 * ��� �� : setFlag(nFlag, 1) �Ǵ� setFlag(TokenTable.nFlag, 1)
	 * 
	 * @param flag  : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ�.
	 * 
	 * ��� �� : getFlag(nFlag) �Ǵ� getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
