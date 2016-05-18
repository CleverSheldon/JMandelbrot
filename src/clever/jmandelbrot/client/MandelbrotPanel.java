package clever.jmandelbrot.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MandelbrotPanel extends JPanel {

	// ״̬����ʾ���¼��(ms)
	private static final int UPDATE_STATUS_MSG_BREAK = 100;

	// ÿ�����̶ֿȶ�Ӧ�����ű���
	final static float RATE_IN_WHELL = 0.20f;

	// ���������
	private MainUI mMainUI;
	// ����������
	private Arithmetic mArithmetic;
	// ��ƽ������ӳ�����
	private ConcurrentHashMap<Integer, Complex[]> mSourceMap;
	// ������ӳ�����
	private ConcurrentHashMap<Integer, Integer[]> mResultMap;
	// �������ͼƬ����
	private BufferedImage mBufferedImage;
	// �̳߳ض���
	private ExecutorService mExecutorService = Executors
			.newSingleThreadExecutor();
	// Future����
	private Future<?> mFuture;
	// ��Ϣ����Ϣ�ַ���
	private String mInfoMsg = "";
	// ��ƽ������������Ͻǵ�
	private Complex mComplexPanelLeftTop = new Complex();
	// ��ǰ���Ʊ���(����/��ƽ�浥λ)
	private float mPixelInComplex;

	public MandelbrotPanel(MainUI mainUI) {

		mMainUI = mainUI;
		mArithmetic = new Arithmetic(Setting.getCPUThread(),
				Setting.getPrecion());

		// ע������¼�������򣬴���ͼ�����Ų���
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				// ��֤�Ƿ�ͬʱ����"Crtl"���Ա�ʾ����ʹ�����Ź���
				if (event.isControlDown())
					zoom(event);
			}

		});

		// ע�ᴰ���¼�������򣬵����ڴ�С�ı�ʱ�����봰�ڴ�С��صĶ������ݲ����м���
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				initObject();
				cleverCal();
			}

		});

	}

	/** ���ػ�ͼ���� */
	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		// ������ͼ������ǰ��
		graphics.drawImage(mBufferedImage, 0, 0, null);
		// ������Ϣ��
		int x = 30;
		int y = 30;
		graphics.setColor(Color.WHITE);
		for (String line : mInfoMsg.split("\n")) {
			graphics.drawString(line, x, y);
			y += graphics.getFontMetrics().getHeight();
		}

	}

	/** ����������Ⱥ������ú���������������㼰Ԥ�������� */
	private void cleverCal() {
		if (mFuture != null && !mFuture.isDone()) {
			mFuture.cancel(true);
		}
		cal();
		updateInfoMsg();
	}

	/** ���Ŵ����� */
	private void zoom(MouseWheelEvent event) {
		// �������ű���
		float rate = 0.0f;
		if (event.getWheelRotation() > 0) {
			rate = (float) Math
					.pow(1 - RATE_IN_WHELL, event.getWheelRotation());
		} else {
			rate = (float) Math.pow(1 + RATE_IN_WHELL,
					-event.getWheelRotation());
		}
		mPixelInComplex *= rate;
		// ���¸�ƽ���������ԭ��
		Point clickPoint = event.getPoint();
		Complex[] lineArray = mSourceMap.get(clickPoint.y);
		mComplexPanelLeftTop.real = lineArray[clickPoint.x].real - clickPoint.x
				/ mPixelInComplex;
		mComplexPanelLeftTop.imaginary = lineArray[clickPoint.x].imaginary
				+ clickPoint.y / mPixelInComplex;
		// ������临ƽ��Ԫ����
		for (Map.Entry<Integer, Complex[]> lineEntry : mSourceMap.entrySet()) {
			Complex[] complexArray = lineEntry.getValue();
			float yImaginary = mComplexPanelLeftTop.imaginary
					- lineEntry.getKey() / mPixelInComplex;

			for (int x = 0; x < complexArray.length; ++x) {
				complexArray[x].real = mComplexPanelLeftTop.real + x
						/ mPixelInComplex;
				complexArray[x].imaginary = yImaginary;
			}
		}
		// ���¼���
		cleverCal();
	}

	/** ���㺯�� */
	private void cal() {
		// ����ʱ��������ȴ��߳�ȷ����������ɺ�ʱ����ͼ�������������������
		CountDownLatch countDownLatch = new CountDownLatch(
				mBufferedImage.getHeight());
		mArithmetic.cal4ResultOfTimes(countDownLatch, mSourceMap, mResultMap);
		mFuture = mExecutorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					long count = 0;
					while ((count = countDownLatch.getCount()) != 0) {
						mMainUI.SendStatusMsg("����ִ������:"
								+ (1 - (float) count
										/ mBufferedImage.getHeight()) * 100
								+ "%��");

						Thread.sleep(UPDATE_STATUS_MSG_BREAK);

					}
					mMainUI.SendStatusMsg("��������㡣");
					// ͼ����ɫ
					setPixelByTimes();

					mMainUI.repaint();
				} catch (InterruptedException e) {
					mArithmetic.stopCal();
				}
			}

		});
	}

	/** ������������ɫ���� */
	private void setPixelByTimes() {
		for (Entry<Integer, Integer[]> lineEntry : mResultMap.entrySet()) {
			int y = lineEntry.getKey();
			Integer[] lineData = lineEntry.getValue();
			for (int x = 0; x < lineData.length; ++x) {
				// ������ֹ����������Ԥ��ֵ��ɫ
				if (lineData[x] % 4 == 0) {
					mBufferedImage.setRGB(x, y, Color.BLUE.getRGB());
				} else if (lineData[x] % 4 == 1) {
					mBufferedImage.setRGB(x, y, Color.DARK_GRAY.getRGB());
				} else if (lineData[x] % 4 == 2) {
					mBufferedImage.setRGB(x, y, Color.RED.getRGB());
				} else {
					mBufferedImage.setRGB(x, y, Color.GREEN.getRGB());
				}
			}
		}
	}

	/** �����ʼ�����������ڳ�ʼ��������С��صĶ������� */
	private void initObject() {

		// ���㸴ƽ���������
		float width = getSize().width;
		float height = getSize().height;
		mPixelInComplex = width > height ? height / 4 : width / 4;
		mComplexPanelLeftTop.real = -width / mPixelInComplex / 2;
		mComplexPanelLeftTop.imaginary = height / mPixelInComplex / 2;

		// ����ͼƬ����
		mBufferedImage = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);

		// ������ƽ������ӳ����󣬲���临ƽ��Ԫ����
		mSourceMap = new ConcurrentHashMap<Integer, Complex[]>();
		for (int y = 0; y < getHeight(); ++y) {
			int x = getWidth();
			Complex[] complexArray = new Complex[x];
			float yImaginary = mComplexPanelLeftTop.imaginary - y
					/ mPixelInComplex;

			for (int i = 0; i < x; ++i)
				complexArray[i] = new Complex(mComplexPanelLeftTop.real + i
						/ mPixelInComplex, yImaginary);

			mSourceMap.put(y, complexArray);
		}

		// ����������ӳ�����
		mResultMap = new ConcurrentHashMap<Integer, Integer[]>();
		for (int y = 0; y < getSize().height; ++y)
			mResultMap.put(y, new Integer[getWidth()]);
	}

	/** ������Ϣ����Ϣ */
	private void updateInfoMsg() {
		mInfoMsg = String.format("������:%.02f����/������λ\n��������:%d\nʹ���߳���:%d",
				mPixelInComplex, Setting.getPrecion(), Setting.getCPUThread());
	}
}
