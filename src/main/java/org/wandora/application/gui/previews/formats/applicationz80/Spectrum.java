/*
 *	Spectrum.java
 *
 *	Copyright 2004-2009 Jan Bobrowski <jb@wizard.ae.krakow.pl>
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	version 2 as published by the Free Software Foundation.
 */
package org.wandora.application.gui.previews.formats.applicationz80;

import java.awt.event.KeyEvent;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.util.Vector;

public class Spectrum extends Thread implements Z80.Env, ImageProducer {

    final Z80 cpu = new Z80(this);
    final Qaop qaop;

    int rom48k[] = new int[16384];
    final int ram[] = new int[49152];
    int rom[] = rom48k;
    int if1rom[];

    final Audio audio;

    Spectrum(Qaop q) {
        super("Spectrum");
        qaop = q;

        for (int i = 0; i < 8; i++) {
            keyboard[i] = 0xFF;
        }

        for (int i = 6144; i < 6912; i++) {
            ram[i] = 070; // white
        }
        audio = new Audio();
        audio.open(3500000);
    }

    public void run() {
        try {
            frames();
        } catch (InterruptedException e) {
        }
        audio.close();
    }

    private void end_frame() {
        refresh_screen();
        if (border != border_solid) {
            int t = refrb_t;
            refresh_border();
            if (t == BORDER_START) {
                border_solid = border;
            }
        }

        for (int i = 0; i < consumers.size();) {
            ImageConsumer c = (ImageConsumer) consumers.elementAt(consumers.size() - ++i);
            update_screen(c);
        }
        send_change();

        cpu.time -= FRTIME;
        if (--flash_count <= 0) {
            flash ^= 0xFF;
            flash_count = 16;
        }
        audio.level -= audio.level >> 8;
    }

    private long time;
    private int timet;

    static final int FRSTART = -14335;
    static final int FRTIME = 69888;

    private void frames() throws InterruptedException {
        time = System.currentTimeMillis();
        cpu.time = FRSTART;
        cpu.time_limit = FRSTART + FRTIME;
        au_time = cpu.time;
        do {
            byte[] tap = null;
            boolean tend = false;
            synchronized (this) {
                int w = want_scale;
                if (w != scale) {
                    if (w < 0) {
                        break; // quit
                    }
                    scale = w;
                    width = w * W;
                    height = w * H;
                    cm = w == 0 ? cm1 : cm2;
                    notifyAll();
                    abort_consumers();
                }
                w = want_pause;
                if ((w != 0) ^ paused) {
                    paused = w != 0;
                    notifyAll();
                }
                if (stop_loading) {
                    loading = stop_loading = false;
                    notifyAll();
                }
                if (!paused) {
                    tap = tape;
                    tend = tape_ready;
                    if (!loading && tap != null) {
                        loading = check_load();
                    }
                }
            }

            update_keyboard();
            refresh_new();

            if (paused) {
                int t = cpu.time = cpu.time_limit;
                audio.step(t - au_time, 0);
                au_time = t;
            } else {
                if (loading) {
                    loading = do_load(tap, tend);
                    cpu.time = cpu.time_limit;
                } else {
                    cpu.interrupt(0xFF);
                    cpu.execute();
                }
                au_update();
            }
            au_time -= FRTIME;
            end_frame();

            /* sync */
            timet += 121;
            if (timet >= 125) {
                timet -= 125;
                time++;
            }
            time += 19;

            long t = System.currentTimeMillis();
            if (t < time) {
                t = time - t;
                sleep(t);
            } else {
                // yield();
                t -= 100;
                if (t > time) {
                    time = t;
                }
            }
        } while (!interrupted());
    }

    boolean paused = true;
    int want_pause = 1;

    public synchronized int pause(int m) throws InterruptedException {
        if ((want_pause = want_pause & ~m >> 3 ^ m & 7) != 0 && !paused) {
            do {
                wait();
            } while (!paused);
        }
        return want_pause;
    }

    public synchronized void reset() {
        stop_loading();
        cpu.reset();
        au_reset();
        rom = rom48k;
    }

    /* Z80.Env */
    public final int m1(int addr, int ir) {
        int n = cpu.time - ctime;
        if (n > 0) {
            cont(n);
        }

        addr -= 0x4000;
        if ((addr & 0xC000) == 0) {
            cont1(0);
        }
        ctime = NOCONT;
        if ((ir & 0xC000) == 0x4000) {
            ctime = cpu.time + 4;
        }
        if (addr >= 0) {
            return ram[addr];
        }
        n = rom[addr += 0x4000];
        if (if1rom != null && (addr & 0xE8F7) == 0) {
            if (addr == 0x0008 || addr == 0x1708) {
                if (rom == rom48k) {
                    rom = if1rom;
                }
            } else if (addr == 0x0700) {
                if (rom == if1rom) {
                    rom = rom48k;
                }
            }
        }
        return n;
    }

