package olivine;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

public class BigRationalTest {
    @Test
    public void of() {
        // From decimal
        assertEquals(BigRational.of(new BigDecimal("0")), BigRational.ZERO);
        assertEquals(BigRational.of(new BigDecimal("1")), BigRational.ONE);
        assertEquals(BigRational.of(new BigDecimal("10")), BigRational.of(10));
        assertEquals(BigRational.of(new BigDecimal("100")), BigRational.of(100));
        assertEquals(BigRational.of(new BigDecimal("100.0")), BigRational.of(100));
        assertEquals(BigRational.of(new BigDecimal("100.00")), BigRational.of(100));
        assertEquals(BigRational.of(new BigDecimal("100.01")), BigRational.of(10001, 100));
        assertEquals(BigRational.of(new BigDecimal("100.10")), BigRational.of(10010, 100));
        assertEquals(BigRational.of(new BigDecimal("101.00")), BigRational.of(10100, 100));
        assertEquals(BigRational.of(new BigDecimal("110.00")), BigRational.of(11000, 100));
        assertEquals(BigRational.of(new BigDecimal("200.00")), BigRational.of(20000, 100));

        // From double
        assertEquals(BigRational.of(new BigDecimal("0.0")), BigRational.of(0.0));
        assertEquals(BigRational.of(new BigDecimal("1.0")), BigRational.of(1.0));
        assertEquals(BigRational.of(new BigDecimal("10.0")), BigRational.of(10.0));
        assertEquals(BigRational.of(new BigDecimal("10.5")), BigRational.of(10.5));
        assertEquals(BigRational.of(new BigDecimal("10.25")), BigRational.of(10.25));
    }

    @Test
    public void ofDecimal() {
        assertEquals(
                BigRational.ofDecimal("123"), BigRational.of(BigInteger.valueOf(123), BigInteger.ONE));
        assertEquals(
                BigRational.ofDecimal("+123"), BigRational.of(BigInteger.valueOf(123), BigInteger.ONE));
        assertEquals(
                BigRational.ofDecimal("-123"), BigRational.of(BigInteger.valueOf(-123), BigInteger.ONE));
        assertEquals(
                BigRational.ofDecimal("123.456"),
                BigRational.of(BigInteger.valueOf(123456), BigInteger.valueOf(1000)));
        assertEquals(
                BigRational.ofDecimal("123.456e3"),
                BigRational.of(BigInteger.valueOf(123456), BigInteger.valueOf(1)));
        assertEquals(
                BigRational.ofDecimal("123.456e-3"),
                BigRational.of(BigInteger.valueOf(123456), BigInteger.valueOf(1000000)));
        assertEquals(BigRational.ofDecimal("1e100"), BigRational.of(1e100));
        assertEquals(BigRational.ofDecimal("5e-1"), BigRational.of(0.5));
    }

    @Test
    public void parseFraction() {
        assertEquals(BigRational.of("123/1"), BigRational.of(BigInteger.valueOf(123), BigInteger.ONE));
        assertEquals(BigRational.of("+123/1"), BigRational.of(BigInteger.valueOf(123), BigInteger.ONE));
        assertEquals(
                BigRational.of("-123/1"), BigRational.of(BigInteger.valueOf(-123), BigInteger.ONE));
        assertEquals(
                BigRational.of("123/456"),
                BigRational.of(BigInteger.valueOf(123), BigInteger.valueOf(456)));
    }
}
