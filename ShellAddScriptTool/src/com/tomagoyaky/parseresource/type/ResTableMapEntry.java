package com.tomagoyaky.parseresource.type;

/**
 struct ResTable_map_entry : public ResTable_entry
 {
     //ָ��ResTable_map_entry����ԴID�����û�и�ResTable_map_entry�������0��
     ResTable_ref parent;
     //���ں���ResTable_map������
     uint32_t count;
 };
 * @author i
 *
 */
public class ResTableMapEntry extends ResTableEntry{
	
	public ResTableRef parent;
	public int count;
	
	public ResTableMapEntry(){
		parent = new ResTableRef();
	}
	
	@Override
	public int getSize(){
		return super.getSize() + parent.getSize() + 4;
	}
	
	@Override
	public String toString(){
		return super.toString() + ",parent:"+parent.toString()+",count:"+count;
	}

}
