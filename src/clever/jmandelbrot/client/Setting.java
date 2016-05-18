package clever.jmandelbrot.client;

import java.net.InetSocketAddress;

public class Setting {

	/**������ʼ��*/
	static{
		init();
	}
	
	//����
	private static int mPrecion=50;
	//�߳���
	private static int mCPUThread;
	//��������ַ���˿ڶ���
	private static InetSocketAddress mServerAddress=new InetSocketAddress("localhost",8831);
	
	/** ����ģʽʵ�֣����ɴ��ⲿ�½����� */
	private Setting()
	{
		return;
	}
	
	/**��ȡ��������*/
	public static int getPrecion()
	{
		return mPrecion;
	}
	
	/**��ȡCPU�߳���*/
	public static int getCPUThread()
	{
		return mCPUThread;
	}
	
	/**��ȡ��������ַ���˿ڶ���*/
	public static InetSocketAddress getServerAddress()
	{
		return mServerAddress;
	}
	
	/**��ʼ������*/
	private static void init()
	{
		//��ȡCPU�߳���
		mCPUThread=Runtime.getRuntime().availableProcessors();
	}
}
