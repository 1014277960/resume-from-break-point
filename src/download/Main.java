package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.FieldPosition;
import java.util.MissingFormatArgumentException;

public class Main {
	
	static String path = "http://192.168.2.1:8080/vb.dmg";
	static int threadCount = 3;
	static int finishedThreadCount = 0;
	public static void main(String[] args) {
		try {
			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(8000);
			connection.setReadTimeout(8000);
			
			if (connection.getResponseCode() == 200) {
				//获取长度
				int length = connection.getContentLength();
				
				//创建临时文件
				File file = new File(getNameFromPath(path));
				//相当于输出流
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				
				//设置大小与目标文件一致
				raf.setLength(length);
				raf.close();
				
				//计算下载区间
				int size = length / threadCount;
				
				for (int id = 0; id != threadCount; id++) {
					int startIndex = id * size;
					int endIndex = (id + 1) * size - 1;
					if (id == (threadCount -1)) {
						endIndex = length - 1;
					}
					System.out.println("线程" + id + "区间" + startIndex + "-" + endIndex);
					new DownloadThread(id, startIndex, endIndex).start();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getNameFromPath(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index + 1);
	}
	
}

//下载线程
class DownloadThread extends Thread {
	
	int threadId;
	int startIndex;
	int endIndex;
	
	public DownloadThread(int threadId, int startIndex, int endIndex) {
		// TODO Auto-generated constructor stub
		this.threadId = threadId;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	@Override
	public void run() {
		try {
			int lastProgress = 0;
			//判断是否存在进度文件,没有就创建
			File fileProgress = new File(threadId + ".txt");
			if (fileProgress.exists()) {
				FileInputStream fis =new FileInputStream(fileProgress);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
				lastProgress = Integer.parseInt(bufferedReader.readLine());
				startIndex += lastProgress;
				fis.close();
			}
			
			URL url = new URL(Main.path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
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
				File file = new File(Main.getNameFromPath(Main.path));
				RandomAccessFile raf = new RandomAccessFile(file, "rwd");
				//设置开始写的位置
				raf.seek(startIndex);
				while ((len = is.read(b)) != -1) {
					raf.write(b, 0, len);
					total += len;
					System.out.println("线程" + threadId + "下载了" + total);

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
				Main.finishedThreadCount++;
				synchronized (Main.path) {
					if (Main.finishedThreadCount == 3) {
						for (int id = 0; id != Main.threadCount; id++) {
							File f = new File(id + ".txt");
							f.delete();
						}
						Main.finishedThreadCount = 0;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}






















