package test;

import java.net.URL;

import download.DownloadCallback;
import download.MultiThread;

public class Main {
	
	public static void main(String[] args) {
		
		try {
			
			MultiThread multiThread = new MultiThread();
			URL url = new URL("http://192.168.2.1:8080/vb.dmg");
			
			multiThread.setURL(url);
			//null则为项目当前文件
			multiThread.setFilePath(null);
			multiThread.setThreadCount(10);
			
			multiThread.setCallback(new DownloadCallback() {
				
				@Override
				public void onUpdate(int progress, int threadId, int idTotal) {
					// TODO Auto-generated method stub
					System.out.println("线程" + threadId + "下载了" + idTotal);
				}
				
				@Override
				public void onStart(int contentLength) {
					// TODO Auto-generated method stub
					System.out.println("目标文件大小：" + contentLength + "");
					
				}
				
				@Override
				public void onFinish(String filePath) {
					// TODO Auto-generated method stub
					System.out.println("finish");
					
				}
				
				@Override
				public void onError() {
					// TODO Auto-generated method stub
					
				}
			});
			multiThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
