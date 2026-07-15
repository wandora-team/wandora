/*
 *	Qaop.java
 *
 *	Copyright 2004-2009 Jan Bobrowski <jb@wizard.ae.krakow.pl>
 *
 *	This program is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU General Public License
 *	version 2 as published by the Free Software Foundation.
 */


package org.wandora.application.gui.previews.formats.applicationz80;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.wandora.application.gui.UIBox;





public class Qaop extends JPanel implements Runnable, KeyListener, FocusListener, ComponentListener, MouseListener {

    protected Spectrum spectrum;
    protected Map params;
    private int screenScaler = 1;
    
    
    public Qaop(Map params) {
        super();
        this.params = params;
        this.setBackground(new Color(0x222222));
        this.addMouseListener(this);
        init();
    }
    
    
    
    
    
    public synchronized void init() {
        showStatus(getAppletInfo(), true);

        String a = param("mask");
        if (a != null) {
            mask = getImage(a);
        }

        /*		a = param("boxbgcolor");
         if(a != null) {
         Color bg = Color.getColor(a);
         if(bg != null) this.setBackground(bg);
         }
         */
        Spectrum s = new Spectrum(this);
        spectrum = s;
        queue = new LinkedList();

        addKeyListener(this);
        addFocusListener(this);

        a = param("rom");
        if (a == null) {
            InputStream in = resource("/org/wandora/application/gui/previews/formats/applicationz80/roms/spectrum48.rom");
            if (in != null) {
                int l = tomem(s.rom48k, 0, 16384, in);
                if(l != 0) {
                    showStatus("Can't read spectrum.rom (2)", true);
                }
            }
            else {
                showStatus("Can't read spectrum.rom", true);
            }
        } else {
            Loader.add(this, a, Loader.ROM);
        }

        a = param("if1rom");
        if (a != null) {
            Loader.add(this, a, Loader.IF1ROM);
        }

        a = param("load");
        if (a != null) {
            Loader.add(this, a, 0);
        }

        a = param("tape");
        if (a != null) {
            Loader.add(this, a, Loader.TAP);
        }

        a = param("arrows");
        if (a != null) {
            s.setArrows(a);
        }

        if (param("ay", false)) {
            s.ay(true);
        }

        if (param("muted", false)) {
            s.mute(true);
        }

        s.volume((int) param("volume", 40));

        s.start();
        th = new Thread(this, "Qaop");
//		th.setPriority(Thread.NORM_PRIORITY-1);
        th.start();

        if (param("focus", true)) {
            addComponentListener(this);
        }
    }

    public synchronized void destroy() {
        th.interrupt();
        spectrum.scale(-1); // signal to quit
        spectrum.interrupt(); // this seems to be not enough
        try {
            th.join();
            spectrum.join();
        } catch (Exception x) {
        }
    }

    public String getAppletInfo() {
        return "Qaop - ZX Spectrum emulator by Jan Bobrowski";
    }

    static final String info[][] = {
        {"rom", "filename", "alternative ROM image"},
        {"if1rom", "filename", "enable Interface1; use this ROM"},
        {"tape", "filename", "tape file"},
        {"load", "filename", "snapshot or tape to load"},
        {"focus", "yes/no", "grab focus on start"},
        {"arrows", "keys", "define arrow keys"},
        {"ay", "yes/no", "with AY"},
        {"muted", "yes/no", "muted to start"},
        {"volume", "number", "volume 0..100"},
        {"mask", "filename", "overlay"}
    };

    public String[][] getParameterInfo() {
        return info;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(spectrum.width, spectrum.height);
    }

    

    
    
    /* javascript interface */
    public void reset() {
        synchronized (queue) {
            Loader.clear(queue, 0);
        }
        spectrum.reset();
    }

    public void load(String name) {
        Loader.add(this, name, 0);
    }

    public void tape(String name) {
        Loader.add(this, name, Loader.TAP);
    }

