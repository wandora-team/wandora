/*
 *	Audio.java
 *
 *	Copyright 2007-2008 Jan Bobrowski <jb@wizard.ae.krakow.pl>
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	version 2 as published by the Free Software Foundation.
 */


package org.wandora.application.gui.previews.formats.applicationz80;

import javax.sound.sampled.*;

class Audio {

    byte buf[] = new byte[4096];
    int bufp;

    long div;
    int idiv, mul;
    int acct;
    int accv0, accv1, level;

    void open(int hz) {
        div = hz;
        acct = hz;
        idiv = (1 << 30) / hz;
    }

    void step(int t, int d) {
        t = acct - mul * t;

        if (t < 0) {
            int p = bufp, v = accv0;
            buf[p++] = (byte) v;
            buf[p++] = (byte) (v >> 8);
            v = accv1;
            accv1 = level;
            loop:
            for (;;) {
                if (p == buf.length) {
                    p = flush(p);
                }
                if ((t += div) >= 0) {
                    break;
                }
                buf[p++] = (byte) v;
                buf[p++] = (byte) (v >> 8);
                v = level;
                if (p == buf.length) {
                    continue;
                }
                if ((t += div) >= 0) {
                    break;
                }
                byte l = (byte) v;
                byte h = (byte) (v >> 8);
                for (;;) {
                    buf[p++] = l;
                    buf[p++] = h;
                    if (p == buf.length) {
                        continue loop;
                    }
                    if ((t += div) >= 0) {
                        break loop;
                    }
                }
            }
            accv0 = v;
            bufp = p;
        }

		// 0 <= t < div
        acct = t;
        int v = level + d;
        if ((short) v != v) {
            v = (short) (v >> 31 ^ 0x7FFF);
            d = v - level;
        }
        level = v;

        int x = idiv * t >> 22;
        int xx = x * x >> 9;
        accv0 += d * xx >> 8;
        xx = 128 - xx + x;
        accv1 += d * xx >> 8;
    }

    static final int FREQ = 22050;
    SourceDataLine line;

    
    
    Audio() {
        try {
            mul = FREQ;
            AudioFormat fmt
                    = new AudioFormat(FREQ, 16, 1, true, false);
            System.out.println(fmt);
            SourceDataLine l = (SourceDataLine) AudioSystem.getLine(
                    new DataLine.Info(SourceDataLine.class, fmt)
            );
            l.open(fmt, 4096);
            l.start();
            line = l;
        } catch (Exception e) {
            System.out.println(e);
        } catch (Error e) {
            // Java on some Linuces throws an error when sound
            // can't start. Thanks for Ricardo Almeida
            e.printStackTrace();
        }
    }

    synchronized int flush(int p) {
        SourceDataLine l = line;
        if (l != null) {
            l.write(buf, 0, p);
        }
        return 0;
    }

    synchronized void close() {
        SourceDataLine l = line;
        if (l != null) {
            line = null;
            l.stop();
            l.close();
        }
    }
}
