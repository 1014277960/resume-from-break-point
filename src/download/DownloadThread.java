package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {
	
	private int threadId;
	private int startIndex;
	private int endIndex;
	private int threadCount;
	private String filePath;
	private URL url;
	private DownloadCallback callback;
	
	public DownloadThread(int threadId, int startIndex, int endIndex, int threadCount,
			String filePath, URL url, DownloadCallback callback) {
		// TODO Auto-generated constructor stub
		this.threadId = threadId;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.threadCount = threadCount;
		this.filePath = filePath;
		this.url = url;
		this.callback = callback;
	}
	
	@Override
	public void run() {
		try {
			int lastProgress = 0;
			//判断是否存在进度文件,没有就创建
			File fileProgress = new File(filePath, threadId + ".txt");
			if (fileProgress.exists()) {
				FileInputStream fis =new FileInputStream(fileProgress);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
				lastProgress = Integer.parseInt(bufferedReader.readLine());
				startIndex += lastProgress;
				fis.close();
			}
			
			//回调更新方法
			MultiThread.progress += lastProgress;
			callback.onUpdate(MultiThread.progress, threadId, lastProgress);
			
			HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			//设置请求的范围
			connection.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
			
			//请求部分数据成功为206
			if (connection.getResponseCode() == 206) {
				InputStream is = connection.getInputStream();
				
				byte[] b = new byte[1024];
				int len;
				int total = lastProgress;
				//获得临时文件
				File file = new File(filePath, MultiThread.getNameFromPath(url.toString()));
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//设置开始写的位置
				raf.seek(startIndex);
				while ((len = is.read(b)) != -1) {
					raf.write(b, 0, len);
					total += len;
					//System.out.println("线程" + threadId + "下载了" + total);
					
					//回调更新方法
					MultiThread.progress += len;
					callback.onUpdate(MultiThread.progress, threadId, total);

					RandomAccessFile rafProgress = new RandomAccessFile(fileProgress, "rwd");
					//每读1k保存一次进度
					rafProgress.write((total + "").getBytes());
					rafProgress.close();
							
				}
				raf.close();
				System.out.println("线程" + threadId + "下载完了");
				
				//若只有一条线程下载完，断点后会发现没有相应进度文件，重新下载，不合逻辑
				//fileProgress.delete();
				//三条线程全部下载完毕才删除
				MultiThread.finishedThreadCount++;
				synchronized (url) {
					if (MultiThread.finishedThreadCount == threadCount) {
						for (int id = 0; id != threadCount; id++) {
							File f = new File(id + ".txt");
							f.delete();
						}
						MultiThread.finishedThreadCount = 0;
						callback.onFinish(filePath);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}