    public String save() {
        try {
            spectrum.pause(022);
            String s = Loader.get_snap(this);
            spectrum.pause(020);
            return s;
        } catch (InterruptedException x) {
            intr();
            return null;
        }
    }

    public boolean pause(boolean v) {
        try {
            spectrum.pause(v ? 044 : 040);
        } catch (InterruptedException x) {
            intr();
        }
        return v;
    }

    public void mask(String name) {
        mask = name != null
                ? getImage(name)
                : null;
        repaint();
    }

    public void mute(boolean y) {
        spectrum.mute(y);
    }

    public int volume() {
        return spectrum.volumeChg(0);
    }

    public int volume(int v) {
        return spectrum.volume(v);
    }

    public void setScreenSize(int scaler) {
        screenScaler = scaler;
        this.setSize(352*scaler, 272*scaler);
    }
    
    
    private int state;

    // bit 0:paused, 1:loading, 2:muted
    public int state() {
        int v = 0;
        try {
            Spectrum s = spectrum;
            v = s.pause(0) >>> 2 & 1;
            if (s.muted) {
                v |= 4;
            }
        } catch (InterruptedException x) {
            intr();
        }
        return state | v;
    }

    public void focus() {
        requestFocus();
    }

    public static void intr() {
        Thread.currentThread().interrupt();
    }

    /* graphics */
    private Image img; // speccy
    private Dimension size;
    private int posx, posy;

    private Image buf_image;
    private Image mask;

    private void resized(Dimension d) {
        size = d;
        int s1 = d.width / 256;
        int s2 = d.height / 192;
        int s = Math.min(s1, s2);
        // Although the s can be bigger than 2, the spectrum doesn't support s > 2.
        if (spectrum.scale() != s) {
            img = null;
            spectrum.scale(s);
            img = createImage(spectrum);
            dl_text_current = null;
        }
        posx = (d.width - spectrum.width) / 2;
        posy = (d.height - spectrum.height) / 2;
        buf_image = null;
    }

    
    @Override
    public void paint(Graphics g) {
        update(g);
    }
    
    

    @Override
    public synchronized void update(Graphics g) {
        Dimension d = getSize();
        if (!d.equals(size)) {
            resized(d);
        }

        String t = dl_text;
        if (mask == null && t == null) {
            g.drawImage(img, posx, posy, null);
            return;
        }

        int sw = spectrum.width; 
        int sh = spectrum.height;
        if (dl_text_current == null ? t != null : !dl_text_current.equals(t)) {
            dl_img = dl_mk_image(t, sw);
        }
        if (buf_image == null) {
            buf_image = createImage(sw, sh);
        }

        Graphics g2 = buf_image.getGraphics();
        Rectangle r = g.getClipBounds();
        int x = posx, y = posy;
        g2.setClip(r.x - x, r.y - y, r.width, r.height);
        g2.drawImage(img, 0, 0, null);
        if (mask != null) {
            g2.drawImage(mask, 0, 0, sw, sh, this);
        }
        if (dl_img != null) {
            g2.translate(0, sh / 2);
            dl_paint(g2, sw);
        }
        g2.dispose();

        g.drawImage(buf_image, x, y, null);
    }

    protected void new_pixels(int x, int y, int w, int h) {
        repaint(posx + x, posy + y, w, h);
    }

    Color pb_color = Color.black;

    protected Image dl_img;
    protected String dl_text_current;

    private void dl_paint(Graphics g, int w) {
        g.drawImage(dl_img, 0, -dl_img.getHeight(null), null);
        String e = dl_msg;
        if (e != null) {
            g.setFont(new Font("SansSerif", 0, 12));
            FontMetrics m = g.getFontMetrics();
            int ex = (w - m.stringWidth(e)) / 2;
            int ey = m.getAscent();
            g.setColor(new Color(0xFF8800));
            g.drawString(e, ex, ey);
            return;
        }

        double perc = dl_length > 0
                ? (double) dl_loaded / dl_length
                : 0;
        if (perc > 1) {
            perc = 1;
        }

        int pb_w = 68;
        int x = (w - pb_w) / 2, y = 5;
        int s = (int) (pb_w * perc);

        g.setColor(new Color(0x7FBBBBBB, true));
        g.fillRect(x, y, pb_w, 3);
        g.setColor(new Color(0x1A646464, true));
        g.fillRect(x, y - 1, pb_w, 1);

        g.setColor(pb_color);
        g.fillRect(x, y, s, 3);
        g.setColor(new Color(0x66FFFFFF, true));
        g.fillRect(x, y, s, 1);
    }

