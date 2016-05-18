package clever.jmandelbrot.client;

import java.net.InetSocketAddress;

public class Setting {

	/**单例初始化*/
	static{
		init();
	}
	
	//精度
	private static int mPrecion=50;
	//线程数
	private static int mCPUThread;
	//服务器地址及端口对象
	private static InetSocketAddress mServerAddress=new InetSocketAddress("localhost",8831);
	
	/** 单例模式实现，不可从外部新建对象 */
	private Setting()
	{
		return;
	}
	
	/**获取精度设置*/
	public static int getPrecion()
	{
		return mPrecion;
	}
	
	/**获取CPU线程数*/
	public static int getCPUThread()
	{
		return mCPUThread;
	}
	
	/**获取服务器地址及端口对象*/
	public static InetSocketAddress getServerAddress()
	{
		return mServerAddress;
	}
	
	/**初始化函数*/
	private static void init()
	{
		//获取CPU线程数
		mCPUThread=Runtime.getRuntime().availableProcessors();
	}
}
