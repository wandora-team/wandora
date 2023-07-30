/*
 * WANDORA
 * Knowledge Extraction, Management, and Publishing Application
 * http://wandora.org
 * 
 * Copyright (C) 2004-2023 Wandora Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wandora.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.wandora.utils.Functional.Fn0;
import org.wandora.utils.Functional.Fn1;
import org.wandora.utils.Functional.Pr1;

/**
 * Use Option&lt;T&gt; instead of T as a function return or argument type to
 * signal that it might be null. The key benefit is that usage that might throw
 * NullPointerException is statically prevented by the compiler. Accessing the
 * value works either through iteration (option implements Iterable&lt;T&gt;)
 * or through the value() getter that throws a checked exception if the value
 * is null.
 * <br />
 * To create a value, use Option.some(x) and to create a null object,
 * use Option.none(). To use a value, "iterate" over the value or use
 * a try/catch with the getter, or use one of the various member functions
 * to access the value in some other manner (for example mapping it to
 * a function or extracting the value with a default value)
 * <br />
 * <code><pre>
 * for(Value val : tryGetItem())
 *     list.addItem(val);
 * 
 * // as opposed to
 * 
 * Value val = tryGetItem();
 * if(val != null)
 *     list.addItem(val);
 * 
 * // or the potential NullPointerException situation:
 * Value val = tryGetItem();
 * list.addItem(val);
 * </pre></code>
 * 
 * Example usage (method of a class storing key/value pairs):
 * <code><pre>
 * class Storage&lt;KeyT, ValueT&gt; {
 *     public Option&lt;ValueT&gt; get(KeyT key) {
 *         ValueT value = find(key);
 *         if(value == null)
 *             return Option.none();
 *         else
 *             return Option.some(value);
 *     }
 * 
 *     ...
 * }
 * </pre></code>
 * 
 * @author anttirt
 */
public class Option<T> implements Iterable<T> {

    /**
     * Creates an Option that contains a non-null value
     * @param The value to encapsulate
     * @return 
     */
    public static <T> Option<T> some(T val) {
        return new Option<T>(val);
    }

    /**
     * Returns an Option that represents null
     * @return
     */
    public static <T> Option<T> none() {
        return none_;
    }
    
    /**
     * Creates an iterator that treats the Option as a list that contains
     * either 0 or 1 elements.
     * @return A new iterator over this Option instance
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private boolean iterationDone = false;

            public boolean hasNext() {
                if(iterationDone || value_ == null)
                    return false;

                iterationDone = true;
                return true;
            }

            public T next() {
                return value_;
            }

            public void remove() {
                throw new UnsupportedOperationException("Attempted removal from Option");
            }
        };
    }

    /**
     * Essentially the map operation from functional programming where Option
     * is a list that contains either 0 or 1 elements.
     * @param f A function to call on the value contained in this instance.
     * @return some(delegate.invoke(value)) if value is not null, none() otherwise
     */
    public <R> Option<R> map(final Delegate<R, ? super T> f) {
        if(value_ != null)
            return some(f.invoke(value_));
        
        return none();
    }
    /**
     * Essentially the map operation from functional programming where Option
     * is a list that contains either 0 or 1 elements.
     * @param f A function to call on the value contained in this instance.
     * @return some(delegate.invoke(value)) if value is not null, none() otherwise
     */
    public <R> Option<R> map(final Fn1<R, ? super T> f) {
        if(value_ != null)
            return some(f.invoke(value_));
        
        return none();
    }
    
    /**
     * Essentially the map operation from functional programming where Option
     * is a list that contains either 0 or 1 elements. flatMap additionally
     * unwraps the result of the call to the passed function, so as to not
     * result in Option&lt;Option&lt;U&gt;&gt; where R = Option&lt;U&gt;
     * Also corresponds to the &gt;&gt;= operation for monads.
     * @param f A function to call on the value contained in this instance.
     * @return f.invoke(value) if value is not null, none() otherwise
     */
    public <R> Option<R> flatMap(final Fn1<Option<R>, ? super T> f) {
        if(value_ != null)
            return f.invoke(value_);
        
        return none();
    }
    
    /**
     * opt.apply(f) is the same as for(Object val : opt) f.invoke(val);
     * @param f The function that is invoked if a value exists
     */
    public void apply(final Pr1<? super T> f) {
        if(value_ != null)
            f.invoke(value_);
    }
    
    /**
     * Gets the value of this Option or if no value exists returns the result of f
     * @param f The function to call if this Option is empty
     * @return value if not null, f.invoke() otherwise
     */
    public T getOrElse(final Fn0<? extends T> f) {
        if(value_ != null)
            return value_;
        
        return f.invoke();
    }
    
