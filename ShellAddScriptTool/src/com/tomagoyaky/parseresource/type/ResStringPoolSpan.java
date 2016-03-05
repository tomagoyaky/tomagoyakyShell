package com.tomagoyaky.parseresource.type;

/**
 struct ResStringPool_span
 {
     enum {
         END = 0xFFFFFFFF
     };
     //ָ����ʽ�ַ������ַ�������ƫ��,���������ʽ<b>XXX</b>,��˴�ָ��b
     ResStringPool_ref name;
     //ָ��Ӧ����ʽ�ĵ�һ���ַ�
     uint32_t firstChar
     //ָ��Ӧ����ʽ�����һ���ַ�
     uin32_t  lastChar;
 };
 * @author i
 *
 */
public class ResStringPoolSpan {
	
	public final static int END = 0xFFFFFFFF;
	
	public ResStringPoolRef name;
	public int firstChar;
	public int lastChar;
	
	@Override
	public String toString(){
		return "name:"+name.toString()+",firstChar:"+firstChar+",lastChar:"+lastChar;
	}

}
