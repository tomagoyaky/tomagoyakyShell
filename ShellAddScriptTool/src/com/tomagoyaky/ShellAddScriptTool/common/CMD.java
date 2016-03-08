package com.tomagoyaky.ShellAddScriptTool.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import com.tomagoyaky.ShellAddScriptTool.Start;

public class CMD {

	public static OutputStream nullOS = new OutputStream() {
		
		@Override
		public void write(int b) throws IOException {
			
		}
	};

	public static boolean execute(String cmdlineStr, String cwd, int exitValue, OutputStream os){
		
		if(cwd == null){
			Logger.LOGE("cwd is null");
			return false;
		}
		if(!new File(cwd).exists()){
			Logger.LOGE("cwd is not exist");
			return false;
		}
		CommandLine cmdLine = CommandLine.parse(cmdlineStr);
		DefaultExecutor	executor = new DefaultExecutor();
		PumpStreamHandler streamHandler = new PumpStreamHandler(os); 
		executor.setStreamHandler(streamHandler);
		executor.setExitValue(exitValue);
		executor.setWorkingDirectory(new File(cwd));
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		try {
			if(Start.Debuggable.CMD_ENABLE) Logger.LOGD("[CMD] " + cmdlineStr
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_Shell_project, "{dir_Shell_project}")
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_pwd, "{dir_pwd}"));
			executor.execute(cmdLine);
		} catch (ExecuteException e) {
			e.printStackTrace();
			Logger.LOGE(e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			Logger.LOGE(e);
			return false;
		}
		return true;
	}
	
	public static String execute(String cmdlineStr, String cwd, int exitValue){
		
		if(cwd == null){
			Logger.LOGE("cwd is null");
			return null;
		}
		if(!new File(cwd).exists()){
			Logger.LOGE("cwd is not exist");
			return null;
		}
		CommandLine cmdLine = CommandLine.parse(cmdlineStr);
		DefaultExecutor	executor = new DefaultExecutor();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream(); 
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream); 
		executor.setStreamHandler(streamHandler);
		executor.setExitValue(exitValue);
		executor.setWorkingDirectory(new File(cwd));
		ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
		executor.setWatchdog(watchdog);
		try {
			if(Start.Debuggable.CMD_ENABLE) Logger.LOGD("[CMD] " + cmdlineStr
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_Shell_project, "{dir_Shell_project}")
					.replace(Constants.dir_workplace, "{dir_workplace}")
					.replace(Constants.dir_pwd, "{dir_pwd}"));
			executor.execute(cmdLine);
			String out = outputStream.toString("utf-8");
			String error = errorStream.toString("utf-8");
			return out + error;
		} catch (ExecuteException e) {
			e.printStackTrace();
			Logger.LOGE(e);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			Logger.LOGE(e);
			return null;
		}
	}
}
