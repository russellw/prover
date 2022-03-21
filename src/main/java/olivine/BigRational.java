package olivine;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public final class BigRational extends Number implements Comparable<BigRational> {
  public static final BigRational ZERO = new BigRational(BigInteger.ZERO);
  public static final BigRational ONE = new BigRational(BigInteger.ONE);
  public final BigInteger num, den;

  private BigRational(BigInteger num) {
    this.num = num;
    den = BigInteger.ONE;
  }

  private BigRational(BigInteger num, BigInteger den) {
    switch (den.signum()) {
      case -1 -> {
        num = num.negate();
        den = den.negate();
      }
      case 0 -> throw new ArithmeticException();
    }
    var g = num.gcd(den);
    num = num.divide(g);
    den = den.divide(g);
    this.num = num;
    this.den = den;
  }

  public BigRational add(BigRational b) {
    return new BigRational(num.multiply(b.den).add(b.num.multiply(den)), den.multiply(b.den));
  }

  public BigInteger ceil() {
    return Etc.divideFloor(num.add(den.subtract(BigInteger.ONE)), den);
  }

  @Override
  public int compareTo(BigRational b) {
    return num.multiply(b.den).compareTo(b.num.multiply(den));
  }

  public BigRational divide(BigRational b) {
    return new BigRational(num.multiply(b.den), den.multiply(b.num));
  }

  public BigRational divideEuclidean(BigRational b) {
    return new BigRational(Etc.divideEuclidean(num.multiply(b.den), den.multiply(b.num)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BigRational that = (BigRational) o;
    return num.equals(that.num) && den.equals(that.den);
  }

  @Override
  public int hashCode() {
    return Objects.hash(num, den);
  }

  public BigRational divideFloor(BigRational b) {
    return new BigRational(Etc.divideFloor(num.multiply(b.den), den.multiply(b.num)));
  }

  public BigRational divideTruncate(BigRational b) {
    return new BigRational(num.multiply(b.den).divide(den.multiply(b.num)));
  }

  // Potential better algorithm:
  // https://stackoverflow.com/questions/33623875/converting-an-arbitrary-precision-rational-number-ocaml-zarith-to-an-approxim
  @Override
  public double doubleValue() {
    // TODO: can we do better using BigDecimal?
    return num.doubleValue() / den.doubleValue();
  }

  @Override
  public float floatValue() {
    return (float) doubleValue();
  }

  public BigInteger floor() {
    return Etc.divideFloor(num, den);
  }

  @Override
  public int intValue() {
    return num.divide(den).intValue();
  }

  @Override
  public long longValue() {
    return num.divide(den).longValue();
  }

  public BigRational multiply(BigRational b) {
    return new BigRational(num.multiply(b.num), den.multiply(b.den));
  }

  public BigRational negate() {
    return new BigRational(num.negate(), den);
  }

  public static BigRational of(BigDecimal value) {
    var n = value.unscaledValue();
    var scale = value.scale();
    return (scale >= 0)
        ? of(n, BigInteger.TEN.pow(scale))
        : of(n.multiply(BigInteger.TEN.pow(-scale)));
  }

  public static BigRational of(BigInteger num) {
    return new BigRational(num);
  }

  public static BigRational of(double value) {
    return of(BigDecimal.valueOf(value));
  }

  public static BigRational of(long num) {
    return of(BigInteger.valueOf(num));
  }

  public static BigRational of(String s) {
    var t = s.split("/");
    var num = new BigInteger(t[0]);
    if (t.length == 1) {
      return of(num);
    }
    var den = new BigInteger(t[1]);
    return of(num, den);
  }

  public static BigRational of(BigInteger num, BigInteger den) {
    return new BigRational(num, den);
  }

  public static BigRational of(long num, long den) {
    return new BigRational(BigInteger.valueOf(num), BigInteger.valueOf(den));
  }

  public static BigRational ofDecimal(String s) {
    return of(new BigDecimal(s));
  }

  public BigRational remainderEuclidean(BigRational b) {
    return new BigRational(Etc.remainderEuclidean(num.multiply(b.den), den.multiply(b.num)));
  }

  public BigRational remainderFloor(BigRational b) {
    return new BigRational(Etc.remainderFloor(num.multiply(b.den), den.multiply(b.num)));
  }

  public BigRational remainderTruncate(BigRational b) {
    return new BigRational(num.multiply(b.den).remainder(den.multiply(b.num)));
  }

  public BigInteger round() {
    var n = num.shiftLeft(1).add(den);
    var d = den.shiftLeft(1);
    n = Etc.divideFloor(n, d);
    if (num.testBit(0) && den.equals(BigInteger.TWO) && n.testBit(0)) {
      n = n.subtract(BigInteger.ONE);
    }
    return n;
  }

  public int signum() {
    return num.signum();
  }

  public BigRational subtract(BigRational b) {
    return new BigRational(num.multiply(b.den).subtract(b.num.multiply(den)), den.multiply(b.den));
  }

  @Override
  public String toString() {
    return num.toString() + '/' + den;
  }

  public BigInteger truncate() {
    return num.divide(den);
  }
}
