package org.wandora.utils;

public class Functional {
    public static interface Fn0<R>
		{ public R invoke(); }
    public static interface Fn1<R, T0>
		{ public R invoke(T0 t0); }
    public static interface Fn2<R, T0, T1>
		{ public R invoke(T0 t0, T1 t1); }
    public static interface Fn3<R, T0, T1, T2>
		{ public R invoke(T0 t0, T1 t1, T2 t2); }
    public static interface Fn4<R, T0, T1, T2, T3>  
		{ public R invoke(T0 t0, T1 t1, T2 t2, T3 t3); }
    public static interface Fn5<R, T0, T1, T2, T3, T4>
		{ public R invoke(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4); }
    
    public static interface Pr0
		{ public void invoke(); }
    public static interface Pr1<T0>
		{ public void invoke(T0 t0); }
    public static interface Pr2<T0, T1>
		{ public void invoke(T0 t0, T1 t1); }
        
    public static <R, T0>
    Fn0<R> partial(final Fn1<R, T0> fn, final T0 t0) {
        return new Fn0<R>() {
            public R invoke() {
                return fn.invoke(t0);
            }};
    }
    
    /**
     * partial :: ((a, b) -> c) -> a -> (b -> c)
     * partial f x = \y -> f (x, y)
     * @param fn
     * @param t0
     * @return
     */
	public static <R, T0, T1>
	Fn1<R, T1> partial(
		final Fn2<R, T0, T1> fn,
		final T0 t0)
	{
		return new Fn1<R, T1>() {
			public R invoke(T1 t1) {
				return fn.invoke(t0, t1);
			}
		};
	}

	public static <R, T0, T1, T2>
	Fn2<R, T1, T2> partial(
		final Fn3<R, T0, T1, T2> fn,
		final T0 t0)
	{
		return new  Fn2<R, T1, T2>() {
			public R invoke(T1 t1, T2 t2) {
				return fn.invoke(t0, t1, t2);
			}
		};
	}
	public static <R, T0, T1, T2, T3>
	Fn3<R, T1, T2, T3> partial(
		final Fn4<R, T0, T1, T2, T3> fn,
		final T0 t0)
	{
		return new  Fn3<R, T1, T2, T3>() {
			public R invoke(T1 t1, T2 t2, T3 t3) {
				return fn.invoke(t0, t1, t2, t3);
			}
		};
	}
    
    /**
     * curry :: ((a, b) -> c) -> a -> b -> c
     * curry f x y = f (x, y)
     * @param fn
     * @return
     */
	public static <R, T0, T1>
	Fn1<Fn1<R, T1>, T0> curry(final Fn2<R, T0, T1> fn) {
		return new Fn1<Fn1<R, T1>, T0>() {
			public Fn1<R, T1> invoke(final T0 t0) {
				return new Fn1<R, T1>() {
					public R invoke(T1 t1) {
						return fn.invoke(t0, t1);
					}
				};
			}
		};
	}
    
    /**
     * curry :: ((a, b, c) -> d) -> a -> b -> c -> d
     * curry f x y z = f (x, y, z)
     * @param fn
     * @return
     */
    public static <R, T0, T1, T2>
    Fn1<Fn1<Fn1<R, T2>, T1>, T0> curry(final Fn3<R, T0, T1, T2> fn) {
        return new Fn1<Fn1<Fn1<R, T2>, T1>, T0>() {
            public Fn1<Fn1<R, T2>, T1> invoke(final T0 t0) {
                return new Fn1<Fn1<R, T2>, T1>() {
                    public Fn1<R, T2> invoke(final T1 t1) {
                        return new Fn1<R, T2>() {
                            public R invoke(final T2 t2) {
                                return fn.invoke(t0, t1, t2);
                            }
                        };
                    }
                };
            }
        };
    }
    
    public static <R, T0, T1, T2, T3>
    Fn1<Fn1<Fn1<Fn1<R, T3>, T2>, T1>, T0> curry(final Fn4<R, T0, T1, T2, T3> fn) {
        return new Fn1<Fn1<Fn1<Fn1<R, T3>, T2>, T1>, T0>() {
            public Fn1<Fn1<Fn1<R, T3>, T2>, T1> invoke(final T0 t0) {
                return new Fn1<Fn1<Fn1<R, T3>, T2>, T1>() {
                    public Fn1<Fn1<R, T3>, T2> invoke(final T1 t1) {
                        return new Fn1<Fn1<R, T3>, T2>() {
                            public Fn1<R, T3> invoke(final T2 t2) {
                                return new Fn1<R, T3>() {
                                    public R invoke(final T3 t3) {
                                        return fn.invoke(t0, t1, t2, t3);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * flip :: ((a, b) -> c) -> (b, a) -> c
     * flip f (y, x) = f (x, y)
     * @param fn
     * @return
     */
	public static <R, T0, T1>
	Fn2<R, T1, T0> flip(final Fn2<R, T0, T1> fn) {
		return new Fn2<R, T1, T0>() {
			public R invoke(T1 t1, T0 t0) {
				return fn.invoke(t0, t1);
			}
		};
	}

    
    /**
     * flip :: (a -> b -> c) -> b -> a -> c
     * flip f y x = f x y
     * @param fn
     * @return
     */
	public static <R, T0, T1>
	Fn1<Fn1<R, T0>, T1> flip(final Fn1<Fn1<R, T1>, T0> fn) {
		return new Fn1<Fn1<R, T0>, T1>() {
			public Fn1<R, T0> invoke(final T1 t1) {
				return new Fn1<R, T0>() {
					public R invoke(T0 t0) {
						return fn.invoke(t0).invoke(t1);
					}
				};
			}
		};
	}
}