    public final int mem(int addr) {
        int n = cpu.time - ctime;
        if (n > 0) {
            cont(n);
        }
        ctime = NOCONT;

        addr -= 0x4000;
        if (addr >= 0) {
            if (addr < 0x4000) {
                cont1(0);
                ctime = cpu.time + 3;
            }
            return ram[addr];
        }
        return rom[addr + 0x4000];
    }

    public final int mem16(int addr) {
        int n = cpu.time - ctime;
        if (n > 0) {
            cont(n);
        }
        ctime = NOCONT;

        int addr1 = addr - 0x3FFF;
        if ((addr1 & 0x3FFF) != 0) {
            if (addr1 < 0) {
                return rom[addr] | rom[addr1 + 0x4000] << 8;
            }
            if (addr1 < 0x4000) {
                cont1(0);
                cont1(3);
                ctime = cpu.time + 6;
            }
            return ram[addr - 0x4000] | ram[addr1] << 8;
        }
        switch (addr1 >>> 14) {
            case 0:
                cont1(3);
                ctime = cpu.time + 6;
                return rom[addr] | ram[0] << 8;
            case 1:
                cont1(0);
            case 2:
                return ram[addr - 0x4000] | ram[addr1] << 8;
            default:
                return ram[0xBFFF] | rom[0] << 8;
        }
    }

    public final void mem(int addr, int v) {
        int n = cpu.time - ctime;
        if (n > 0) {
            cont(n);
        }
        ctime = NOCONT;

        addr -= 0x4000;
        if (addr < 0x4000) {
            if (addr < 0) {
                return;
            }
            cont1(0);
            ctime = cpu.time + 3;
            if (ram[addr] == v) {
                return;
            }
            if (addr < 6912) {
                refresh_screen();
            }
        }
        ram[addr] = v;
    }

    public final void mem16(int addr, int v) {

        int addr1 = addr - 0x3FFF;
        if ((addr1 & 0x3FFF) != 0) {
            int n = cpu.time - ctime;
            if (n > 0) {
                cont(n);
            }
            ctime = NOCONT;

            if (addr1 < 0) {
                return;
            }
            if (addr1 >= 0x4000) {
                ram[addr1 - 1] = v & 0xFF;
                ram[addr1] = v >>> 8;
                return;
            }
        }
        mem(addr, v & 0xFF);
        cpu.time += 3;
        mem((char) (addr + 1), v >>> 8);
        cpu.time -= 3;
    }

    protected byte ay_idx;
    private byte ula28;

    public void out(int port, int v) {
        cont_port(port);

        if ((port & 0x0001) == 0) {
            ula28 = (byte) v;
            int n = v & 7;
            if (n != border) {
                refresh_border();
                border = (byte) n;
            }
            n = sp_volt[v >> 3 & 3];
            if (n != speaker) {
                au_update();
                speaker = n;
            }
        }
        if ((port & 0x8002) == 0x8000 && ay_enabled) {
            if ((port & 0x4000) != 0) {
                ay_idx = (byte) (v & 15);
            } else {
                au_update();
                ay_write(ay_idx, v);
            }
        }
    }

    private int ear = 0x1BBA4; // EAR noise

    public int in(int port) {
        cont_port(port);

        if ((port & 0x00E0) == 0) {
            return kempston;
        }
        if ((port & 0xC002) == 0xC000 && ay_enabled) {
            if (ay_idx >= 14 && (ay_reg[7] >> ay_idx - 8 & 1) == 0) {
                return 0xFF;
            }
            return ay_reg[ay_idx];
        }
        int v = 0xFF;
        if ((port & 0x0001) == 0) {
            for (int i = 0; i < 8; i++) {
                if ((port & 0x100 << i) == 0) {
                    v &= keyboard[i];
                }
            }
            int e = 0;
			// Apply tape noise only when SPK==0 and MIC==1.
            // thanks Jose Luis for bug report
            if ((ula28 & 0x18) == 8) {
                e = ear - 0x100000;
                if ((e & 0xFFF00000) == 0) {
                    e = e << 2 | e >> 18;
                }
                ear = e;
            }
            v &= ula28 << 2 | e | 0xBF;
        } else if (cpu.time >= 0) {
            int t = cpu.time;
            int y = t / 224;
            t %= 224;
            if (y < 192 && t < 124 && (t & 4) == 0) {
                int x = t >> 1 & 1 | t >> 2;
                if ((t & 1) == 0) {
                    x += y & 0x1800 | y << 2 & 0xE0 | y << 8 & 0x700;
                } else {
                    x += 6144 | y << 2 & 0x3E0;
                }
                v = ram[x];
            }
        }
        return v;
    }

    public int halt(int n, int ir) {
        return n;
    }

    /* contention */
	// according to scratchpad.wikia.com/wiki/Contended_memory
    static final int NOCONT = 99999;
    int ctime;

    private final void cont1(int t) {
        t += cpu.time;
        if (t < 0 || t >= SCRENDT) {
            return;
        }
        if ((t & 7) >= 6) {
            return;
        }
        if (t % 224 < 126) {
            cpu.time += 6 - (t & 7);
        }
    }

