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

	// 状态栏提示更新间隔(ms)
	private static final int UPDATE_STATUS_MSG_BREAK = 100;

	// 每个滚轮刻度对应的缩放倍数
	final static float RATE_IN_WHELL = 0.20f;

	// 主界面对象
	private MainUI mMainUI;
	// 运算器对象
	private Arithmetic mArithmetic;
	// 复平面区域映射对象
	private ConcurrentHashMap<Integer, Complex[]> mSourceMap;
	// 运算结果映射对象
	private ConcurrentHashMap<Integer, Integer[]> mResultMap;
	// 缓冲绘制图片对象
	private BufferedImage mBufferedImage;
	// 线程池对象
	private ExecutorService mExecutorService = Executors
			.newSingleThreadExecutor();
	// Future对象
	private Future<?> mFuture;
	// 信息区信息字符串
	private String mInfoMsg = "";
	// 复平面绘制区域左上角点
	private Complex mComplexPanelLeftTop = new Complex();
	// 当前绘制比例(像素/复平面单位)
	private float mPixelInComplex;

	public MandelbrotPanel(MainUI mainUI) {

		mMainUI = mainUI;
		mArithmetic = new Arithmetic(Setting.getCPUThread(),
				Setting.getPrecion());

		// 注册滚轮事件处理程序，处理图像缩放操作
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent event) {
				// 验证是否同时按下"Crtl"键以表示正在使用缩放功能
				if (event.isControlDown())
					zoom(event);
			}

		});

		// 注册窗口事件处理程序，当窗口大小改变时更新与窗口大小相关的对象及数据并进行计算
		this.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				initObject();
				cleverCal();
			}

		});

	}

	/** 重载绘图函数 */
	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		// 将缓存图像复制至前景
		graphics.drawImage(mBufferedImage, 0, 0, null);
		// 绘制信息区
		int x = 30;
		int y = 30;
		graphics.setColor(Color.WHITE);
		for (String line : mInfoMsg.split("\n")) {
			graphics.drawString(line, x, y);
			y += graphics.getFontMetrics().getHeight();
		}

	}

	/** 智能运算调度函数，该函数将合理调度运算及预运算任务 */
	private void cleverCal() {
		if (mFuture != null && !mFuture.isDone()) {
			mFuture.cancel(true);
		}
		cal();
		updateInfoMsg();
	}

	/** 缩放处理函数 */
	private void zoom(MouseWheelEvent event) {
		// 计算缩放倍数
		float rate = 0.0f;
		if (event.getWheelRotation() > 0) {
			rate = (float) Math
					.pow(1 - RATE_IN_WHELL, event.getWheelRotation());
		} else {
			rate = (float) Math.pow(1 + RATE_IN_WHELL,
					-event.getWheelRotation());
		}
		mPixelInComplex *= rate;
		// 更新复平面绘制区域原点
		Point clickPoint = event.getPoint();
		Complex[] lineArray = mSourceMap.get(clickPoint.y);
		mComplexPanelLeftTop.real = lineArray[clickPoint.x].real - clickPoint.x
				/ mPixelInComplex;
		mComplexPanelLeftTop.imaginary = lineArray[clickPoint.x].imaginary
				+ clickPoint.y / mPixelInComplex;
		// 重新填充复平面元数组
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
		// 重新计算
		cleverCal();
	}

	/** 运算函数 */
	private void cal() {
		// 倒计时器锁及其等待线程确保在运算完成后及时进行图像后续处理并更新主界面
		CountDownLatch countDownLatch = new CountDownLatch(
				mBufferedImage.getHeight());
		mArithmetic.cal4ResultOfTimes(countDownLatch, mSourceMap, mResultMap);
		mFuture = mExecutorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					long count = 0;
					while ((count = countDownLatch.getCount()) != 0) {
						mMainUI.SendStatusMsg("正在执行运算:"
								+ (1 - (float) count
										/ mBufferedImage.getHeight()) * 100
								+ "%。");

						Thread.sleep(UPDATE_STATUS_MSG_BREAK);

					}
					mMainUI.SendStatusMsg("已完成运算。");
					// 图像着色
					setPixelByTimes();

					mMainUI.repaint();
				} catch (InterruptedException e) {
					mArithmetic.stopCal();
				}
			}

		});
	}

	/** 按迭代次数着色函数 */
	private void setPixelByTimes() {
		for (Entry<Integer, Integer[]> lineEntry : mResultMap.entrySet()) {
			int y = lineEntry.getKey();
			Integer[] lineData = lineEntry.getValue();
			for (int x = 0; x < lineData.length; ++x) {
				// 根据终止迭代次数按预设值着色
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

	/** 对象初始化函数，用于初始化与面板大小相关的对象及数据 */
	private void initObject() {

		// 计算复平面绘制区域
		float width = getSize().width;
		float height = getSize().height;
		mPixelInComplex = width > height ? height / 4 : width / 4;
		mComplexPanelLeftTop.real = -width / mPixelInComplex / 2;
		mComplexPanelLeftTop.imaginary = height / mPixelInComplex / 2;

		// 创建图片缓冲
		mBufferedImage = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);

		// 创建复平面区域映射对象，并填充复平面元数组
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

		// 创建运算结果映射对象
		mResultMap = new ConcurrentHashMap<Integer, Integer[]>();
		for (int y = 0; y < getSize().height; ++y)
			mResultMap.put(y, new Integer[getWidth()]);
	}

	/** 更新信息区信息 */
	private void updateInfoMsg() {
		mInfoMsg = String.format("比例尺:%.02f像素/复数单位\n迭代精度:%d\n使用线程数:%d",
				mPixelInComplex, Setting.getPrecion(), Setting.getCPUThread());
	}
}
