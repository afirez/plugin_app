package com.afirez.android.plugin;

public class YCombinator {

    public interface Func<T, R> {
        R apply(T t);
    }

    public interface RFunc<F> extends Func<RFunc<F>, F> {

    }

    public static <T, R> Func<T, R> y(final Func<Func<T, R>, Func<T, R>> func) {
        RFunc<Func<T, R>> r = new RFunc<Func<T, R>>() {
            @Override
            public Func<T, R> apply(final RFunc<Func<T, R>> rf) {
                return func.apply(new Func<T, R>() {
                    @Override
                    public R apply(T t) {
                        return rf.apply(rf).apply(t);
                    }
                });
            }
        };
        return r.apply(r);
    }

    public static void main(String[] args) {
        Func<Integer, Integer> fib = YCombinator.y(
                new Func<Func<Integer, Integer>, Func<Integer, Integer>>() {

                    @Override
                    public Func<Integer, Integer> apply(final Func<Integer, Integer> f) {
                        return new Func<Integer, Integer>() {
                            @Override
                            public Integer apply(Integer n) {
                                return n <= 2 ? 1 : (f.apply(n - 1) + f.apply(n - 2));
                            }
                        };
                    }
                }
        );

        Func<Integer, Integer> fac = YCombinator.y(
                new Func<Func<Integer, Integer>, Func<Integer, Integer>>() {

                    @Override
                    public Func<Integer, Integer> apply(final Func<Integer, Integer> f) {
                        return new Func<Integer, Integer>() {
                            @Override
                            public Integer apply(Integer n) {
                                return n <= 1 ? 1 : (n * f.apply(n - 1));
                            }
                        };
                    }
                }
        );

        System.out.println("fib(10) = " + fib.apply(10));
        System.out.println("fac(10) = " + fac.apply(10));
    }

}