    private final void cont(int n) {
        int s, k;
        int t = ctime;
        if (t + n <= 0) {
            return;
        }
        s = SCRENDT - t;
        if (s < 0) {
            return;
        }
        s %= 224;
        if (s > 126) {
            n -= s - 126;
            if (n <= 0) {
                return;
            }
            t = 6;
            k = 15;
        } else {
            k = s >>> 3;
            s &= 7;
            if (s == 7) {
                s--;
                if (--n == 0) {
                    return;
                }
            }
            t = s;
        }
        n = n - 1 >> 1;
        if (k < n) {
            n = k;
        }
        cpu.time += t + 6 * n;
    }

    private void cont_port(int port) {
        int n = cpu.time - ctime;
        if (n > 0) {
            cont(n);
        }

        if ((port & 0xC000) != 0x4000) {
            if ((port & 0x0001) == 0) {
                cont1(1);
            }
            ctime = NOCONT;
        } else {
            ctime = cpu.time;
            cont(2 + ((port & 1) << 1));
            ctime = cpu.time + 4;
        }
    }

    /* ImageProducer */
    static final String pal1
            = "\0\0\0\25\25\311\312\41\41\313\46\313\54\313\54\57\314\314\315\315\65\315\315\315"
            + "\0\0\0\33\33\373\374\51\51\374\57\374\67\375\67\73\376\376\377\377\101\377\377\377";

    static final String pal2
            = "\1\1\1\27\27\320\321\43\43\322\50\322\56\323\56\62\323\323\324\324\67\324\324\324"
            + "\1\1\1\34\34\377\377\53\53\377\61\377\71\377\71\76\377\377\377\377\104\377\377\377"
            + "\0\0\0\24\24\302\303\37\37\303\43\303\51\304\51\55\305\305\305\305\62\306\306\306"
            + "\0\0\0\31\31\364\367\46\46\370\54\370\64\372\64\70\373\373\375\375\76\376\376\376";

    static byte[] palcolor(String p, int n, int m) {
        byte a[] = new byte[n];
        for (int i = 0; i < n; i++) {
            a[i] = (byte) p.charAt(m);
            m += 3;
        }
        return a;
    }

    static final ColorModel cm1 = new IndexColorModel(8, 16,
            palcolor(pal1, 16, 0), palcolor(pal1, 16, 1), palcolor(pal1, 16, 2));

    static final ColorModel cm2 = new IndexColorModel(8, 32,
            palcolor(pal2, 32, 0), palcolor(pal2, 32, 1), palcolor(pal2, 32, 2));

    private ColorModel cm;
    private Vector consumers = new Vector(1);

    public synchronized void addConsumer(ImageConsumer ic) {
        try {
            update_buf = new byte[8 * W * scale * scale];
            ic.setDimensions(width, height);
            consumers.addElement(ic); // XXX it may have been just removed
            ic.setHints(ic.RANDOMPIXELORDER | ic.SINGLEPASS);
            if (isConsumer(ic)) {
                ic.setColorModel(cm);
            }
            force_refresh();
        } catch (Exception e) {
            if (isConsumer(ic)) {
                ic.imageComplete(ImageConsumer.IMAGEERROR);
            }
        }
    }

    public boolean isConsumer(ImageConsumer ic) {
        return consumers.contains(ic);
    }

    public synchronized void removeConsumer(ImageConsumer ic) {
        consumers.removeElement(ic);
    }

