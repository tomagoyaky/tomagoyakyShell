package com.tomagoyaky.apkeditor.axmleditor.editor;

import com.tomagoyaky.ShellAddScriptTool.common.Logger;
import com.tomagoyaky.apkeditor.axmleditor.decode.AXMLDoc;
import com.tomagoyaky.apkeditor.axmleditor.decode.BTagNode;
import com.tomagoyaky.apkeditor.axmleditor.decode.BTagNode.Attribute;
import com.tomagoyaky.apkeditor.axmleditor.decode.BXMLNode;
import com.tomagoyaky.apkeditor.axmleditor.decode.StringBlock;
import com.tomagoyaky.apkeditor.axmleditor.utils.TypedValue;

/**
 * 修改<application>节点属性
 *
 * ApplicationInfoEditor applicationInfoEditor = new ApplicationInfoEditor(doc);
 * applicationInfoEditor.setEditorInfo(new ApplicationInfoEditor.EditorInfo("app_name", false)); //设置app name 和是否开启debuggable
 * applicationInfoEditor.commit();
 *
 * Created by zl on 15/9/8.
 */
public class ApplicationInfoEditor extends BaseEditor<ApplicationInfoEditor.EditorInfo> {


    public ApplicationInfoEditor(AXMLDoc doc) {
        super(doc);
    }

    @Override
    public String getEditorName() {
        return NODE_APPLICATION;
    }

    private void addStringValue(BTagNode node, StringBlock stringBlock, int xx_Index, int xx_Value, String xx){

		// 判断是否含有android:name属性
		BTagNode.Attribute nameAttribute = null;
		BTagNode.Attribute[] attrArr = node.getAttribute();
		for (int i = 0; i < attrArr.length; i++) {

			String attrName = stringBlock.getStringFor(attrArr[i].mName);
			if(attrName != null && attrName.contains("name")){
				nameAttribute = attrArr[i];
			}
		}
		if(nameAttribute == null){
			// 创建name属性
			BTagNode.Attribute name_attr = new BTagNode.Attribute(
					namespace, editorInfo.name_Index,
					TypedValue.TYPE_STRING);
			node.setAttribute(name_attr);
		}
		node.setAttrStringForKey(xx_Index, xx_Value);
		stringBlock.setString(xx_Value, xx);
    }
    
    @Override
	protected void editor() {
		BTagNode node = (BTagNode) findNode();
		if (node != null) {
			final StringBlock stringBlock = doc.getStringBlock();
			if (editorInfo.label != null) {
				addStringValue(node, stringBlock, editorInfo.label_Index, editorInfo.label_Value, editorInfo.label);
				Logger.LOGW("[Editor] label:" + editorInfo.label);
			}
			if (editorInfo.name != null) {
				addStringValue(node, stringBlock, editorInfo.name_Index, editorInfo.name_Value, editorInfo.name);
				Logger.LOGW("[Editor] name:" + editorInfo.name);
			}
			if (editorInfo.debuggable != -1) {
				if(!stringBlock.containsString(EditorInfo.DEBUGGABLE)){
					BTagNode.Attribute debug_attr = new BTagNode.Attribute(
							namespace, editorInfo.debuggable_Index,
							TypedValue.TYPE_STRING);
					debug_attr.setValue(TypedValue.TYPE_INT_BOOLEAN, editorInfo.debuggable);
					node.setAttribute(debug_attr);
					stringBlock.setString(editorInfo.debuggable_Value, editorInfo.debuggable == 1 ? "true" : "false");
				}else{
					final BTagNode.Attribute[] attributes = node.getAttribute();
                    for (BTagNode.Attribute attr : attributes) {
                        if (attr.mName == editorInfo.debuggable_Index) {
                            attr.setValue(TypedValue.TYPE_INT_BOOLEAN, editorInfo.debuggable == 1 ? 1 : 0);
                            break;
                        }
                    }
				}
				stringBlock.setString(editorInfo.debuggable_Value, editorInfo.debuggable == 1 ? "true" : "false");
				Logger.LOGW("[Editor] debuggable:" + (editorInfo.debuggable == 1 ? "true" : "false"));
			}
		}
	}

    @Override
    protected BXMLNode findNode() {
        return doc.getApplicationNode();
    }

    @Override
    protected void registStringBlock(StringBlock sb) {
        namespace = sb.putString(NAME_SPACE);

		attr_name = sb.putString(NAME);
		attr_value = sb.putString(VALUE);

		if (editorInfo.label != null) {
			editorInfo.label_Index = sb.putString(EditorInfo.LABEL);
			editorInfo.label_Value = sb.addString(String.valueOf(editorInfo.label));
		}
		if (editorInfo.name != null) {
			editorInfo.name_Index = sb.putString(EditorInfo.NAME);
			editorInfo.name_Value = sb.addString(String.valueOf(editorInfo.name));
		}
        if(editorInfo.debuggable != -1){
			if(sb.containsString(EditorInfo.DEBUGGABLE)){
	            editorInfo.debuggable_Index = sb.getStringMapping(EditorInfo.DEBUGGABLE);
	            editorInfo.debuggable_Value = sb.putString(editorInfo.debuggable == 1 ? "true" : "false");
			}else{
				editorInfo.debuggable_Index = sb.addString(EditorInfo.DEBUGGABLE);
                editorInfo.debuggable_Value = sb.putString(editorInfo.debuggable == 1 ? "true" : "false");
			}
        }
    }


    public static class EditorInfo {

		public static final String LABEL = "label";
		public static final String NAME = "name";
		public static final String DEBUGGABLE = "debuggable";

		private String label = null;  //初始值
        private int label_Index;
        private int label_Value;

		private String name = null;  //初始值
        private int name_Index;
        private int name_Value;
        
		private int debuggable = -1;  //初始值
        private int debuggable_Index;
        private int debuggable_Value;

    	public static EditorInfo mEditorInfo;
    	public static EditorInfo getInstance(){
    		if(mEditorInfo == null){
    			mEditorInfo = new EditorInfo();
    		}
    		return mEditorInfo;
    	}

		public EditorInfo label_ValueReplaceWith(String label) {
			mEditorInfo.label = label;
			return mEditorInfo;
		}

		public EditorInfo name_ValueReplaceWith(String name) {
			mEditorInfo.name = name;
			return mEditorInfo;
		}
		
		// -1,0,1
		public EditorInfo debugable_switch(int debuggable) {
			mEditorInfo.debuggable = debuggable;
			return mEditorInfo;
		}
	}
}
