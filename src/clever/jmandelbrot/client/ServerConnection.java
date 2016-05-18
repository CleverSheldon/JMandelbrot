package clever.jmandelbrot.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * 服务器连接类 用于提供与服务器通信的基础支持
 * 
 * @author Sheldon
 *
 */
public class ServerConnection {

	// 默认连接超时时间
	public final static int DEFAULT_CONNECT_TIME_OUT = 5000;

	/** 可用通信指令枚举 */
	private enum Opcode {
		SUCCESS, // 成功
		GET_MARK_MAP, // 获取基准成绩集合
		SEND_MARK, // 发送测试成绩
		SEND_USAGE_INFO // 发送使用情况
	}

	// 唯一的对象实例
	private static ServerConnection mThis;
	// 客户端Socket
	private static Socket mSocket;
	// 输入流
	private static ObjectInputStream mInputStream;
	// 输出流
	private static ObjectOutputStream mOutputStream;
	// 引用计数器
	private static int counter = 0;

	/** 单例初始化 */
	static {
		mThis = new ServerConnection();
	}

	/** 单例模式实现，不可从外部新建对象 */
	private ServerConnection() {
		return;
	}

	/** 获取对象实例 */
	public static ServerConnection getServerConnection() throws Exception {
		// 若连接未建立，则尝试建立连接
		// 若连接未能建立，将抛出异常，对象实例将不会返回
		if (mSocket == null)
			init();
		++counter;
		return mThis;
	}

	/** 释放连接 */
	public void close() throws Exception {
		if (--counter != 0)
			return;
		mSocket.close();
		mInputStream.close();
		mOutputStream.close();
		mSocket = null;
	}

	/** 获取基准成绩映射 */
	public Map<Float, String> getMarkMap() throws IOException,
			ClassNotFoundException {
		mOutputStream.writeObject(new Package(Opcode.GET_MARK_MAP, null));
		return (Map<Float, String>) ((Package) mInputStream.readObject()).mObj;
	}

	/** 上传使用信息 */
	public <T> boolean sendUsageInfo(T info) throws IOException,
			ClassNotFoundException {
		mOutputStream.writeObject(new Package(Opcode.GET_MARK_MAP, info));
		return ((Package) mInputStream.readObject()).mOpcode == Opcode.SUCCESS;
	}

	/** 上传测试成绩 */
	public boolean sendTestMark(float mark, String info) {
		return true;
	}

	/** 建立连接 */
	private static void init() throws Exception {
		mSocket = new Socket();
		mSocket.connect(Setting.getServerAddress(), DEFAULT_CONNECT_TIME_OUT);
		mInputStream = new ObjectInputStream(mSocket.getInputStream());
		mOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
	}

	private class Package {
		// 操作码
		public Opcode mOpcode;
		// 对象引用
		public Object mObj;

		public Package(Opcode opcode, Object obj) {
			mOpcode = opcode;
			mObj = obj;
		}

	}
}
