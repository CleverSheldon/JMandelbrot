package clever.jmandelbrot.client;

import java.util.*;
import java.util.concurrent.*;

/**
 * Mandelbrot���㷨ʵ����
 * 
 * @author Sheldon
 *
 */
public class Arithmetic {

	// ��������
	private int mPrecion = 200;
	// �̳߳ض���
	private ExecutorService mExcutorService;
	// Future���󼯺�
	private Set<Future<Void>> mFutureSet = new HashSet<>();

	public Arithmetic(int numOfThread) throws IllegalArgumentException {
		if (numOfThread <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Arithmetic\nָ���߳�����Ч��");

		// �����̳߳�
		mExcutorService = Executors.newFixedThreadPool(numOfThread);
	}

	public Arithmetic(int numOfThread, int precion)
			throws IllegalArgumentException {
		this(numOfThread);

		if (precion <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Arithmetic\nָ������������Ч��");

		mPrecion = precion;
	}

	/**
	 * ����Mandelbrot�������Ϊ�������� Notice: �������첽��ʽ���������ú��������أ���������ɴ���ĵ�����������֪
	 * ����ǰ���ȷ��ǰ�μ����Ѿ���ɣ��������ǰ���ȵ���stopCal()ֹͣ��ǰ�����飬���������ܻ᲻��ȷ
	 */
	public void cal4ResultOfTimes(CountDownLatch progressIndicator,
			Map<Integer, Complex[]> sourceMap, Map<Integer, Integer[]> resultMap)
			throws IllegalArgumentException {

		if (sourceMap == null || resultMap == null)
			throw new IllegalArgumentException(
					"Error in Arithmetic/Performer/cal4ResultOfTimes\nԪ���ݼ��϶����������ӳ���������Ϊ�ա�");

		// �����ϴμ�����������
		mFutureSet.clear();

		// ����Ϊ��Ԫ��������
		for (Map.Entry<Integer, Complex[]> line : sourceMap.entrySet()) {
			Performer performer = new Performer();
			performer.setSourceAndResultArray(line.getValue(),
					resultMap.get(line.getKey()));
			performer.setCountDownLatch(progressIndicator);
			mFutureSet.add(mExcutorService.submit(performer));
		}

	}

	/** ֹͣ��ǰ���������� */
	public void stopCal() {
		for (Future<Void> future : mFutureSet)
			future.cancel(true);
		mFutureSet.clear();
	}

	/** ����ʹ�õ��߳��� */
	public void setNumOfThread(int numOfThread) throws IllegalArgumentException {
		if (numOfThread <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/setNumOfThread\nָ���߳�����Ч��");

		// �����̳߳�
		mExcutorService = Executors.newFixedThreadPool(numOfThread);
	}

	/** ���õ����������� */
	public void setPrecion(int precion) throws IllegalArgumentException {
		if (precion <= 0)
			throw new IllegalArgumentException(
					"Error in Arithmetic/setPrecion\nָ������������Ч��");

		mPrecion = precion;
	}

	/** ���㹤���߳��� */
	private class Performer implements Callable<Void> {
		// Ԫ��������
		private Complex[] mSourceArray;
		// �����������
		private Integer[] mResultArray;
		// ��������
		private CountDownLatch mCountDownLatch;

		/** ����Ԫ���ݼ��������������� */
		public void setSourceAndResultArray(Complex[] sourceArray,
				Integer[] resultArray) throws IllegalArgumentException {
			if (sourceArray.length != resultArray.length)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/sendSourceAndResultArray\nԪ�������������������鳤�Ȳ�һ�¡�");

			mSourceArray = sourceArray;
			mResultArray = resultArray;
		}

		/** ���õ������� */
		public void setCountDownLatch(CountDownLatch countDownLatch) {
			mCountDownLatch = countDownLatch;
		}

		@Override
		public Void call() throws IllegalArgumentException {
			if (mSourceArray == null || mResultArray == null)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/call\nԪ�����������������������Ϊ�ա�");
			if (mCountDownLatch == null)
				throw new IllegalArgumentException(
						"Error in Arithmetic/Performer/call\n������������Ϊ�ա�");

			// ��������
			Complex z = new Complex();
			// ��ǰ���鸴����
			Complex c;
			// ����������ʵ��ƽ��
			float zRealSquare;
			// �����������鲿ƽ��
			float zImaginarySquare;
			// ��������������
			int counter;

			// ����Ԫ���ݵ�
			for (int i = 0; i < mSourceArray.length; ++i) {
				// ���ó�ʼֵ
				c = mSourceArray[i];
				z.real = c.real;
				z.imaginary = c.imaginary;
				zRealSquare = z.real * z.real;
				zImaginarySquare = z.imaginary * z.imaginary;
				counter = 0;

				// ����
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
