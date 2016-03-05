package com.tomagoyaky.apkeditor.axmleditor.editor;

import com.tomagoyaky.apkeditor.axmleditor.decode.AXMLDoc;
import com.tomagoyaky.apkeditor.axmleditor.decode.BXMLNode;
import com.tomagoyaky.apkeditor.axmleditor.decode.StringBlock;

/**
 * Created by zl on 15/9/8.
 */
public abstract class BaseEditor<T> implements XEditor {

    public BaseEditor(AXMLDoc doc){
        this.doc=doc;
    }

    protected AXMLDoc doc;

    protected String attrName;
    protected String arrrValue;

    protected int namespace;

    protected int attr_name;
    protected int attr_value;

    protected T editorInfo;

    public void setEditorInfo(T editorInfo) {
        this.editorInfo = editorInfo;
    }

    public void setEditor(String attrName, String attrValue) {
        this.attrName=attrName;
        this.arrrValue=attrValue;
    }

    public void commit() {
        if(editorInfo != null) {
            registStringBlock(doc.getStringBlock());
            editor();
        }
    }

    public abstract String  getEditorName();

    protected abstract void editor();

    protected abstract BXMLNode findNode();

    protected abstract void registStringBlock(StringBlock block);
}