    /**
     * Gets the value of this Option or if no value exists, returns other
     * @param other The value to return if this Option is empty
     * @return value if not null, other otherwise
     */
    public T getOrElse(final T other) {
        if(value_ != null)
            return value_;
        
        return other;
    }
    
    /**
     * Maps the value through f or if no value exists returns the result of g
     * @param f The function to call if a value exists
     * @param g The function to call otherwise
     * @return f.invoke(value) if value is not null, g.invoke() otherwise
     */
    public <R> R mapOrElse(final Delegate<R, ? super T> f, final Fn0<R> g) {
        if(value_ != null)
            return f.invoke(value_);
        
        return g.invoke();
    }
    
    /**
     * Maps the value through f or if no value exists returns other
     * @param f The function to call if a value exists
     * @param other The value to return otherwise
     * @return f.invoke(value) if value is not null, other otherwise
     */
    public <R> R mapOrElse(final Delegate<R, ? super T> f, final R other) {
        if(value_ != null)
            return f.invoke(value_);
        
        return other;
    }
    
    /**
     * Maps the value through f or if no value exists returns the result of g
     * @param f The function to call if a value exists
     * @param g The function to call otherwise
     * @return f.invoke(value) if value is not null, g.invoke() otherwise
     */
    public <R> R mapOrElse(final Fn1<R, ? super T> f, final Fn0<R> g) {
        if(value_ != null)
            return f.invoke(value_);
        
        return g.invoke();
    }
    
    /**
     * Maps the value through f or if no value exists returns other
     * @param f The function to call if a value exists
     * @param other The value to return otherwise
     * @return f.invoke(value) if value is not null, other otherwise
     */
    public <R> R mapOrElse(final Fn1<R, ? super T> f, final R other) {
        if(value_ != null)
            return f.invoke(value_);
        
        return other;
    }
    
    /**
     * Replaces this with other if this is empty, otherwise
     * yields this.
     * @param other
     * @return
     */
    public Option<T> or(final Option<? extends T> other) {
        if(value_ != null)
            return this;
        
        if(other.value_ != null)
            return some(other.value_);
        
        return none();
    }
    
    /**
     * Replaces this with other if this is empty, otherwise
     * yields this.
     * @param other
     * @return
     */
    public Option<T> or(final T other) {
        if(value_ == null)
        {
            return some(other);
        }
        
        return this;
    }
    
    /**
     * Throws a checked exception if the value is null,
     * in order to prevent situations where NullPointerException
     * is thrown from arising.
     * @return The contained value.
     * @throws org.wandora.utils.Option.EmptyOptionException
     */
    public T value() throws EmptyOptionException {
        if(value_ == null)
            throw new EmptyOptionException();

        return value_;
    }

    /**
     * Checks whether this instance contains a value.
     * @return true if this instance contains a value.
     */
    public boolean empty() {
        return value_ == null;
    }
    
    @Override
    public boolean equals(Object other) {
        // traverse potentially nested options recursively
        
        if(value_ instanceof Option) {
            return value_.equals(other);
        }
        else {
            if(other == null)
                return value_ == null;
            else
                return other.equals(value_);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.value_ != null ? this.value_.hashCode() : 0);
        return hash;
    }
    
    /**
     * A checked exception that is thrown if the value getter
     * is used on an empty Option.
     */
    public static class EmptyOptionException extends Exception {
        public EmptyOptionException() { super(); }
    }
    
    /**
     * Creates a collection containing only the actual values from a collection of options
     * @param col The Collection to filter
     * @return The resulting Collection of non-null values
     */
    public static <T, C extends Collection<Option<T>>> Collection<T> somes(C col) {
        Collection<T> ret = new ArrayList();
        for(Option<? extends T> opt : col)
            for(T val : opt)
                ret.add(val);
            
        return ret;
    }

    private Option() { value_ = null; }
    private Option(T val) { value_ = val; }
    private static final Option none_ = new Option();
    private final T value_;
    
    /**
     * A convenience procedure that can be used with Option.apply to
     * add the value of an Option into a Collection:
     * <code><pre>
     * List&lt;String&gt; foo;
     * getOpt(bar).apply(Option.inserter(foo));
     * </pre></code>
     * @param collection A Collection into which the value will be added if it exists
     * @return A function that inserts its argument into collection
     */
    public static <U, C extends Collection<U>> Pr1<U> inserter(final C collection) {
        return new Pr1<U>() {
            public void invoke(U value) {
                collection.add(value);
            }
        };
    }
    
    /**
     * A convenience procedure that can be used with Option.apply to
     * run the value of an Option&lt;T extends Runnable&gt;
     * <code><pre>
     * getProc().apply(Option.runner());
     * </pre></code>
     * @return A function that calls .run() on its argument
     */
    public static <U extends Runnable> Pr1<U> runner() {
        return new Pr1<U>() {
            public void invoke(U value) {
                value.run();
            }
        };
    }
}