    public void startProduction(ImageConsumer ic) {
        addConsumer(ic);
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    private void abort_consumers() {
        for (;;) {
            int s = consumers.size();
            if (s == 0) {
                break;
            }
            s--;
            ImageConsumer c = (ImageConsumer) consumers.elementAt(s);
            consumers.removeElementAt(s);
            c.imageComplete(ImageConsumer.IMAGEABORTED);
        }
    }

    /* screen */
    private static final int SCRENDT = 191 * 224 + 126;
    private static final int Mh = 6; // margin
    private static final int Mv = 5;
    static final int W = 256 + 8 * Mh * 2; // 352
    static final int H = 192 + 8 * Mv * 2; // 272
    int width = W, height = H, scale = 0, want_scale = 0;

    public synchronized void scale(int m) {
        want_scale = m;
        if (m >= 0) {
            try {
                while (scale != m) {
                    wait();
                }
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }

    public int scale() {
        return scale;
    }

    private void force_refresh() {
        bordchg = (1L << Mv + 24 + Mv) - 1;
        for (int r = 0; r < 24; r++) {
            scrchg[r] = ~0;
        }
    }

    final int screen[] = new int[W / 8 * H];	// canonicalized scr. content
    int flash_count = 16;
    int flash = 0x8000;

    /* screen refresh */
    private static final int REFRESH_END = 99999;
    final int scrchg[] = new int[24];	// where the picture changed
    private int refrs_t, refrs_a, refrs_b, refrs_s;

    private final void refresh_new() {
        refrs_t = refrs_b = 0;
        refrs_s = Mv * W + Mh;
        refrs_a = 0x1800;

        refrb_x = -Mh;
        refrb_y = -8 * Mv;
        refrb_t = BORDER_START;
    }

    private final void refresh_screen() {
        int ft = cpu.time;
        if (ft < refrs_t) {
            return;
        }
        final int flash = this.flash;
        int a = refrs_a, b = refrs_b;
        int t = refrs_t, s = refrs_s;
        do {
            int sch = 0;

            int v = ram[a] << 8 | ram[b++];
            if (v >= 0x8000) {
                v ^= flash;
            }
            v = canonic[v];
            if (v != screen[s]) {
                screen[s] = v;
                sch = 1;
            }

            v = ram[a + 1] << 8 | ram[b++];
            if (v >= 0x8000) {
                v ^= flash;
            }
            v = canonic[v];
            if (v != screen[++s]) {
                screen[s] = v;
                sch += 2;
            }

            if (sch != 0) {
                scrchg[a - 0x1800 >> 5] |= sch << (a & 31);
            }

            a += 2;
            t += 8;
            s++;
            if ((a & 31) != 0) {
                continue;
            }
            // next line
            t += 96;
            s += 2 * Mh;
            a -= 32;
            b += 256 - 32;
            if ((b & 0x700) != 0) {
                continue;
            }
            // next row
            a += 32;
            b += 32 - 0x800;
            if ((b & 0xE0) != 0) {
                continue;
            }
            // next segment
            b += 0x800 - 256;
            if (b >= 6144) {
                t = REFRESH_END;
                break;
            }
        } while (ft >= t);
        refrs_a = a;
        refrs_b = b;
        refrs_t = t;
        refrs_s = s;
    }

    /* border refresh */
    private static final int BORDER_START = -224 * 8 * Mv - 4 * Mh + 4;
    private long bordchg;
    private int refrb_t, refrb_x, refrb_y;

    private final void refresh_border() {
        int ft = cpu.time;
        int t = refrb_t;
//		if(t == BORDER_END) XXX only if within screen
        if (ft < t) {
            return;
        }
        border_solid = -1;

        int c = canonic[border << 11];
        int x = refrb_x;
        int y = refrb_y;
        int p = Mh + (Mh + 32 + Mh) * 8 * Mv + x + (Mh + 32 + Mh) * y;
        long m = 1L << (y >>> 3) + Mv;
        boolean chg = false;

        do {
            if (screen[p] != c) {
                screen[p] = c;
                chg = true;
            }
            p++;
            t += 4;
            if (++x == 0 && (char) y < 192) {
                p += (x = 32);
                t += 128;
                continue;
            }
            if (x < 32 + Mh) {
                continue;
            }
            x = -Mh;
            t += 224 - 4 * (Mh + 32 + Mh);
            if ((++y & 7) != 0) {
                continue;
            }
            if (y == 8 * (24 + Mv)) {
                t = REFRESH_END;
                break;
            }
            if (chg) {
                bordchg |= m;
                chg = false;
            }
            m <<= 1;
        } while (t <= ft);

        if (chg) {
            bordchg |= m;
        }

        refrb_x = x;
        refrb_y = y;
        refrb_t = t;
    }

    /* image */
    private final void update_box(ImageConsumer ic,
            int y, int x, int w, byte buf[]) {
        int si = y * W + x;
        int p = 0;

        x <<= 3;
        y <<= 3;
        int h, s;

        if (scale == 1) {
            s = w * 8;
            for (int n = 0; n < 8; n++) {
                for (int k = 0; k < w; k++) {
                    int m = screen[si++];
                    byte c0 = (byte) (m >>> 8 & 0xF);
                    byte c1 = (byte) (m >>> 12);
                    m &= 0xFF;
                    do {
                        buf[p++] = (m & 1) == 0 ? c0 : c1;
                    } while ((m >>>= 1) != 0);
                }
                si += (W / 8) - w;
            }
            h = 8;
        } else {
            h = scale << 3;
            s = w * h;
            for (int n = 0; n < 8; n++) {
                for (int k = 0; k < w; k++) {
                    int m = screen[si++];
                    byte c0 = (byte) (m >>> 8 & 0xF);
                    byte c1 = (byte) (m >>> 12);
                    m &= 0xFF;
                    do {
                        buf[p + s] = buf[p + s + 1] = (byte) ((buf[p] = buf[p + 1]
                                = (m & 1) == 0 ? c0 : c1) + 16);
                        p += 2;
                    } while ((m >>>= 1) != 0);
                }
                p += s;
                si += (W / 8) - w;
            }
            x *= scale;
            y *= scale;
        }
        ic.setPixels(x, y, s, h, cm, buf, 0, s);
    }

    private byte[] update_buf;

    private void update_screen(ImageConsumer ic) {
        long bm = bordchg;
        boolean chg = false;
        byte buf[] = update_buf;
        for (int r = -Mv; r < 24 + Mv; r++, bm >>>= 1) {
            if ((bm & 1) != 0) {
                update_box(ic, r + Mv, 0, Mh + 32 + Mh, buf);
                chg = true;
                continue;
            }
            if ((char) r >= 24) {
                continue;
            }
            int v = scrchg[r];
            if (v != 0) {
                int x = max_bit(v ^ v - 1);
                update_box(ic, Mv + r, Mh + x, max_bit(v) + 1 - x, buf);
                chg = true;
            }
        }
        if (chg) {
            ic.imageComplete(ImageConsumer.SINGLEFRAMEDONE);
        }
    }

    private void send_change() {
        int y1, y2, s;
        long vv;
        loop:
        for (int i = 0;;) {
            s = scrchg[i];
            if (s != 0) {
                y1 = i;
                for (;;) {
                    scrchg[y2 = i] = 0;
                    do {
                        if (++i == 24) {
                            break loop;
                        }
                    } while (scrchg[i] == 0);
                    s |= scrchg[i];
                }
            }
            if (++i < 24) {
                continue;
            }
            vv = bordchg;
            if (vv == 0) {
                return;
            }
            bordchg = 0;
            y1 = max_bit(vv ^ vv - 1);
            y2 = max_bit(vv);
            int sc8 = scale * 8;
            qaop.new_pixels(0, y1 * sc8, (Mh + 32 + Mh) * sc8, (y2 - y1 + 1) * sc8);
            return;
        }
        int x, w;
        y1 += Mv;
        y2 += Mv;
        if ((vv = bordchg) != 0) {
            bordchg = 0;
            int v = max_bit(vv ^ vv - 1);
            if (v < y1) {
                y1 = v;
            }
            v = max_bit(vv);
            if (v > y2) {
                y2 = v;
            }
            x = 0;
            w = Mh + 32 + Mh;
        } else {
            x = max_bit(s ^ s - 1) + Mh;
            w = max_bit(s) + (Mh + 1) - x;
        }
        int sc8 = scale * 8;
        qaop.new_pixels(x * sc8, y1 * sc8, w * sc8, (y2 - y1 + 1) * sc8);
    }

    static final int max_bit(long vv) {
        int v = (int) (vv >>> 32);
        return (v != 0 ? 32 : 0) + max_bit(v != 0 ? v : (int) vv);
    }

    static final int max_bit(int v) {
        int b = 0;
        if ((char) v != v) {
            v >>>= (b = 16);
        }
        if (v > 0xFF) {
            v >>>= 8;
            b += 8;
        }
        if (v > 0xF) {
            v >>>= 4;
            b += 4;
        }
        return b + (-0x55B0 >>> 2 * v & 3);
    }

    static final int canonic[] = new int[32768];

    static {
        // .bpppiii 76543210 -> bppp biii 01234567
        for (int a = 0; a < 0x8000; a += 0x100) {
            int b = a >> 3 & 0x0800;
            int p = a >> 3 & 0x0700;
            int i = a & 0x0700;
            if (p != 0) {
                p |= b;
            }
            if (i != 0) {
                i |= b;
            }
            canonic[a] = p << 4 | 0xFF;
            canonic[a | 0xFF] = i << 4 | 0xFF;
            for (int m = 1; m < 255; m += 2) {
                if (i != p) {
                    int xm = m >>> 4 | m << 4;
                    xm = xm >>> 2 & 0x33 | xm << 2 & 0xCC;
                    xm = xm >>> 1 & 0x55 | xm << 1 & 0xAA;
                    canonic[a | m] = i << 4 | p | xm;
                    canonic[a | m ^ 0xFF] = p << 4 | i | xm;
                } else {
                    canonic[a | m] = canonic[a | m ^ 0xFF]
                            = p << 4 | 0xFF;
                }
            }
        }
    }

    byte border = (byte) 7;		// border color
    byte border_solid = -1;		// nonnegative: solid border color

    /* audio */
    static final int CHANNEL_VOLUME = 26000;
    static final int SPEAKER_VOLUME = 49000;

    boolean ay_enabled;

    void ay(boolean y) // enable
    {
        if (!y) {
            ay_mix = 0;
        }
        ay_enabled = y;
    }

    private int speaker;
    private static final int sp_volt[];

    protected final byte ay_reg[] = new byte[16];

    private int ay_aper, ay_bper, ay_cper, ay_nper, ay_eper;
    private int ay_acnt, ay_bcnt, ay_ccnt, ay_ncnt, ay_ecnt;
    private int ay_gen, ay_mix, ay_ech, ay_dis;
    private int ay_avol, ay_bvol, ay_cvol;
    private int ay_noise = 1;
    private int ay_ekeep; // >=0:hold, ==0:stop
    private boolean ay_div16;
    private int ay_eattack, ay_ealt, ay_estep;

    private static final int ay_volt[];

    void ay_write(int n, int v) {
        switch (n) {
            case 0:
                ay_aper = ay_aper & 0xF00 | v;
                break;
            case 1:
                ay_aper = ay_aper & 0x0FF | (v &= 15) << 8;
                break;
            case 2:
                ay_bper = ay_bper & 0xF00 | v;
                break;
            case 3:
                ay_bper = ay_bper & 0x0FF | (v &= 15) << 8;
                break;
            case 4:
                ay_cper = ay_cper & 0xF00 | v;
                break;
            case 5:
                ay_cper = ay_cper & 0x0FF | (v &= 15) << 8;
                break;
            case 6:
                ay_nper = v &= 31;
                break;
            case 7:
                ay_mix = ~(v | ay_dis);
                break;
            case 8:
            case 9:
            case 10:
                int a = v &= 31,
                 x = 011 << (n - 8);
                if (v == 0) {
                    ay_dis |= x;
                    ay_ech &= ~x;
                } else if (v < 16) {
                    ay_dis &= (x = ~x);
                    ay_ech &= x;
                } else {
                    ay_dis &= ~x;
                    ay_ech |= x;
                    a = ay_estep ^ ay_eattack;
                }
                ay_mix = ~(ay_reg[7] | ay_dis);
                a = ay_volt[a];
                switch (n) {
                    case 8:
                        ay_avol = a;
                        break;
                    case 9:
                        ay_bvol = a;
                        break;
                    case 10:
                        ay_cvol = a;
                        break;
                }
                break;
            case 11:
                ay_eper = ay_eper & 0xFF00 | v;
                break;
            case 12:
                ay_eper = ay_eper & 0xFF | v << 8;
                break;
            case 13:
                ay_eshape(v &= 15);
                break;
        }
        ay_reg[n] = (byte) v;
    }

    private void ay_eshape(int v) {
        if (v < 8) {
            v = v < 4 ? 1 : 7;
        }

        ay_ekeep = (v & 1) != 0 ? 1 : -1;
        ay_ealt = (v + 1 & 2) != 0 ? 15 : 0;
        ay_eattack = (v & 4) != 0 ? 15 : 0;
        ay_estep = 15;

        ay_ecnt = -1; // ?
        ay_echanged();
    }

    private void ay_echanged() {
        int v = ay_volt[ay_estep ^ ay_eattack];
        int x = ay_ech;
        if ((x & 1) != 0) {
            ay_avol = v;
        }
        if ((x & 2) != 0) {
            ay_bvol = v;
        }
        if ((x & 4) != 0) {
            ay_cvol = v;
        }
    }

    private int ay_tick() {
        int x = 0;
        if ((--ay_acnt & ay_aper) == 0) {
            ay_acnt = -1;
            x ^= 1;
        }
        if ((--ay_bcnt & ay_bper) == 0) {
            ay_bcnt = -1;
            x ^= 2;
        }
        if ((--ay_ccnt & ay_cper) == 0) {
            ay_ccnt = -1;
            x ^= 4;
        }

        if (ay_div16 ^= true) {
            ay_gen ^= x;
            return x & ay_mix;
        }

        if ((--ay_ncnt & ay_nper) == 0) {
            ay_ncnt = -1;
            if ((ay_noise & 1) != 0) {
                x ^= 070;
                ay_noise ^= 0x28000;
            }
            ay_noise >>= 1;
        }

        if ((--ay_ecnt & ay_eper) == 0) {
            ay_ecnt = -1;
            if (ay_ekeep != 0) {
                if (ay_estep == 0) {
                    ay_eattack ^= ay_ealt;
                    ay_ekeep >>= 1;
                    ay_estep = 16;
                }
                ay_estep--;
                if (ay_ech != 0) {
                    ay_echanged();
                    x |= 0x100;
                }
            }
        }
        ay_gen ^= x;
        return x & ay_mix;
    }

    private int au_value() {
        int g = ay_mix & ay_gen;
        int v = speaker;
        if ((g & 011) == 0) {
            v += ay_avol;
        }
        if ((g & 022) == 0) {
            v += ay_bvol;
        }
        if ((g & 044) == 0) {
            v += ay_cvol;
        }
        return v;
    }

    private int au_time;
    private int au_val, au_dt;

    private void au_update() {
        int t = cpu.time;
        au_time += (t -= au_time);

        int dv = au_value() - au_val;
        if (dv != 0) {
            au_val += dv;
            audio.step(0, dv);
        }
        int dt = au_dt;
        for (; t >= dt; dt += 16) {
            if (ay_tick() == 0) {
                continue;
            }
            dv = au_value() - au_val;
            if (dv == 0) {
                continue;
            }
            au_val += dv;
            audio.step(dt, dv);
            t -= dt;
            dt = 0;
        }
        au_dt = dt - t;
        audio.step(t, 0);
    }

    void au_reset() {
        /* XXX */
        speaker = 0;
        ay_mix = ay_gen = 0;
        ay_avol = ay_bvol = ay_cvol = 0;
        ay_ekeep = 0;
        ay_dis = 077;
    }

    static boolean muted = false;
    static int volume = 40; // %

    void mute(boolean v) {
        muted = v;
        setvol();
    }

    int volume(int v) {
        if (v < 0) {
            v = 0;
        } else if (v > 100) {
            v = 100;
        }
        volume = v;
        setvol();
        return v;
    }

    int volumeChg(int chg) {
        return volume(volume + chg);
    }

    static {
        sp_volt = new int[4];
        ay_volt = new int[16];
        setvol();
    }

    static void setvol() {
        double a = muted ? 0 : volume / 100.;
        a *= a;

        sp_volt[2] = (int) (SPEAKER_VOLUME * a);
        sp_volt[3] = (int) (SPEAKER_VOLUME * 1.06 * a);

        a *= CHANNEL_VOLUME;
        int n;
        ay_volt[n = 15] = (int) a;
        do {
            ay_volt[--n] = (int) (a *= 0.7071);
        } while (n > 1);
    }

    /* keyboard & joystick */
    public final int keyboard[] = new int[8];
    public int kempston = 0;
    public final KeyEvent keys[] = new KeyEvent[8];
    static final int arrowsDefault[] = {0143, 0124, 0134, 0144};
    int arrows[] = arrowsDefault;

    void update_keyboard() {
        for (int i = 0; i < 8; i++) {
            keyboard[i] = 0xFF;
        }
        kempston = 0;

        int m[] = new int[]{-1, -1, -1, -1, -1};
        int s = 0;
        synchronized (keys) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] != null) {
                    int k = key(keys[i]);
                    if (k < 0) {
                        continue;
                    }
				// .......xxx row
                    // ....xxx... column
                    // ...x...... caps shift
                    // ..x....... symbol shift
                    // .x........ caps shift alone
                    // x......... symbol shift alone
                    s |= k;
                    if (k < 01000) {
                        pressed(k, m);
                    }
                }
            }
        }
        if ((s & 0300) == 0) {
            s |= s >>> 3 & 0300;
        }
        if ((s & 0100) != 0) {
            pressed(000, m);
        }
        if ((s & 0200) != 0) {
            pressed(017, m);
        }
    }

    private final void pressed(int k, int m[]) {
        int a = k & 7, b = k >>> 3 & 7;
        int v = keyboard[a] & ~(1 << b);
        int n = m[b];
        keyboard[a] = v;
        m[b] = a;
        if (n >= 0) {
            v |= keyboard[n];
        }
        for (n = 0; n < 8; n++) {
            if ((keyboard[n] | v) != 0xFF) {
                keyboard[n] = v;
            }
        }
    }

    private int key(KeyEvent e) {
        int c = e.getKeyCode();
        int a = e.getKeyChar();
        int i = "[AQ10P\n ZSW29OL]XDE38IKMCFR47UJNVGT56YHB".indexOf((char) c);
        if (i >= 0) {
            simple:
            {
                int s = 0;
                if (c >= KeyEvent.VK_0 && c <= KeyEvent.VK_9) {
                    if (c != (int) a) {
                        break simple;
                    }
                    if (e.isAltDown()) {
                        s = 0100;
                    }
                }
                return i | s;
            }
        }
        if (a != '\0') {
            i = "\t\0\0!_\"\0\0:\0\0@);=\0\0\0\0#(\0+.?\0<$'\0-,/\0>%&\0^*".indexOf(a);
            if (i >= 0) {
                return i | 0200;
            }
        }
        switch (c) {
            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_ESCAPE:
                return 0103;
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_LEFT:
                i = 0;
                break;
            case KeyEvent.VK_KP_DOWN:
            case KeyEvent.VK_DOWN:
                i = 3;
                break;
            case KeyEvent.VK_KP_UP:
            case KeyEvent.VK_UP:
                i = 2;
                break;
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_RIGHT:
                i = 1;
                break;
            case KeyEvent.VK_BACK_SPACE:
                return 0104;
            case KeyEvent.VK_SHIFT:
                return 01000;
            case KeyEvent.VK_CONTROL:
                kempston |= 0x10; /* fall */

            case KeyEvent.VK_ALT:
                return 02000;
            default:
                return -1;
        }
        kempston |= 1 << (i ^ 1);
        return e.isAltDown() ? arrowsDefault[i] : arrows[i];
    }

    public void setArrows(String s) {
        arrows = new int[4];
        for (int i = 0; i < 4; i++) {
            int c = -1;
            if (i < s.length()) {
                c = "Caq10pE_zsw29olSxde38ikmcfr47ujnvgt56yhb"
                        .indexOf(s.charAt(i));
            }
            if (c < 0) {
                c = arrowsDefault[i];
            }
            arrows[i] = c;
        }
    }

    /* tape */
    private boolean check_load() {
        int pc = cpu.pc();
        if (cpu.ei() || pc < 0x56B || pc > 0x604) {
            return false;
        }
        int sp = cpu.sp();
        if (pc >= 0x5E3) {
            pc = mem16(sp);
            sp = (char) (sp + 2);
            if (pc == 0x5E6) {
                pc = mem16(sp);
                sp = (char) (sp + 2);
            }
        }
        if (pc < 0x56B || pc > 0x58E) {
            return false;
        }
        cpu.sp(sp);
        cpu.ex_af();

        if (tape_changed || tape_ready && tape.length <= tape_blk) {
            tape_changed = false;
            tape_blk = 0;
        }
        tape_pos = tape_blk;
        return true;
    }

    private boolean loading, stop_loading;
    private byte[] tape;
    private int tape_blk;
    private int tape_pos;
    private boolean tape_changed = false;
    private boolean tape_ready = false;

    public synchronized void stop_loading() {
        stop_loading = true;
        try {
            while (loading) {
                wait();
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    public synchronized void tape(byte[] tape, boolean end) {
        if (tape == null) {
            tape_changed = true;
        }
        tape_ready = end;
        this.tape = tape;
    }

    private final boolean do_load(byte[] tape, boolean ready) {
        if (tape_changed || (keyboard[7] & 1) == 0) {
            cpu.f(0);
            return false;
        }

        int p = tape_pos;

        int ix = cpu.ix();
        int de = cpu.de();
        int h, l = cpu.hl();
        h = l >> 8 & 0xFF;
        l &= 0xFF;
        int a = cpu.a();
        int f = cpu.f();
        int rf = -1;

        if (p == tape_blk) {
            p += 2;
            if (tape.length < p) {
                if (ready) {
                    cpu.pc(cpu.pop());
                    cpu.f(cpu.FZ);
                }
                return !ready;
            }
            tape_blk = p + (tape[p - 2] & 0xFF | tape[p - 1] << 8 & 0xFF00);
            h = 0;
        }

        for (;;) {
            if (p == tape_blk) {
                rf = cpu.FZ;
                break;
            }
            if (p == tape.length) {
                if (ready) {
                    rf = cpu.FZ;
                }
                break;
            }
            l = tape[p++] & 0xFF;
            h ^= l;
            if (de == 0) {
                a = h;
                rf = 0;
                if (a < 1) {
                    rf = cpu.FC;
                }
                break;
            }
            if ((f & cpu.FZ) == 0) {
                a ^= l;
                if (a != 0) {
                    rf = 0;
                    break;
                }
                f |= cpu.FZ;
                continue;
            }
            if ((f & cpu.FC) != 0) {
                mem(ix, l);
            } else {
                a = mem(ix) ^ l;
                if (a != 0) {
                    rf = 0;
                    break;
                }
            }
            ix = (char) (ix + 1);
            de--;
        }

        cpu.ix(ix);
        cpu.de(de);
        cpu.hl(h << 8 | l);
        cpu.a(a);
        if (rf >= 0) {
            f = rf;
            cpu.pc(cpu.pop());
        }
        cpu.f(f);
        tape_pos = p;
        return rf < 0;
    }

    /* LOAD "" */
    public final void basic_exec(String cmd) {
        rom = rom48k;

        cpu.i(0x3F);
        int p = 16384;
        do {
            mem(p++, 0);
        } while (p < 22528);
        do {
            mem(p++, 070);
        } while (p < 23296);
        do {
            mem(p++, 0);
        } while (p < 65536);
        mem16(23732, --p); // P-RAMT
        p -= 0xA7;
        System.arraycopy(rom48k, 0x3E08, ram, p - 16384, 0xA8);
        mem16(23675, p--); // UDG
        mem(23608, 0x40); // RASP
        mem16(23730, p); // RAMTOP
        mem16(23606, 0x3C00); // CHARS
        mem(p--, 0x3E);
        cpu.sp(p);
        mem16(23613, p - 2); // ERR-SP
        cpu.iy(0x5C3A);
        cpu.im(1);
        cpu.ei(true);

        mem16(23631, 0x5CB6); // CHANS
        System.arraycopy(rom48k, 0x15AF, ram, 0x1CB6, 0x15);
        p = 0x5CB6 + 0x14;
        mem16(23639, p++); // DATAADD
        mem16(23635, p); // PROG
        mem16(23627, p); // VARS
        mem(p++, 0x80);
        mem16(23641, p); // E-LINE
        for (int i = 0; i < cmd.length(); i++) {
            mem(p++, cmd.charAt(i));
        }
        mem16(p, 0x800D);
        p += 2;
        mem16(23649, p); // WORKSP
        mem16(23651, p); // STKBOT
        mem16(23653, p); // STKEND

        mem(23693, 070);
        mem(23695, 070);
        mem(23624, 070);
        mem16(23561, 0x0523);

        mem(23552, 0xFF);
        mem(23556, 0xFF); // KSTATE

        System.arraycopy(rom48k, 0x15C6, ram, 0x1C10, 14);

        mem16(23688, 0x1821); // S-POSN
        mem(23659, 2); // DF-SZ
        mem16(23656, 0x5C92); // MEM
        mem(23611, 0x0C); // FLAGS

        /*		int r = (int)Math.floor(Math.random() * 128);
         cpu.r(r);
         mem(23672, r); // FRAMES
         */
        cpu.pc(4788);
        au_reset();
    }
}