    private Image dl_mk_image(String t, int w) {
        Image i = null;
        if (t != null) {
            i = new BufferedImage(w, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) i.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(new Color(0x66EEEECC, true));
            FontMetrics m = g.getFontMetrics();
            g.translate((w - m.stringWidth(t)) / 2, m.getAscent());
            int dx = 1, dy = 0;
            do {
                g.drawString(t, +dx, +dy);
                g.drawString(t, -dx, -dy);
                dx -= dy;
                dy = 1;
            } while (dx >= -1);
            g.setColor(new Color(0x111111));
            g.drawString(t, 0, 0);
            g.dispose();
        }
        dl_text_current = t;
        return i;
    }

    /* KeyListener, FocusListener */
    @Override
    public void keyTyped(KeyEvent e) {
        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        boolean m;
        int v;
        if (c == e.VK_DELETE && e.isControlDown()) {
            spectrum.reset();
            return;
        } else if (c == e.VK_F11) {
            spectrum.mute(m = !spectrum.muted);
            v = spectrum.volumeChg(0);
        } else if (c == e.VK_PAGE_UP || c == e.VK_PAGE_DOWN) {
            m = spectrum.muted;
            v = spectrum.volumeChg(c == e.VK_PAGE_UP ? +5 : -5);
        } else if (c == e.VK_PAUSE) {
            try {
                spectrum.pause(4);
            } catch (InterruptedException x) {
                intr();
            }
            return;
        } else {
            keyEvent(e);
            return;
        }
        String s = "Volume: ";
        for (int i = 0; i < v; i += 4) {
            s += "|";
        }
        s += " " + v + "%";
        if (m) {
            s += " (muted)";
        }
        showStatus(s, false);
        
        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyEvent(e);
        
        e.consume();
    }

