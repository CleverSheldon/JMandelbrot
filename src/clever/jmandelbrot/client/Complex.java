package clever.jmandelbrot.client;

/**
 * 简单复数类 该类仅作简单数据封装之用，不提供更多功能支持
 * 因为该对象需在每一次迭代中多次访问，基于性能考虑，该类将直接暴露内部变量并不提供相应Getter/Setter方法
 * 
 * @author Sheldon
 *
 */
class Complex {

	// 实部
	public float real;
	// 虚部
	public float imaginary;

	public Complex() {
	}

	public Complex(float real, float imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}

}
