package com.tomagoyaky.shell;

import dalvik.system.DexClassLoader;

public class FatherDexClassLoader extends DexClassLoader {
	
	private boolean isCodeLoaded = false;
	public FatherDexClassLoader(String dexPath, String optimizedDirectory,
			String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if(!isCodeLoaded){
			// Òþ²Ø´úÂë
		}
		return super.findClass(name);
	}

	@Override
	protected Class<?> loadClass(String arg0, boolean arg1)
			throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return super.loadClass(arg0, arg1);
	}

	@Override
	public Class<?> loadClass(String arg0) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return super.loadClass(arg0);
	}
	
}