    void keyEvent(KeyEvent e) {
        KeyEvent[] k = spectrum.keys;
        int c = e.getKeyCode();
        int j = -1;
        synchronized (k) {
            for (int i = 0; i < k.length; i++) {
                if (k[i] == null) {
                    j = i;
                    continue;
                }
                int d = k[i].getKeyCode();
                if (d == c) {
                    j = i;
                    break;
                }
            }
            if (j >= 0) {
                k[j] = e.getID() == KeyEvent.KEY_PRESSED ? e : null;
            }
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        showStatus(getAppletInfo(), false);
    }

    @Override
    public void focusLost(FocusEvent e) {
        KeyEvent[] k = spectrum.keys;
        synchronized (k) {
            for (int i = 0; i < k.length; i++) {
                k[i] = null;
            }
        }
    }

    /* ComponentListener */
    @Override
    public void componentResized(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
        removeComponentListener(this);
        requestFocus();
    }

    /* parameters */
    String param(String n) {
        return params != null ? (String) params.get(n) : null;
    }

    boolean param(String name, boolean dflt) {
        String p = param(name);
        if (p == null || p.length() == 0) {
            return dflt;
        }
        p = p.toUpperCase();
        char c = p.charAt(0);
        return c != 'N' && c != 'F' && c != '0' && !p.equals("OFF");
    }

    double param(String name, double dflt) {
        try {
            String p = param(name);
            return Double.parseDouble(p);
        } catch (Exception x) {
            return dflt;
        }
    }

    /* resource */
    private InputStream resource(String name) {
        //return ClassLoader.getResourceAsStream(name);
        return Qaop.class.getResourceAsStream(name);
    }

    protected static int tomem(int[] m, int p, int l, InputStream in) {
        do {
            try {
                int v = in.read();
                if (v < 0) {
                    break;
                }
                m[p++] = v;
            } catch (IOException ignored) {
                break;
            }
        } while (--l > 0);
        return l;
    }



    public void showStatus(String s, boolean log) {
        if (log) {
            System.out.println(s);
        }
        try {
            // TODO
            // super.showStatus(s);
        } catch (Exception e) {
        }
    }

    public Image getImage(URL u) {
        try {
            return ImageIO.read(u);
        }
        catch(Exception e) {
            return null;
        }
    }



    /* download */
    protected java.util.List queue;
    private Thread th;

    @Override
    public void run() {
        Loader l;
        for (;;) {
            try {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        state &= ~2;
                        spectrum.pause(010);
                        queue.wait();
                        continue;
                    }
                    state |= 2;
                    l = (Loader) queue.remove(0);
                }
                l.exec();
            } catch (InterruptedException x) {
                break;
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    protected int dl_length, dl_loaded;
    protected String dl_text, dl_msg;

    private Image getImage(String name) {
        return UIBox.getImage(name);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.requestFocus();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.requestFocus(); 
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
}

// ------------------------------------------------------------------ LOADER ---




class Loader extends Thread {

    Qaop qaop;
    String file;
    int kind;
    boolean gz, zip;
    DataInputStream in;

    static final int ROM = 0x4001, IF1ROM = 0x2011, CART = 0x4021,
            TAP = 2, SNA = 3, Z80 = 4;

    static final String MIME = "application/x.zx.";

    protected static Loader add(Qaop a, String f, int k) {
        Loader l = new Loader();
        l.qaop = a;
        l.file = f;
        l.kind = k;
        List q = a.queue;
        synchronized (q) {
            clear(q, k);
            q.add(l);
            q.notify();
        }
        return l;
    }

    protected static void clear(List q, int k) {
        int i = q.size();
        while (--i >= 0) {
            Loader l = (Loader) q.get(i);
            if (l.kind == k) {
                q.remove(i);
            }
        }
    }

    void exec() throws InterruptedException {
        Qaop q = qaop;
        try {
            do_exec();
        } catch (InterruptedException x) {
            throw x;
        } catch (InterruptedIOException x) {
            throw new InterruptedException();
        } catch (Exception x) {
            System.out.println(x);
            if (q.dl_msg == null) {
                String m;
                if (x instanceof FileNotFoundException) {
                    m = "Not found";
                } else if (x instanceof EOFException) {
                    m = "File truncated";
                } else {
                    m = x.getMessage();
                    if (m == null) {
                        m = "Error";
                    }
                }
                q.dl_msg = m;
            }
            if (q.dl_text == null) {
                q.dl_text = "";
            }
            q.repaint(100);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException xx) {
                throw xx;
            }
        } finally {
            try {
                interrupt();
                join();
            } catch (Exception x) {
            }
            q.dl_text = q.dl_msg = null;
            q.repaint(200);
        }
    }

    private void do_exec() throws Exception {
        InputStream s;
        String n = file;
        int k = kind;

        if (n == null) {
            qaop.spectrum.tape(null, false);
            return;
        }

        qaop.pb_color = Color.gray;
        file = null; // now ZIP entry
        if (n.startsWith("data:")) {
            s = data_url(n);
        } else {
            raw_in = start_download(n);
            pipe = new PipedOutputStream();
//			setPriority(Thread.NORM_PRIORITY-1);
            s = new PipedInputStream(pipe);
            start();
        }
        if (zip) {
            s = unzip(s);
        } else if (gz) {
            s = new GZIPInputStream(s);
        }

        pb_color:
        {
            int c;
            switch (kind & 0xF) {
                case SNA & 0xF:
                case Z80 & 0xF:
                    c = 0x0077FF;
                    break;
                case TAP & 0xF:
                    c = 0x00CC22;
                    break;
                case ROM & 0xF:
                    c = 0xFF1100;
                    break;
                default:
                    break pb_color;
            }
            qaop.pb_color = new Color(c);
        }

        in = new DataInputStream(s);

        Spectrum spec = qaop.spectrum;
        spec.pause(011);

        switch (kind) {
            case 0:
                throw new IOException("Unknown format");
            case TAP:
                spec.tape(null, false);
                if (k != TAP) {
                    spec.basic_exec("\u00EF\"\""); // LOAD ""
                }
                spec.pause(010);
                load_tape();
                break;
            case SNA:
                load_sna();
                break;
            case Z80:
                load_z80();
                break;
            case CART:
                qaop.spectrum.reset();
            default:
                load_rom(kind);
        }
    }

    private InputStream start_download(String f) throws IOException {
        Qaop a = qaop;
        a.dl_length = a.dl_loaded = 0;
        a.dl_text = f;

        URL u = new URL(f);

        f = u.getFile();
        int i = f.lastIndexOf('/');
        if (i >= 0) {
            f = f.substring(i + 1);
        }

        a.dl_text = f;
        a.repaint(200);

        URLConnection c = u.openConnection();
        try {
            c.setConnectTimeout(15000);
            c.setReadTimeout(10000);
        } catch (Error e) {
        }

        if (c instanceof java.net.HttpURLConnection) {
            int x = ((java.net.HttpURLConnection) c).getResponseCode();
            if (x < 200 || x > 299) {
                throw new FileNotFoundException();
            }
        }

        check_type(c.getContentType(), f);

        String s = c.getContentEncoding();
        if (s != null && s.contains("gzip")) {
            gz = true;
        }

        int l = c.getContentLength();
        qaop.dl_length = l > 0 ? l : 1 << 16;

        file = u.getRef();
        return c.getInputStream();
    }

    private InputStream unzip(InputStream s) throws IOException {
        ZipInputStream zip = new ZipInputStream(s);
        int k;
        for (;;) {
            ZipEntry e = zip.getNextEntry();
            if (e == null) {
                throw new IOException("No suitable file found");
            }
            if (e.isDirectory()) {
                continue;
            }
            String n = e.getName();
            k = get_type(n.toLowerCase());
            if (file == null) {
                if (k == 0) {
                    continue;
                }
                if (kind == 0) {
                    break;
                }
                if (((kind ^ k) & 0xF) == 0) {
                    break;
                }
            } else if (n.equals(file)) {
                break;
            }
        }
        if (kind == 0) {
            kind = k;
        }
        return zip;
    }

    private InputStream data_url(String s) throws IOException {
        // data:application/x.zx.tap;base64,EwAAAGJvcm
        int i = s.indexOf(',', 5);
        if (i < 0) {
            throw new IOException();
        }
        String h = s.substring(5, i);
        s = s.substring(i + 1);
        boolean b64;
        if (b64 = h.endsWith(";base64")) {
            h = h.substring(0, i - 5 - 7);
        }
        check_type(h, null);
        s = URLDecoder.decode(s, "iso-8859-1");
        byte[] b = b64 ? base64decode(s) : s.getBytes("iso-8859-1");
        return new ByteArrayInputStream(b);
    }


    static byte[] base64decode(String s) throws IOException {
        int l = s.length();
        l = l / 4 * 3;
        if (s.endsWith("=")) {
            l -= s.endsWith("==") ? 2 : 1;
        }
        byte[] o = new byte[l];
        int ii = 0, oi = 0, b = 0, k = 2;
        for (;;) {
            //                                                         replaced -> +
            int n = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 /"
                    .indexOf(s.charAt(ii++));
            if (n < 0) {
                throw new IOException("data: '" + s.charAt(ii - 1) + "' " + (ii - 1));
            }
            if (k <= 0) {
                o[oi] = (byte) (b | n >> -k);
                if (++oi == l) {
                    break;
                }
                k += 8;
                b = 0;
            }
            b |= n << k;
            k -= 6;
        }
        return o;
    }


    private void check_type(String mime, String name) {
        int tk = 0, nk = 0;
        if (mime != null) {
            int i = mime.indexOf(';');
            if (i > 0) {
                mime = mime.substring(0, i).trim();
            }
            mime = mime.toLowerCase();
            if (zip = mime.equals("application/zip")) {
                return;
            }
            if (mime.startsWith("application/x-spectrum-")) {
                tk = get_type("." + mime.substring(23));
            } else if (mime.startsWith(MIME)) {
                tk = get_type(mime);
            }
        }
        if (name != null) {
            name = name.toLowerCase();
            if (zip = name.endsWith(".zip")) {
                return;
            }
            nk = get_type(name);
        }
        if (kind == 0) {
            kind = tk != 0 ? tk : nk;
        }
    }

    private int get_type(String f) {
        int k;
        if (gz = f.endsWith(".gz")) {
            f = f.substring(0, f.length() - 3);
        }
        k = 0;
        if (f.endsWith(".sna")) {
            k = SNA;
        } else if (f.endsWith(".z80") || f.endsWith(".slt")) {
            k = Z80;
        } else if (f.endsWith(".tap")) {
            k = TAP;
        } else if (f.endsWith(".rom")) {
            k = CART;
        }
        return k;
    }

    PipedOutputStream pipe;
    InputStream raw_in;

    public void run() {
        // yield();
        byte buf[] = new byte[4096];
        try {
            while (!interrupted()) {
                int n = raw_in.read(buf, 0, buf.length);
                if (n <= 0) {
                    break;
                }
                qaop.dl_loaded += n;
                qaop.repaint(100);
                try {
                    pipe.write(buf, 0, n);
                } catch (InterruptedIOException x) {
                    break;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        } finally {
            try {
                pipe.close();
            } catch (Exception x) {
            }
        }
    }

    /* file handlers */
    private void load_rom(int kind) throws Exception {
        int m[] = new int[0x8000];
        if (qaop.tomem(m, 0, kind & 0xF000, in) != 0) {
            throw new Exception("Rom image truncated");
        }
        Spectrum spec = qaop.spectrum;
        switch (kind) {
            case IF1ROM:
                System.arraycopy(m, 0, m, 0x4000, 0x4000);
                spec.if1rom = m;
                break;
            case ROM:
                spec.rom48k = m;
            default:
                spec.rom = m;
        }
    }

    private void load_tape() {
        byte data[] = null;
        int pos = 0;
        for (;;) {
            try {
                byte buf[] = new byte[pos + 512];
                int n = in.read(buf, pos, 512);
                if (n < 512) {
                    if (n <= 0) {
                        break;
                    }
                    byte buf2[] = new byte[pos + n];
                    System.arraycopy(buf, pos, buf2, pos, n);
                    buf = buf2;
                }
                if (data != null) {
                    System.arraycopy(data, 0, buf, 0, pos);
                }
                data = buf;
                pos += n;
                qaop.spectrum.tape(data, false);
                Thread.yield();
            } catch (IOException e) {
                break;
            }
        }
        if (data != null) {
            qaop.spectrum.tape(data, true);
        }
    }

    private int get8() throws IOException {
        return in.readUnsignedByte();
    }

    private int get16() throws IOException {
        int b = in.readUnsignedByte();
        return b | in.readUnsignedByte() << 8;
    }

    private void poke_stream(int pos, int len) throws IOException {
        Spectrum s = qaop.spectrum;
        do {
            s.mem(pos++, get8());
        } while (--len > 0);
    }

    private void get_regs(Z80 z, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                z.exx();
                z.ex_af();
                continue;
            }
            int v = c < 'a' ? get16() : get8();
            switch (c) {
                case 'a':
                    z.a(v);
                    break;
                case 'f':
                    z.f(v);
                    break;
                case 'B':
                    z.bc(v);
                    break;
                case 'D':
                    z.de(v);
                    break;
                case 'H':
                    z.hl(v);
                    break;
                case 'X':
                    z.ix(v);
                    break;
                case 'Y':
                    z.iy(v);
                    break;
                case 'S':
                    z.sp(v);
                    break;
                case 'i':
                    z.i(v);
                    break;
                case 'r':
                    z.r(v);
                    break;
            }
        }
    }

    private void load_sna() throws IOException {
        Spectrum spectrum = qaop.spectrum;
        Z80 cpu = spectrum.cpu;

        spectrum.reset();
        get_regs(cpu, "iHDBfa'HDBYX");
        cpu.ei(get8() != 0);
        get_regs(cpu, "rfaS");
        cpu.im(get8());
        spectrum.border = (byte) (get8() & 7);
        poke_stream(16384, 49152);

        try {
            cpu.pc(get16());
            System.out.println("Is it 128K .SNA?");
        } catch (EOFException e) {
            int sp = cpu.sp();
            cpu.pc(spectrum.mem16(sp));
            cpu.sp((char) (sp + 2));
            spectrum.mem16(sp, 0);
        }
    }

    private void load_z80() throws IOException {
        Spectrum spectrum = qaop.spectrum;
        Z80 cpu = spectrum.cpu;

        spectrum.reset();
        get_regs(cpu, "afBH");
        int pc = get16();
        cpu.pc(pc);
        get_regs(cpu, "Si");
        int f1 = get16();
        cpu.r(f1 & 0x7F | f1 >> 1 & 0x80);
        if ((f1 >>>= 8) == 0xFF) {
            f1 = 1;
        }
        spectrum.border = (byte) (f1 >> 1 & 7);
        get_regs(cpu, "D'BDHaf'YX");
        int v = get8();
        cpu.iff((v == 0 ? 0 : 1) | (get8() == 0 ? 0 : 2));
        cpu.im(get8());

        if (pc != 0) {
            if ((f1 & 0x20) != 0) {
                uncompress_z80(16384, 49152);
            } else {
                poke_stream(16384, 49152);
            }
            return;
        }

        int l = get16();
        cpu.pc(get16());
        int hm = get8();
        if (hm > 1) {
            System.out.println("Unsupported model: #" + hm);
        }
        get8();
        if (get8() == 0xFF && spectrum.if1rom != null) {
            spectrum.rom = spectrum.if1rom;
        }

        int ay_idx = 0, ay_reg[] = null;
        if ((get8() & 4) != 0 && spectrum.ay_enabled && l >= 23) {
            ay_idx = get8() & 15;
            ay_reg = new int[16];
            for (int i = 0; i < 16; i++) {
                ay_reg[i] = get8();
            }
            l -= 17;
        }
        in.skip(l - 6);

        for (;;) {
            try {
                l = get16();
            } catch (IOException x) {
                break;
            }
            int a;
            switch (get8()) {
                case 8:
                    a = 0x4000;
                    break;
                case 4:
                    a = 0x8000;
                    break;
                case 5:
                    a = 0xC000;
                    break;
                default:
                    in.skip(l);
                    continue;
            }
            if (l == 0xFFFF) {
                poke_stream(a, 16384);
            } else {
                uncompress_z80(a, 16384);
            }
        }
        if (ay_reg != null) {
            for (int i = 0; i < 16; i++) {
                spectrum.ay_write(i, ay_reg[i]);
            }
            spectrum.ay_idx = (byte) ay_idx;
        }
    }

    private int uncompress_z80(int pos, int count) throws IOException {
        Spectrum spectrum = qaop.spectrum;
        int end = pos + count;
        int n = 0;
        loop:
        do {
            int v = get8();
            n++;
            if (v != 0xED) {
                spectrum.mem(pos++, v);
                continue;
            }
            v = get8();
            n++;
            if (v != 0xED) {
                spectrum.mem16(pos, v << 8 | 0xED);
                pos += 2;
                continue;
            }
            int l = get8();
            v = get8();
            n += 2;
            while (l > 0) {
                spectrum.mem(pos++, v);
                if (pos >= end) {
                    break loop;
                }
                l--;
            }
        } while (pos < end);
        return n;
    }

    /* make snapshot */
    protected static String get_snap(Qaop q) {
        byte[] b;
        try {
            b = save_z80(q);
            return to_data("z80", b);
        } catch (Exception x) {
            x.printStackTrace();
            return null;
        }
    }

    private static String to_data(String t, byte[] b) {
        return "data:" + MIME + t + ";base64," + base64encode(b);
        //return "data:" + MIME + t + ";base64," + Base64.encodeBytes(b);
    }


    private static String base64encode(byte[] b) {
        int l = b.length;
        if (l == 0) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        int a = b[0], k = 2;
        for (int i = 1;;) {
            s.append(
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
                    .charAt(a >>> k & 63)
            );
            if ((k -= 6) >= 0) {
                continue;
            }
            if (i < l) {
                a <<= 8;
                k += 8;
                a += b[i++] & 0xFF;
                continue;
            }
            if (k == -6) {
                break;
            }
            a <<= 5;
            k += 5;
            if (i++ == l) {
                continue;
            }
            s.append('=');
            if (k == 0) {
                s.append('=');
            }
            break;
        }
        return s.toString();
    }

    
    
    private static OutputStream put16(OutputStream o, int v) throws IOException {
        o.write(v);
        o.write(v >> 8);
        return o;
    }

    private static byte[] save_z80(Qaop q) throws IOException {
        Spectrum s = q.spectrum;
        Z80 cpu = s.cpu;

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(cpu.a());
        o.write(cpu.f());
        put16(o, cpu.bc());
        put16(o, cpu.hl());
        put16(o, 0); // v2
        put16(o, cpu.sp());
        o.write(cpu.i());
        int n = cpu.r();
        put16(o, n | n << 1 & 256 | s.border << 9);
        put16(o, cpu.de());
        cpu.exx();
        cpu.ex_af();
        put16(o, cpu.bc());
        put16(o, cpu.de());
        put16(o, cpu.hl());
        o.write(cpu.a());
        o.write(cpu.f());
        cpu.exx();
        cpu.ex_af();
        put16(o, cpu.iy());
        put16(o, cpu.ix());
        put16(o, 0x81 * cpu.iff() & 0x101);
        o.write(cpu.im());

        put16(o, 23);
        put16(o, cpu.pc());
        put16(o, s.if1rom != null ? 1 : 0);
        put16(o, (s.rom == s.if1rom ? 0xFF : 0) | (s.ay_enabled ? 0x400 : 0));
        o.write(s.ay_idx);
        for (int i = 0; i < 16; i++) {
            o.write(s.ay_reg[i]);
        }

        byte[] b = new byte[24576];
        for (int i = 0; i < 3; i++) {
            int l = compress_z80(b, s.ram, i << 14, 1 << 14);
            put16(o, l).write(0x050408 >> 8 * i);
            o.write(b, 0, l);
        }
        o.close();
        return o.toByteArray();
    }

    private static int compress_z80(byte b[], int m[], int i, int l) {
        int j = 0;
        l += i;
        loop:
        do {
            int n = i;
            int a = m[i];
            do {
                i++;
            } while (i < l && m[i] == a);
            n = i - n;
            do {
                if (a != 0xED) {
                    if (n < 5) {
                        do {
                            b[j++] = (byte) a;
                        } while (--n != 0);
                        continue loop;
                    }
                } else if (n == 1) {
                    b[j++] = (byte) a;
                    if (i == l) {
                        break loop;
                    }
                    b[j++] = (byte) m[i++];
                    continue loop;
                }
                b[j] = b[j + 1] = (byte) 0xED;
                b[j + 2] = (byte) (n > 255 ? 255 : n);
                b[j + 3] = (byte) a;
                j += 4;
                n -= 255;
            } while (n > 0);
        } while (i < l);
        return j;
    }
    
    
    
    // -------------------------------------------------------------------------
    

}
