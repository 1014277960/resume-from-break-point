package download;

public interface DownloadCallback {
	
	public void onStart(int contentLength);
	public void onFinish(String filePath);
	public void onUpdate(int progress, int threadId, int idTotal);
	public void onError();
	
}
