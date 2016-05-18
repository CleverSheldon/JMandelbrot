package clever.jmandelbrot.client;

import java.util.*;
import java.util.concurrent.*;

/**
 * Mandelbrot集算法实现类
 * 
 * @author Sheldon
 *
 */
public class Arithmetic {

	// 迭代精度
	private int mPrecion = 200;
	// 线程池对象
	private ExecutorService mExcutorService;
	// Future对象集合
	private Set<Future<Void>> mFutureSet = new HashSet<>();

	public Arithmetic(int numOfThread) throws IllegalArgumentException {
		if (numOfThread <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Arithmetic\n指定线程数无效。");

		// 创建线程池
		mExcutorService = Executors.newFixedThreadPool(numOfThread);
	}

	public Arithmetic(int numOfThread, int precion)
			throws IllegalArgumentException {
		this(numOfThread);

		if (precion <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Arithmetic\n指定迭代精度无效。");

		mPrecion = precion;
	}

	/**
	 * 计算Mandelbrot集，结果为迭代次数 Notice: 函数以异步方式运作，调用后将立即返回，计算进度由传入的倒计数器锁告知
	 * 调用前务必确认前次计算已经完成，否则调用前请先调用stopCal()停止当前任务组，否则结果可能会不正确
	 */
	public void cal4ResultOfTimes(CountDownLatch progressIndicator,
			Map<Integer, Complex[]> sourceMap, Map<Integer, Integer[]> resultMap)
			throws IllegalArgumentException {

		if (sourceMap == null || resultMap == null)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Performer/cal4ResultOfTimes\n元数据集合对象或结果数据映射对象引用为空。");

		// 清理上次计算遗留数据
		mFutureSet.clear();

		// 以行为单元进行运算
		for (Map.Entry<Integer, Complex[]> line : sourceMap.entrySet()) {
			Performer performer = new Performer();
			performer.setSourceAndResultArray(line.getValue(),
					resultMap.get(line.getKey()));
			performer.setCountDownLatch(progressIndicator);
			mFutureSet.add(mExcutorService.submit(performer));
		}

	}

	/** 停止当前计算任务组 */
	public void stopCal() {
		for (Future<Void> future : mFutureSet)
			future.cancel(true);
		mFutureSet.clear();
	}

	/** 设置使用的线程数 */
	public void setNumOfThread(int numOfThread) throws IllegalArgumentException {
		if (numOfThread <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/setNumOfThread\n指定线程数无效。");

		// 创建线程池
		mExcutorService = Executors.newFixedThreadPool(numOfThread);
	}

	/** 设置迭代次数精度 */
	public void setPrecion(int precion) throws IllegalArgumentException {
		if (precion <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/setPrecion\n指定迭代精度无效。");

		mPrecion = precion;
	}

	/** 运算工作线程类 */
	private class Performer implements Callable<Void> {
		// 元数据数组
		private Complex[] mSourceArray;
		// 结果数据数组
		private Integer[] mResultArray;
		// 倒计数锁
		private CountDownLatch mCountDownLatch;

		/** 设置元数据及结果数据数组对象 */
		public void setSourceAndResultArray(Complex[] sourceArray,
				Integer[] resultArray) throws IllegalArgumentException {
			if (sourceArray.length != resultArray.length)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/sendSourceAndResultArray\n元数据数组与结果数据数组长度不一致。");

			mSourceArray = sourceArray;
			mResultArray = resultArray;
		}

		/** 设置倒计数锁 */
		public void setCountDownLatch(CountDownLatch countDownLatch) {
			mCountDownLatch = countDownLatch;
		}

		@Override
		public Void call() throws IllegalArgumentException {
			if (mSourceArray == null || mResultArray == null)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/call\n元数据数组或结果数据数组引用为空。");
			if (mCountDownLatch == null)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/call\n倒计数锁引用为空。");

			// 迭代变量
			Complex z = new Complex();
			// 当前检验复数点
			Complex c;
			// 迭代变量的实部平方
			float zRealSquare;
			// 迭代变量的虚部平方
			float zImaginarySquare;
			// 迭代次数计数器
			int counter;

			// 遍历元数据点
			for (int i = 0; i < mSourceArray.length; ++i) {
				// 设置初始值
				c = mSourceArray[i];
				z.real = c.real;
				z.imaginary = c.imaginary;
				zRealSquare = z.real * z.real;
				zImaginarySquare = z.imaginary * z.imaginary;
				counter = 0;

				// 迭代
				while (zRealSquare + zImaginarySquare < 4 && counter < mPrecion) {
					z.real = zRealSquare - zImaginarySquare + c.real;
					z.imaginary = 2 * z.real * z.imaginary + c.imaginary;

					zRealSquare = z.real * z.real;
					zImaginarySquare = z.imaginary * z.imaginary;
					++counter;
				}

				mResultArray[i] = counter;
			}

			mCountDownLatch.countDown();

			return null;
		}

	}

}
