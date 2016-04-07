package download;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiThread {
	
	private int threadCount;
	private String filePath;
	private URL url;
	public static int finishedThreadCount = 0;
	private DownloadCallback callback;
	public static int progress = 0;
	
	//设置线程数
	public void setThreadCount(int count) {
		this.threadCount = count;
	}
	
	//设置文件保存路径
	public void setFilePath(String path) {
		this.filePath = path;
	}
	
	//设置下载地址
	public void setURL(URL url) {
		this.url = url;
	}
	
	//设置回调方法
	public void setCallback(DownloadCallback call) {
		callback = call;
	}
	
	public static  String getNameFromPath(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index + 1);
	}
	
	public void start() {
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			
			if (connection.getResponseCode() == 200) {
				//获取长度
				int length = connection.getContentLength();
				
				//创建临时文件
				File file = new File(filePath, getNameFromPath(url.toString()));
				//相当于输出流
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				
				//设置大小与目标文件一致
				raf.setLength(length);
				raf.close();
				
				//计算下载区间
				int size = length / threadCount;
				
				//回调
				callback.onStart(length);
				
				for (int id = 0; id != threadCount; id++) {
					int startIndex = id * size;
					int endIndex = (id + 1) * size - 1;
					if (id == (threadCount -1)) {
						endIndex = length - 1;
					}
					new DownloadThread(id, startIndex, endIndex, threadCount, filePath, url,
							callback).start();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
