package clever.jmandelbrot.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * ������������ �����ṩ�������ͨ�ŵĻ���֧��
 * 
 * @author Sheldon
 *
 */
public class ServerConnection {

	// Ĭ�����ӳ�ʱʱ��
	public final static int DEFAULT_CONNECT_TIME_OUT = 5000;

	/** ����ͨ��ָ��ö�� */
	private enum Opcode {
		SUCCESS, // �ɹ�
		GET_MARK_MAP, // ��ȡ��׼�ɼ�����
		SEND_MARK, // ���Ͳ��Գɼ�
		SEND_USAGE_INFO // ����ʹ�����
	}

	// Ψһ�Ķ���ʵ��
	private static ServerConnection mThis;
	// �ͻ���Socket
	private static Socket mSocket;
	// ������
	private static ObjectInputStream mInputStream;
	// �����
	private static ObjectOutputStream mOutputStream;
	// ���ü�����
	private static int counter = 0;

	/** ������ʼ�� */
	static {
		mThis = new ServerConnection();
	}

	/** ����ģʽʵ�֣����ɴ��ⲿ�½����� */
	private ServerConnection() {
		return;
	}

	/** ��ȡ����ʵ�� */
	public static ServerConnection getServerConnection() throws Exception {
		// ������δ���������Խ�������
		// ������δ�ܽ��������׳��쳣������ʵ�������᷵��
		if (mSocket == null)
			init();
		++counter;
		return mThis;
	}

	/** �ͷ����� */
	public void close() throws Exception {
		if (--counter != 0)
			return;
		mSocket.close();
		mInputStream.close();
		mOutputStream.close();
		mSocket = null;
	}

	/** ��ȡ��׼�ɼ�ӳ�� */
	public Map<Float, String> getMarkMap() throws IOException,
			ClassNotFoundException {
		mOutputStream.writeObject(new Package(Opcode.GET_MARK_MAP, null));
		return (Map<Float, String>) ((Package) mInputStream.readObject()).mObj;
	}

	/** �ϴ�ʹ����Ϣ */
	public <T> boolean sendUsageInfo(T info) throws IOException,
			ClassNotFoundException {
		mOutputStream.writeObject(new Package(Opcode.GET_MARK_MAP, info));
		return ((Package) mInputStream.readObject()).mOpcode == Opcode.SUCCESS;
	}

	/** �ϴ����Գɼ� */
	public boolean sendTestMark(float mark, String info) {
		return true;
	}

	/** �������� */
	private static void init() throws Exception {
		mSocket = new Socket();
		mSocket.connect(Setting.getServerAddress(), DEFAULT_CONNECT_TIME_OUT);
		mInputStream = new ObjectInputStream(mSocket.getInputStream());
		mOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
	}

	private class Package {
		// ������
		public Opcode mOpcode;
		// ��������
		public Object mObj;

		public Package(Opcode opcode, Object obj) {
			mOpcode = opcode;
			mObj = obj;
		}

	}
}
